package com.simon.arcadedisasterplus.arcadedisasterplus.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GameModeDetector {

    private static boolean isInDisastersGame = false;
    private static final Pattern DISASTER_START_PATTERN = Pattern.compile("^([A-Z\\s]+) - (.*!)$");

    private static final List<String> pendingNames = new ArrayList<>();
    private static final List<String> pendingDescriptions = new ArrayList<>();
    private static int titleDelayTimer = 0;

    private static int customTitleTimer = 0;
    private static final List<String[]> displayEntries = new ArrayList<>();

    private static final Set<String> endedDisasters = new HashSet<>();

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            boolean wasInGame = isInDisastersGame;
            isInDisastersGame = checkIsInDisastersGame(client);

            if (isInDisastersGame && !wasInGame && ArcadedisasterplusConfig.devMode) {
                if (client.player != null) {
                    // don't actually know what the point of these is but...
                    client.player.sendMessage(Text.literal("§c[ArcadeDisaster+] §aJoined Disasters game!"), false);
                }
            } else if (!isInDisastersGame && wasInGame && ArcadedisasterplusConfig.devMode) {
                endedDisasters.clear();
                if (client.player != null) {
                    client.player.sendMessage(Text.literal("§c[ArcadeDisaster+] §eLeft Disasters game."), false);
                }
            }

            if (isInDisastersGame) {
                checkEndedDisasters(client);
            }

            if (customTitleTimer > 0) {
                customTitleTimer--;
            }

            if (titleDelayTimer > 0) {
                titleDelayTimer--;
                if (titleDelayTimer == 0 && !pendingNames.isEmpty() && client.inGameHud != null) {
                    if (ArcadedisasterplusConfig.showDisasterTitles) {
                        displayEntries.clear();
                        for (int i = 0; i < pendingNames.size(); i++) {
                            displayEntries.add(new String[]{pendingNames.get(i), pendingDescriptions.get(i)});
                        }
                        customTitleTimer = 100;
                    }

                    pendingNames.clear();
                    pendingDescriptions.clear();
                }
            }
        });

        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.of("arcadedisasterplus", "disaster_title"),
                (context, tickDelta) -> {
                    if (customTitleTimer <= 0 || displayEntries.isEmpty()) return;

                    MinecraftClient client = MinecraftClient.getInstance();
                    int width = context.getScaledWindowWidth();
                    int height = context.getScaledWindowHeight();
                    TextRenderer textRenderer = client.textRenderer;
                    float titleScale = 2.0f;
                    int maxSubtitleWidth = Math.min(300, width - 40);

                    float yPos = height / 3f;

                    for (String[] entry : displayEntries) {
                        String name = entry[0];
                        String description = entry[1];
                        boolean ended = entry.length > 2 && "ended".equals(entry[2]);

                        context.getMatrices().pushMatrix();
                        context.getMatrices().translate(width / 2f, yPos);
                        context.getMatrices().scale(titleScale, titleScale);
                        Text titleText = ended
                                ? Text.literal("§7§l§m" + name)
                                : Text.literal("§c§l" + name);
                        context.drawText(textRenderer, titleText, -textRenderer.getWidth(titleText) / 2, 0, 0xFFFFFFFF, true);
                        context.getMatrices().popMatrix();

                        yPos += textRenderer.fontHeight * titleScale + 3;

                        String subtitleColor = ended ? "§8" : "§e";
                        List<OrderedText> lines = textRenderer.wrapLines(Text.literal(subtitleColor + description), maxSubtitleWidth);
                        context.getMatrices().pushMatrix();
                        context.getMatrices().translate(width / 2f, yPos);
                        for (OrderedText line : lines) {
                            context.drawText(textRenderer, line, -textRenderer.getWidth(line) / 2, 0, 0xFFFFFFFF, true);
                            context.getMatrices().translate(0f, textRenderer.fontHeight + 2f);
                            yPos += textRenderer.fontHeight + 2;
                        }
                        context.getMatrices().popMatrix();

                        yPos += 8;
                    }
                });

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!isInDisastersGame) return;
            if (overlay) return;

            String unformattedText = message.getString();
            Matcher matcher = DISASTER_START_PATTERN.matcher(unformattedText);

            if (matcher.find()) {
                String disasterName = matcher.group(1).trim();
                String description = matcher.group(2).trim();

                pendingNames.add(disasterName);
                pendingDescriptions.add(description);

                titleDelayTimer = 2;

                if (ArcadedisasterplusConfig.showChatMessages) {
                    MinecraftClient.getInstance().execute(() -> {
                        if (MinecraftClient.getInstance().player != null) {
                            MinecraftClient.getInstance().player.sendMessage(Text.literal("§c[ArcadeDisaster+] §6Disaster started: §e" + disasterName), false);
                        }
                    });
                }
            }
        });
    }

    private static void checkEndedDisasters(MinecraftClient client) {
        if (client.world == null || client.player == null) return;

        Scoreboard scoreboard = client.world.getScoreboard();
        ScoreboardObjective objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        if (objective == null) return;

        boolean inDisasterSection = false;
        List<Map.Entry<Integer, String>> entries = new ArrayList<>();

        scoreboard.getScoreboardEntries(objective).forEach(entry -> {
            Team team = scoreboard.getScoreHolderTeam(entry.owner());
            if (team == null) return;
            String text = team.getPrefix().getString() + team.getSuffix().getString();
            String rawText = team.getPrefix().toString() + team.getSuffix().toString();
            entries.add(Map.entry(entry.value(), rawText + "|" + text));
        });

        entries.sort((a, b) -> Integer.compare(b.getKey(), a.getKey()));

        List<String> newlyEnded = new ArrayList<>();

        for (Map.Entry<Integer, String> entry : entries) {
            String[] parts = entry.getValue().split("\\|", 2);
            String rawText = parts[0];
            String text = parts.length > 1 ? parts[1] : "";

            if (text.trim().equals("Disasters:")) {
                inDisasterSection = true;
                continue;
            }

            if (inDisasterSection) {
                if (text.trim().isEmpty()) break;

                String disasterName = text.replaceAll("§[0-9a-fk-or]", "").trim();
                if (disasterName.isEmpty()) continue;

                if (rawText.contains("§m") && !endedDisasters.contains(disasterName)) {
                    endedDisasters.add(disasterName);
                    newlyEnded.add(disasterName);
                    if (ArcadedisasterplusConfig.showChatMessages) {
                        client.player.sendMessage(Text.literal("§c[ArcadeDisaster+] §7Disaster ended: §m" + disasterName), false);
                    }
                }
            }
        }

        if (!newlyEnded.isEmpty() && ArcadedisasterplusConfig.showEndedTitles) {
            displayEntries.clear();
            for (String name : newlyEnded) {
                displayEntries.add(new String[]{name, "Disaster ended!", "ended"});
            }
            customTitleTimer = 60;
        }
    }

    private static boolean checkIsInDisastersGame(MinecraftClient client) {
        if (client.world == null || client.player == null) return false;

        Scoreboard scoreboard = client.world.getScoreboard();
        if (scoreboard == null) return false;

        ScoreboardObjective objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        if (objective != null) {
            String title = objective.getDisplayName().getString();
            return title.contains("DISASTERS");
        }

        return false;
    }
}
