package com.qwq.holoinventory.render;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.qwq.holoinventory.Config;
import com.qwq.holoinventory.ModItems;
import com.qwq.holoinventory.integration.CuriosIntegration;
import com.qwq.holoinventory.inventory.InventoryData;
import com.qwq.holoinventory.network.InventoryRequestPayload;
import com.qwq.holoinventory.network.NetworkHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 全息渲染处理器
 */
public class HoloRenderer {
    /**
     * 渲染事件监听器
     */
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        // 使用 AFTER_TRANSLUCENT_BLOCKS
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.hitResult == null || mc.hitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }

        // 核心显示逻辑：默认显示开启，或者玩家佩戴了全息护目镜
        if (!shouldShowHolo(mc.player)) {
            return;
        }

        BlockHitResult hitResult = (BlockHitResult) mc.hitResult;
        BlockPos pos = hitResult.getBlockPos();
        
        // 检查最大渲染距离
        double distSq = mc.player.distanceToSqr(Vec3.atCenterOf(pos));
        if (distSq > Config.renderDistance * Config.renderDistance) {
            return;
        }
        
        // 检查最小显示距离：距离太近时不显示
        if (distSq < Config.minRenderDistance * Config.minRenderDistance) {
            return;
        }

        // 优先使用缓存的数据
        InventoryData data = NetworkHandler.getCachedInventory(pos);
        
        // 如果没有缓存或需要更新，发送请求给服务器
        if (NetworkHandler.shouldRequest(pos)) {
            int lastHash = (data != null) ? data.hash() : 0;
            PacketDistributor.sendToServer(new InventoryRequestPayload(pos, lastHash));
        }

        if (data == null || data.items().isEmpty()) {
            return;
        }

        renderHolo(event, data, pos);
    }

    private static boolean lastShouldShow = false;
    private static long lastCheckTime = 0;

    public static boolean shouldShowHolo(Player player) {
        if (Config.defaultDisplay) return true;
        if (player == null) return false;

        long currentTime = player.level().getGameTime();
        if (currentTime == lastCheckTime) {
            return lastShouldShow;
        }

        lastCheckTime = currentTime;
        
        // 检查原版头部栏位
        ItemStack headItem = player.getItemBySlot(EquipmentSlot.HEAD);
        if (!headItem.isEmpty() && headItem.is(ModItems.HOLO_GLASSES.get())) {
            lastShouldShow = true;
            return true;
        }

        // 检查 Curios 饰品栏
        if (ModList.get().isLoaded("curios")) {
            lastShouldShow = CuriosIntegration.hasHoloGlasses(player);
            return lastShouldShow;
        }

        lastShouldShow = false;
        return false;
    }

    private static void renderHolo(RenderLevelStageEvent event, InventoryData data, BlockPos pos) {
        Minecraft mc = Minecraft.getInstance();
        List<ItemStack> items = data.items();
        PoseStack poseStack = event.getPoseStack();
        Vec3 cameraPos = event.getCamera().getPosition();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        // 禁用深度测试，使内容可以透过方块显示
        RenderSystem.disableDepthTest();

        poseStack.pushPose();
        // 1. 移动到方块中心
        poseStack.translate(pos.getX() + 0.5 - cameraPos.x, pos.getY() + 0.5 - cameraPos.y, pos.getZ() + 0.5 - cameraPos.z);

        // 2. 看板效果：使渲染平面始终面向玩家
        poseStack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
        
        // 3. 向玩家方向偏移更远一些 (增加 Z 偏移)
        poseStack.translate(0, 0, 0.9);

        // 4. 计算动态缩放和布局
        int totalItems = items.size();
        int columns = 9; // 宽 9 格布局
        int displayRows = 3; // 固定显示 3 行
        int totalRows = (int) Math.ceil((double) totalItems / columns);
        
        // 获取滚动偏移并修正
        int scrollOffset = NetworkHandler.getScrollOffset(pos);
        if (scrollOffset > Math.max(0, totalRows - displayRows)) {
            scrollOffset = Math.max(0, totalRows - displayRows);
            NetworkHandler.setScrollOffset(pos, scrollOffset);
        }
        
        int startIndex = scrollOffset * columns;
        int endIndex = Math.min(startIndex + columns * displayRows, totalItems);
        int count = endIndex - startIndex;
        int currentRows = (int) Math.ceil((double) count / columns);
        
        // 调慢旋转速度：将 0.05f 降为 0.02f
        float time = (mc.level.getGameTime() + event.getPartialTick().getGameTimeDeltaTicks()) * 0.01f;
        
        // 缩放逻辑
        float globalScale = (float) Config.scale;
        float itemScaleMultiplier = (float) Config.itemScale;
        float fixedSpacing = (float) Config.itemSpacing;
        
        // 统一缩放系数，避免物品数量变化时产生突变
        // 0.4f 是一个比较合适的基准值，确保在默认配置下显示效果适中
        float adaptiveScale = 0.4f; 
        
        // finalItemScale 决定了物品图标的大小，随 globalScale 和 itemScaleMultiplier 变化
        float finalItemScale = 1f * globalScale * itemScaleMultiplier * adaptiveScale;
        
        // 间距采用固定值，仅受整体缩放 (globalScale) 和自适应缩放 (adaptiveScale) 影响
        float spacing = fixedSpacing * globalScale * adaptiveScale;
        
        // 调整 3D 模式下的间距：缩小垂直和水平间距
        float verticalSpacing = spacing;
        if (Config.render3D) {
            verticalSpacing *= 1.25f; // 从 1.4f 降到 1.25f，缩小垂直重叠感
            spacing *= 1.05f; // 从 1.1f 降到 1.05f，稍微紧凑一点
        } else {
            // 2D 模式下显著增加间距
            spacing *= 1.5f;
            verticalSpacing = spacing;
        }

        // 6. 渲染物品网格 (9列布局)
        float startX = -(Math.min(count, columns) - 1) * spacing / 2f;
        float startY = (currentRows - 1) * verticalSpacing / 2f;

        // --- 第一阶段：渲染物品图标 ---
        for (int i = 0; i < count; i++) {
            int row = i / columns;
            int col = i % columns;
            
            ItemStack stack = items.get(startIndex + i);
            poseStack.pushPose();
            
            float x = startX + col * spacing;
            float y = startY - row * verticalSpacing;
            poseStack.translate(x, y, 0);
            
            if (Config.render3D) {
                // 3D 模式：掉落物旋转效果
                poseStack.mulPose(Axis.YP.rotation(time * 1.5f + (startIndex + i)));
                poseStack.scale(finalItemScale, finalItemScale, finalItemScale);
                mc.getItemRenderer().renderStatic(stack, ItemDisplayContext.GROUND, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, mc.level, 0);
            } else {
                // 2D 模式：使用 GUI 渲染模式，不旋转
                // GUI 模式下图标稍微放大一点，因为没有 3D 旋转看起来会小一些
                float guiScale = finalItemScale * 0.65f; 
                poseStack.scale(guiScale, guiScale, 0.01f);
                // 移除翻转，用户反馈图标是倒着的
                mc.getItemRenderer().renderStatic(stack, ItemDisplayContext.GUI, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, mc.level, 0);
            }
            
            poseStack.popPose();
        }

        // 提交物品渲染，确保文字在其之上
        bufferSource.endBatch();

        // --- 第二阶段：渲染文字 (名称和数量) ---
        // 渲染容器名称 (在列表上方，增加间距防止遮挡)
        if (Config.showContainerName) {
            // 如果有更多物品未显示，在名称后加一个提示
            String displayName = data.name();
            if (totalRows > displayRows) {
                displayName += String.format(" (%d/%d)", scrollOffset + 1, totalRows - displayRows + 1);
            }
            HoloTextRenderer.renderContainerName(poseStack, displayName, 0, startY + spacing * 1.2f, globalScale);
        }

        if (Config.showItemCount) {
            for (int i = 0; i < count; i++) {
                int row = i / columns;
                int col = i % columns;
                
                ItemStack stack = items.get(startIndex + i);
                poseStack.pushPose();
                
                float x = startX + col * spacing;
                float y = startY - row * verticalSpacing;
                // 移动到物品的中心位置
                poseStack.translate(x, y, 0);
                
                // 传入 finalItemScale 以确保文字偏移与图标缩放同步
                HoloTextRenderer.renderItemCount(poseStack, stack.getCount(), finalItemScale);
                
                poseStack.popPose();
            }
        }

        poseStack.popPose();
        
        // 再次提交文字渲染
        bufferSource.endBatch();
        
        // 重新启用深度测试
        RenderSystem.enableDepthTest();
    }
}
