package com.leclowndu93150.loottickets;

import com.leclowndu93150.loottickets.block.RedemptionCenterScreen;
import com.leclowndu93150.loottickets.item.container.TicketBagScreen;
import com.leclowndu93150.loottickets.component.LootTicketData;
import com.leclowndu93150.loottickets.item.LootTicketItem;
import com.leclowndu93150.loottickets.registry.*;
import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

@Mod(LootTickets.MODID)
public class LootTickets {
    public static final String MODID = "loottickets";
    public static final Logger LOGGER = LogUtils.getLogger();

    public LootTickets(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        ModDataComponents.DATA_COMPONENTS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModMenuTypes.MENUS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        ModPOITypes.POI_TYPES.register(modEventBus);
        ModVillagerProfessions.VILLAGER_PROFESSIONS.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Loot Tickets mod initialized!");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Loot Tickets: Server starting");
    }

    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("Loot Tickets: Client setup");
        }

        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(ModMenuTypes.REDEMPTION_CENTER_MENU.get(), RedemptionCenterScreen::new);
            event.register(ModMenuTypes.TICKET_BAG_MENU.get(), TicketBagScreen::new);
        }

        @SubscribeEvent
        public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
            event.register((stack, tintIndex) -> {
                if (tintIndex == 1) {
                    LootTicketData data = stack.get(ModDataComponents.LOOT_TICKET_DATA.get());
                    if (data != null) {
                        return LootTicketItem.getColorForLootTable(data.lootTable());
                    }
                    return 0xFFFFFF;
                }
                return -1;
            }, ModItems.LOOT_TICKET.get());
        }
    }
}
