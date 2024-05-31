package com.theplace.bot.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.theplace.bot.Main;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class DiscordCommand {
    private static final Supplier<Component> NO_CONNECTION_FOUND = () -> Component.literal("Failed to disconnect, no connection was found.");
    private static final Supplier<Component> CONNECTION_EXISTS = () -> Component.literal("Failed to connect, there is already an ongoing connection.");
    private static final Supplier<Component> CONNECTION_FAILED = () -> Component.literal("Failed to connect.");
    private static final Supplier<Component> CONNECTION_ESTABLISHED = () -> Component.literal("Successfully established connection.");
    private static final Supplier<Component> CONNECTION_TERMINATED = () -> Component.literal("Successfully terminated connection.");

    private static final Predicate<CommandSourceStack> IS_OPERATOR = source -> source.hasPermission(2);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var root = Commands.literal("discord").requires(IS_OPERATOR);
        root.then(Commands.literal("connect").executes(DiscordCommand::connect));
        root.then(Commands.literal("disconnect").executes(DiscordCommand::disconnect));
        dispatcher.register(root);
    }

    private static int connect(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        if (Main.CLIENT.isConnected()) {
            source.sendFailure(CONNECTION_EXISTS.get());
            return 0;
        }
        if (Main.CLIENT.connect(context.getSource().getServer())) {
            Main.CLIENT.updatePlayersOnline();
            source.sendSuccess(CONNECTION_ESTABLISHED, true);
            return 1;
        }
        source.sendFailure(CONNECTION_FAILED.get());
        return 0;
    }

    private static int disconnect(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        if (!Main.CLIENT.isConnected()) {
            context.getSource().sendFailure(NO_CONNECTION_FOUND.get());
            return 0;
        }
        Main.CLIENT.disconnect();
        source.sendSuccess(CONNECTION_TERMINATED, true);
        return 1;
    }
}
