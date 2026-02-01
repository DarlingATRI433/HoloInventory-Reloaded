package com.qwq.holoinventory.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class ModConfig {
    public static final ForgeConfigSpec SPEC;
    public static final Common COMMON;

    static {
        Pair<Common, ForgeConfigSpec> commonPair = new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON = commonPair.getLeft();
        SPEC = commonPair.getRight();
    }

    public static class Common {
        public final ForgeConfigSpec.BooleanValue enableHoloInventory;
        public final ForgeConfigSpec.DoubleValue renderRange;
        public final ForgeConfigSpec.DoubleValue renderScale;
        public final ForgeConfigSpec.IntValue maxItems;
        public final ForgeConfigSpec.DoubleValue minHideDistance;
        public final ForgeConfigSpec.BooleanValue showItemCount;
        public final ForgeConfigSpec.BooleanValue showName;
        public final ForgeConfigSpec.BooleanValue mode3D;
        public final ForgeConfigSpec.IntValue itemsPerRow;

        public Common(ForgeConfigSpec.Builder builder) {
            builder.push("General");
            enableHoloInventory = builder.comment("默认显示").define("enableHoloInventory", true);
            renderRange = builder.comment("最大渲染距离").defineInRange("renderRange", 8.0, 1.0, 32.0);
            renderScale = builder.comment("显示缩放").defineInRange("renderScale", 1.0, 0.1, 5.0);
            maxItems = builder.comment("最大显示物品数").defineInRange("maxItems", 1024, 1, 10000);
            minHideDistance = builder.comment("最小隐藏距离").defineInRange("minHideDistance", 1.5, 0.0, 10.0);
            showItemCount = builder.comment("显示物品数量").define("showItemCount", true);
            showName = builder.comment("显示容器名称").define("showName", true);
            mode3D = builder.comment("3D 渲染模式").define("mode3D", true);
            itemsPerRow = builder.comment("每行显示的物品数量").defineInRange("itemsPerRow", 9, 1, 18);
            builder.pop();
        }
    }

    public enum RenderMode {
        MODE_2D,
        MODE_3D
    }
}
