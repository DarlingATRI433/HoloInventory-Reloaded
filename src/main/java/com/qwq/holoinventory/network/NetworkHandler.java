package com.qwq.holoinventory.network;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.qwq.holoinventory.inventory.InventoryData;
import com.qwq.holoinventory.inventory.InventoryScanner;

import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class NetworkHandler {
    // 客户端缓存：坐标 -> 容器数据 (使用 ConcurrentHashMap 保证线程安全)
    private static final Map<BlockPos, InventoryData> CLIENT_CACHE = new ConcurrentHashMap<>();
    // 上次请求时间：坐标 -> 时间戳 (防止请求过于频繁)
    private static final Map<BlockPos, Long> LAST_REQUEST_TIME = new ConcurrentHashMap<>();
    // 最大缓存条目数，防止内存溢出
    private static final int MAX_CACHE_SIZE = 500;

    public static void handleRequest(final InventoryRequestPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            BlockPos pos = payload.pos();
            InventoryData data = InventoryScanner.getInventoryContent(context.player().level(), pos);
            if (data != null) {
                context.reply(new InventoryResponsePayload(pos, data.items(), data.name()));
            }
        });
    }

    public static void handleResponse(final InventoryResponsePayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            // 如果缓存过大，清理一下，防止内存溢出
            if (CLIENT_CACHE.size() > MAX_CACHE_SIZE) {
                CLIENT_CACHE.clear();
            }
            CLIENT_CACHE.put(payload.pos(), new InventoryData(payload.items(), payload.name()));
        });
    }

    public static InventoryData getCachedInventory(BlockPos pos) {
        return CLIENT_CACHE.get(pos);
    }

    public static boolean shouldRequest(BlockPos pos) {
        long now = System.currentTimeMillis();
        long last = LAST_REQUEST_TIME.getOrDefault(pos, 0L);
        if (now - last > 1000) { // 每秒最多请求一次
            // 防止 LAST_REQUEST_TIME 无限增长
            if (LAST_REQUEST_TIME.size() > MAX_CACHE_SIZE) {
                LAST_REQUEST_TIME.clear();
            }
            LAST_REQUEST_TIME.put(pos, now);
            return true;
        }
        return false;
    }
    
    public static void clearCache() {
        CLIENT_CACHE.clear();
        LAST_REQUEST_TIME.clear();
    }
}
