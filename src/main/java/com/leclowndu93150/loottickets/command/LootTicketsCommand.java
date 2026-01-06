package com.leclowndu93150.loottickets.command;

import com.leclowndu93150.loottickets.LootTickets;
import com.leclowndu93150.loottickets.component.LootTicketData;
import com.leclowndu93150.loottickets.registry.ModDataComponents;
import com.leclowndu93150.loottickets.registry.ModItems;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Collection;

@EventBusSubscriber(modid = LootTickets.MODID)
public class LootTicketsCommand {

    private static final SuggestionProvider<CommandSourceStack> CHEST_LOOT_TABLE_SUGGESTIONS = (context, builder) -> {
        var registry = context.getSource().getServer().reloadableRegistries().get().registryOrThrow(Registries.LOOT_TABLE);
        return SharedSuggestionProvider.suggestResource(
            registry.holders()
                .filter(holder -> holder.value().getParamSet() == LootContextParamSets.CHEST)
                .map(holder -> holder.key().location()),
            builder
        );
    };

    private static final SuggestionProvider<CommandSourceStack> ALL_LOOT_TABLE_SUGGESTIONS = (context, builder) -> {
        var registry = context.getSource().getServer().reloadableRegistries().get().registryOrThrow(Registries.LOOT_TABLE);
        return SharedSuggestionProvider.suggestResource(
            registry.holders().map(holder -> holder.key().location()),
            builder
        );
    };

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("loottickets")
            .requires(source -> source.hasPermission(2))
            .then(Commands.literal("give")
                .then(Commands.argument("targets", EntityArgument.players())
                    .then(Commands.argument("loot_table", ResourceLocationArgument.id())
                        .suggests(CHEST_LOOT_TABLE_SUGGESTIONS)
                        .executes(context -> giveTicket(context, 1))
                        .then(Commands.argument("count", IntegerArgumentType.integer(1, 64))
                            .executes(context -> giveTicket(context, IntegerArgumentType.getInteger(context, "count")))
                        )
                    )
                )
            )
            .then(Commands.literal("list")
                .executes(LootTicketsCommand::listLootTables)
            )
        );
    }

    private static int giveTicket(CommandContext<CommandSourceStack> context, int count) throws CommandSyntaxException {
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "targets");
        ResourceLocation lootTableLocation = ResourceLocationArgument.getId(context, "loot_table");
        ResourceKey<LootTable> lootTableKey = ResourceKey.create(Registries.LOOT_TABLE, lootTableLocation);

        for (ServerPlayer player : targets) {
            ItemStack ticket = new ItemStack(ModItems.LOOT_TICKET.get(), count);
            ticket.set(ModDataComponents.LOOT_TICKET_DATA.get(), new LootTicketData(lootTableKey));

            if (!player.getInventory().add(ticket)) {
                player.drop(ticket, false);
            }
        }

        if (targets.size() == 1) {
            context.getSource().sendSuccess(() -> Component.translatable(
                "commands.loottickets.give.success.single",
                count,
                lootTableKey.location().toString(),
                targets.iterator().next().getDisplayName()
            ), true);
        } else {
            context.getSource().sendSuccess(() -> Component.translatable(
                "commands.loottickets.give.success.multiple",
                count,
                lootTableKey.location().toString(),
                targets.size()
            ), true);
        }

        return targets.size();
    }

    private static int listLootTables(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        source.sendSuccess(() -> Component.literal("=== All Loot Tables ==="), false);

        var tables = source.getServer().reloadableRegistries().get().registryOrThrow(Registries.LOOT_TABLE)
            .holders()
            .map(holder -> holder.key().location().toString())
            .sorted()
            .toList();

        for (String table : tables) {
            source.sendSuccess(() -> Component.literal("  - " + table), false);
        }

        source.sendSuccess(() -> Component.literal("Total: " + tables.size() + " loot tables"), false);
        source.sendSuccess(() -> Component.literal("Use: /loottickets give @p <loot_table>"), false);

        return 1;
    }
}
