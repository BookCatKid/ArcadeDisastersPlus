package com.simon.arcadedisasterplus.arcadedisasterplus.client;

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

public class ArcadedisasterplusClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (client.getCurrentServerEntry() != null && client.getCurrentServerEntry().address.toLowerCase().contains("hypixel.net")) {
                client.execute(() -> {
                    if (client.player != null) {
                        client.player.sendMessage(Text.literal("§c[ArcadeDisaster+] §aSuccessfully logged into Hypixel! Mod base active."), false);
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

            //TODO: Remove debug command soon please
            dispatcher.register(literal("debugscoreboard").executes(context -> {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.world == null || client.player == null) {
                    context.getSource().sendFeedback(Text.literal("§cNo world loaded."));
                    return 0;
                }

                Scoreboard scoreboard = client.world.getScoreboard();
                ScoreboardObjective objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
                if (objective == null) {
                    context.getSource().sendFeedback(Text.literal("§cNo sidebar objective found."));
                    return 0;
                }

                context.getSource().sendFeedback(Text.literal("§6--- Scoreboard Debug ---"));
                context.getSource().sendFeedback(Text.literal("§eTitle: §f" + objective.getDisplayName().getString()));
                context.getSource().sendFeedback(Text.literal("§eTitle (raw): §f" + objective.getDisplayName().toString()));

                scoreboard.getScoreboardEntries(objective).forEach(entry -> {
                    String playerName = entry.owner();
                    int score = entry.value();
                    Text display = entry.display();

                    Team team = scoreboard.getScoreHolderTeam(playerName);
                    String prefix = team != null ? team.getPrefix().getString() : "";
                    String suffix = team != null ? team.getSuffix().getString() : "";
                    String rawPrefix = team != null ? team.getPrefix().toString() : "null";
                    String rawSuffix = team != null ? team.getSuffix().toString() : "null";

                    context.getSource().sendFeedback(Text.literal(
                            "§a[" + score + "] §fowner='" + playerName
                                    + "' display=" + (display != null ? display.getString() : "null")
                                    + " prefix='" + prefix + "' suffix='" + suffix + "'"
                    ));
                    context.getSource().sendFeedback(Text.literal(
                            "  §7rawPrefix=" + rawPrefix
                    ));
                    context.getSource().sendFeedback(Text.literal(
                            "  §7rawSuffix=" + rawSuffix
                    ));
                });

                context.getSource().sendFeedback(Text.literal("§6--- End ---"));
                return 1;
            }));
        });

        GameModeDetector.register();
    }
}
