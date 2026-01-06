package com.leclowndu93150.loottickets.event;

import com.leclowndu93150.loottickets.LootTickets;
import com.leclowndu93150.loottickets.network.ModNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = LootTickets.MODID)
public class PlayerJoinHandler {

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            ModNetworking.sendLootTablesToPlayer(serverPlayer);
        }
    }
}
