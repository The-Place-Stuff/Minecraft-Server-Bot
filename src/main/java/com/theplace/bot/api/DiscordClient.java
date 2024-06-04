package com.theplace.bot.api;

import com.theplace.bot.Main;
import com.theplace.bot.config.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class DiscordClient extends ListenerAdapter {
    private Optional<JDA> connection = Optional.empty();
    private Optional<MinecraftServer> server = Optional.empty();

    private final Config config;

    private static final List<GatewayIntent> INTENTS = List.of(
            GatewayIntent.MESSAGE_CONTENT,
            GatewayIntent.GUILD_MEMBERS,
            GatewayIntent.GUILD_MESSAGE_REACTIONS
    );

    public DiscordClient() {
        this.config = Config.load();

        if (this.config.token().isEmpty()) {

        }
    }

    public boolean connect(MinecraftServer server) {
        JDABuilder builder = JDABuilder.createDefault(this.config.token());
        builder.setStatus(OnlineStatus.ONLINE);
        builder.enableIntents(INTENTS);
        builder.setMemberCachePolicy(MemberCachePolicy.ALL);
        builder.addEventListeners(this);
        this.connection = Optional.of(builder.build());
        this.server = Optional.of(server);

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
            action.accept(this.connection.get());
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
}
