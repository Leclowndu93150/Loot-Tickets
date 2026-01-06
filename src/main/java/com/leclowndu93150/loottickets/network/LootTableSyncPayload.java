package com.leclowndu93150.loottickets.network;

import com.leclowndu93150.loottickets.LootTickets;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

public record LootTableSyncPayload(Map<ResourceLocation, List<LootEntryData>> lootTables) implements CustomPacketPayload {

    public static final Type<LootTableSyncPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LootTickets.MODID, "loot_table_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, LootTableSyncPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.map(
            HashMap::new,
            ResourceLocation.STREAM_CODEC,
            LootEntryData.LIST_STREAM_CODEC
        ),
        LootTableSyncPayload::lootTables,
        LootTableSyncPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public record LootEntryData(Item item, float chance) {
        public static final StreamCodec<RegistryFriendlyByteBuf, LootEntryData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(BuiltInRegistries.ITEM.key()),
            LootEntryData::item,
            ByteBufCodecs.FLOAT,
            LootEntryData::chance,
            LootEntryData::new
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, List<LootEntryData>> LIST_STREAM_CODEC =
            LootEntryData.STREAM_CODEC.apply(ByteBufCodecs.list());
    }
}
