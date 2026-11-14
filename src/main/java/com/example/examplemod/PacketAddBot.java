package com.example.examplemod;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class PacketAddBot {
    public PacketAddBot() {}
    public PacketAddBot(FriendlyByteBuf buf) {}

    public void encode(FriendlyByteBuf buf) {}

    public static PacketAddBot decode(FriendlyByteBuf buf) {
        return new PacketAddBot(buf);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            net.minecraft.server.level.ServerPlayer player = context.get().getSender();
            if (player != null) {
                GameLogic.addBot(player);
            }
        });
        context.get().setPacketHandled(true);
    }
}