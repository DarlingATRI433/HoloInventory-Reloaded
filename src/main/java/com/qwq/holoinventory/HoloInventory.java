package com.qwq.holoinventory;

import com.mojang.logging.LogUtils;
import com.qwq.holoinventory.config.ClothConfigScreen;
import com.qwq.holoinventory.config.ModConfig;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import com.qwq.holoinventory.network.PacketHandler;

@Mod(HoloInventory.MOD_ID)
public class HoloInventory {
    public static final String MOD_ID = "holoinventoryreloaded";
    private static final Logger LOGGER = LogUtils.getLogger();

    public HoloInventory() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // 注册配置
        ModLoadingContext.get().registerConfig(Type.COMMON, ModConfig.SPEC);

        // 注册网络数据包
        PacketHandler.register();

        // 注册客户端相关的事件
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            // 注册 Cloth Config 界面
            ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                    () -> new ConfigScreenHandler.ConfigScreenFactory((client, parent) -> ClothConfigScreen.create(parent)));
        });

        MinecraftForge.EVENT_BUS.register(this);
    }
}
