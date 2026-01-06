package com.leclowndu93150.loottickets.registry;

import com.leclowndu93150.loottickets.LootTickets;
import com.leclowndu93150.loottickets.component.LootTicketData;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.UnaryOperator;

public class ModDataComponents {

    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
        DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, LootTickets.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<LootTicketData>> LOOT_TICKET_DATA =
        DATA_COMPONENTS.register("loot_ticket_data", () ->
            DataComponentType.<LootTicketData>builder()
                .persistent(LootTicketData.CODEC)
                .networkSynchronized(LootTicketData.STREAM_CODEC)
                .build()
        );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemContainerContents>> BAG_CONTENTS =
        DATA_COMPONENTS.register("bag_contents", () ->
            DataComponentType.<ItemContainerContents>builder()
                .persistent(ItemContainerContents.CODEC)
                .networkSynchronized(ItemContainerContents.STREAM_CODEC)
                .build()
        );
}
