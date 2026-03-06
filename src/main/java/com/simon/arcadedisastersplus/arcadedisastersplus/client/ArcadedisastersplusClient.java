package com.simon.arcadedisastersplus.arcadedisastersplus.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class ArcadedisastersplusClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ArcadedisastersplusConfig.load();

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (ArcadedisastersplusConfig.devMode && client.getCurrentServerEntry() != null && (client.getCurrentServerEntry().address.toLowerCase().equals("hypixel.net") || client.getCurrentServerEntry().address.toLowerCase().endsWith(".hypixel.net"))) {
                client.execute(() -> {
                    if (client.player != null) {
                        client.player.sendMessage(Text.literal("§c[ArcadeDisasters+] §aYou are on Hypixel."), false);
                    }
                });
            }
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(literal("disasters").executes(context -> {
                MinecraftClient client = MinecraftClient.getInstance();
                // Is this the right way to do this? Who knows!
                // TODO: this maybe should have settings, not run on non-hypixel, and maybe detect when it won't work in some contexts
                if (client.player != null) {
                    client.player.networkHandler.sendChatCommand("play arcade_disasters");
                }
                return 1;
            }));
        });

        GameModeDetector.register();
    }
}
