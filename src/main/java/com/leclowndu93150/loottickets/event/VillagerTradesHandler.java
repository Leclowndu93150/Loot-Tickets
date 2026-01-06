package com.leclowndu93150.loottickets.event;

import com.leclowndu93150.loottickets.Config;
import com.leclowndu93150.loottickets.LootTickets;
import com.leclowndu93150.loottickets.item.LootTicketItem;
import com.leclowndu93150.loottickets.registry.ModItems;
import com.leclowndu93150.loottickets.registry.ModVillagerProfessions;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@EventBusSubscriber
public class VillagerTradesHandler {

    @SubscribeEvent
    public static void onVillagerTradesEvent(VillagerTradesEvent event) {
        if (!Config.enableTrades) {
            return;
        }

        if (event.getType() == ModVillagerProfessions.REDEMPTION_CENTER_EMPLOYEE.get()) {
            LootTickets.LOGGER.info("Adding trades for Redemption Center Employee");
            addEmeraldForTicketTrades(event);
            addTicketExchangeTrades(event);
        }
    }

    private static void addEmeraldForTicketTrades(VillagerTradesEvent event) {
        var lootTableRegistry = event.getRegistryAccess().lookupOrThrow(Registries.LOOT_TABLE);
        List<ResourceLocation> tradeableLootTables = lootTableRegistry.listElementIds()
            .map(ResourceKey::location)
            .filter(Config::isLootTableTradeable)
            .toList();

        LootTickets.LOGGER.info("Found {} tradeable loot tables", tradeableLootTables.size());

        // Distribute trades across all levels (1-5) so villagers always have trades available
        // This provides better progression and ensures the villager can trade from level 1
        int tradesPerLevel = Math.max(1, tradeableLootTables.size() / 5);
        int currentIndex = 0;

        for (int level = 1; level <= 5; level++) {
            List<VillagerTrades.ItemListing> levelTrades = new ArrayList<>();

            // Calculate how many trades this level should have
            int endIndex = (level == 5)
                ? tradeableLootTables.size()
                : Math.min(currentIndex + tradesPerLevel, tradeableLootTables.size());

            // Add trades for this level
            for (int i = currentIndex; i < endIndex; i++) {
                ResourceLocation lootTablePath = tradeableLootTables.get(i);
                ResourceKey<LootTable> lootTableKey = ResourceKey.create(Registries.LOOT_TABLE, lootTablePath);
                int price = Config.getPriceForLootTable(lootTablePath);

                levelTrades.add((trader, random) -> {
                    ItemStack outputTicket = LootTicketItem.createTicket(ModItems.LOOT_TICKET.get(), lootTableKey);
                    return new MerchantOffer(
                        new ItemCost(Items.EMERALD, price),
                        outputTicket,
                        Config.tradeMaxUses,
                        5,
                        0.05f
                    );
                });
            }

            if (!levelTrades.isEmpty()) {
                event.getTrades().get(level).addAll(levelTrades);
                LootTickets.LOGGER.info("Added {} emerald-for-ticket trades at level {}", levelTrades.size(), level);
            }

            currentIndex = endIndex;
        }
    }

    private static void addTicketExchangeTrades(VillagerTradesEvent event) {
        List<VillagerTrades.ItemListing> trades = new ArrayList<>();

        for (Config.TicketExchangeTrade exchange : Config.ticketExchangeTrades) {
            trades.add((trader, random) -> {
                ResourceKey<LootTable> outputLootKey = ResourceKey.create(Registries.LOOT_TABLE, exchange.output());
                ItemStack outputTicket = LootTicketItem.createTicket(ModItems.LOOT_TICKET.get(), outputLootKey);
                outputTicket.setCount(exchange.outputCount());

                if (exchange.inputs().isEmpty()) {
                    return null;
                }

                Config.TicketExchangeTrade.TradeInput primaryInput = exchange.inputs().get(0);
                ResourceKey<LootTable> primaryLootKey = ResourceKey.create(Registries.LOOT_TABLE, primaryInput.lootTable());
                ItemStack primaryInputTicket = LootTicketItem.createTicket(ModItems.LOOT_TICKET.get(), primaryLootKey);

                Optional<ItemCost> secondaryCost = Optional.empty();
                if (exchange.inputs().size() > 1) {
                    Config.TicketExchangeTrade.TradeInput secondaryInput = exchange.inputs().get(1);
                    ResourceKey<LootTable> secondaryLootKey = ResourceKey.create(Registries.LOOT_TABLE, secondaryInput.lootTable());
                    ItemStack secondaryInputTicket = LootTicketItem.createTicket(ModItems.LOOT_TICKET.get(), secondaryLootKey);
                    secondaryCost = Optional.of(new ItemCost(
                        secondaryInputTicket.getItemHolder(),
                        secondaryInput.count(),
                        DataComponentPredicate.allOf(secondaryInputTicket.getComponents())
                    ));
                }

                ItemCost primaryCost = new ItemCost(
                    primaryInputTicket.getItemHolder(),
                    primaryInput.count(),
                    DataComponentPredicate.allOf(primaryInputTicket.getComponents())
                );

                return new MerchantOffer(
                    primaryCost,
                    secondaryCost,
                    outputTicket,
                    Config.tradeMaxUses,
                    8,
                    0.05f
                );
            });
        }

        // Add ticket exchange trades starting at level 2 (after they have basic tickets)
        if (!trades.isEmpty()) {
            event.getTrades().get(2).addAll(trades);
            LootTickets.LOGGER.info("Added {} ticket exchange trades at level 2", trades.size());
        }
    }
}
