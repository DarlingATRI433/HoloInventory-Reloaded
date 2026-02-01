package com.qwq.holoinventory.client.handler;

import net.minecraft.core.BlockPos;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.qwq.holoinventory.HoloInventory;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.BlockHitResult;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = HoloInventory.MOD_ID, value = Dist.CLIENT)
public class ClientScrollHandler {
    private static final Map<BlockPos, Integer> SCROLL_PAGES = new HashMap<>();

    public static int getPage(BlockPos pos) {
        return SCROLL_PAGES.getOrDefault(pos, 0);
    }

    public static void setPage(BlockPos pos, int page, int maxPage) {
        if (page < 0) page = maxPage - 1;
        if (page >= maxPage) page = 0;
        SCROLL_PAGES.put(pos, page);
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.isShiftKeyDown()) {
            HitResult hitResult = mc.hitResult;
            if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHitResult = (BlockHitResult) hitResult;
                BlockPos pos = blockHitResult.getBlockPos();
                
                // Only handle scroll if we are looking at a block entity that we can display
                if (mc.level != null && mc.level.getBlockEntity(pos) != null) {
                    double scrollDelta = event.getScrollDelta();
                    if (scrollDelta != 0) {
                        int currentPage = getPage(pos);
                        // We'll update the page in InventoryHandler when we know the max page
                        // For now, just mark that we want to scroll
                        int delta = scrollDelta > 0 ? -1 : 1;
                        SCROLL_PAGES.put(pos, currentPage + delta);
                        event.setCanceled(true);
                    }
                }
            }
        }
    }
}
