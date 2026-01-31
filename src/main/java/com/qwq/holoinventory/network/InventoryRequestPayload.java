package com.qwq.holoinventory.network;

import com.qwq.holoinventory.HoloInventoryReloaded;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record InventoryRequestPayload(BlockPos pos) implements CustomPacketPayload {
    public static final Type<InventoryRequestPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(HoloInventoryReloaded.MODID, "inventory_request"));

    public static final StreamCodec<FriendlyByteBuf, InventoryRequestPayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, InventoryRequestPayload::pos,
            InventoryRequestPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
