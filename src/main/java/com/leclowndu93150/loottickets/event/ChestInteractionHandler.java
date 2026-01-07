package com.leclowndu93150.loottickets.event;

import com.leclowndu93150.loottickets.LootTickets;
import com.leclowndu93150.loottickets.compat.lootr.LootrCompat;
import com.leclowndu93150.loottickets.component.LootTicketData;
import com.leclowndu93150.loottickets.registry.ModDataComponents;
import com.leclowndu93150.loottickets.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TrappedChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = LootTickets.MODID)
public class ChestInteractionHandler {

    private static final String LOOTR_MODID = "lootr";

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        ItemStack heldItem = event.getItemStack();

        if (!heldItem.is(ModItems.TICKET_EXTRACTOR.get())) {
            return;
        }

        if (player instanceof FakePlayer) {
            return;
        }

        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (ModList.get().isLoaded(LOOTR_MODID)) {
            if (handleLootrInteraction(blockEntity, player, level, pos, event)) {
                return;
            }
        }

        if (!(blockEntity instanceof RandomizableContainer container)) {
            return;
        }

        ResourceKey<LootTable> lootTable = container.getLootTable();

        if (lootTable != null) {
            if (!level.isClientSide && level instanceof ServerLevel) {
                ItemStack ticket = new ItemStack(ModItems.LOOT_TICKET.get());
                ticket.set(ModDataComponents.LOOT_TICKET_DATA.get(), new LootTicketData(lootTable));

                TicketPickupHandler.giveTicketToPlayer(player, ticket);

                container.setLootTable(null);
                blockEntity.setChanged();

                triggerTrapIfNeeded(level, pos, blockEntity, player);

                level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP,
                    SoundSource.PLAYERS, 1.0F, 1.2F);
            }

            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        } else {
            if (!level.isClientSide) {
                level.playSound(null, pos, SoundEvents.VILLAGER_NO,
                    SoundSource.PLAYERS, 1.0F, 1.0F);
            }

            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }

    private static boolean handleLootrInteraction(BlockEntity blockEntity, Player player, Level level, BlockPos pos, PlayerInteractEvent.RightClickBlock event) {
        if (blockEntity != null && LootrCompat.isLootrBlockEntity(blockEntity)) {
            boolean handled = LootrCompat.handleLootrExtraction(blockEntity, player, level, pos);
            if (handled) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
            }
            return handled;
        }
        return false;
    }

    private static void triggerTrapIfNeeded(Level level, BlockPos pos, BlockEntity blockEntity, Player player) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof TrappedChestBlock && blockEntity instanceof ChestBlockEntity chestBE) {
            // Temporarily increase open count so getSignal() returns > 0
            chestBE.startOpen(player);
            chestBE.stopOpen(player);
        }
    }
}
