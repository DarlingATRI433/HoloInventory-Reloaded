package com.qwq.holoinventory.integration;

import com.qwq.holoinventory.ClothConfigScreen;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

public class ClothConfigIntegration {
    public static void register(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, (c, parent) -> ClothConfigScreen.create(parent));
    }
}
