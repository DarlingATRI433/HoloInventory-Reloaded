package com.qwq.holoinventory;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import net.neoforged.fml.event.config.ModConfigEvent;

import com.qwq.holoinventory.network.InventoryRequestPayload;
import com.qwq.holoinventory.network.InventoryResponsePayload;
import com.qwq.holoinventory.network.NetworkHandler;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@Mod(HoloInventoryReloaded.MODID)
public class HoloInventoryReloaded {
    public static final String MODID = "holoinventoryreloaded";
    public static final Logger LOGGER = LogUtils.getLogger();

    public HoloInventoryReloaded(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::onConfigLoad);
        modEventBus.addListener(this::registerNetworking);
        modEventBus.addListener(this::addCreative);
        
        ModItems.register(modEventBus);
        
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.HOLO_GLASSES);
        }
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("HoloInventory Reloaded initialized");
    }

    private void registerNetworking(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(MODID);
        
        // 注册请求包：客户端发送到服务端
        registrar.playToServer(
                InventoryRequestPayload.TYPE,
                InventoryRequestPayload.CODEC,
                NetworkHandler::handleRequest
        );
        
        // 注册响应包：服务端发送到客户端
        registrar.playToClient(
                InventoryResponsePayload.TYPE,
                InventoryResponsePayload.CODEC,
                NetworkHandler::handleResponse
        );
    }

    private void onConfigLoad(ModConfigEvent event) {
        if (event.getConfig().getModId().equals(MODID)) {
            Config.load();
        }
    }
}
