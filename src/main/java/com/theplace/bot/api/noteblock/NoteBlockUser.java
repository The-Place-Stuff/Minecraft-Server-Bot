package com.theplace.bot.api.noteblock;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record NoteBlockUser(List<String> favorites, String id, @Nullable Either<String, Boolean> minecraftName, String voice) {
    public static final Codec<Either<String, Boolean>> NAME_CODEC = Codec.either(Codec.STRING, Codec.BOOL);

    public static final Codec<NoteBlockUser> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.listOf().fieldOf("favorites").forGetter(NoteBlockUser::favorites),
            Codec.STRING.fieldOf("id").forGetter(NoteBlockUser::id),
            NAME_CODEC.fieldOf("minecraft_name").forGetter(NoteBlockUser::minecraftName),
            Codec.STRING.fieldOf("voice").forGetter(NoteBlockUser::voice)
    ).apply(instance, NoteBlockUser::new));
}
