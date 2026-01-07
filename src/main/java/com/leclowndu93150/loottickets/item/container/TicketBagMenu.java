package com.leclowndu93150.loottickets.item.container;

import com.leclowndu93150.loottickets.item.TicketBagItem;
import com.leclowndu93150.loottickets.registry.ModDataComponents;
import com.leclowndu93150.loottickets.registry.ModItems;
import com.leclowndu93150.loottickets.registry.ModMenuTypes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class TicketBagMenu extends AbstractContainerMenu {

    private final TicketBagContainer container;
    private final int blockedSlot;

    public TicketBagMenu(int containerId, Inventory playerInventory, int slotIndex) {
        this(containerId, playerInventory, ItemStack.EMPTY, slotIndex);
    }

    public TicketBagMenu(int containerId, Inventory playerInventory, ItemStack bagStack, int slotIndex) {
        super(ModMenuTypes.TICKET_BAG_MENU.get(), containerId);
        this.blockedSlot = slotIndex;

        if (bagStack.isEmpty()) {
            this.container = new TicketBagContainer(new ItemStack(ModItems.TICKET_BAG.get()));
        } else {
            this.container = new TicketBagContainer(bagStack);
        }

        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new TicketBagSlot(container, col + row * 9, 8 + col * 18, 19 + row * 18));
            }
        }

        int playerInvY = 141;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, playerInvY + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, playerInvY + 58));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);

        if (slot != null && slot.hasItem()) {
            ItemStack slotItem = slot.getItem();
            itemstack = slotItem.copy();

            if (slotIndex < TicketBagItem.CONTAINER_SIZE) {
                if (!this.moveItemStackTo(slotItem, TicketBagItem.CONTAINER_SIZE, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (slotItem.is(ModItems.LOOT_TICKET.get()) && slotItem.has(ModDataComponents.LOOT_TICKET_DATA.get())) {
                    if (!this.moveItemStackTo(slotItem, 0, TicketBagItem.CONTAINER_SIZE, false)) {
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
        return true;
    }

    @Override
    public void broadcastChanges() {
        container.reloadFromStack();
        super.broadcastChanges();
    }

    @Override
    protected boolean moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        if (startIndex < TicketBagItem.CONTAINER_SIZE) {
            if (!stack.is(ModItems.LOOT_TICKET.get()) || !stack.has(ModDataComponents.LOOT_TICKET_DATA.get())) {
                return false;
            }
        }
        return super.moveItemStackTo(stack, startIndex, endIndex, reverseDirection);
    }

    @Override
    public void clicked(int slotId, int button, net.minecraft.world.inventory.ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < this.slots.size()) {
            Slot slot = this.slots.get(slotId);
            int actualSlot = slot.getSlotIndex();
            if (slot.container == player.getInventory() && actualSlot == blockedSlot) {
                return;
            }
        }
        super.clicked(slotId, button, clickType, player);
    }
}
