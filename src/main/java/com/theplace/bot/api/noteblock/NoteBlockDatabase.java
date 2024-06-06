package com.theplace.bot.api.noteblock;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.theplace.bot.Main;
import com.theplace.bot.config.DatabaseSettings;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.RestAction;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class NoteBlockDatabase {
    public static final List<NoteBlockUser> USERS = Lists.newArrayList();



    public static void buildFrom(Message message) {
        String content = message.getContentRaw();
        DataResult<Pair<List<NoteBlockUser>, JsonElement>> result = NoteBlockUser.CODEC.listOf().decode(JsonOps.INSTANCE, JsonParser.parseString(content));
        try {
            List<NoteBlockUser> users = result.getOrThrow().getFirst();
            USERS.clear();
            USERS.addAll(users);
        }
        catch (Exception e) {
            Main.LOGGER.error(e.getMessage());
        }
    }

    public static @Nullable Message searchFor(DatabaseSettings settings) {
        AtomicReference<Message> message = new AtomicReference<>();
        Main.CLIENT.execute(connection -> {
            TextChannel channel = connection.getTextChannelById(settings.channel());
            if (channel == null) return;
            RestAction<Message> rest = channel.retrieveMessageById(settings.message());
            message.set(rest.complete());
        });
        return message.get();
    }

    public static Optional<NoteBlockUser> findFrom(ServerPlayer player) {
        String name = player.getName().getString();
        return USERS.stream().filter(searched -> {
            if (searched.minecraftName().left().isPresent()) {
                return searched.minecraftName().left().get().equals(name);
            }
            return false;
        }).findFirst();
    }
}
