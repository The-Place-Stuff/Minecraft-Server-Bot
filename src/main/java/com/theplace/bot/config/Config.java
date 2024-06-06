package com.theplace.bot.config;

import com.google.gson.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.theplace.bot.Main;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public record Config(String token, String textChannel, DatabaseSettings database) {
    private static final Config DEFAULT = new Config("", "", DatabaseSettings.DEFAULT);
    private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve(Main.MOD_ID + ".json").toFile();

    public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("token").forGetter(Config::token),
            Codec.STRING.fieldOf("text_channel").forGetter(Config::textChannel),
            DatabaseSettings.CODEC.fieldOf("database").forGetter(Config::database)
    ).apply(instance, Config::new));

    public static Config load() {
        if (!CONFIG_FILE.exists()) {
            try {
                CONFIG_FILE.createNewFile();
            } catch (Exception ignored) {}

            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                DataResult<JsonElement> json = CODEC.encode(DEFAULT, JsonOps.INSTANCE, new JsonObject());
                writer.write(Main.GSON.toJson(json.getOrThrow()));
            }
            catch (Exception e) {
                return DEFAULT;
            }
            return DEFAULT;
        }
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
           var result = CODEC.decode(JsonOps.INSTANCE, JsonParser.parseReader(reader));
           return result.getOrThrow().getFirst();
        }
        catch (Exception e) {
            return DEFAULT;
        }
    }
}
