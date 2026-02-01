package com.qwq.holoinventory.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.qwq.holoinventory.client.handler.ClientScrollHandler;
import com.qwq.holoinventory.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Nameable;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.GameRenderer;

public class InventoryRenderer {

    private static final int ITEMS_PER_PAGE = 27;

    /**
     * 渲染全息物品栏
     * @param poseStack 矩阵栈
     * @param partialTick 分层渲染的时间偏移
     * @param pos 容器位置
     * @param blockEntity 方块实体
     * @param stacks 物品列表（从缓存获取）
     */
    public static void render(PoseStack poseStack, float partialTick, BlockPos pos, BlockEntity blockEntity, List<ItemStack> stacks) {
        Minecraft mc = Minecraft.getInstance();
        ItemRenderer itemRenderer = mc.getItemRenderer();
        Font font = mc.font;
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        List<ItemStack> nonEmptyStacks = new ArrayList<>();
        if (stacks != null) {
            for (ItemStack stack : stacks) {
                if (!stack.isEmpty()) {
                    nonEmptyStacks.add(stack);
                }
            }
        }

        // 限制最大物品数
        int maxItems = ModConfig.COMMON.maxItems.get();
        if (nonEmptyStacks.size() > maxItems) {
            nonEmptyStacks = nonEmptyStacks.subList(0, maxItems);
        }

        if (nonEmptyStacks.isEmpty() && !ModConfig.COMMON.showName.get()) return;

        // 检查最小隐藏距离
        double distSq = mc.player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        double minHideDist = ModConfig.COMMON.minHideDistance.get();
        if (distSq < minHideDist * minHideDist) return;

        // 分页逻辑
        int totalItems = nonEmptyStacks.size();
        int maxPage = Math.max(1, (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE));
        int currentPage = ClientScrollHandler.getPage(pos);
        
        if (currentPage < 0) {
            currentPage = maxPage - 1;
            ClientScrollHandler.setPage(pos, currentPage, maxPage);
        } else if (currentPage >= maxPage) {
            currentPage = 0;
            ClientScrollHandler.setPage(pos, currentPage, maxPage);
        }

        int startIndex = currentPage * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, totalItems);
        List<ItemStack> pageStacks = nonEmptyStacks.subList(startIndex, endIndex);

        poseStack.pushPose();

        // 获取准星指向的具体位置，或者使用方块中心稍微靠近玩家一点
        Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();
        
        // 渲染位置逻辑修改：不再跟随准星指针的具体坐标，而是固定在方块中心并向玩家视角方向偏移
        Vec3 blockCenter = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        Vec3 toCamera = cameraPos.subtract(blockCenter).normalize();
        
        // 将渲染位置向摄像机方向偏移更多，防止穿模和方块遮挡
        Vec3 targetPos = blockCenter.add(toCamera.scale(1.1));
        
        poseStack.translate(targetPos.x - cameraPos.x, targetPos.y - cameraPos.y + 0.05, targetPos.z - cameraPos.z);

