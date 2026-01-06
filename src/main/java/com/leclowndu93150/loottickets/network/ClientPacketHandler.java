package com.leclowndu93150.loottickets.network;

import net.neoforged.fml.ModList;

public class ClientPacketHandler {

    public static void onLootTablesReceived() {
        if (ModList.get().isLoaded("jei")) {
            try {
                Class<?> clazz = Class.forName("com.leclowndu93150.loottickets.compat.jei.LootTicketsJEIPlugin");
                clazz.getMethod("onLootTablesReceived").invoke(null);
            } catch (Exception ignored) {
            }
        }
    }
}
