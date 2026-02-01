package com.qwq.holoinventory.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.network.NetworkEvent;
import net.minecraft.world.Container;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ContainerResponsePacket {
    private final BlockPos pos;
    private final List<ItemStack> stacks;

    public ContainerResponsePacket(BlockPos pos, List<ItemStack> stacks) {
        this.pos = pos;
        this.stacks = stacks;
    }

    public ContainerResponsePacket(BlockPos pos, IItemHandler handler) {
        this.pos = pos;
        this.stacks = new ArrayList<>();
        for (int i = 0; i < handler.getSlots(); i++) {
            stacks.add(handler.getStackInSlot(i).copy());
        }
    }

    public ContainerResponsePacket(BlockPos pos, Container container) {
        this.pos = pos;
        this.stacks = new ArrayList<>();
        for (int i = 0; i < container.getContainerSize(); i++) {
            stacks.add(container.getItem(i).copy());
        }
    }

    public static void encode(ContainerResponsePacket pkt, FriendlyByteBuf buf) {
        buf.writeBlockPos(pkt.pos);
        buf.writeInt(pkt.stacks.size());
        for (ItemStack stack : pkt.stacks) {
            buf.writeItem(stack);
        }
    }

    public static ContainerResponsePacket decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        int size = buf.readInt();
        List<ItemStack> stacks = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            stacks.add(buf.readItem());
        }
        return new ContainerResponsePacket(pos, stacks);
    }

    public static void handle(ContainerResponsePacket pkt, Supplier<NetworkEvent.Context> ctxGetter) {
        NetworkEvent.Context ctx = ctxGetter.get();
        ctx.enqueueWork(() -> {
            ClientInventoryCache.update(pkt.pos, pkt.stacks);
        });
        ctx.setPacketHandled(true);
    }
}
