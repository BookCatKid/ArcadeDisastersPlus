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
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GameModeDetector {

    private static boolean isInDisastersGame = false;
    private static final Pattern DISASTER_START_PATTERN = Pattern.compile("^([A-Z\\s]+) - (.*!)$");

    private static final List<String> currentDisasters = new ArrayList<>();
    private static final List<String> currentDescriptions = new ArrayList<>();
    private static int titleDelayTimer = 0;

    private static int customTitleTimer = 0;
    private static final List<String[]> displayEntries = new ArrayList<>();

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            boolean wasInGame = isInDisastersGame;
            isInDisastersGame = checkIsInDisastersGame(client);

            if (isInDisastersGame && !wasInGame) {
                if (client.player != null) {
                    client.player.sendMessage(Text.literal("§c[ArcadeDisaster+] §aJoined Disasters game!"), false);
                }
            } else if (!isInDisastersGame && wasInGame) {
                if (client.player != null) {
                    client.player.sendMessage(Text.literal("§c[ArcadeDisaster+] §eLeft Disasters game."), false);
                }
            }

            if (customTitleTimer > 0) {
                customTitleTimer--;
            }

            if (titleDelayTimer > 0) {
                titleDelayTimer--;
                if (titleDelayTimer == 0 && !currentDisasters.isEmpty() && client.inGameHud != null) {
                    displayEntries.clear();
                    for (int i = 0; i < currentDisasters.size(); i++) {
                        displayEntries.add(new String[]{currentDisasters.get(i), currentDescriptions.get(i)});
                    }
                    customTitleTimer = 100;

                    currentDisasters.clear();
                    currentDescriptions.clear();
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

                        context.getMatrices().pushMatrix();
                        context.getMatrices().translate(width / 2f, yPos);
                        context.getMatrices().scale(titleScale, titleScale);
                        Text titleText = Text.literal("§c§l" + name);
                        context.drawText(textRenderer, titleText, -textRenderer.getWidth(titleText) / 2, 0, 0xFFFFFFFF, true);
                        context.getMatrices().popMatrix();

                        yPos += textRenderer.fontHeight * titleScale + 3;

                        List<OrderedText> lines = textRenderer.wrapLines(Text.literal("§e" + description), maxSubtitleWidth);
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

                currentDisasters.add(disasterName);
                currentDescriptions.add(description);

                titleDelayTimer = 2;

                MinecraftClient.getInstance().execute(() -> {
                    if (MinecraftClient.getInstance().player != null) {
                        MinecraftClient.getInstance().player.sendMessage(Text.literal("§c[ArcadeDisaster+] §6Disaster started: §e" + disasterName), false);
                    }
                });
            }
        });
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
