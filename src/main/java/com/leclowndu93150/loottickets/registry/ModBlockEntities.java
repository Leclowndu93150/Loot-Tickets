package com.leclowndu93150.loottickets.registry;

import com.leclowndu93150.loottickets.LootTickets;
import com.leclowndu93150.loottickets.block.RedemptionCenterBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, LootTickets.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RedemptionCenterBlockEntity>> REDEMPTION_CENTER =
        BLOCK_ENTITIES.register("redemption_center", () ->
            BlockEntityType.Builder.of(
                RedemptionCenterBlockEntity::new,
                ModBlocks.REDEMPTION_CENTER.get()
            ).build(null)
        );
}
