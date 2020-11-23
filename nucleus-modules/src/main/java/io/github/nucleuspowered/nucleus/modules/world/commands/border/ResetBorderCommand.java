/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.world.commands.border;

import io.github.nucleuspowered.nucleus.modules.world.WorldPermissions;
import io.github.nucleuspowered.nucleus.core.scaffold.command.ICommandContext;
import io.github.nucleuspowered.nucleus.core.scaffold.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.core.scaffold.command.ICommandResult;
import io.github.nucleuspowered.nucleus.core.scaffold.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.core.scaffold.command.annotation.Command;
import io.github.nucleuspowered.nucleus.core.services.INucleusServiceCollection;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.world.WorldBorder;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.storage.WorldProperties;

import java.util.Optional;

@Command(
        aliases = { "reset" },
        basePermission = WorldPermissions.BASE_BORDER_SET,
        commandDescriptionKey = "world.border.reset",
        parentCommand = BorderCommand.class
)
public class ResetBorderCommand implements ICommandExecutor {

    @Override
    public Parameter[] parameters(final INucleusServiceCollection serviceCollection) {
        return new Parameter[] {
                NucleusParameters.OPTIONAL_WORLD_PROPERTIES_ALL
        };
    }

    @Override public ICommandResult execute(final ICommandContext context) throws CommandException {
        final WorldProperties wp = context.getWorldPropertiesOrFromSelfOptional(NucleusParameters.OPTIONAL_WORLD_PROPERTIES_ALL.getKey())
                .orElseThrow(() -> context.createException("command.world.player"));

        final WorldBorder worldBorder = wp.getWorldBorder();
        worldBorder.setCenter(0, 0);
        final Optional<ServerWorld> world = wp.getWorld();

        // A world to get defaults from.
        final ServerWorld toDiameter = world.orElseGet(() ->
                Sponge.getServer().getWorldManager().getDefaultProperties().flatMap(WorldProperties::getWorld).orElseThrow(IllegalStateException::new));

        // +1 includes the final block (1 -> -1 would otherwise give 2, not 3).
        final long diameter = Math.abs(toDiameter.getBlockMax().getX() - toDiameter.getBlockMin().getX()) + 1;
        worldBorder.setDiameter(diameter);

        world.ifPresent(w -> {
            worldBorder.setCenter(0, 0);
            worldBorder.setDiameter(diameter);
        });

        context.sendMessage("command.world.setborder.set",
                wp.getKey().asString(),
                "0",
                "0",
                String.valueOf(diameter));

        return context.successResult();
    }
}