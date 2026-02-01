package com.qwq.holoinventory.inventory;

import java.util.ArrayList;
import java.util.List;

import com.qwq.holoinventory.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * 容器物品扫描工具类
 */
public class InventoryScanner {
    /**
     * 获取指定位置容器的物品列表
     * @param level 世界
     * @param pos 方块坐标
     * @return 容器数据，若非容器则返回 null
     */
    public static InventoryData getInventoryContent(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) return null;

        List<ItemStack> stacks = new ArrayList<>();
        String name = "";

        // 获取名称
        if (blockEntity instanceof Nameable nameable) {
            name = nameable.getDisplayName().getString();
        } else if (blockEntity instanceof MenuProvider menuProvider) {
            name = menuProvider.getDisplayName().getString();
        } else {
            name = blockEntity.getBlockState().getBlock().getName().getString();
        }

        // 1. 尝试通过 NeoForge Capability 获取 (适用于大多数支持 NeoForge 的模组)
        IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
        if (handler != null) {
            for (int i = 0; i < handler.getSlots(); i++) {
                if (stacks.size() >= Config.maxItems) break;
                ItemStack stack = handler.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    mergeStack(stacks, stack);
                }
            }
        } 
        // 2. 尝试作为原生 Container 获取 (适用于部分原生容器或未适配 Capability 的模组)
        else if (blockEntity instanceof Container container) {
            for (int i = 0; i < container.getContainerSize(); i++) {
                if (stacks.size() >= Config.maxItems) break;
                ItemStack stack = container.getItem(i);
                if (!stack.isEmpty()) {
                    mergeStack(stacks, stack);
                }
            }
        }

        return stacks.isEmpty() ? null : new InventoryData(List.copyOf(stacks), name);
    }

    /**
     * 合并物品堆，减少不必要的 copy() 调用
     */
    private static void mergeStack(List<ItemStack> stacks, ItemStack newStack) {
        for (ItemStack stack : stacks) {
            if (ItemStack.isSameItemSameComponents(stack, newStack)) {
                stack.grow(newStack.getCount());
                return;
            }
        }
        // 只有在无法合并，需要存入列表时才进行 copy，避免影响原容器
        stacks.add(newStack.copy());
    }
}
