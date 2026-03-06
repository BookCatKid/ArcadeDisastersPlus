package com.simon.arcadedisastersplus.arcadedisastersplus.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class ArcadedisastersplusConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("arcadedisastersplus.json");

    public static boolean showDisasterTitles = true;
    public static boolean showChatMessages = true;
    public static boolean showEndedTitles = true;
    public static boolean devMode = false;

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            save();
            return;
        }
        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            ConfigData data = GSON.fromJson(reader, ConfigData.class);
            if (data != null) {
                showDisasterTitles = data.showDisasterTitles;
                showChatMessages = data.showChatMessages;
                showEndedTitles = data.showEndedTitles;
                devMode = data.devMode;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        ConfigData data = new ConfigData();
        data.showDisasterTitles = showDisasterTitles;
        data.showChatMessages = showChatMessages;
        data.showEndedTitles = showEndedTitles;
        data.devMode = devMode;
        try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
            GSON.toJson(data, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ConfigData {
        boolean showDisasterTitles = true;
        boolean showChatMessages = true;
        boolean showEndedTitles = true;
        boolean devMode = false;
    }
}
