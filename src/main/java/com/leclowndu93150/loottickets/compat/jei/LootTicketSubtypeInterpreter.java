package com.leclowndu93150.loottickets.compat.jei;

import com.leclowndu93150.loottickets.component.LootTicketData;
import com.leclowndu93150.loottickets.registry.ModDataComponents;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class LootTicketSubtypeInterpreter implements ISubtypeInterpreter<ItemStack> {

    public static final LootTicketSubtypeInterpreter INSTANCE = new LootTicketSubtypeInterpreter();

    private LootTicketSubtypeInterpreter() {}

    @Override
    @Nullable
    public Object getSubtypeData(ItemStack ingredient, UidContext context) {
        LootTicketData data = ingredient.get(ModDataComponents.LOOT_TICKET_DATA.get());
        if (data != null) {
            return data.lootTable().location().toString();
        }
        return null;
    }

    @Override
    public String getLegacyStringSubtypeInfo(ItemStack ingredient, UidContext context) {
        LootTicketData data = ingredient.get(ModDataComponents.LOOT_TICKET_DATA.get());
        if (data != null) {
            return data.lootTable().location().toString();
        }
        return "";
    }
}
