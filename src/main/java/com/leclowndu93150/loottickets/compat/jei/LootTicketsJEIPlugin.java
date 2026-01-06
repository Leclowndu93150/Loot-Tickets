package com.leclowndu93150.loottickets.compat.jei;

import com.leclowndu93150.loottickets.LootTickets;
import com.leclowndu93150.loottickets.network.ClientLootTableCache;
import com.leclowndu93150.loottickets.network.LootTableSyncPayload;
import com.leclowndu93150.loottickets.registry.ModBlocks;
import com.leclowndu93150.loottickets.registry.ModItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.*;

@JeiPlugin
public class LootTicketsJEIPlugin implements IModPlugin {

    private static final ResourceLocation PLUGIN_ID = ResourceLocation.fromNamespaceAndPath(LootTickets.MODID, "jei_plugin");
    private static IJeiRuntime jeiRuntime;

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(
            VanillaTypes.ITEM_STACK,
            ModItems.LOOT_TICKET.get(),
            LootTicketSubtypeInterpreter.INSTANCE
        );
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
            new LootTicketRecipeCategory(registration.getJeiHelpers().getGuiHelper())
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(
            new ItemStack(ModBlocks.REDEMPTION_CENTER_ITEM.get()),
            LootTicketRecipeCategory.RECIPE_TYPE
        );

        registration.addRecipeCatalyst(
            new ItemStack(ModItems.LOOT_TICKET.get()),
            LootTicketRecipeCategory.RECIPE_TYPE
        );
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        jeiRuntime = runtime;
    }

    public static void onLootTablesReceived() {
        if (jeiRuntime == null) {
            return;
        }

        List<LootTicketRecipe> recipes = buildLootRecipes();
        if (!recipes.isEmpty()) {
            jeiRuntime.getRecipeManager().addRecipes(LootTicketRecipeCategory.RECIPE_TYPE, recipes);
        }
    }

    private static List<LootTicketRecipe> buildLootRecipes() {
        List<LootTicketRecipe> recipes = new ArrayList<>();

        Map<ResourceLocation, List<LootTableSyncPayload.LootEntryData>> cachedTables = ClientLootTableCache.getLootTables();

        for (Map.Entry<ResourceLocation, List<LootTableSyncPayload.LootEntryData>> entry : cachedTables.entrySet()) {
            ResourceKey<LootTable> tableKey = ResourceKey.create(Registries.LOOT_TABLE, entry.getKey());

            List<LootTicketRecipe.LootEntry> lootEntries = new ArrayList<>();
            for (LootTableSyncPayload.LootEntryData data : entry.getValue()) {
                lootEntries.add(new LootTicketRecipe.LootEntry(new ItemStack(data.item()), data.chance()));
            }

            if (!lootEntries.isEmpty()) {
                recipes.add(new LootTicketRecipe(tableKey, lootEntries));
            }
        }

        recipes.sort(Comparator.comparing(a -> a.lootTable().location()));

        return recipes;
    }
}
