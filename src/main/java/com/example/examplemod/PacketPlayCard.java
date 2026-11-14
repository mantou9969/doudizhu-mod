package com.example.examplemod;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PacketPlayCard {
    private final List<Integer> indices;

    public PacketPlayCard(List<Integer> indices) {
        this.indices = indices;
    }

    public PacketPlayCard(FriendlyByteBuf buf) {
        // 读取整数列表
        this.indices = buf.readCollection(ArrayList::new, FriendlyByteBuf::readInt);
    }

    public void encode(FriendlyByteBuf buf) {
        // 写入整数列表
        buf.writeCollection(indices, FriendlyByteBuf::writeInt);
    }

    // ★★★ 补上了这个方法 ★★★
    public static PacketPlayCard decode(FriendlyByteBuf buf) {
        return new PacketPlayCard(buf);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            net.minecraft.server.level.ServerPlayer player = context.get().getSender();
            if (player != null) {
                GameLogic.handlePlayCard(player, indices);
            }
        });
        context.get().setPacketHandled(true);
    }
}