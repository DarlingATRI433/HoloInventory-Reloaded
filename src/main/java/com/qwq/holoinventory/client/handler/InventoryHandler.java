package com.qwq.holoinventory.client.handler;

import com.qwq.holoinventory.client.renderer.InventoryRenderer;
import com.qwq.holoinventory.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.qwq.holoinventory.HoloInventory;

import net.minecraft.world.Container;
import net.minecraftforge.items.wrapper.InvWrapper;

import com.qwq.holoinventory.network.ClientInventoryCache;
import com.qwq.holoinventory.network.ContainerRequestPacket;
import com.qwq.holoinventory.network.PacketHandler;
import net.minecraft.world.item.ItemStack;
import java.util.List;

@Mod.EventBusSubscriber(modid = HoloInventory.MOD_ID, value = Dist.CLIENT)
public class InventoryHandler {

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        // 使用更早的渲染阶段，确保在大多数方块之后渲染但仍在透明层附近
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS) return;
        if (!ModConfig.COMMON.enableHoloInventory.get()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        HitResult hitResult = mc.hitResult;
        if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult) hitResult;
            BlockPos pos = blockHitResult.getBlockPos();
            Level level = mc.level;
            
            // 检查渲染距离
            if (mc.player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > 
                ModConfig.COMMON.renderRange.get() * ModConfig.COMMON.renderRange.get()) {
                return;
            }

            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null) {
                // 请求数据更新
                if (ClientInventoryCache.shouldRequest(pos)) {
                    PacketHandler.INSTANCE.sendToServer(new ContainerRequestPacket(pos));
                }

                // 从缓存中获取物品
                List<ItemStack> cachedStacks = ClientInventoryCache.get(pos);
                InventoryRenderer.render(event.getPoseStack(), event.getPartialTick(), pos, blockEntity, cachedStacks);
            }
        }
    }
}
