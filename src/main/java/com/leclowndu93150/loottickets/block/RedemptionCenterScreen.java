package com.leclowndu93150.loottickets.block;

import com.leclowndu93150.loottickets.LootTickets;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class RedemptionCenterScreen extends AbstractContainerScreen<RedemptionCenterMenu> {

    private static final ResourceLocation CONTAINER_BACKGROUND =
        ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");
    private static final ResourceLocation TICKET_SLOT_OVERLAY =
        ResourceLocation.fromNamespaceAndPath(LootTickets.MODID, "textures/gui/ticket_slot_overlay.png");
    private static final ResourceLocation BLOCKED_WARNING =
        ResourceLocation.fromNamespaceAndPath(LootTickets.MODID, "textures/gui/blocked_warning.png");

    private static final int WARNING_SIZE = 8;

    public RedemptionCenterScreen(RedemptionCenterMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
        this.imageHeight = 114 + 3 * 18;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    private boolean isHoveringWarning(int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        int warningX = x + 8 + 16 - WARNING_SIZE;
        int warningY = y + 18;
        return mouseX >= warningX && mouseX < warningX + WARNING_SIZE && mouseY >= warningY && mouseY < warningY + WARNING_SIZE;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // Render warning icon on top of items (blocked or blacklisted)
        if (menu.isBlocked() || menu.isBlacklisted()) {
            int x = (this.width - this.imageWidth) / 2;
            int y = (this.height - this.imageHeight) / 2;
            int warningX = x + 8 + 16 - WARNING_SIZE;
            int warningY = y + 18;

            blitNoDepth(guiGraphics, BLOCKED_WARNING, warningX, warningY, WARNING_SIZE, WARNING_SIZE);
        }

        // Render tooltips - warning tooltip takes priority over slot tooltip
        if (isHoveringWarning(mouseX, mouseY)) {
            if (menu.isBlacklisted()) {
                guiGraphics.renderTooltip(this.font, List.of(
                    Component.translatable("gui.loottickets.blacklisted.title").withStyle(ChatFormatting.RED),
                    Component.translatable("gui.loottickets.blacklisted.description").withStyle(ChatFormatting.GRAY)
                ), java.util.Optional.empty(), mouseX, mouseY);
            } else if (menu.isBlocked()) {
                guiGraphics.renderTooltip(this.font, List.of(
                    Component.translatable("gui.loottickets.blocked.title").withStyle(ChatFormatting.RED),
                    Component.translatable("gui.loottickets.blocked.description").withStyle(ChatFormatting.GRAY)
                ), java.util.Optional.empty(), mouseX, mouseY);
            } else {
                this.renderTooltip(guiGraphics, mouseX, mouseY);
            }
        } else {
            this.renderTooltip(guiGraphics, mouseX, mouseY);
        }
    }

    private void blitNoDepth(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y, int width, int height) {
        RenderSystem.disableDepthTest();
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        Matrix4f matrix = guiGraphics.pose().last().pose();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.addVertex(matrix, x, y, 0).setUv(0, 0);
        buffer.addVertex(matrix, x, y + height, 0).setUv(0, 1);
        buffer.addVertex(matrix, x + width, y + height, 0).setUv(1, 1);
        buffer.addVertex(matrix, x + width, y, 0).setUv(1, 0);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.enableDepthTest();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(CONTAINER_BACKGROUND, x, y, 0, 0, this.imageWidth, 3 * 18 + 17);
        guiGraphics.blit(CONTAINER_BACKGROUND, x, y + 3 * 18 + 17, 0, 126, this.imageWidth, 96);

        // Render ticket slot overlay on slot 0
        guiGraphics.blit(TICKET_SLOT_OVERLAY, x + 8, y + 18, 0, 0, 16, 16, 16, 16);
    }
}
