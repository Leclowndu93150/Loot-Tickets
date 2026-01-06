package com.leclowndu93150.loottickets;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = LootTickets.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue ENABLE_TRADES = BUILDER
        .comment("Enable villager trades for loot tickets")
        .define("trades.enabled", true);

    private static final ModConfigSpec.BooleanValue USE_MANUAL_TRADES = BUILDER
        .comment("Use manual trade list instead of automatic generation (recommended)")
        .define("trades.useManualTrades", true);

    private static final ModConfigSpec.ConfigValue<List<? extends String>> MANUAL_TRADES = BUILDER
        .comment("Manual trade list in format: 'lootTablePath:price'",
                 "Example: 'minecraft:chests/end_city_treasure:32' means end city tickets cost 32 emeralds",
                 "Trades are distributed evenly across villager levels 1-5")
        .defineListAllowEmpty("trades.manualTrades",
            List.of(
                // Common/Early Game (cheaper)
                "minecraft:chests/simple_dungeon:5",
                "minecraft:chests/abandoned_mineshaft:6",
                "minecraft:chests/shipwreck_treasure:7",
                "minecraft:chests/village/village_armorer:4",
                "minecraft:chests/village/village_weaponsmith:4",
                "minecraft:chests/village/village_toolsmith:4",
                "minecraft:chests/igloo_chest:6",
                "minecraft:chests/pillager_outpost:8",

                // Mid Game
                "minecraft:chests/desert_pyramid:10",
                "minecraft:chests/jungle_temple:10",
                "minecraft:chests/underwater_ruin_big:9",
                "minecraft:chests/buried_treasure:12",
                "minecraft:chests/nether_bridge:14",
                "minecraft:chests/ruined_portal:11",

                // Late Game
                "minecraft:chests/stronghold_corridor:18",
                "minecraft:chests/stronghold_library:20",
                "minecraft:chests/stronghold_crossing:18",
                "minecraft:chests/bastion_treasure:24",
                "minecraft:chests/bastion_bridge:20",

                // End Game (most expensive)
                "minecraft:chests/end_city_treasure:28",
                "minecraft:chests/ancient_city:32"
            ),
            Config::validatePriceOverride
        );

    // Automatic generation settings (used when useManualTrades is false)
    private static final ModConfigSpec.BooleanValue ENABLE_ALL_CHEST_LOOT_TABLES = BUILDER
        .comment("(Automatic Mode Only) Enable trades for all chest loot tables automatically (minecraft:chests/*)")
        .define("trades.auto.enableAllChestLootTables", false);

    private static final ModConfigSpec.IntValue BASE_PRICE = BUILDER
        .comment("(Automatic Mode Only) Base emerald price used for automatic price calculation")
        .defineInRange("trades.auto.basePrice", 8, 1, 64);

    private static final ModConfigSpec.ConfigValue<List<? extends String>> BLACKLISTED_LOOT_TABLES = BUILDER
        .comment("(Automatic Mode Only) Loot tables that should NOT be available for trade")
        .defineListAllowEmpty("trades.auto.blacklist",
            List.of(),
            Config::validateLootTableString
        );

    private static final ModConfigSpec.ConfigValue<List<? extends String>> REDEMPTION_BLACKLIST = BUILDER
        .comment("Loot tables that cannot be redeemed in the Redemption Center",
                 "These tables cause lag or other issues when generated (e.g., explorer maps)")
        .defineListAllowEmpty("redemption.blacklist",
            List.of(
                "minecraft:chests/shipwreck_map",
                "minecraft:chests/shipwreck_supply",
                "minecraft:chests/shipwreck_treasure"
            ),
            Config::validateLootTableString
        );

    // Common settings
    private static final ModConfigSpec.IntValue TRADE_MAX_USES = BUILDER
        .comment("Maximum number of uses per trade before villager needs to restock")
        .defineInRange("trades.maxUses", 3, 1, 64);

    private static final ModConfigSpec.ConfigValue<List<? extends String>> TICKET_EXCHANGE_TRADES = BUILDER
        .comment("Ticket-for-ticket trades in format: 'outputLootTable:count|inputLootTable1:count1,inputLootTable2:count2'",
                 "Example: 'minecraft:chests/stronghold_corridor:1|minecraft:chests/simple_dungeon:3,minecraft:chests/abandoned_mineshaft:2'",
                 "This would trade 3 dungeon tickets + 2 mineshaft tickets for 1 stronghold ticket",
                 "These trades become available at villager level 2")
        .defineListAllowEmpty("trades.ticketExchanges",
            List.of(
                "minecraft:chests/stronghold_corridor:1|minecraft:chests/simple_dungeon:5",
                "minecraft:chests/bastion_treasure:1|minecraft:chests/desert_pyramid:3,minecraft:chests/jungle_temple:3"
            ),
            Config::validateTradeString
        );

    static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean enableTrades;
    public static boolean useManualTrades;
    public static Map<ResourceLocation, Integer> manualTradeMap;
    public static boolean enableAllChestLootTables;
    public static int basePrice;
    public static int tradeMaxUses;
    public static java.util.Set<ResourceLocation> blacklistedLootTables;
    public static java.util.Set<ResourceLocation> redemptionBlacklist;
    public static List<TicketExchangeTrade> ticketExchangeTrades;

    public record TicketExchangeTrade(ResourceLocation output, int outputCount, List<TradeInput> inputs) {
        public record TradeInput(ResourceLocation lootTable, int count) {}
    }

    private static boolean validateLootTableString(Object obj) {
        if (obj instanceof String s) {
            try {
                ResourceLocation.parse(s);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    private static boolean validatePriceOverride(Object obj) {
        if (obj instanceof String s) {
            try {
                String[] parts = s.split(":");
                if (parts.length < 2) return false;
                ResourceLocation.parse(String.join(":", java.util.Arrays.copyOf(parts, parts.length - 1)));
                int price = Integer.parseInt(parts[parts.length - 1]);
                return price > 0 && price <= 64;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    private static boolean validateTradeString(Object obj) {
        if (obj instanceof String s) {
            try {
                String[] parts = s.split("\\|");
                if (parts.length != 2) return false;

                String[] outputParts = parts[0].split(":");
                if (outputParts.length < 2) return false;
                ResourceLocation.parse(String.join(":", java.util.Arrays.copyOf(outputParts, outputParts.length - 1)));
                Integer.parseInt(outputParts[outputParts.length - 1]);

                String[] inputs = parts[1].split(",");
                for (String input : inputs) {
                    String[] inputParts = input.split(":");
                    if (inputParts.length < 2) return false;
                    ResourceLocation.parse(String.join(":", java.util.Arrays.copyOf(inputParts, inputParts.length - 1)));
                    Integer.parseInt(inputParts[inputParts.length - 1]);
                }
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        enableTrades = ENABLE_TRADES.get();
        useManualTrades = USE_MANUAL_TRADES.get();
        enableAllChestLootTables = ENABLE_ALL_CHEST_LOOT_TABLES.get();
        basePrice = BASE_PRICE.get();
        tradeMaxUses = TRADE_MAX_USES.get();

        // Load manual trades
        manualTradeMap = new LinkedHashMap<>();
        for (String tradeStr : MANUAL_TRADES.get()) {
            try {
                String[] parts = tradeStr.split(":");
                int lastIdx = parts.length - 1;
                ResourceLocation lootTable = ResourceLocation.parse(String.join(":", java.util.Arrays.copyOf(parts, lastIdx)));
                int price = Integer.parseInt(parts[lastIdx]);
                manualTradeMap.put(lootTable, price);
            } catch (Exception e) {
                LOGGER.error("Failed to parse manual trade: " + tradeStr, e);
            }
        }

        blacklistedLootTables = new java.util.HashSet<>();
        for (String lootTableStr : BLACKLISTED_LOOT_TABLES.get()) {
            try {
                blacklistedLootTables.add(ResourceLocation.parse(lootTableStr));
            } catch (Exception e) {
                LOGGER.error("Failed to parse blacklisted loot table: " + lootTableStr, e);
            }
        }

        redemptionBlacklist = new java.util.HashSet<>();
        for (String lootTableStr : REDEMPTION_BLACKLIST.get()) {
            try {
                redemptionBlacklist.add(ResourceLocation.parse(lootTableStr));
            } catch (Exception e) {
                LOGGER.error("Failed to parse redemption blacklist entry: " + lootTableStr, e);
            }
        }

        ticketExchangeTrades = new ArrayList<>();
        for (String tradeStr : TICKET_EXCHANGE_TRADES.get()) {
            try {
                String[] parts = tradeStr.split("\\|");

                String[] outputParts = parts[0].split(":");
                int lastIdx = outputParts.length - 1;
                ResourceLocation outputLoot = ResourceLocation.parse(String.join(":", java.util.Arrays.copyOf(outputParts, lastIdx)));
                int outputCount = Integer.parseInt(outputParts[lastIdx]);

                List<TicketExchangeTrade.TradeInput> inputs = new ArrayList<>();
                String[] inputStrs = parts[1].split(",");
                for (String inputStr : inputStrs) {
                    String[] inputParts = inputStr.trim().split(":");
                    int inputLastIdx = inputParts.length - 1;
                    ResourceLocation inputLoot = ResourceLocation.parse(String.join(":", java.util.Arrays.copyOf(inputParts, inputLastIdx)));
                    int inputCount = Integer.parseInt(inputParts[inputLastIdx]);
                    inputs.add(new TicketExchangeTrade.TradeInput(inputLoot, inputCount));
                }

                ticketExchangeTrades.add(new TicketExchangeTrade(outputLoot, outputCount, inputs));
            } catch (Exception e) {
                LOGGER.error("Failed to parse ticket exchange trade: " + tradeStr, e);
            }
        }

        LOGGER.info("Loaded {} manual trades, {} ticket exchanges", manualTradeMap.size(), ticketExchangeTrades.size());
    }

    public static int getPriceForLootTable(ResourceLocation lootTable) {
        // In manual mode, just return the configured price
        if (useManualTrades && manualTradeMap.containsKey(lootTable)) {
            return manualTradeMap.get(lootTable);
        }

        // Fallback to automatic calculation
        return calculateAutomaticPrice(lootTable);
    }

    private static int calculateAutomaticPrice(ResourceLocation lootTable) {
        String path = lootTable.getPath().toLowerCase();
        String namespace = lootTable.getNamespace();

        int price = basePrice;

        if (path.contains("end_city") || path.contains("ancient_city")) {
            price = (int) (basePrice * 3.5);
        } else if (path.contains("stronghold") || path.contains("bastion") || path.contains("nether_bridge")) {
            price = (int) (basePrice * 2.5);
        } else if (path.contains("treasure") || path.contains("buried")) {
            price = (int) (basePrice * 2.0);
        } else if (path.contains("village") || path.contains("igloo") || path.contains("outpost")) {
            price = (int) (basePrice * 1.5);
        } else if (path.contains("dungeon") || path.contains("mineshaft") || path.contains("shipwreck")) {
            price = (int) (basePrice * 1.2);
        }

        if (!namespace.equals("minecraft")) {
            price = (int) (price * 1.3);
        }

        int hash = Math.abs(lootTable.toString().hashCode());
        int variance = (hash % 5) - 2;
        price += variance;

        return Math.max(1, Math.min(64, price));
    }

    public static boolean isLootTableTradeable(ResourceLocation lootTable) {
        if (blacklistedLootTables.contains(lootTable)) {
            return false;
        }

        // In manual mode, only trade what's explicitly configured
        if (useManualTrades) {
            return manualTradeMap.containsKey(lootTable);
        }

        // In automatic mode, use the old logic
        if (!enableAllChestLootTables) {
            return false;
        }

        String path = lootTable.getPath();
        return path.startsWith("chests/") || path.startsWith("gameplay/");
    }

    public static boolean isLootTableRedeemable(ResourceLocation lootTable) {
        return redemptionBlacklist == null || !redemptionBlacklist.contains(lootTable);
    }
}
