package com.leclowndu93150.loottickets.event;

import com.leclowndu93150.loottickets.LootTickets;
import com.leclowndu93150.loottickets.item.container.TicketBagContainer;
import com.leclowndu93150.loottickets.registry.ModDataComponents;
import com.leclowndu93150.loottickets.registry.ModItems;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;

@EventBusSubscriber(modid = LootTickets.MODID)
public class TicketPickupHandler {

    @SubscribeEvent
    public static void onItemPickup(ItemEntityPickupEvent.Pre event) {
        ItemStack pickedUp = event.getItemEntity().getItem();

        if (!pickedUp.is(ModItems.LOOT_TICKET.get()) || !pickedUp.has(ModDataComponents.LOOT_TICKET_DATA.get())) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack bagStack = findTicketBag(player);

        if (bagStack.isEmpty()) {
            return;
        }

        TicketBagContainer container = new TicketBagContainer(bagStack);
        ItemStack remaining = container.tryInsert(pickedUp);

        if (remaining.isEmpty()) {
            event.getItemEntity().discard();
            event.setCanPickup(TriState.FALSE);
        } else {
            event.getItemEntity().setItem(remaining);
        }
    }

    /**
     * Try to insert a ticket into the player's ticket bag.
     * Returns the remaining items that couldn't be inserted.
     */
    public static ItemStack tryInsertIntoBag(Player player, ItemStack ticket) {
        ItemStack bagStack = findTicketBag(player);
        if (bagStack.isEmpty()) {
            return ticket;
        }

        TicketBagContainer container = new TicketBagContainer(bagStack);
        return container.tryInsert(ticket);
    }

    /**
     * Give a ticket to the player, preferring the ticket bag if available.
     * Falls back to inventory, then drops if inventory is full.
     */
    public static void giveTicketToPlayer(Player player, ItemStack ticket) {
        ItemStack remaining = tryInsertIntoBag(player, ticket);
        if (!remaining.isEmpty()) {
            if (!player.getInventory().add(remaining)) {
                player.drop(remaining, false);
            }
        }
    }

    private static ItemStack findTicketBag(Player player) {
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.is(ModItems.TICKET_BAG.get())) {
                return stack;
            }
        }

        if (ModList.get().isLoaded("curios")) {
            ItemStack curiosBag = CuriosHelper.findTicketBagInCurios(player);
            if (!curiosBag.isEmpty()) {
                return curiosBag;
            }
        }

        return ItemStack.EMPTY;
    }
}
