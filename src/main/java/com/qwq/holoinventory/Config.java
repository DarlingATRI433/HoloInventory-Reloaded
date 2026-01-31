package com.qwq.holoinventory;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue DEFAULT_DISPLAY = BUILDER
            .comment("当此项开启时，物品图标列表始终可见；关闭时，仅在装备特定头部物品时显示")
            .define("defaultDisplay", true);

    public static final ModConfigSpec.DoubleValue RENDER_DISTANCE = BUILDER
            .comment("全息物品显示的渲染距离")
            .defineInRange("renderDistance", 8.0, 1.0, 32.0);

    public static final ModConfigSpec.DoubleValue SCALE = BUILDER
            .comment("全息图整体缩放比例")
            .defineInRange("scale", 1.0, 0.1, 5.0);

    public static final ModConfigSpec.DoubleValue ITEM_SCALE = BUILDER
            .comment("物品图标的缩放比例 (相对于整体)")
            .defineInRange("itemScale", 1.0, 0.1, 5.0);

    public static final ModConfigSpec.DoubleValue ITEM_SPACING = BUILDER
            .comment("物品之间的固定间距")
            .defineInRange("itemSpacing", 0.45, 0.0, 2.0);

    public static final ModConfigSpec.IntValue MAX_ITEMS = BUILDER
            .comment("最大显示的物品数量")
            .defineInRange("maxItems", 27, 1, 81);

    public static final ModConfigSpec.DoubleValue MIN_RENDER_DISTANCE = BUILDER
            .comment("玩家距离方块过近时不显示全息图的最小距离")
            .defineInRange("minRenderDistance", 1.5, 0.0, 5.0);

    public static final ModConfigSpec.BooleanValue SHOW_ITEM_COUNT = BUILDER
            .comment("是否显示物品数量")
            .define("showItemCount", true);

    public static final ModConfigSpec.BooleanValue SHOW_CONTAINER_NAME = BUILDER
            .comment("是否显示容器名称")
            .define("showContainerName", true);

    public static final ModConfigSpec.BooleanValue RENDER_3D = BUILDER
            .comment("是否以 3D 模式显示物品 (关闭则为 2D 图标模式)")
            .define("render3D", true);

    public static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean defaultDisplay = true;
    public static boolean showItemCount = true;
    public static boolean showContainerName = true;
    public static boolean render3D = true;
    public static double renderDistance = 8.0;
    public static double scale = 1.0;
    public static double itemScale = 1.0;
    public static double itemSpacing = 0.45;
    public static int maxItems = 27;
    public static double minRenderDistance = 1.5;

    public static void load() {
        defaultDisplay = DEFAULT_DISPLAY.get();
        renderDistance = RENDER_DISTANCE.get();
        scale = SCALE.get();
        itemScale = ITEM_SCALE.get();
        itemSpacing = ITEM_SPACING.get();
        maxItems = MAX_ITEMS.get();
        minRenderDistance = MIN_RENDER_DISTANCE.get();
        showItemCount = SHOW_ITEM_COUNT.get();
        showContainerName = SHOW_CONTAINER_NAME.get();
        render3D = RENDER_3D.get();
    }
}
