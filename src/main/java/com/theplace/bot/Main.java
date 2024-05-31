package com.theplace.bot;

import com.theplace.bot.api.DiscordClient;
import com.theplace.bot.api.EmbedColors;
import com.theplace.bot.command.DiscordCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main implements ModInitializer {
	public static final String MOD_ID = "the_place_bot";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final DiscordClient CLIENT = new DiscordClient();

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> DiscordCommand.register(dispatcher));
		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			CLIENT.connect(server);
			CLIENT.setActivity("Starting...");
		});
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			EmbedBuilder embed = new EmbedBuilder();
			embed.setAuthor("Server started!");
			embed.setColor(EmbedColors.START_GREEN);
			CLIENT.sendEmbed(embed);
			CLIENT.updatePlayersOnline();
		});
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			EmbedBuilder embed = new EmbedBuilder();
			embed.setAuthor("Server stopped!");
			embed.setColor(EmbedColors.STOP_RED);
			CLIENT.sendEmbed(embed);
		});
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			CLIENT.disconnect();
		});
	}
}