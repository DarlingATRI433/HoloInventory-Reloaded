package com.qwq.holoinventory.network;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientInventoryCache {
    private static final Map<BlockPos, List<ItemStack>> CACHE = new HashMap<>();
    private static final Map<BlockPos, Long> LAST_REQUEST_TIME = new HashMap<>();

    public static void update(BlockPos pos, List<ItemStack> stacks) {
        CACHE.put(pos, stacks);
    }

    public static List<ItemStack> get(BlockPos pos) {
        return CACHE.get(pos);
    }

    public static boolean shouldRequest(BlockPos pos) {
        long now = System.currentTimeMillis();
        long lastTime = LAST_REQUEST_TIME.getOrDefault(pos, 0L);
        if (now - lastTime > 1000) { // 每秒最多请求一次
            LAST_REQUEST_TIME.put(pos, now);
            return true;
        }
        return false;
    }
}
