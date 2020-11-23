/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.protection.listeners;

import io.github.nucleuspowered.nucleus.modules.protection.config.ProtectionConfig;
import io.github.nucleuspowered.nucleus.core.scaffold.listener.ListenerBase;
import io.github.nucleuspowered.nucleus.core.services.INucleusServiceCollection;
import io.github.nucleuspowered.nucleus.core.services.interfaces.IReloadableService;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.filter.type.Exclude;

import java.util.List;

public class MobProtectionListener implements IReloadableService.Reloadable, ListenerBase.Conditional {

    private List<EntityType<?>> whitelistedTypes;

    @Listener
    @Exclude({ChangeBlockEvent.Grow.class, ChangeBlockEvent.Decay.class})
    public void onMobChangeBlock(final ChangeBlockEvent event, @Root final Living living) {
        if (living instanceof ServerPlayer || this.whitelistedTypes.contains(living.getType())) {
            return;
        }

        // If the entity is not in the whitelist, then cancel the event.
        event.setCancelled(true);
    }

    @Override
    public void onReload(final INucleusServiceCollection serviceCollection) {
        this.whitelistedTypes = serviceCollection.configProvider().getModuleConfig(ProtectionConfig.class).getWhitelistedEntities();
    }

    @Override
    public boolean shouldEnable(final INucleusServiceCollection serviceCollection) {
        return serviceCollection.configProvider().getModuleConfig(ProtectionConfig.class).isEnableProtection();
    }
}