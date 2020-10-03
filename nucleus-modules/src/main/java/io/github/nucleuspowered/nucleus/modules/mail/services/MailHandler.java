/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.mail.services;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.module.mail.NucleusMailService;
import io.github.nucleuspowered.nucleus.api.module.mail.data.MailMessage;
import io.github.nucleuspowered.nucleus.modules.mail.MailKeys;
import io.github.nucleuspowered.nucleus.modules.mail.data.MailData;
import io.github.nucleuspowered.nucleus.modules.mail.events.InternalNucleusSendMailEvent;
import io.github.nucleuspowered.nucleus.scaffold.service.ServiceBase;
import io.github.nucleuspowered.nucleus.scaffold.service.annotations.APIService;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import io.github.nucleuspowered.nucleus.services.impl.storage.dataobjects.modular.IUserDataObject;
import io.github.nucleuspowered.nucleus.services.interfaces.IMessageProviderService;
import io.github.nucleuspowered.nucleus.services.interfaces.IStorageManager;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import javax.annotation.Nullable;
import com.google.inject.Inject;

@SuppressWarnings("UnstableApiUsage")
@APIService(NucleusMailService.class)
public class MailHandler implements NucleusMailService, ServiceBase {

    private final INucleusServiceCollection serviceCollection;

    @Inject
    public MailHandler(final INucleusServiceCollection serviceCollection) {
        this.serviceCollection = serviceCollection;
    }

    @Override
    public final List<MailMessage> getMail(final User player, final MailFilter... filters) {
        return Lists.newArrayList(getMailInternal(player, filters));
    }

    public final List<MailMessage> getMailInternal(final User player, final MailFilter... filters) {
        final List<MailMessage> data = this.serviceCollection.storageManager().getUserService()
                .getOrNewOnThread(player.getUniqueId())
                .getNullable(MailKeys.MAIL_DATA);
        if (data == null || data.isEmpty()) {
            return ImmutableList.of();
        }

        if (filters.length == 0) {
            return ImmutableList.copyOf(data);
        }

        final Predicate<MailMessage> lmf = Arrays.stream(filters).map(x -> (Predicate<MailMessage>)x).reduce(Predicate::and).orElse(x -> true);
        return data.stream().filter(lmf).collect(ImmutableList.toImmutableList());
    }

    @Override
    public boolean removeMail(final User player, final MailMessage mailData) {
        final IUserDataObject dataObject = this.serviceCollection.storageManager().getUserService().getOrNewOnThread(player.getUniqueId());
        final List<MailMessage> data = dataObject.get(MailKeys.MAIL_DATA).orElseGet(ArrayList::new);
        final boolean result = data.removeIf(x ->
                mailData.getDate().equals(x.getDate()) &&
                mailData.getMessage().equalsIgnoreCase(x.getMessage()) &&
                Objects.equals(mailData.getSender().orElse(null), x.getSender().orElse(null)));

        if (result) {
            dataObject.set(MailKeys.MAIL_DATA, data);
            this.serviceCollection.storageManager().getUserService().save(player.getUniqueId(), dataObject);
        }

        return result;
    }

    @Override
    public void sendMail(@Nullable final User playerFrom, final User playerTo, final String message) {
        final IUserDataObject dataObject = this.serviceCollection.storageManager().getUserService().getOrNewOnThread(playerTo.getUniqueId());

        // Message is about to be sent. Send the event out. If canceled, then
        // that's that.
        final IMessageProviderService messageProvider = this.serviceCollection.messageProvider();
        if (Sponge.getEventManager().post(new InternalNucleusSendMailEvent(playerFrom, playerTo, message))) {
            if (playerFrom == null) {
                messageProvider.sendMessageTo(
                        Sponge.getServer().getConsole(),
                        "message.cancel");
            } else {
                playerFrom.getPlayer()
                        .ifPresent(x -> messageProvider.sendMessageTo(x, "message.cancel"));
            }
            return;
        }

        final List<MailMessage> messages = dataObject.get(MailKeys.MAIL_DATA).orElseGet(ArrayList::new);
        final MailData md = new MailData(playerFrom == null ? Util.CONSOLE_FAKE_UUID : playerFrom.getUniqueId(), Instant.now(), message);
        messages.add(md);
        dataObject.set(MailKeys.MAIL_DATA, messages);
        this.serviceCollection.storageManager().getUserService().save(playerTo.getUniqueId(), dataObject);

        final TextComponent from = playerFrom == null ? Text.of(Sponge.getServer().getConsole().getName()) :
                this.serviceCollection.playerDisplayNameService().getDisplayName(playerFrom);
        playerTo.getPlayer().ifPresent(x ->
                x.sendMessage(Text.builder().append(messageProvider.getMessageFor(x, "mail.youvegotmail")).append(Text.of(" ", from)).build()));
    }

    @Override
    public void sendMailFromConsole(final User playerTo, final String message) {
        sendMail(null, playerTo, message);
    }

    @Override
    public boolean clearUserMail(final User player) {
        final IStorageManager storageManager = this.serviceCollection.storageManager();
        final IUserDataObject dataObject = storageManager.getUserService().getOrNewOnThread(player.getUniqueId());
        if (dataObject.getNullable(MailKeys.MAIL_DATA) != null) {
            dataObject.remove(MailKeys.MAIL_DATA);
            storageManager.getUserService().save(player.getUniqueId(), dataObject);
            return true;
        }

        return false;
    }
}