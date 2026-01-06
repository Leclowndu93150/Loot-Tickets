package com.leclowndu93150.loottickets.registry;

import com.leclowndu93150.loottickets.LootTickets;
import com.leclowndu93150.loottickets.mixin.PoiTypesAccessor;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModPOITypes {

    public static final DeferredRegister<PoiType> POI_TYPES =
        DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, LootTickets.MODID);

    public static final DeferredHolder<PoiType, PoiType> REDEMPTION_CENTER_POI = POI_TYPES.register(
        "redemption_center_poi",
        () -> new PoiType(PoiTypesAccessor.invokeGetBlockStates(ModBlocks.REDEMPTION_CENTER.get()), 1, 1)
    );
}
