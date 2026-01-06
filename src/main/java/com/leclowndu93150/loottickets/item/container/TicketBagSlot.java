package com.leclowndu93150.loottickets.item.container;

import com.leclowndu93150.loottickets.registry.ModDataComponents;
import com.leclowndu93150.loottickets.registry.ModItems;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class TicketBagSlot extends Slot {

    public TicketBagSlot(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.is(ModItems.LOOT_TICKET.get()) && stack.has(ModDataComponents.LOOT_TICKET_DATA.get());
    }
}
