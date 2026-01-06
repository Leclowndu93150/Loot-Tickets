package com.leclowndu93150.loottickets.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class TicketExtractorItem extends Item {

    public TicketExtractorItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("item.loottickets.ticket_extractor.tooltip")
            .withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
