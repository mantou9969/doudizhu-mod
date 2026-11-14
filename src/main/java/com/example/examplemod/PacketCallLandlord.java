package com.example.examplemod;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class PacketCallLandlord {
    private final boolean want;

    public PacketCallLandlord(boolean want) {
        this.want = want;
    }

    public PacketCallLandlord(FriendlyByteBuf buf) {
        this.want = buf.readBoolean();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(want);
    }

    // ★★★ 补上了这个方法 ★★★
    public static PacketCallLandlord decode(FriendlyByteBuf buf) {
        return new PacketCallLandlord(buf);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            net.minecraft.server.level.ServerPlayer player = context.get().getSender();
            if (player != null) {
                GameLogic.handlePlayerGrabLandlord(player, want);
            }
        });
        context.get().setPacketHandled(true);
    }
}