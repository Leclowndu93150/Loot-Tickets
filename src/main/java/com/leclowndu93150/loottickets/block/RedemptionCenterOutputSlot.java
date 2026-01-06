package com.leclowndu93150.loottickets.block;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class RedemptionCenterOutputSlot extends Slot {

    public RedemptionCenterOutputSlot(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }
}
