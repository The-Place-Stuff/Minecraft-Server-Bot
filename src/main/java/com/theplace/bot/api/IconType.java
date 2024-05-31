package com.theplace.bot.api;

import net.minecraft.server.level.ServerPlayer;

import java.util.function.Function;

public class IconType {
    public static final Function<ServerPlayer, String> FROM_PLAYER = player -> {
        if (player != null) {
            return "https://crafatar.com/avatars/" + player.getStringUUID();
        }
        return "";
    };

    public static String fromUrl(String url) {
        return url;
    }
}
