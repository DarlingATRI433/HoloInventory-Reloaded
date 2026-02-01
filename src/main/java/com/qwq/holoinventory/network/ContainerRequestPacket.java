package com.qwq.holoinventory.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.network.NetworkEvent;
import net.minecraft.world.Container;

import java.util.function.Supplier;

public class ContainerRequestPacket {
    private final BlockPos pos;

    public ContainerRequestPacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(ContainerRequestPacket pkt, FriendlyByteBuf buf) {
        buf.writeBlockPos(pkt.pos);
    }

    public static ContainerRequestPacket decode(FriendlyByteBuf buf) {
        return new ContainerRequestPacket(buf.readBlockPos());
    }

    public static void handle(ContainerRequestPacket pkt, Supplier<NetworkEvent.Context> ctxGetter) {
        NetworkEvent.Context ctx = ctxGetter.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null && player.level().isLoaded(pkt.pos)) {
                BlockEntity be = player.level().getBlockEntity(pkt.pos);
                if (be != null) {
                    be.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
                        PacketHandler.INSTANCE.reply(new ContainerResponsePacket(pkt.pos, handler), ctx);
                    });
                    
                    // 如果没有 Capability，尝试 Container
                    if (!be.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent() && be instanceof Container container) {
                        PacketHandler.INSTANCE.reply(new ContainerResponsePacket(pkt.pos, container), ctx);
                    }
                }
            }
        });
        ctx.setPacketHandled(true);
    }
}
