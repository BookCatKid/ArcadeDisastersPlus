package com.simon.arcadedisastersplus.arcadedisastersplus.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.text.Text;

public class ConfigScreen extends Screen {

    private final Screen parent;

    public ConfigScreen(Screen parent) {
        super(Text.literal("ArcadeDisasters+ Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = width / 2;
        int y = height / 4;

        addDrawableChild(CyclingButtonWidget.onOffBuilder(
                Text.literal("§aEnabled"),
                Text.literal("§cDisabled"),
                ArcadedisastersplusConfig.showDisasterTitles
        ).build(centerX - 100, y, 200, 20,
                Text.literal("Disaster Started Titles"),
                (button, value) -> ArcadedisastersplusConfig.showDisasterTitles = value));

        y += 28;

        addDrawableChild(CyclingButtonWidget.onOffBuilder(
                Text.literal("§aEnabled"),
                Text.literal("§cDisabled"),
                ArcadedisastersplusConfig.showEndedTitles
        ).build(centerX - 100, y, 200, 20,
                Text.literal("Disaster Ended Titles"),
                (button, value) -> ArcadedisastersplusConfig.showEndedTitles = value));

        y += 28;

        addDrawableChild(CyclingButtonWidget.onOffBuilder(
                Text.literal("§aEnabled"),
                Text.literal("§cDisabled"),
                ArcadedisastersplusConfig.showChatMessages
        ).build(centerX - 100, y, 200, 20,
                Text.literal("Chat Messages"),
                (button, value) -> ArcadedisastersplusConfig.showChatMessages = value));

        y += 28;

        addDrawableChild(CyclingButtonWidget.onOffBuilder(
                Text.literal("§aEnabled"),
                Text.literal("§cDisabled"),
                ArcadedisastersplusConfig.devMode
        ).build(centerX - 100, y, 200, 20,
                Text.literal("Random Dev Stuff"),
                (button, value) -> ArcadedisastersplusConfig.devMode = value));

        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), button -> close())
                .dimensions(centerX - 100, height - 40, 200, 20)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, height / 4 - 20, 0xFFFFFF);
    }

    @Override
    public void close() {
        ArcadedisastersplusConfig.save();
        client.setScreen(parent);
    }
}
