package com.theplace.bot.api;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.theplace.bot.Main;
import com.theplace.bot.api.noteblock.NoteBlockDatabase;
import com.theplace.bot.api.noteblock.NoteBlockUser;
import com.theplace.bot.config.Config;
import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.TamableAnimal;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class DiscordClient extends ListenerAdapter {
    private Optional<JDA> connection = Optional.empty();
    private Optional<MinecraftServer> server = Optional.empty();

    private final Config config;

    private static final List<GatewayIntent> INTENTS = List.of(
            GatewayIntent.MESSAGE_CONTENT,
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.GUILD_MEMBERS,
            GatewayIntent.GUILD_MESSAGE_REACTIONS
    );

    public DiscordClient() {
        this.config = Config.load();
    }

    public boolean connect(MinecraftServer server) {
        JDABuilder builder = JDABuilder.createDefault(this.config.token());
        builder.setStatus(OnlineStatus.ONLINE);
        builder.enableIntents(INTENTS);
        builder.setMemberCachePolicy(MemberCachePolicy.ALL);
        builder.addEventListeners(this);
        this.connection = Optional.of(builder.build());
        this.server = Optional.of(server);

        Message message = NoteBlockDatabase.searchFor(config.database());
        if (message != null) {
            NoteBlockDatabase.buildFrom(message);
        }
        try {
            this.connection.get().awaitReady();
            return true;
        }
        catch (InterruptedException e) {
            this.connection = Optional.empty();
            Main.LOGGER.error(e.getMessage());
        }
        return false;
    }

    public void disconnect() {
        this.execute(JDA::shutdownNow);
        this.connection = Optional.empty();
        this.server = Optional.empty();
    }

    public void execute(Consumer<JDA> action) {
        if (connection.isPresent()) {
            try {
                action.accept(this.connection.get());
            }
            catch (Exception e) {
                Main.LOGGER.error(e.getMessage());
            }
        }
    }

    public void executeInGame(Consumer<MinecraftServer> action) {
        if (server.isPresent()) {
            action.accept(this.server.get());
        }
    }

    public void setActivity(String text) {
        this.execute(connection -> {
            connection.getPresence().setActivity(Activity.customStatus(text));
        });
    }

    public void updatePlayersOnline() {
        this.executeInGame(ongoingServer -> {
            int maxPlayerCount = ongoingServer.getMaxPlayers();
            int playersOnline = ongoingServer.getPlayerList().getPlayerCount();
            this.setActivity("Online: " + playersOnline + "/" + maxPlayerCount);
        });
    }

    public void sendEmbed(EmbedBuilder embed) {
        this.execute(connection -> {
            TextChannel channel = connection.getTextChannelById(this.config.textChannel());
            if (channel != null) {
                channel.sendMessageEmbeds(embed.build()).queue();
            }
        });
    }

    public void notifyOwner(TamableAnimal pet) {
        this.execute(connection -> {
            TextChannel channel = connection.getTextChannelById(this.config.textChannel());
            if (channel == null) return;

            EmbedBuilder embed = new EmbedBuilder();
            embed.setAuthor(pet.getCombatTracker().getDeathMessage().getString(), null, IconType.fromRepository("death"));
            embed.setColor(EmbedColors.DEATH);
            MessageCreateAction message = channel.sendMessageEmbeds(embed.build());

            if (pet.getOwner() instanceof ServerPlayer owner) {
                NoteBlockDatabase.findFrom(owner).ifPresent(user -> message.mentionUsers(user.id()));
            }
            message.queue();
        });
    }

    public boolean isConnected() {
        return this.connection.isPresent();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.getChannel().getId().equals(config.textChannel())) return;
        if (connection.isEmpty()) return;

        Member member = event.getMember();

        if (member == null || member.getId().equals(connection.get().getSelfUser().getId())) return;

        String name = member.getNickname() != null ? member.getNickname() : member.getEffectiveName();
        Component broadcast = Component.literal(String.format("§9<%s>§r " + event.getMessage().getContentDisplay(), name));

        this.executeInGame(ongoingServer -> ongoingServer.getPlayerList().broadcastSystemMessage(broadcast, false));
    }

    @Override
    public void onMessageUpdate(MessageUpdateEvent event) {
        if (!event.getChannel().getId().equals(config.database().channel())) return;
        if (!event.getMessageId().equals(config.database().message())) return;

        NoteBlockDatabase.buildFrom(event.getMessage());
    }
}
