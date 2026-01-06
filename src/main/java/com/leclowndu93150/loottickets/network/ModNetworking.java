package com.leclowndu93150.loottickets.network;

import com.leclowndu93150.loottickets.LootTickets;
import com.leclowndu93150.loottickets.mixin.LootItemAccessor;
import com.leclowndu93150.loottickets.mixin.LootPoolAccessor;
import com.leclowndu93150.loottickets.mixin.LootPoolSingletonContainerAccessor;
import com.leclowndu93150.loottickets.mixin.LootTableAccessor;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = LootTickets.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModNetworking {

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(LootTickets.MODID);

        registrar.playToClient(
            LootTableSyncPayload.TYPE,
            LootTableSyncPayload.STREAM_CODEC,
            (payload, context) -> {
                context.enqueueWork(() -> {
                    ClientLootTableCache.setLootTables(payload.lootTables());
                    ClientPacketHandler.onLootTablesReceived();
                });
            }
        );
    }

    public static void sendLootTablesToPlayer(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        Map<ResourceLocation, List<LootTableSyncPayload.LootEntryData>> lootTableData = new HashMap<>();

        server.reloadableRegistries().get().registryOrThrow(Registries.LOOT_TABLE)
            .holders()
            .forEach(holder -> {
                try {
                    LootTable table = holder.value();
                    if (table.getParamSet() == LootContextParamSets.CHEST) {
                        List<LootTableSyncPayload.LootEntryData> entries = analyzeLootTable(table);
                        if (!entries.isEmpty()) {
                            lootTableData.put(holder.key().location(), entries);
                        }
                    }
                } catch (Exception ignored) {
                }
            });

        PacketDistributor.sendToPlayer(player, new LootTableSyncPayload(lootTableData));
    }

    private static List<LootTableSyncPayload.LootEntryData> analyzeLootTable(LootTable table) {
        Map<Item, Float> itemChances = new HashMap<>();

        List<LootPool> pools = ((LootTableAccessor) table).getPools();
        for (LootPool pool : pools) {
            analyzePool(pool, itemChances);
        }

        List<LootTableSyncPayload.LootEntryData> entries = new ArrayList<>();
        for (Map.Entry<Item, Float> entry : itemChances.entrySet()) {
            entries.add(new LootTableSyncPayload.LootEntryData(entry.getKey(), entry.getValue()));
        }

        entries.sort((a, b) -> Float.compare(b.chance(), a.chance()));
        return entries;
    }

    private static void analyzePool(LootPool pool, Map<Item, Float> itemChances) {
        List<LootPoolEntryContainer> entries = ((LootPoolAccessor) pool).getEntries();

        int totalWeight = 0;
        List<ItemWithWeight> itemsWithWeight = new ArrayList<>();

        for (LootPoolEntryContainer entry : entries) {
            if (entry instanceof LootItem lootItem) {
                Holder<Item> itemHolder = ((LootItemAccessor) lootItem).getItem();
                int weight = ((LootPoolSingletonContainerAccessor) lootItem).getWeight();
                if (weight <= 0) weight = 1;

                itemsWithWeight.add(new ItemWithWeight(itemHolder.value(), weight));
                totalWeight += weight;
            }
        }

        if (totalWeight > 0) {
            for (ItemWithWeight item : itemsWithWeight) {
                float chance = (float) item.weight / totalWeight;
                itemChances.merge(item.item, chance, Float::max);
            }
        }
    }

    private record ItemWithWeight(Item item, int weight) {}
}
