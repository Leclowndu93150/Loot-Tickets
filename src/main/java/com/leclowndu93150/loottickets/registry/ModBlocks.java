package com.leclowndu93150.loottickets.registry;

import com.leclowndu93150.loottickets.LootTickets;
import com.leclowndu93150.loottickets.block.RedemptionCenterBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(LootTickets.MODID);

    public static final DeferredBlock<RedemptionCenterBlock> REDEMPTION_CENTER = BLOCKS.register(
        "redemption_center",
        () -> new RedemptionCenterBlock(BlockBehaviour.Properties.of()
            .strength(2.5F)
            .sound(SoundType.WOOD)
            .requiresCorrectToolForDrops())
    );

    public static final DeferredItem<BlockItem> REDEMPTION_CENTER_ITEM = ModItems.ITEMS.register(
        "redemption_center",
        () -> new BlockItem(REDEMPTION_CENTER.get(), new Item.Properties())
    );
}
