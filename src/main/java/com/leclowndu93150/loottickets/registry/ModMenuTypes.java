package com.leclowndu93150.loottickets.registry;

import com.leclowndu93150.loottickets.LootTickets;
import com.leclowndu93150.loottickets.block.RedemptionCenterBlockEntity;
import com.leclowndu93150.loottickets.block.RedemptionCenterMenu;
import com.leclowndu93150.loottickets.item.container.TicketBagMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENUS =
        DeferredRegister.create(Registries.MENU, LootTickets.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<RedemptionCenterMenu>> REDEMPTION_CENTER_MENU =
        MENUS.register("redemption_center_menu", () ->
            IMenuTypeExtension.create((containerId, playerInventory, data) -> {
                BlockPos pos = data.readBlockPos();
                Level level = playerInventory.player.level();
                if (level.getBlockEntity(pos) instanceof RedemptionCenterBlockEntity blockEntity) {
                    return new RedemptionCenterMenu(containerId, playerInventory, blockEntity);
                }
                return new RedemptionCenterMenu(containerId, playerInventory);
            })
        );

    public static final DeferredHolder<MenuType<?>, MenuType<TicketBagMenu>> TICKET_BAG_MENU =
        MENUS.register("ticket_bag_menu", () ->
            IMenuTypeExtension.create((containerId, playerInventory, data) -> {
                int slotIndex = data.readInt();
                return new TicketBagMenu(containerId, playerInventory, slotIndex);
            })
        );
}
