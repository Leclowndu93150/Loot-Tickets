package com.leclowndu93150.loottickets.compat.jei;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.List;

public record LootTicketRecipe(
    ResourceKey<LootTable> lootTable,
    List<LootEntry> entries
) {
    public record LootEntry(ItemStack stack, float chance) {}

    public ResourceLocation getUid() {
        return lootTable.location();
    }
}
