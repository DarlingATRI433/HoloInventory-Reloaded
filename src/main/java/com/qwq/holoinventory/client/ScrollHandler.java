package com.qwq.holoinventory.client;

import com.qwq.holoinventory.inventory.InventoryData;
import com.qwq.holoinventory.network.NetworkHandler;
import com.qwq.holoinventory.render.HoloRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;

@EventBusSubscriber(value = Dist.CLIENT)
public class ScrollHandler {
    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        // 检查是否按住 Shift
        if (!mc.player.isShiftKeyDown()) return;

        // 检查准星是否指向方块
        if (mc.hitResult == null || mc.hitResult.getType() != HitResult.Type.BLOCK) return;

        BlockHitResult hitResult = (BlockHitResult) mc.hitResult;
        BlockPos pos = hitResult.getBlockPos();

        // 检查该方块是否有缓存的容器数据
        InventoryData data = NetworkHandler.getCachedInventory(pos);
        if (data == null || data.items().isEmpty()) return;

        // 计算最大滚动行数
        int totalItems = data.items().size();
        int rows = (int) Math.ceil(totalItems / 9.0);
        int maxScroll = Math.max(0, rows - 3); // 窗口显示 3 行

        if (maxScroll <= 0) return;

        // 获取当前偏移并更新
        int currentOffset = NetworkHandler.getScrollOffset(pos);
        double scrollDelta = event.getScrollDeltaY();

        if (scrollDelta > 0) {
            currentOffset = Math.max(0, currentOffset - 1);
        } else if (scrollDelta < 0) {
            currentOffset = Math.min(maxScroll, currentOffset + 1);
        }

        NetworkHandler.setScrollOffset(pos, currentOffset);

        // 取消事件，防止滚动导致快捷栏切换
        event.setCanceled(true);
    }
}
