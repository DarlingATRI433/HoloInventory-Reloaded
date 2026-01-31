package com.qwq.holoinventory;

import com.qwq.holoinventory.integration.ClothConfigIntegration;
import com.qwq.holoinventory.integration.CuriosClientIntegration;
import com.qwq.holoinventory.network.NetworkHandler;
import com.qwq.holoinventory.render.HoloRenderer;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = HoloInventoryReloaded.MODID, dist = Dist.CLIENT)
public class HoloInventoryReloadedClient {
    public HoloInventoryReloadedClient(ModContainer container) {
        if (ModList.get().isLoaded("cloth_config")) {
            ClothConfigIntegration.register(container);
        }
        
        // 注册渲染事件
        NeoForge.EVENT_BUS.addListener(HoloRenderer::onRenderLevelStage);

        // 玩家离开服务器时清理缓存
        NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> NetworkHandler.clearCache());

        // 注册 Curios 渲染器
        container.getEventBus().addListener(this::onClientSetup);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        if (ModList.get().isLoaded("curios")) {
            event.enqueueWork(CuriosClientIntegration::register);
        }
    }
}
