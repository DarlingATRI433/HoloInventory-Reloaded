package com.qwq.holoinventory.network;

import com.qwq.holoinventory.HoloInventoryReloaded;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record InventoryResponsePayload(BlockPos pos, List<ItemStack> items, String name, int hash, boolean needsUpdate) implements CustomPacketPayload {
    public static final Type<InventoryResponsePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(HoloInventoryReloaded.MODID, "inventory_response"));

    public static final StreamCodec<RegistryFriendlyByteBuf, InventoryResponsePayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, InventoryResponsePayload::pos,
            ItemStack.OPTIONAL_LIST_STREAM_CODEC, InventoryResponsePayload::items,
            ByteBufCodecs.STRING_UTF8, InventoryResponsePayload::name,
            ByteBufCodecs.VAR_INT, InventoryResponsePayload::hash,
            ByteBufCodecs.BOOL, InventoryResponsePayload::needsUpdate,
            InventoryResponsePayload::new
    );

    // 全量更新构造函数
    public InventoryResponsePayload(BlockPos pos, List<ItemStack> items, String name, int hash) {
        this(pos, items, name, hash, true);
    }

    // 保持现状构造函数 (无需更新)
    public static InventoryResponsePayload noChange(BlockPos pos, int hash) {
        return new InventoryResponsePayload(pos, List.of(), "", hash, false);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
