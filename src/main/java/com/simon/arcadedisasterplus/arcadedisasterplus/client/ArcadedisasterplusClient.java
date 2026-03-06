package com.simon.arcadedisasterplus.arcadedisasterplus.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.text.Text;

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

        GameModeDetector.register();
    }
}
