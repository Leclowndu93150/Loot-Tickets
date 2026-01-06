package com.leclowndu93150.loottickets.compat.jei;

import com.leclowndu93150.loottickets.LootTickets;
import com.leclowndu93150.loottickets.item.LootTicketItem;
import com.leclowndu93150.loottickets.registry.ModBlocks;
import com.leclowndu93150.loottickets.registry.ModItems;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

import java.util.ArrayList;
import java.util.List;

public class LootTicketRecipeCategory implements IRecipeCategory<LootTicketRecipe> {

    public static final RecipeType<LootTicketRecipe> RECIPE_TYPE = RecipeType.create(
        LootTickets.MODID, "loot_ticket", LootTicketRecipe.class
    );

    private final IDrawable background;
    private final IDrawable icon;
    private final Component title;
    private static final int SLOTS_PER_PAGE = 54;
    private static final int ITEMS_PER_ROW = 9;

    public LootTicketRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(170, 128);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
            new ItemStack(ModBlocks.REDEMPTION_CENTER_ITEM.get()));
        this.title = Component.translatable("gui.loottickets.jei.loot_ticket");
    }

    @Override
    public RecipeType<LootTicketRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, LootTicketRecipe recipe, IFocusGroup focuses) {
        ItemStack ticketStack = LootTicketItem.createTicket(ModItems.LOOT_TICKET.get(), recipe.lootTable());
        builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).addItemStack(ticketStack);

        List<LootTicketRecipe.LootEntry> entries = recipe.entries();

        int x = 8;
        int y = 20;
        int slotIndex = 0;

        for (int i = 0; i < Math.min(SLOTS_PER_PAGE, entries.size()); i++) {
            int startIndex = i;

            List<ItemStack> cyclableStacks = new ArrayList<>();
            for (int j = startIndex; j < entries.size(); j += SLOTS_PER_PAGE) {
                cyclableStacks.add(entries.get(j).stack());
            }

            builder.addSlot(RecipeIngredientRole.OUTPUT, x, y)
                .addItemStacks(cyclableStacks)
                .addTooltipCallback((recipeSlotView, tooltip) -> {
                    long currentTime = System.currentTimeMillis() / 1000;
                    int cycleIndex = (int) (currentTime % (entries.size() / SLOTS_PER_PAGE + 1));
                    int actualIndex = startIndex + (cycleIndex * SLOTS_PER_PAGE);

                    if (actualIndex < entries.size()) {
                        LootTicketRecipe.LootEntry entry = entries.get(actualIndex);
                        float percent = entry.chance() * 100;
                        String chanceText;
                        if (percent >= 1) {
                            chanceText = String.format("%.0f%%", percent);
                        } else {
                            chanceText = String.format("%.1f%%", percent);
                        }
                        tooltip.add(Component.literal("Chance: " + chanceText)
                            .withStyle(ChatFormatting.YELLOW));
                    }
                });

            x += 18;
            slotIndex++;
            if (slotIndex % ITEMS_PER_ROW == 0) {
                x = 8;
                y += 18;
            }
        }
    }

    @Override
    public void draw(LootTicketRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        String name = LootTicketItem.formatLootTablePath(recipe.lootTable().location());
        var font = Minecraft.getInstance().font;
        int titleWidth = font.width(name);
        int centerX = (170 - titleWidth) / 2;

        guiGraphics.drawString(font, name, centerX, 5, 0x404040, false);
    }

    @Override
    public List<Component> getTooltipStrings(LootTicketRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        String name = LootTicketItem.formatLootTablePath(recipe.lootTable().location());
        var font = Minecraft.getInstance().font;
        int titleWidth = font.width(name);
        int centerX = (170 - titleWidth) / 2;

        if (mouseX >= centerX && mouseX <= centerX + titleWidth && mouseY >= 5 && mouseY <= 5 + font.lineHeight) {
            String namespace = recipe.lootTable().location().getNamespace();
            String modName = ModList.get().getModContainerById(namespace)
                .map(container -> container.getModInfo().getDisplayName())
                .orElse(namespace);

            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.literal(modName).withStyle(ChatFormatting.BLUE));
            tooltip.add(Component.literal(recipe.lootTable().location().toString()).withStyle(ChatFormatting.GRAY));
            return tooltip;
        }

        return List.of();
    }

    @Override
    public ResourceLocation getRegistryName(LootTicketRecipe recipe) {
        return recipe.getUid();
    }
}
