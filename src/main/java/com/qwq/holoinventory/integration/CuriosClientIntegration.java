package com.qwq.holoinventory.integration;

import com.qwq.holoinventory.ModItems;
import com.qwq.holoinventory.client.render.HoloGlassesCurioRenderer;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

public class CuriosClientIntegration {
    public static void register() {
        CuriosRendererRegistry.register(ModItems.HOLO_GLASSES.get(), HoloGlassesCurioRenderer::new);
    }
}
