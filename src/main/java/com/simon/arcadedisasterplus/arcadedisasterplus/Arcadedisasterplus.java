package com.simon.arcadedisasterplus.arcadedisasterplus;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class Arcadedisasterplus implements ModInitializer {

    @Override
    public void onInitialize() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(literal("disasters").executes(context -> {
                // Is this the right way to do this? Who knows!
                // Also this might should have settings, not run on non-hypixel, and maybe detect when it won't work in some contexts
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.player != null) {
                    client.player.networkHandler.sendChatCommand("play arcade_disasters");
                }
                return 1;
            }));
        });
    }
}
