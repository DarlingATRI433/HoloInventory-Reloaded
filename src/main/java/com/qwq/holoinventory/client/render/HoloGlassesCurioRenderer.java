package com.qwq.holoinventory.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class HoloGlassesCurioRenderer implements ICurioRenderer {
    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(
            ItemStack stack,
            SlotContext slotContext,
            PoseStack matrixStack,
            RenderLayerParent<T, M> renderLayerParent,
            MultiBufferSource renderTypeBuffer,
            int light,
            float limbSwing,
            float limbSwingAmount,
            float partialTicks,
            float ageInTicks,
            float netHeadYaw,
            float headPitch) {

        if (renderLayerParent.getModel() instanceof HumanoidModel<?> humanoidModel) {
            matrixStack.pushPose();

            // 锁定到头部模型的位置
            humanoidModel.head.translateAndRotate(matrixStack);

            // 修复倒置问题并调整贴合度
            // 1. Curios 渲染通常需要翻转 Z 轴或 X 轴旋转 180 度来匹配 Minecraft 的模型空间
            matrixStack.mulPose(Axis.XP.rotationDegrees(180f));
            matrixStack.mulPose(Axis.YP.rotationDegrees(180f));

            // 2. 调整垂直高度：由于旋转了 180 度，正负号会反转
            // 我们需要将眼镜向上移动到眼睛位置。
            matrixStack.translate(0, 0.25, 0);

            // 3. 调整大小：ItemRenderer 在渲染 HEAD 上下文时默认会放大
            // 我们将其缩小到 0.625 左右，这通常是原版盔甲模型的比例，
            // 这样它就会和 JSON 中 scale: [0.92, 0.92, 0.92] 叠加后达到正确的大小
            float scale = 0.625f;
            matrixStack.scale(scale, scale, scale);

            // 4. 渲染物品模型
            // 使用 ItemDisplayContext.HEAD
            Minecraft.getInstance().getItemRenderer().renderStatic(
                    stack,
                    ItemDisplayContext.HEAD,
                    light,
                    net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,
                    matrixStack,
                    renderTypeBuffer,
                    slotContext.entity().level(),
                    0
            );

            matrixStack.popPose();
        }
    }
}
