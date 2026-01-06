package com.leclowndu93150.loottickets.network;

import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientLootTableCache {

    private static Map<ResourceLocation, List<LootTableSyncPayload.LootEntryData>> cachedLootTables = new ConcurrentHashMap<>();

    public static void setLootTables(Map<ResourceLocation, List<LootTableSyncPayload.LootEntryData>> lootTables) {
        cachedLootTables = new ConcurrentHashMap<>(lootTables);
    }

    public static Map<ResourceLocation, List<LootTableSyncPayload.LootEntryData>> getLootTables() {
        return Collections.unmodifiableMap(cachedLootTables);
    }

    public static void clear() {
        cachedLootTables.clear();
    }
}
