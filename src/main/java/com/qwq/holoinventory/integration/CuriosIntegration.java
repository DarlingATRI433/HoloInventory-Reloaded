package com.qwq.holoinventory.integration;

import com.qwq.holoinventory.ModItems;
import net.minecraft.world.entity.LivingEntity;
import top.theillusivec4.curios.api.CuriosApi;

public class CuriosIntegration {
    public static boolean hasHoloGlasses(LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity)
                .map(handler -> handler.findFirstCurio(ModItems.HOLO_GLASSES.get()).isPresent())
                .orElse(false);
    }
}
