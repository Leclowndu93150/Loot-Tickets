package com.leclowndu93150.loottickets.registry;

import com.leclowndu93150.loottickets.LootTickets;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, LootTickets.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> LOOT_TICKETS_TAB =
        CREATIVE_MODE_TABS.register("loot_tickets_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.loottickets"))
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> ModItems.LOOT_TICKET.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(ModItems.TICKET_EXTRACTOR.get());
                output.accept(ModItems.LOOT_TICKET.get());
                output.accept(ModItems.TICKET_BAG.get());
                output.accept(ModBlocks.REDEMPTION_CENTER_ITEM.get());
            })
            .build()
        );
}
