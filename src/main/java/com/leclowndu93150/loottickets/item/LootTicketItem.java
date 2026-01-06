package com.leclowndu93150.loottickets.item;

import com.leclowndu93150.loottickets.component.LootTicketData;
import com.leclowndu93150.loottickets.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.storage.loot.LootTable;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LootTicketItem extends Item {

    public LootTicketItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        LootTicketData data = stack.get(ModDataComponents.LOOT_TICKET_DATA.get());
        if (data != null) {
            ResourceLocation path = data.lootTable().location();
            String formatted = formatLootTablePath(path);
            tooltipComponents.add(Component.literal(formatted)
                .withStyle(ChatFormatting.GOLD));

            if (tooltipFlag.isAdvanced()) {
                tooltipComponents.add(Component.literal(path.toString())
                    .withStyle(ChatFormatting.DARK_GRAY));
            }
        }
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    public static String formatLootTablePath(ResourceLocation location) {
        String path = location.getPath();

        if (path.startsWith("chests/")) {
            path = path.substring(7);
        } else if (path.startsWith("gameplay/")) {
            path = path.substring(9);
        } else if (path.startsWith("entities/")) {
            path = path.substring(9);
        }

        return Arrays.stream(path.split("[_/]"))
            .filter(s -> !s.isEmpty())
            .map(s -> Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase())
            .collect(Collectors.joining(" "));
    }

    public static int getColorForLootTable(ResourceKey<LootTable> lootTable) {
        if (lootTable == null) {
            return -1;
        }
        int hash = lootTable.location().toString().hashCode();
        float hue = (hash & 0xFFFF) / 65535.0f;
        float saturation = 0.7f;
        float brightness = 0.9f;
        return Color.HSBtoRGB(hue, saturation, brightness);
    }

    public static ItemStack createTicket(Item ticketItem, ResourceKey<LootTable> lootTable) {
        ItemStack stack = new ItemStack(ticketItem);
        stack.set(ModDataComponents.LOOT_TICKET_DATA.get(), new LootTicketData(lootTable));
        return stack;
    }
}
