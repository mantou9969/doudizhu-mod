package com.example.examplemod;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ExampleMod.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;

        // 注册所有现存的网络包
        INSTANCE.registerMessage(id++, PacketPlayerReady.class, PacketPlayerReady::encode, PacketPlayerReady::decode, PacketPlayerReady::handle);
        INSTANCE.registerMessage(id++, PacketCallLandlord.class, PacketCallLandlord::encode, PacketCallLandlord::decode, PacketCallLandlord::handle);
        INSTANCE.registerMessage(id++, PacketPlayCard.class, PacketPlayCard::encode, PacketPlayCard::decode, PacketPlayCard::handle);
        INSTANCE.registerMessage(id++, PacketPass.class, PacketPass::encode, PacketPass::decode, PacketPass::handle);
        INSTANCE.registerMessage(id++, PacketSyncTable.class, PacketSyncTable::encode, PacketSyncTable::decode, PacketSyncTable::handle);

        // 这两个包通常是空的或者简单包，使用 ::new 或 ::decode
        INSTANCE.registerMessage(id++, PacketAddBot.class, PacketAddBot::encode, PacketAddBot::decode, PacketAddBot::handle);
        INSTANCE.registerMessage(id++, PacketRunAway.class, PacketRunAway::encode, PacketRunAway::decode, PacketRunAway::handle);

        INSTANCE.registerMessage(id++, PacketVoiceMessage.class, PacketVoiceMessage::encode, PacketVoiceMessage::decode, PacketVoiceMessage::handle);

        // 已移除: PacketGameStart, PacketSortCard
    }
}