package com.leclowndu93150.loottickets.block;

import com.leclowndu93150.loottickets.Config;
import com.leclowndu93150.loottickets.component.LootTicketData;
import com.leclowndu93150.loottickets.item.TicketBagItem;
import com.leclowndu93150.loottickets.registry.ModBlockEntities;
import com.leclowndu93150.loottickets.registry.ModDataComponents;
import com.leclowndu93150.loottickets.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class RedemptionCenterBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {

    private static final int TICKET_SLOT = 0;
    private static final int OUTPUT_SLOT_START = 1;
    private static final int OUTPUT_SLOT_COUNT = 27;
    private static final int CONTAINER_SIZE = 1 + OUTPUT_SLOT_COUNT;

    private static final int[] SLOTS_FOR_DOWN;

    static {
        SLOTS_FOR_DOWN = new int[OUTPUT_SLOT_COUNT];
        for (int i = 0; i < OUTPUT_SLOT_COUNT; i++) {
            SLOTS_FOR_DOWN[i] = OUTPUT_SLOT_START + i;
        }
    }

    private NonNullList<ItemStack> items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);

    public RedemptionCenterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.REDEMPTION_CENTER.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RedemptionCenterBlockEntity blockEntity) {
        blockEntity.tick(level, pos);
    }

    private void tick(Level level, BlockPos pos) {
        if (hasOutputItems()) {
            return;
        }

        ItemStack slotStack = items.get(TICKET_SLOT);
        if (slotStack.isEmpty()) {
            return;
        }

        // Handle ticket bag
        if (slotStack.is(ModItems.TICKET_BAG.get())) {
            processTicketFromBag(slotStack, level, pos);
            return;
        }

        // Handle regular ticket
        if (!slotStack.is(ModItems.LOOT_TICKET.get())) {
            return;
        }

        LootTicketData data = slotStack.get(ModDataComponents.LOOT_TICKET_DATA.get());
        if (data == null || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        // Skip blacklisted loot tables
        if (!Config.isLootTableRedeemable(data.lootTable().location())) {
            return;
        }

        LootTable lootTable = serverLevel.getServer()
            .reloadableRegistries()
            .getLootTable(data.lootTable());

        if (lootTable == LootTable.EMPTY) {
            return;
        }

        LootParams params = new LootParams.Builder(serverLevel)
            .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(worldPosition))
            .create(LootContextParamSets.CHEST);

        lootTable.getRandomItems(params).forEach(this::addLootToOutput);

        slotStack.shrink(1);
        setChanged();

        level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    private void processTicketFromBag(ItemStack bagStack, Level level, BlockPos pos) {
        ItemStack ticketStack = TicketBagItem.getFirstTicket(bagStack);
        if (ticketStack.isEmpty()) {
            return;
        }

        LootTicketData data = ticketStack.get(ModDataComponents.LOOT_TICKET_DATA.get());
        if (data == null || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        // Skip blacklisted loot tables
        if (!Config.isLootTableRedeemable(data.lootTable().location())) {
            return;
        }

        LootTable lootTable = serverLevel.getServer()
            .reloadableRegistries()
            .getLootTable(data.lootTable());

        if (lootTable == LootTable.EMPTY) {
            return;
        }

        LootParams params = new LootParams.Builder(serverLevel)
            .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(worldPosition))
            .create(LootContextParamSets.CHEST);

        lootTable.getRandomItems(params).forEach(this::addLootToOutput);

        TicketBagItem.removeOneTicket(bagStack);
        setChanged();

        level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    private boolean hasOutputItems() {
        for (int i = OUTPUT_SLOT_START; i < CONTAINER_SIZE; i++) {
            if (!items.get(i).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void addLootToOutput(ItemStack loot) {
        for (int i = OUTPUT_SLOT_START; i < CONTAINER_SIZE && !loot.isEmpty(); i++) {
            ItemStack existing = items.get(i);
            if (existing.isEmpty()) {
                items.set(i, loot.copy());
                return;
            } else if (ItemStack.isSameItemSameComponents(existing, loot)) {
                int canAdd = existing.getMaxStackSize() - existing.getCount();
                int toAdd = Math.min(canAdd, loot.getCount());
                existing.grow(toAdd);
                loot.shrink(toAdd);
            }
        }

        if (!loot.isEmpty() && level != null) {
            Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 1, worldPosition.getZ() + 0.5, loot);
        }
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        // Only expose output slots from bottom - ticket slot is never exposed to hoppers
        return side == Direction.DOWN ? SLOTS_FOR_DOWN : new int[0];
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction direction) {
        if (slot == TICKET_SLOT) {
            return stack.is(ModItems.LOOT_TICKET.get())
                && stack.has(ModDataComponents.LOOT_TICKET_DATA.get())
                && !hasOutputItems();
        }
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return slot >= OUTPUT_SLOT_START && direction == Direction.DOWN;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.loottickets.redemption_center");
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    public int getContainerSize() {
        return CONTAINER_SIZE;
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new RedemptionCenterMenu(containerId, inventory, this);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == TICKET_SLOT) {
            if (stack.is(ModItems.LOOT_TICKET.get()) && stack.has(ModDataComponents.LOOT_TICKET_DATA.get())) {
                return true;
            }
            if (stack.is(ModItems.TICKET_BAG.get()) && TicketBagItem.getTicketCount(stack) > 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
    }
}
