package com.leclowndu93150.loottickets.event;

import com.leclowndu93150.loottickets.registry.ModItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.Optional;

public class CuriosHelper {

    public static ItemStack findTicketBagInCurios(Player player) {
        Optional<ICuriosItemHandler> curiosInventory = CuriosApi.getCuriosInventory(player);
        if (curiosInventory.isPresent()) {
            var handler = curiosInventory.get();
            var result = handler.findFirstCurio(stack -> stack.is(ModItems.TICKET_BAG.get()));
            if (result.isPresent()) {
                return result.get().stack();
            }
        }
        return ItemStack.EMPTY;
    }
}
