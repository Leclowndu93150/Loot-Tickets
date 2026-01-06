package com.leclowndu93150.loottickets.item.container;

import com.leclowndu93150.loottickets.item.TicketBagItem;
import com.leclowndu93150.loottickets.registry.ModDataComponents;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

public class TicketBagContainer implements Container {

    private final ItemStack bagStack;
    private final NonNullList<ItemStack> items;

    public TicketBagContainer(ItemStack bagStack) {
        this.bagStack = bagStack;
        this.items = NonNullList.withSize(TicketBagItem.CONTAINER_SIZE, ItemStack.EMPTY);
        loadFromStack();
    }

    private void loadFromStack() {
        ItemContainerContents contents = bagStack.get(ModDataComponents.BAG_CONTENTS.get());
        if (contents != null) {
            contents.copyInto(items);
        }
    }

    public void reloadFromStack() {
        for (int i = 0; i < items.size(); i++) {
            items.set(i, ItemStack.EMPTY);
        }
        loadFromStack();
    }

    private void saveToStack() {
        bagStack.set(ModDataComponents.BAG_CONTENTS.get(), ItemContainerContents.fromItems(items));
    }

    @Override
    public int getContainerSize() {
        return TicketBagItem.CONTAINER_SIZE;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) {
            saveToStack();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack result = ContainerHelper.takeItem(items, slot);
        saveToStack();
        return result;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        saveToStack();
    }

    @Override
    public void setChanged() {
        saveToStack();
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        items.clear();
        saveToStack();
    }

    public ItemStack tryInsert(ItemStack toInsert) {
        if (toInsert.isEmpty()) {
            return ItemStack.EMPTY;
        }

        for (int i = 0; i < items.size(); i++) {
            ItemStack slotStack = items.get(i);
            if (slotStack.isEmpty()) {
                items.set(i, toInsert.copy());
                toInsert.setCount(0);
                saveToStack();
                return ItemStack.EMPTY;
            } else if (ItemStack.isSameItemSameComponents(slotStack, toInsert)) {
                int space = slotStack.getMaxStackSize() - slotStack.getCount();
                if (space > 0) {
                    int toTransfer = Math.min(space, toInsert.getCount());
                    slotStack.grow(toTransfer);
                    toInsert.shrink(toTransfer);
                    if (toInsert.isEmpty()) {
                        saveToStack();
                        return ItemStack.EMPTY;
                    }
                }
            }
        }

        saveToStack();
        return toInsert;
    }
}
