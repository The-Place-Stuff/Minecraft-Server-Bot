package com.theplace.bot.api;

import net.minecraft.server.level.ServerPlayer;

import java.util.function.Function;

public class IconUtils {
    public static final Function<ServerPlayer, String> FROM_PLAYER = player -> {
        if (player != null) {
            return "https://crafatar.com/avatars/" + player.getStringUUID();
        }
        return "";
    };

    public static String fromRepository(String path) {
        return "https://raw.githubusercontent.com/The-Place-Stuff/Minecraft-Server-Bot/main/src/main/resources/assets/the_place_bot/icons/" + path + ".png";
    }
}
