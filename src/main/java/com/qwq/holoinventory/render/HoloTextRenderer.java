package com.qwq.holoinventory.render;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * 全息文本渲染器
 */
public class HoloTextRenderer {

    /**
     * 在指定位置渲染文本
     *
     * @param poseStack   变换栈
     * @param text        文本内容
     * @param x           X 坐标
     * @param y           Y 坐标
     * @param color       颜色
     * @param shadow      是否绘制阴影
     * @param scale       缩放比例
     * @param centered    是否居中
     */
    public static void renderText(PoseStack poseStack, String text, float x, float y, int color, boolean shadow, float scale, boolean centered) {
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        poseStack.pushPose();
        // 调整偏移和缩放
        poseStack.translate(x, y, 0.05f); 
        
        // 修正文字镜像问题：
        // 在 3D 渲染中，由于 poseStack 已经有了摄像机旋转，
        // 我们只需要微调文字本身的缩放方向。
        // Minecraft 的 Font 默认是在 Y 轴正方向向下生长的，
        // 而我们在 3D 空间中通常希望 Y 轴正方向向上。
        poseStack.scale(scale, -scale, scale);

        float renderX = centered ? -font.width(text) / 2f : 0;
        // 因为 scale 翻转了 Y 轴，所以这里的偏移也要对应调整
        float renderY = -font.lineHeight / 2f;

        Matrix4f matrix = poseStack.last().pose();
        font.drawInBatch(text, renderX, renderY, color, shadow, matrix, bufferSource, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);

        poseStack.popPose();
    }

    /**
     * 渲染物品数量
     */
    public static void renderItemCount(PoseStack poseStack, int count, float itemScale) {
        if (count <= 1) return;
        
        String text = formatCount(count);
        // 将文字移动到图标下方更远一点的位置，避免与图标中心重叠
        // 增加 Y 轴负方向偏移（在 Minecraft 渲染中，由于我们 scale(s, -s, s)，负值代表向下）
        float yOffset = -0.12f * itemScale;
        
        // 稍微增大字体缩放，使其在图标下方更清晰
        renderText(poseStack, text, 0, yOffset, 0xFFFFFFFF, true, itemScale * 0.025f, true);
    }

    /**
     * 格式化物品数量，支持 K, M, G 缩写
     * 优化：减少 String.format 的调用，对于常见小数量直接返回
     */
    private static String formatCount(int count) {
        if (count < 1000) {
            return Integer.toString(count);
        } else if (count < 1000000) {
            return formatDecimal(count / 1000.0) + "K";
        } else if (count < 1000000000) {
            return formatDecimal(count / 1000000.0) + "M";
        } else {
            double gCount = count / 1000000000.0;
            if (gCount > 2.1) gCount = 2.1;
            return formatDecimal(gCount) + "G";
        }
    }

    private static String formatDecimal(double value) {
        // 对于整数，直接转换，避免 String.format
        if (value == (long) value) {
            return Long.toString((long) value);
        }
        // 使用 Locale.US 确保小数点为 "."
        String formatted = String.format(java.util.Locale.US, "%.1f", value);
        // 如果以 .0 结尾，去掉它
        if (formatted.endsWith(".0")) {
            return formatted.substring(0, formatted.length() - 2);
        }
        return formatted;
    }

    /**
     * 渲染容器名称
     */
    public static void renderContainerName(PoseStack poseStack, String name, float x, float y, float scale) {
        if (name == null || name.isEmpty()) return;
        // 稍微减小缩放，防止挡住太多
        renderText(poseStack, name, x, y, 0xFFFFFFFF, true, scale * 0.018f, true);
    }
}
