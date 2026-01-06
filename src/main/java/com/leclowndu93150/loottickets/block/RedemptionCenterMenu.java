package com.leclowndu93150.loottickets.block;

import com.leclowndu93150.loottickets.Config;
import com.leclowndu93150.loottickets.component.LootTicketData;
import com.leclowndu93150.loottickets.registry.ModDataComponents;
import com.leclowndu93150.loottickets.registry.ModItems;
import com.leclowndu93150.loottickets.registry.ModMenuTypes;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class RedemptionCenterMenu extends AbstractContainerMenu {

    private final Container container;

    public RedemptionCenterMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(28));
    }

    public RedemptionCenterMenu(int containerId, Inventory playerInventory, Container container) {
        super(ModMenuTypes.REDEMPTION_CENTER_MENU.get(), containerId);
        this.container = container;
        container.startOpen(playerInventory.player);

        int k = (3 - 4) * 18;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int slotIndex = col + row * 9;
                if (slotIndex == 0) {
                    this.addSlot(new RedemptionCenterTicketSlot(container, slotIndex, 8 + col * 18, 18 + row * 18));
                } else {
                    this.addSlot(new RedemptionCenterOutputSlot(container, slotIndex, 8 + col * 18, 18 + row * 18));
                }
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 103 + row * 18 + k));
            }
        }

        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 161 + k));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);

        if (slot != null && slot.hasItem()) {
            ItemStack slotItem = slot.getItem();
            itemstack = slotItem.copy();

            if (slotIndex < 27) {
                if (!this.moveItemStackTo(slotItem, 27, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (slotItem.is(ModItems.LOOT_TICKET.get())
                    && slotItem.has(ModDataComponents.LOOT_TICKET_DATA.get())) {
                    if (!this.moveItemStackTo(slotItem, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    return ItemStack.EMPTY;
                }
            }

            if (slotItem.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }

    public boolean isBlocked() {
        ItemStack ticketStack = container.getItem(0);
        if (ticketStack.isEmpty()) {
            return false;
        }

        // Check if output slots have items (normal blocked state)
        for (int i = 1; i < container.getContainerSize(); i++) {
            if (!container.getItem(i).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public boolean isBlacklisted() {
        ItemStack ticketStack = container.getItem(0);
        if (ticketStack.isEmpty()) {
            return false;
        }

        LootTicketData data = ticketStack.get(ModDataComponents.LOOT_TICKET_DATA.get());
        if (data == null) {
            return false;
        }

        return !Config.isLootTableRedeemable(data.lootTable().location());
    }
}
