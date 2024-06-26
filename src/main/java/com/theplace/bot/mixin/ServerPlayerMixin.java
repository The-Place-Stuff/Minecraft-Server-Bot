package com.theplace.bot.mixin;

import com.theplace.bot.Main;
import com.theplace.bot.api.EmbedColors;
import com.theplace.bot.api.IconUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
    @Unique
    ServerPlayer $this = (ServerPlayer) (Object) this;

    @Inject(method = "die", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"))
    private void broadcastDeath(DamageSource source, CallbackInfo ci) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor($this.getCombatTracker().getDeathMessage().getString(), null, IconUtils.fromRepository("death"));
        embed.setColor(EmbedColors.DEATH);

        Main.CLIENT.sendEmbed(embed);
    }
}
