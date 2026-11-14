package com.example.examplemod;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class PacketPass {
    public PacketPass() {}
    public PacketPass(FriendlyByteBuf buf) {}

    public void encode(FriendlyByteBuf buf) {}

    // ★★★ 补上了这个方法 ★★★
    public static PacketPass decode(FriendlyByteBuf buf) {
        return new PacketPass(buf);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            net.minecraft.server.level.ServerPlayer player = context.get().getSender();
            if (player != null) {
                GameLogic.handlePass(player);
            }
        });
        context.get().setPacketHandled(true);
    }
}