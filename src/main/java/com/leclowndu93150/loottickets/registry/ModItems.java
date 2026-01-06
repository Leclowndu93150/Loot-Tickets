package com.leclowndu93150.loottickets.registry;

import com.leclowndu93150.loottickets.LootTickets;
import com.leclowndu93150.loottickets.item.LootTicketItem;
import com.leclowndu93150.loottickets.item.TicketBagItem;
import com.leclowndu93150.loottickets.item.TicketExtractorItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(LootTickets.MODID);

    public static final DeferredItem<TicketExtractorItem> TICKET_EXTRACTOR = ITEMS.register(
        "ticket_extractor",
        () -> new TicketExtractorItem(new Item.Properties().stacksTo(1))
    );

    public static final DeferredItem<LootTicketItem> LOOT_TICKET = ITEMS.register(
        "loot_ticket",
        () -> new LootTicketItem(new Item.Properties().stacksTo(64))
    );

    public static final DeferredItem<TicketBagItem> TICKET_BAG = ITEMS.register(
        "ticket_bag",
        () -> new TicketBagItem(new Item.Properties().stacksTo(1))
    );
}
