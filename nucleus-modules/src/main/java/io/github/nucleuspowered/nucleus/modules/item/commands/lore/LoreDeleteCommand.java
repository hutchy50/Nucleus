/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.item.commands.lore;

import io.github.nucleuspowered.nucleus.modules.item.ItemPermissions;
import io.github.nucleuspowered.nucleus.scaffold.command.ICommandContext;
import io.github.nucleuspowered.nucleus.scaffold.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.scaffold.command.ICommandResult;
import io.github.nucleuspowered.nucleus.scaffold.command.annotation.Command;
import io.github.nucleuspowered.nucleus.scaffold.command.annotation.CommandModifier;
import io.github.nucleuspowered.nucleus.scaffold.command.modifier.CommandModifiers;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import org.spongepowered.api.command.exception.CommandException;;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.item.LoreData;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import java.util.List;

@Command(
        aliases = { "delete" },
        basePermission = ItemPermissions.BASE_LORE_SET,
        commandDescriptionKey = "lore.delete",
        parentCommand = LoreCommand.class,
        modifiers = {
                @CommandModifier(value = CommandModifiers.HAS_COOLDOWN, exemptPermission = ItemPermissions.EXEMPT_COOLDOWN_LORE_SET),
                @CommandModifier(value = CommandModifiers.HAS_WARMUP, exemptPermission = ItemPermissions.EXEMPT_WARMUP_LORE_SET),
                @CommandModifier(value = CommandModifiers.HAS_COST, exemptPermission = ItemPermissions.EXEMPT_COST_LORE_SET)
        }
)
public class LoreDeleteCommand implements ICommandExecutor {

    private final String loreLine = "line";

    @Override
    public CommandElement[] parameters(final INucleusServiceCollection serviceCollection) {
        return new CommandElement[] {
                new PositiveIntegerArgument(Text.of(this.loreLine), false, serviceCollection)
        };
    }

    @Override
    public ICommandResult execute(final ICommandContext context) throws CommandException {
        final Player src = context.getIfPlayer();
        final int line = context.requireOne(this.loreLine, Integer.class) - 1;

        final ItemStack stack = src.getItemInHand(HandTypes.MAIN_HAND).orElseThrow(() -> context.createException("command.lore.clear.noitem"));
        final LoreData loreData = stack.getOrCreate(LoreData.class).get();

        final List<Text> loreList = loreData.lore().get();
        if (loreList.size() < line) {
            return context.errorResult("command.lore.set.invalidLine");
        }

        loreList.remove(line);

        if (stack.offer(Keys.ITEM_LORE, loreList).isSuccessful()) {
            src.setItemInHand(HandTypes.MAIN_HAND, stack);

            context.sendMessage("command.lore.set.success");
            return context.successResult();
        }

        return context.errorResult("command.lore.set.fail");
    }
}