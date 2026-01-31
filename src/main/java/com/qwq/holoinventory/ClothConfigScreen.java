package com.qwq.holoinventory;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ClothConfigScreen {
    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("config.holoinventoryreloaded.title"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory general = builder.getOrCreateCategory(Component.translatable("config.holoinventoryreloaded.category.general"));

        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.holoinventoryreloaded.defaultDisplay"), Config.defaultDisplay)
                .setDefaultValue(true)
                .setSaveConsumer(newValue -> {
                    Config.defaultDisplay = newValue;
                    Config.DEFAULT_DISPLAY.set(newValue);
                })
                .build());

        general.addEntry(entryBuilder.startDoubleField(Component.translatable("config.holoinventoryreloaded.renderDistance"), Config.renderDistance)
                .setDefaultValue(8.0)
                .setMin(1.0)
                .setMax(32.0)
                .setSaveConsumer(newValue -> {
                    Config.renderDistance = newValue;
                    Config.RENDER_DISTANCE.set(newValue);
                })
                .build());

        general.addEntry(entryBuilder.startDoubleField(Component.translatable("config.holoinventoryreloaded.scale"), Config.scale)
                .setDefaultValue(1.0)
                .setMin(0.1)
                .setMax(5.0)
                .setSaveConsumer(newValue -> {
                    Config.scale = newValue;
                    Config.SCALE.set(newValue);
                })
                .build());

        general.addEntry(entryBuilder.startIntField(Component.translatable("config.holoinventoryreloaded.maxItems"), Config.maxItems)
                .setDefaultValue(27)
                .setMin(1)
                .setMax(81)
                .setSaveConsumer(newValue -> {
                    Config.maxItems = newValue;
                    Config.MAX_ITEMS.set(newValue);
                })
                .build());

        general.addEntry(entryBuilder.startDoubleField(Component.translatable("config.holoinventoryreloaded.minRenderDistance"), Config.minRenderDistance)
                .setDefaultValue(1.5)
                .setMin(0.0)
                .setMax(5.0)
                .setSaveConsumer(newValue -> {
                    Config.minRenderDistance = newValue;
                    Config.MIN_RENDER_DISTANCE.set(newValue);
                })
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.holoinventoryreloaded.showItemCount"), Config.showItemCount)
                .setDefaultValue(true)
                .setSaveConsumer(newValue -> {
                    Config.showItemCount = newValue;
                    Config.SHOW_ITEM_COUNT.set(newValue);
                })
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.holoinventoryreloaded.showContainerName"), Config.showContainerName)
                .setDefaultValue(true)
                .setSaveConsumer(newValue -> {
                    Config.showContainerName = newValue;
                    Config.SHOW_CONTAINER_NAME.set(newValue);
                })
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.holoinventoryreloaded.render3D"), Config.render3D)
                .setDefaultValue(true)
                .setSaveConsumer(newValue -> {
                    Config.render3D = newValue;
                    Config.RENDER_3D.set(newValue);
                })
                .build());

        builder.setSavingRunnable(() -> {
            Config.SPEC.save();
        });

        return builder.build();
    }
}
