package com.qwq.holoinventory.inventory;

import net.minecraft.world.item.ItemStack;
import java.util.List;
import java.util.Objects;

/**
 * 容器数据包，包含物品列表和容器名称
 */
public record InventoryData(List<ItemStack> items, String name, int hash) {
    public InventoryData(List<ItemStack> items, String name) {
        this(items, name, computeHash(items, name));
    }

    private static int computeHash(List<ItemStack> items, String name) {
        int result = name.hashCode();
        for (ItemStack stack : items) {
            // 组合物品 ID、数量和组件的哈希
            result = 31 * result + stack.getItem().hashCode();
            result = 31 * result + stack.getCount();
            result = 31 * result + stack.getComponents().hashCode();
        }
        return result;
    }
}
