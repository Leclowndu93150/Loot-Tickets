package com.leclowndu93150.loottickets.registry;

import com.google.common.collect.ImmutableSet;
import com.leclowndu93150.loottickets.LootTickets;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModVillagerProfessions {

    public static final DeferredRegister<VillagerProfession> VILLAGER_PROFESSIONS =
        DeferredRegister.create(Registries.VILLAGER_PROFESSION, LootTickets.MODID);

    public static final DeferredHolder<VillagerProfession, VillagerProfession> REDEMPTION_CENTER_EMPLOYEE =
        VILLAGER_PROFESSIONS.register("redemption_center_employee", () -> new VillagerProfession(
            "redemption_center_employee",
            holder -> holder.value().equals(ModPOITypes.REDEMPTION_CENTER_POI.get()),
            holder -> holder.value().equals(ModPOITypes.REDEMPTION_CENTER_POI.get()),
            ImmutableSet.of(),
            ImmutableSet.of(),
            SoundEvents.VILLAGER_WORK_LIBRARIAN
        ));

    @EventBusSubscriber(modid = LootTickets.MODID, bus = EventBusSubscriber.Bus.MOD)
    public static class TradesSetup {
        @SubscribeEvent
        public static void setup(FMLCommonSetupEvent event) {
            event.enqueueWork(() -> {
                Int2ObjectMap<VillagerTrades.ItemListing[]> trades = new Int2ObjectOpenHashMap<>();
                for (int i = 1; i <= 5; i++) {
                    trades.put(i, new VillagerTrades.ItemListing[0]);
                }
                VillagerTrades.TRADES.put(REDEMPTION_CENTER_EMPLOYEE.get(), trades);
            });
        }
    }
}
