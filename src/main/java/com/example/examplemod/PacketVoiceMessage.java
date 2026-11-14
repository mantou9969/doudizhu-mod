package com.example.examplemod;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class PacketVoiceMessage {
    private final String soundName;

    public PacketVoiceMessage(String soundName) {
        this.soundName = soundName;
    }

    public PacketVoiceMessage(FriendlyByteBuf buf) {
        this.soundName = buf.readUtf();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(soundName);
    }

    // ★★★ 补上了这个方法 ★★★
    public static PacketVoiceMessage decode(FriendlyByteBuf buf) {
        return new PacketVoiceMessage(buf);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            net.minecraft.server.level.ServerPlayer sender = context.get().getSender();
            if (sender != null) {
                // 收到消息后，在所有玩家那里播放声音
                CardSoundHelper.playSoundByName(sender, soundName);
            }
        });
        context.get().setPacketHandled(true);
    }
}