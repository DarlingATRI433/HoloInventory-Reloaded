package com.qwq.holoinventory.inventory;

import net.minecraft.world.item.ItemStack;
import java.util.List;

/**
 * 容器数据包，包含物品列表和容器名称
 */
public record InventoryData(List<ItemStack> items, String name) {
}
