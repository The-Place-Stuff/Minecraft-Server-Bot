package com.theplace.bot.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record DatabaseSettings(String channel, String message) {
    public static final Codec<DatabaseSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("channel").forGetter(DatabaseSettings::channel),
            Codec.STRING.fieldOf("message").forGetter(DatabaseSettings::message)
    ).apply(instance, DatabaseSettings::new));

    public static final DatabaseSettings DEFAULT = new DatabaseSettings("", "");
}
