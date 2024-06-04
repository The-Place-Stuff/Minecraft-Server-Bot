package com.theplace.bot.mixin;

import com.theplace.bot.Main;
import com.theplace.bot.api.EmbedColors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.TamableAnimal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(TamableAnimal.class)
public class TamableAnimalMixin {
    @Unique
    TamableAnimal $this = (TamableAnimal) (Object) this;

    @Inject(method = "die", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/damagesource/CombatTracker;getDeathMessage()Lnet/minecraft/network/chat/Component;"))
    private void onTameableDeath(DamageSource damageSource, CallbackInfo ci) {
        EmbedBuilder embed = new EmbedBuilder();
        // TODO: Unique color and icon.
        embed.setAuthor($this.getCombatTracker().getDeathMessage().getString());
        embed.setColor(EmbedColors.DEATH);

        Main.CLIENT.sendEmbed(embed);
    }
}
