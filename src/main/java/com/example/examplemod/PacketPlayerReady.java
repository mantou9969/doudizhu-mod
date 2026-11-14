package com.example.examplemod;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class PacketPlayerReady {
    public PacketPlayerReady() {}
    public PacketPlayerReady(FriendlyByteBuf buf) {}

    public void encode(FriendlyByteBuf buf) {}

    // ★★★ 补上了这个方法 ★★★
    public static PacketPlayerReady decode(FriendlyByteBuf buf) {
        return new PacketPlayerReady(buf);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            net.minecraft.server.level.ServerPlayer player = context.get().getSender();
            if (player != null) {
                GameLogic.handlePlayerReady(player);
            }
        });
        context.get().setPacketHandled(true);
    }
}