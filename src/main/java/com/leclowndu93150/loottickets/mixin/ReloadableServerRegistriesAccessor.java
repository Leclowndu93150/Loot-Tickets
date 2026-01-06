package com.leclowndu93150.loottickets.mixin;

import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ReloadableServerRegistries.Holder.class)
public interface ReloadableServerRegistriesAccessor {

    @Accessor("registries")
    RegistryAccess.Frozen getRegistries();
}
