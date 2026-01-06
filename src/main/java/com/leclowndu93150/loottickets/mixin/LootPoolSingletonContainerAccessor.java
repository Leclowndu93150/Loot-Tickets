package com.leclowndu93150.loottickets.mixin;

import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LootPoolSingletonContainer.class)
public interface LootPoolSingletonContainerAccessor {
    @Accessor("weight")
    int getWeight();
}
