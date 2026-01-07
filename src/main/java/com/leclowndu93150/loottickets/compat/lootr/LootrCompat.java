package com.leclowndu93150.loottickets.compat.lootr;

import com.leclowndu93150.loottickets.component.LootTicketData;
import com.leclowndu93150.loottickets.event.TicketPickupHandler;
import com.leclowndu93150.loottickets.registry.ModDataComponents;
import com.leclowndu93150.loottickets.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.neoforge.network.PacketDistributor;
import noobanidus.mods.lootr.common.api.LootrAPI;
import noobanidus.mods.lootr.common.api.data.ILootrInfoProvider;
import noobanidus.mods.lootr.common.api.data.blockentity.ILootrBlockEntity;
import noobanidus.mods.lootr.common.api.data.inventory.ILootrInventory;
import noobanidus.mods.lootr.neoforge.network.toClient.PacketOpenContainer;

public class LootrCompat {

    public static boolean isLootrBlockEntity(BlockEntity blockEntity) {
        return blockEntity instanceof ILootrBlockEntity;
    }

    public static boolean handleLootrExtraction(BlockEntity blockEntity, Player player, Level level, BlockPos pos) {
        if (!(blockEntity instanceof ILootrBlockEntity lootrBE)) {
            return false;
        }

        ResourceKey<LootTable> lootTable = lootrBE.getInfoLootTable();

        if (lootTable == null) {
            if (!level.isClientSide) {
                level.playSound(null, pos, SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
            return true;
        }

        if (!level.isClientSide && level instanceof ServerLevel && player instanceof ServerPlayer serverPlayer) {
            if (lootrBE.hasServerOpened(serverPlayer)) {
                level.playSound(null, pos, SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1.0F, 1.0F);
                return true;
            }

            ItemStack ticket = new ItemStack(ModItems.LOOT_TICKET.get());
            ticket.set(ModDataComponents.LOOT_TICKET_DATA.get(), new LootTicketData(lootTable));

            TicketPickupHandler.giveTicketToPlayer(player, ticket);

            if (lootrBE instanceof ILootrInfoProvider provider) {
                provider.addOpener(serverPlayer);
                provider.performUpdate(serverPlayer);
                ILootrInventory inventory = LootrAPI.getInventory(provider, serverPlayer);
                if (inventory != null) {
                    inventory.clearContent();
                }

                sendVisualUpdatePacket(serverPlayer, pos);
            }

            level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.2F);
        }

        return true;
    }

    private static void sendVisualUpdatePacket(ServerPlayer player, BlockPos pos) {
        PacketDistributor.sendToPlayer(player, new PacketOpenContainer(pos));
    }
}
