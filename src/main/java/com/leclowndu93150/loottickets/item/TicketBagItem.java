package com.leclowndu93150.loottickets.item;

import com.leclowndu93150.loottickets.component.LootTicketData;
import com.leclowndu93150.loottickets.item.container.TicketBagMenu;
import com.leclowndu93150.loottickets.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TicketBagItem extends Item {

    public static final int CONTAINER_SIZE = 54;

    public TicketBagItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            int slotIndex = hand == InteractionHand.MAIN_HAND ? player.getInventory().selected : -1;
            serverPlayer.openMenu(new SimpleMenuProvider(
                (containerId, playerInventory, p) -> new TicketBagMenu(containerId, playerInventory, stack, slotIndex),
                Component.translatable("container.loottickets.ticket_bag")
            ), buf -> buf.writeInt(slotIndex));
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        ItemContainerContents contents = stack.get(ModDataComponents.BAG_CONTENTS.get());

        int totalTickets = 0;
        int usedSlots = 0;
        java.util.Map<String, Integer> ticketCounts = new java.util.LinkedHashMap<>();

        if (contents != null) {
            for (int i = 0; i < contents.getSlots(); i++) {
                ItemStack slotStack = contents.getStackInSlot(i);
                if (!slotStack.isEmpty()) {
                    totalTickets += slotStack.getCount();
                    usedSlots++;

                    LootTicketData data = slotStack.get(ModDataComponents.LOOT_TICKET_DATA.get());
                    if (data != null) {
                        String tableName = formatLootTableName(data.lootTable().location().getPath());
                        ticketCounts.merge(tableName, slotStack.getCount(), Integer::sum);
                    }
                }
            }
        }

        // Show capacity bar
        float fillPercent = (float) usedSlots / CONTAINER_SIZE;
        ChatFormatting capacityColor = fillPercent >= 1.0f ? ChatFormatting.RED :
                                        fillPercent >= 0.75f ? ChatFormatting.YELLOW : ChatFormatting.GREEN;
        tooltipComponents.add(Component.literal(String.format("%d/%d slots ", usedSlots, CONTAINER_SIZE))
                .withStyle(capacityColor)
                .append(Component.literal("(" + totalTickets + " tickets)").withStyle(ChatFormatting.GRAY)));

        if (!ticketCounts.isEmpty()) {
            tooltipComponents.add(Component.empty());

            // Show up to 5 ticket types, sorted by count
            List<java.util.Map.Entry<String, Integer>> sorted = ticketCounts.entrySet().stream()
                    .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                    .limit(5)
                    .toList();

            for (var entry : sorted) {
                tooltipComponents.add(Component.literal(" " + entry.getValue() + "x ")
                        .withStyle(ChatFormatting.WHITE)
                        .append(Component.literal(entry.getKey()).withStyle(ChatFormatting.AQUA)));
            }

            int remaining = ticketCounts.size() - 5;
            if (remaining > 0) {
                tooltipComponents.add(Component.literal(" +" + remaining + " more types...")
                        .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            }
        }

        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    private static String formatLootTableName(String path) {
        String name = path;
        if (name.contains("/")) {
            name = name.substring(name.lastIndexOf('/') + 1);
        }

        String[] words = name.split("_");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                if (result.length() > 0) result.append(" ");
                result.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) result.append(word.substring(1));
            }
        }
        return result.toString();
    }

    public static int getTicketCount(ItemStack bagStack) {
        ItemContainerContents contents = bagStack.get(ModDataComponents.BAG_CONTENTS.get());
        if (contents == null) {
            return 0;
        }

        int count = 0;
        for (int i = 0; i < contents.getSlots(); i++) {
            ItemStack slotStack = contents.getStackInSlot(i);
            if (!slotStack.isEmpty()) {
                count += slotStack.getCount();
            }
        }
        return count;
    }

    public static ItemStack getFirstTicket(ItemStack bagStack) {
        ItemContainerContents contents = bagStack.get(ModDataComponents.BAG_CONTENTS.get());
        if (contents == null) {
            return ItemStack.EMPTY;
        }

        for (int i = 0; i < contents.getSlots(); i++) {
            ItemStack slotStack = contents.getStackInSlot(i);
            if (!slotStack.isEmpty()) {
                return slotStack;
            }
        }
        return ItemStack.EMPTY;
    }

    public static void removeOneTicket(ItemStack bagStack) {
        ItemContainerContents contents = bagStack.get(ModDataComponents.BAG_CONTENTS.get());
        if (contents == null) {
            return;
        }

        List<ItemStack> items = new java.util.ArrayList<>();
        for (int i = 0; i < contents.getSlots(); i++) {
            items.add(contents.getStackInSlot(i).copy());
        }

        for (int i = 0; i < items.size(); i++) {
            ItemStack slotStack = items.get(i);
            if (!slotStack.isEmpty()) {
                slotStack.shrink(1);
                break;
            }
        }

        bagStack.set(ModDataComponents.BAG_CONTENTS.get(), ItemContainerContents.fromItems(items));
    }
}
