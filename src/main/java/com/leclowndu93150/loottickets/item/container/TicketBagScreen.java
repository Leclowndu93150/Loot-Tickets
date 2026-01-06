package com.leclowndu93150.loottickets.item.container;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TicketBagScreen extends AbstractContainerScreen<TicketBagMenu> {

    private static final ResourceLocation CONTAINER_BACKGROUND =
        ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");

    public TicketBagScreen(TicketBagMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageHeight = 222;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2 + 1;
        guiGraphics.blit(CONTAINER_BACKGROUND, x, y, 0, 0, this.imageWidth, 6 * 18 + 17);
        guiGraphics.blit(CONTAINER_BACKGROUND, x, y + 6 * 18 + 17, 0, 126, this.imageWidth, 96);
    }
}