        poseStack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180f));

        // 应用显示缩放配置
        float baseScale = 0.4f;
        float configScale = ModConfig.COMMON.renderScale.get().floatValue();
        float finalScale = baseScale * configScale;
        poseStack.scale(finalScale, finalScale, finalScale);

        int itemsPerRow = ModConfig.COMMON.itemsPerRow.get();
        boolean is3D = ModConfig.COMMON.mode3D.get();
        float itemSpacing = is3D ? 0.77f : 0.7f; // 3D 模式间距也缩小，2D 保持 0.7
        int totalRows = Math.max(1, (int) Math.ceil((double) pageStacks.size() / itemsPerRow));
        int colsInFirstRow = pageStacks.isEmpty() ? 2 : Math.min(pageStacks.size(), itemsPerRow);

        float width = Math.max(1.0f, colsInFirstRow * itemSpacing + 0.3f);
        float height = (pageStacks.isEmpty() ? 0.6f : totalRows * itemSpacing) + (ModConfig.COMMON.showName.get() ? (is3D ? 1.2f : 1.0f) : 0.4f);
        
        renderBackground(poseStack, width, height, ModConfig.COMMON.showName.get() ? (is3D ? 0.4f : 0.35f) : 0f);

        if (ModConfig.COMMON.showName.get()) {
            Component name;
            if (blockEntity instanceof Nameable nameable) {
                name = nameable.getDisplayName();
            } else {
                name = blockEntity.getBlockState().getBlock().getName();
            }
            
            String title = name.getString();
            if (maxPage > 1) {
                title += String.format(" (%d/%d)", currentPage + 1, maxPage);
            }
            
            poseStack.pushPose();
            // 根据模式动态调整标题高度
            float nameY = pageStacks.isEmpty() ? 0.3f : (totalRows - 1) * itemSpacing / 2f + (is3D ? 1.2f : 1.0f);
            poseStack.translate(0, nameY, 0.01f);
            poseStack.scale(0.06f, -0.06f, 0.06f);
            font.drawInBatch(title, -font.width(title) / 2f, 0, 0xFFFFFFFF, true, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
            poseStack.popPose();
        }

        if (!pageStacks.isEmpty()) {
            float startY = (totalRows - 1) * itemSpacing / 2f;
            long time = mc.level.getGameTime();

            for (int i = 0; i < pageStacks.size(); i++) {
                ItemStack stack = pageStacks.get(i);
                int row = i / itemsPerRow;
                int col = i % itemsPerRow;

                int currentItemsInRow = (row == totalRows - 1) ? (pageStacks.size() % itemsPerRow == 0 ? itemsPerRow : pageStacks.size() % itemsPerRow) : itemsPerRow;
                float x = (col - (currentItemsInRow - 1) / 2f) * itemSpacing;
                float y = startY - row * itemSpacing;

                poseStack.pushPose();
                poseStack.translate(x, y, 0.02f);
                
                // 渲染物品，使用 push/pop 隔离缩放，防止影响后续的文字位移
                poseStack.pushPose();
                if (is3D) {
                    poseStack.scale(1.3f, 1.3f, 1.3f); 
                    float rotation = (time + partialTick) * 4f;
                    poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
                    itemRenderer.renderStatic(stack, ItemDisplayContext.GROUND, 15728880, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, mc.level, 0);
                } else {
                    poseStack.scale(0.5f, 0.5f, 0.5f); // 缩小 2D 图标，匹配原版 GUI 比例
                    itemRenderer.renderStatic(stack, ItemDisplayContext.GUI, 15728880, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, mc.level, 0);
                }
                poseStack.popPose();
                
                if (ModConfig.COMMON.showItemCount.get() && stack.getCount() > 1) {
                    poseStack.pushPose();
                    if (is3D) {
                        // 文字紧贴在 3D 图标下方中心
                        poseStack.translate(0, -0.2, 0.15); 
                    } else {
                        // 2D 模式下将文字放在图标右下角，模拟原版物品栏效果
                        poseStack.translate(0.25, -0.25, 0.05);
                    }
                    poseStack.scale(0.025f, -0.025f, 0.025f);
                    String countText = formatCount(stack.getCount());
                    // 3D 模式居中，2D 模式右对齐
                    font.drawInBatch(countText, is3D ? -font.width(countText) / 2f : -font.width(countText), 0, 0xFFFFFFFF, true, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
                    poseStack.popPose();
                }
                
                poseStack.popPose();
            }
        }

        bufferSource.endBatch();
        poseStack.popPose();
    }

    private static String formatCount(int count) {
        if (count >= 1000000) return String.format("%.1fM", count / 1000000.0f);
        if (count >= 1000) return String.format("%.1fK", count / 1000.0f);
        return String.valueOf(count);
    }

    private static void renderBackground(PoseStack poseStack, float width, float height, float yOffset) {
        Matrix4f matrix = poseStack.last().pose();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        float x1 = -width / 2f;
        float x2 = width / 2f;
        float y1 = -height / 2f + yOffset;
        float y2 = height / 2f + yOffset;
        float z = 0.0f;

        // 深色半透明背景 (ARGB: 0x99000000)
        int r = 0, g = 0, b = 0, a = 160;
        bufferBuilder.vertex(matrix, x1, y1, z).color(r, g, b, a).endVertex();
        bufferBuilder.vertex(matrix, x1, y2, z).color(r, g, b, a).endVertex();
        bufferBuilder.vertex(matrix, x2, y2, z).color(r, g, b, a).endVertex();
        bufferBuilder.vertex(matrix, x2, y1, z).color(r, g, b, a).endVertex();

        tesselator.end();
        RenderSystem.disableBlend();
    }
}
