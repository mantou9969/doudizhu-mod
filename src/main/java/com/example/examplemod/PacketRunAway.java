package com.example.examplemod;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class PacketRunAway {
    public PacketRunAway() {}
    public PacketRunAway(FriendlyByteBuf buf) {}

    public void encode(FriendlyByteBuf buf) {}

    public static PacketRunAway decode(FriendlyByteBuf buf) {
        return new PacketRunAway(buf);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            net.minecraft.server.level.ServerPlayer player = context.get().getSender();
            if (player != null) {
                GameLogic.handlePlayerQuit(player);
            }
        });
        context.get().setPacketHandled(true);
    }
}