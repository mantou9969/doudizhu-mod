package com.example.examplemod;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PacketSyncTable {
    private boolean isFullSync;

    public int gameState;
    public String landlordName = "";
    public String currentTurnName = "";
    public boolean canPass = false;
    public String winnerName = "";

    public List<Integer> handCounts = new ArrayList<>();
    public int lastPlayerIdx = -1;

    public List<String> playerNames = new ArrayList<>();
    public List<Boolean> readyStates = new ArrayList<>();
    public String ownerName = "";

    public List<ItemStack> bottomCards;
    public List<ItemStack> simplePlayedCards;

    // 全量构造
    public PacketSyncTable(int state, String lName, String turnName, boolean pass,
                           List<ItemStack> played, int lastIdx, List<ItemStack> bottom,
                           List<String> pNames, List<Boolean> pReadies, List<Integer> hCounts,
                           String owner, String winner) {
        this.isFullSync = true;
        this.gameState = state;
        this.landlordName = lName;
        this.currentTurnName = turnName;
        this.canPass = pass;
        this.simplePlayedCards = played;
        this.lastPlayerIdx = lastIdx;
        this.bottomCards = bottom;
        this.playerNames = pNames;
        this.readyStates = pReadies;
        this.handCounts = hCounts;
        this.ownerName = owner;
        this.winnerName = winner;
    }

    // 简单构造
    public PacketSyncTable(List<ItemStack> playedCards, int lastIdx, List<ItemStack> bottomCards) {
        this.isFullSync = false;
        this.simplePlayedCards = playedCards;
        this.lastPlayerIdx = lastIdx;
        this.bottomCards = bottomCards;
    }

    // 兼容构造
    public PacketSyncTable(List<ItemStack> playedCards, List<ItemStack> bottomCards) {
        this(playedCards, -1, bottomCards);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(isFullSync);
        if (isFullSync) {
            buf.writeInt(gameState);
            buf.writeUtf(landlordName);
            buf.writeUtf(currentTurnName);
            buf.writeBoolean(canPass);
            buf.writeUtf(winnerName);

            buf.writeCollection(simplePlayedCards, FriendlyByteBuf::writeItem);
            buf.writeInt(lastPlayerIdx);

            buf.writeCollection(bottomCards, FriendlyByteBuf::writeItem);
            buf.writeCollection(playerNames, FriendlyByteBuf::writeUtf);
            buf.writeCollection(readyStates, FriendlyByteBuf::writeBoolean);
            buf.writeCollection(handCounts, FriendlyByteBuf::writeInt);
            buf.writeUtf(ownerName);
        } else {
            buf.writeCollection(simplePlayedCards, FriendlyByteBuf::writeItem);
            buf.writeInt(lastPlayerIdx);
            buf.writeCollection(bottomCards, FriendlyByteBuf::writeItem);
        }
    }

    public static PacketSyncTable decode(FriendlyByteBuf buf) {
        boolean mode = buf.readBoolean();
        if (mode) {
            int state = buf.readInt();
            String lName = buf.readUtf();
            String turnName = buf.readUtf();
            boolean pass = buf.readBoolean();
            String winner = buf.readUtf();

            List<ItemStack> played = buf.readCollection(ArrayList::new, FriendlyByteBuf::readItem);
            int lastIdx = buf.readInt();

            List<ItemStack> bottom = buf.readCollection(ArrayList::new, FriendlyByteBuf::readItem);
            List<String> pNames = buf.readCollection(ArrayList::new, FriendlyByteBuf::readUtf);
            List<Boolean> pReadies = buf.readCollection(ArrayList::new, FriendlyByteBuf::readBoolean);
            List<Integer> hCounts = buf.readCollection(ArrayList::new, FriendlyByteBuf::readInt);
            String owner = buf.readUtf();

            return new PacketSyncTable(state, lName, turnName, pass, played, lastIdx, bottom, pNames, pReadies, hCounts, owner, winner);
        } else {
            List<ItemStack> played = buf.readCollection(ArrayList::new, FriendlyByteBuf::readItem);
            int lastIdx = buf.readInt();
            List<ItemStack> bottom = buf.readCollection(ArrayList::new, FriendlyByteBuf::readItem);
            return new PacketSyncTable(played, lastIdx, bottom);
        }
    }

    // ★★★ 核心修复：服务器看到这里会直接跳过，不会加载 ClientHandler ★★★
    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHandler.handle(this));
        });
        context.get().setPacketHandled(true);
    }

    // 内部类：专门放客户端代码，服务器永远不会加载这个类
    private static class ClientHandler {
        public static void handle(PacketSyncTable msg) {
            // 这里可以安全地使用 Minecraft 类，因为只有客户端会进到这里
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            Player player = mc.player;
            if (player != null && player.containerMenu instanceof CardTableMenu menu) {
                menu.updateData(
                        msg.isFullSync, msg.gameState,
                        msg.landlordName, msg.currentTurnName, msg.canPass,
                        msg.bottomCards, msg.playerNames, msg.readyStates, msg.handCounts, msg.ownerName,
                        msg.simplePlayedCards, msg.lastPlayerIdx, msg.winnerName
                );
            }
        }
    }
}