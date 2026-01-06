package com.leclowndu93150.loottickets.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;

public record LootTicketData(ResourceKey<LootTable> lootTable) {

    public static final Codec<LootTicketData> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            ResourceKey.codec(Registries.LOOT_TABLE)
                .fieldOf("loot_table")
                .forGetter(LootTicketData::lootTable)
        ).apply(instance, LootTicketData::new)
    );

    public static final StreamCodec<ByteBuf, LootTicketData> STREAM_CODEC = StreamCodec.composite(
        ResourceLocation.STREAM_CODEC.map(
            loc -> ResourceKey.create(Registries.LOOT_TABLE, loc),
            ResourceKey::location
        ),
        LootTicketData::lootTable,
        LootTicketData::new
    );
}
