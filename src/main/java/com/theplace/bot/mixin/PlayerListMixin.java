package com.theplace.bot.mixin;

import com.theplace.bot.Main;
import com.theplace.bot.api.EmbedColors;
import com.theplace.bot.api.IconUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(PlayerList.class)
public class PlayerListMixin {

    @Inject(method = "placeNewPlayer", at = @At(value = "TAIL"))
    private void onPlayerConnected(Connection connection, ServerPlayer player, CommonListenerCookie commonListenerCookie, CallbackInfo ci) {
        if (player.getServer() != null) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setAuthor(player.getName().getString() + " has joined the server!", null, IconUtils.FROM_PLAYER.apply(player));
            embed.setColor(EmbedColors.LIGHT_YELLOW);
            Main.CLIENT.sendEmbed(embed);
            Main.CLIENT.updatePlayersOnline();
        }
    }

    @Inject(method = "remove", at = @At("TAIL"))
    private void onPlayerDisconnected(ServerPlayer player, CallbackInfo ci) {
        if (player.getServer() != null) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setAuthor(player.getName().getString() + " has left the server!", null, IconUtils.FROM_PLAYER.apply(player));
            embed.setColor(EmbedColors.LIGHT_YELLOW);
            Main.CLIENT.sendEmbed(embed);
            Main.CLIENT.updatePlayersOnline();
        }
    }

    @Inject(method = "broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Ljava/util/function/Predicate;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/network/chat/ChatType$Bound;)V", at = @At("TAIL"))
    private void onPlayerChat(PlayerChatMessage playerChatMessage, Predicate<ServerPlayer> predicate, ServerPlayer serverPlayer, ChatType.Bound bound, CallbackInfo ci) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor(serverPlayer.getName().getString(), null, IconUtils.FROM_PLAYER.apply(serverPlayer));
        embed.setTitle(playerChatMessage.signedContent());

        Main.CLIENT.sendEmbed(embed);
    }
}
