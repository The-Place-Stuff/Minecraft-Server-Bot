package com.theplace.bot.mixin;

import com.theplace.bot.Main;
import com.theplace.bot.api.EmbedColors;
import com.theplace.bot.api.IconType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerAdvancements.class)
public class PlayerAdvancementsMixin {

    @Shadow private ServerPlayer player;

    @Inject(method = "method_53637", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"))
    private void broadcastAdvancement(AdvancementHolder advancementHolder, DisplayInfo displayInfo, CallbackInfo ci) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(EmbedColors.LIGHT_YELLOW);
        embed.setAuthor(
                displayInfo.getType().createAnnouncement(advancementHolder, this.player).getString(), null,
                IconType.fromRepository("player_advancement")
        );
        Main.CLIENT.sendEmbed(embed);
    }
}
