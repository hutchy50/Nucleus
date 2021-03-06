/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.services.interfaces;

import com.google.inject.ImplementedBy;
import io.github.nucleuspowered.nucleus.services.impl.messageprovider.MessageProviderService;
import io.github.nucleuspowered.nucleus.services.impl.messageprovider.repository.ConfigFileMessagesRepository;
import io.github.nucleuspowered.nucleus.services.impl.messageprovider.repository.IMessageRepository;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

@ImplementedBy(MessageProviderService.class)
public interface IMessageProviderService {

    boolean hasKey(String key);

    Locale getDefaultLocale();

    void invalidateLocaleCacheFor(UUID uuid);

    Optional<Locale> getLocaleFromName(String name);

    Locale getLocaleFor(CommandSource commandSource);

    Text getMessageFor(Locale locale, String key);

    Text getMessageFor(Locale locale, String key, Text... args);

    Text getMessageFor(Locale locale, String key, Object... replacements);

    Text getMessageFor(Locale locale, String key, String... replacements);

    String getMessageString(Locale locale, String key, String... replacements);

    String getMessageString(Locale locale, String key, Object... replacements);

    default Text getMessageForDefault(String key, Text... args) {
        return getMessageFor(getDefaultLocale(), key, args);
    }

    default Text getMessageFor(CommandSource source, String key) {
        return getMessageFor(getLocaleFor(source), key);
    }

    default Text getMessageFor(CommandSource source, String key, Text... args) {
        return getMessageFor(getLocaleFor(source), key, args);
    }

    default Text getMessageFor(CommandSource source, String key, String... args) {
        Text[] t = Arrays.stream(args).map(TextSerializers.FORMATTING_CODE::deserialize).toArray(Text[]::new);
        return getMessageFor(getLocaleFor(source), key, t);
    }

    default Text getMessage(String key) {
        return getMessageForDefault(key);
    }

    default Text getMessage(String key, String... replacements) {
        return getMessageFor(getDefaultLocale(), key, replacements);
    }

    default Text getMessage(String key, Text... replacements) {
        return getMessageFor(getDefaultLocale(), key, replacements);
    }

    default Text getMessage(String key, Object... replacements) {
        return getMessageFor(getDefaultLocale(), key, replacements);
    }

    default String getMessageString(String key, Object... replacements) {
        return getMessageString(getDefaultLocale(), key, replacements);
    }

    default String getMessageString(CommandSource source, String key, String... replacements) {
        return getMessageString(getLocaleFor(source), key, replacements);
    }

    default Text getMessageFor(CommandSource source, String key, Object... replacements) {
        return getMessageFor(getLocaleFor(source), key, replacements);
    }

    default void sendMessageTo(CommandSource receiver, String key) {
        receiver.sendMessage(getMessageFor(getLocaleFor(receiver), key));
    }

    default void sendMessageTo(MessageReceiver receiver, String key, Object... replacements) {
        if (receiver instanceof CommandSource) {
            receiver.sendMessage(getMessageFor(getLocaleFor((CommandSource) receiver), key, replacements));
        } else {
            receiver.sendMessage(getMessageFor(Sponge.getServer().getConsole(), key, replacements));
        }
    }

    default void sendMessageTo(CommandSource receiver, String key, Object... replacements) {
        receiver.sendMessage(getMessageFor(getLocaleFor(receiver), key, replacements));
    }

    default void sendMessageTo(CommandSource receiver, String key, Text... replacements) {
        receiver.sendMessage(getMessageFor(getLocaleFor(receiver), key, replacements));
    }

    default void sendMessageTo(CommandSource receiver, String key, String... replacements) {
        receiver.sendMessage(getMessageFor(getLocaleFor(receiver), key, replacements));
    }

    default void sendMessageTo(Supplier<CommandSource> receiver, String key, String... replacements) {
        sendMessageTo(receiver.get(), key, replacements);
    }

    default void sendMessageTo(Supplier<CommandSource> receiver, String key, Object... replacements) {
        sendMessageTo(receiver.get(), key, replacements);
    }

    boolean reloadMessageFile();

    IMessageRepository getMessagesRepository(Locale locale);

    ConfigFileMessagesRepository getConfigFileMessageRepository();

    default String getTimeToNow(Locale locale, Instant instant) {
        return getTimeString(locale, Instant.now().getEpochSecond() - instant.getEpochSecond());
    }

    String getTimeString(Locale locale, Duration duration);

    String getTimeString(Locale locale, long time);

    List<String> getAllLocaleNames();
}
