package com.qwq.holoinventory.config;

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

        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.holoinventoryreloaded.enableHoloInventory"), ModConfig.COMMON.enableHoloInventory.get())
                .setDefaultValue(true)
                .setSaveConsumer(ModConfig.COMMON.enableHoloInventory::set)
                .build());

        general.addEntry(entryBuilder.startDoubleField(Component.translatable("config.holoinventoryreloaded.renderRange"), ModConfig.COMMON.renderRange.get())
                .setDefaultValue(8.0)
                .setSaveConsumer(ModConfig.COMMON.renderRange::set)
                .build());

        general.addEntry(entryBuilder.startDoubleField(Component.translatable("config.holoinventoryreloaded.renderScale"), ModConfig.COMMON.renderScale.get())
                .setDefaultValue(1.0)
                .setSaveConsumer(ModConfig.COMMON.renderScale::set)
                .build());

        general.addEntry(entryBuilder.startIntField(Component.translatable("config.holoinventoryreloaded.maxItems"), ModConfig.COMMON.maxItems.get())
                .setDefaultValue(1024)
                .setSaveConsumer(ModConfig.COMMON.maxItems::set)
                .build());

        general.addEntry(entryBuilder.startDoubleField(Component.translatable("config.holoinventoryreloaded.minHideDistance"), ModConfig.COMMON.minHideDistance.get())
                .setDefaultValue(1.5)
                .setSaveConsumer(ModConfig.COMMON.minHideDistance::set)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.holoinventoryreloaded.showItemCount"), ModConfig.COMMON.showItemCount.get())
                .setDefaultValue(true)
                .setSaveConsumer(ModConfig.COMMON.showItemCount::set)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.holoinventoryreloaded.showName"), ModConfig.COMMON.showName.get())
                .setDefaultValue(true)
                .setSaveConsumer(ModConfig.COMMON.showName::set)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.holoinventoryreloaded.mode3D"), ModConfig.COMMON.mode3D.get())
                .setDefaultValue(true)
                .setSaveConsumer(ModConfig.COMMON.mode3D::set)
                .build());

        builder.setSavingRunnable(() -> {
            ModConfig.SPEC.save();
        });

        return builder.build();
    }
}
