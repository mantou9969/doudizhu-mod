package com.example.examplemod;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.network.PacketDistributor;

import java.util.*;

public class GameLogic {
    // =================================================================
    // 1. 全局数据结构
    // =================================================================
    public static List<ServerPlayer> tablePlayers = new ArrayList<>();
    public static Map<String, Boolean> readyMap = new HashMap<>();

    private static final Map<UUID, List<ItemStack>> inventoryBackups = new HashMap<>();
    private static final Map<UUID, GameType> gameModeBackups = new HashMap<>();

    public static List<ItemStack> currentBottomCards = new ArrayList<>();
    public static List<ItemStack> currentPlayedCards = new ArrayList<>();

    // 状态机: 0=大厅, 1=抢地主, 2=打牌, 3=结算(惩罚阶段)
    public static int gameState = 0;
    public static boolean isGameStarted = false;

    public static ServerPlayer currentLandlord = null;
    public static int turnIndex = 0;
    public static int lastWinnerIndex = -1;
    public static PokerRules.HandResult lastHandType = null;
    public static String winnerName = "";

    // 记录谁赢了
    public static boolean lastLandlordWon = false;

    public static int timeOutCounter = 0;
    public static int botThinkTime = 0;

    public static int punishmentTimer = 0;

    // =================================================================
    // 2. 进房、准备
    // =================================================================

    public static void joinTable(ServerPlayer player) {
        tablePlayers.removeIf(p -> p.hasDisconnected() && !(p instanceof FakePlayer));

        boolean hasReal = false;
        for(ServerPlayer p : tablePlayers) if(!(p instanceof FakePlayer)) hasReal = true;
        if(!tablePlayers.isEmpty() && !hasReal) resetGame();

        if (!tablePlayers.contains(player)) {
            if (tablePlayers.size() >= 3) {
                player.sendSystemMessage(Component.literal("§c座位已满，无法加入！"));
                return;
            }
            if (gameState != 0 && gameState != 3) {
                player.sendSystemMessage(Component.literal("§c游戏正在进行中！"));
                return;
            }

            tablePlayers.add(player);
            readyMap.put(player.getStringUUID(), false);
            syncTableToAll();
        } else {
            syncTableToAll();
        }
    }

    public static void handlePlayerQuit(ServerPlayer leaver) {
        if(tablePlayers.contains(leaver)) {
            // ★★★ 核心修复：惩罚阶段禁止退出！ ★★★
            // 这样你关闭界面后，服务端不会重置游戏，Buff 会继续保留，直到倒计时结束
            if (gameState == 3) {
                leaver.sendSystemMessage(Component.literal("§c[系统] 惩罚尚未结束，无法退出牌局！"));
                return;
            }

            broadcast("§c玩家 " + leaver.getName().getString() + " 离开了。");
            if (gameState > 0 || inventoryBackups.containsKey(leaver.getUUID())) {
                restoreInventory(leaver);
            } else {
                leaver.inventoryMenu.broadcastChanges();
                leaver.containerMenu.broadcastChanges();
                leaver.closeContainer();
            }
            resetGame();
            syncTableToAll();
        }
    }

    public static void handlePlayerReady(ServerPlayer player) {
        if (gameState == 3) {
            if (punishmentTimer > 0) {
                player.sendSystemMessage(Component.literal("§c惩罚时间！请反省 " + (punishmentTimer / 20) + " 秒！"));
                return;
            }
            resetGame();
            syncTableToAll();
            return;
        }
        if (gameState != 0) return;

        readyMap.put(player.getStringUUID(), !readyMap.getOrDefault(player.getStringUUID(), false));

        if (tablePlayers.size() == 3) {
            boolean allReady = true;
            for (ServerPlayer p : tablePlayers) {
                if (!readyMap.getOrDefault(p.getStringUUID(), false)) {
                    allReady = false;
                    break;
                }
            }
            if (allReady) startNewGame();
        }
        syncTableToAll();
    }

    public static void addBot(ServerPlayer req) {
        if(tablePlayers.size() >= 3) return;
        FakePlayer bot = FakePlayerFactory.get(req.serverLevel(), new GameProfile(UUID.randomUUID(), "Bot_" + (tablePlayers.size()+1)));
        tablePlayers.add(bot);
        readyMap.put(bot.getStringUUID(), true);
        checkAutoStart();
        syncTableToAll();
    }

    private static void checkAutoStart() {
        if (tablePlayers.size() == 3) {
            boolean allReady = true;
            for (ServerPlayer p : tablePlayers) {
                if (!readyMap.getOrDefault(p.getStringUUID(), false)) {
                    allReady = false;
                    break;
                }
            }
            if (allReady) startNewGame();
        }
    }

    // =================================================================
    // 3. 核心游戏流程
    // =================================================================

    public static void startNewGame() {
        if (tablePlayers.size() != 3) return;
        if (isGameStarted || gameState != 0) return;

        for (ServerPlayer p : tablePlayers) backupInventory(p);

        gameState = 1; isGameStarted = true; winnerName = ""; currentLandlord = null;
        currentPlayedCards.clear(); currentBottomCards.clear();
        lastWinnerIndex = -1; lastHandType = null;
        timeOutCounter = 0;

        List<ItemStack> deck = new ArrayList<>();
        addAllCardsToDeck(deck);
        Collections.shuffle(deck);

        for (int i = 0; i < 3; i++) currentBottomCards.add(deck.remove(0));

        int pIdx = 0;
        int[] counts = new int[3];
        while(!deck.isEmpty()) {
            ServerPlayer p = tablePlayers.get(pIdx % 3);
            int slot = 9 + counts[pIdx % 3];
            if(slot < 36) {
                p.getInventory().setItem(slot, deck.remove(0));
                counts[pIdx % 3]++;
            } else deck.remove(0);
            pIdx++;
        }

        for(ServerPlayer p : tablePlayers) sortPlayerHand(p);

        turnIndex = 0;
        broadcast("§e游戏开始！请房主叫地主。");
        syncTableToAll();
    }

    public static void handlePlayerGrabLandlord(ServerPlayer player, boolean want) {
        if (gameState != 1) return;
        if (tablePlayers.indexOf(player) != turnIndex) return;

        if (want) {
            currentLandlord = player;
            try { CardSoundHelper.playOrderSound(player, true); } catch (Exception e) {}
            broadcast("§6" + player.getName().getString() + " 成为地主！");

            int cardIdx = 0;
            for (int i = 9; i < 36; i++) {
                if (player.getInventory().getItem(i).isEmpty() && cardIdx < currentBottomCards.size()) {
                    player.getInventory().setItem(i, currentBottomCards.get(cardIdx).copy());
                    cardIdx++;
                }
            }

            sortPlayerHand(player);
            gameState = 2; lastWinnerIndex = turnIndex; timeOutCounter = 0;
        } else {
            try { CardSoundHelper.playOrderSound(player, false); } catch (Exception e) {}
            broadcast("§7" + player.getName().getString() + " 不叫。");
            turnIndex = (turnIndex + 1) % 3;
            timeOutCounter = 0;

            if(turnIndex == 0 && currentLandlord == null) {
                broadcast("§c无人叫牌，强制房主坐庄！");
                currentLandlord = tablePlayers.get(0);
                int cardIdx = 0;
                for (int i = 9; i < 36; i++) {
                    if (currentLandlord.getInventory().getItem(i).isEmpty() && cardIdx < currentBottomCards.size()) {
                        currentLandlord.getInventory().setItem(i, currentBottomCards.get(cardIdx).copy());
                        cardIdx++;
                    }
                }
                sortPlayerHand(currentLandlord);
                gameState = 2; lastWinnerIndex = 0; turnIndex = 0; timeOutCounter = 0;
            }
        }
        syncTableToAll();
    }

    public static void handlePlayCard(ServerPlayer player, List<Integer> indices) {
        if (gameState != 2) return;
        int pIdx = tablePlayers.indexOf(player);
        if (pIdx != turnIndex) return;

        List<ItemStack> selected = new ArrayList<>();
        for(int i : indices) selected.add(player.getInventory().getItem(i));

        PokerRules.HandResult res = PokerRules.evaluate(selected);
        if(res.type == PokerRules.HandType.UNKNOWN) {
            if(!(player instanceof FakePlayer)) player.sendSystemMessage(Component.literal("§c无效牌型！"));
            return;
        }
        if(lastWinnerIndex != pIdx && lastHandType != null) {
            if(!PokerRules.canBeat(lastHandType, res)) {
                if(!(player instanceof FakePlayer)) player.sendSystemMessage(Component.literal("§c管不上！"));
                return;
            }
        }

        List<ItemStack> played = new ArrayList<>();
        for(int i : indices) {
            played.add(player.getInventory().getItem(i).copy());
            player.getInventory().setItem(i, ItemStack.EMPTY);
        }
        currentPlayedCards = played;
        lastHandType = res;
        lastWinnerIndex = pIdx;

        try { CardSoundHelper.playCardSound(player, played); } catch (Exception e) {}

        int remaining = 0;
        for(int i=9; i<36; i++) {
            if(!player.getInventory().getItem(i).isEmpty()) remaining++;
        }
        try { CardSoundHelper.playRemainingSound(player, remaining); } catch (Exception e) {}

        if(remaining == 0) {
            gameState = 3;
            lastLandlordWon = (currentLandlord != null && player.getUUID().equals(currentLandlord.getUUID()));
            winnerName = lastLandlordWon ? "地主" : "农民";

            int punishSeconds = lastLandlordWon ? 7 : 30;
            punishmentTimer = punishSeconds * 20;

            broadcast("§6§l" + winnerName + " (" + player.getName().getString() + ") 获胜！进入" + punishSeconds + "秒惩罚时间！");

            for(ServerPlayer p : tablePlayers) {
                p.level().playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.0f, 1.0f);
            }

            readyMap.replaceAll((k,v)->false);

            applyPunishment();

        } else {
            turnIndex = (turnIndex + 1) % 3;
            timeOutCounter = 0;
        }

        sortPlayerHand(player);
        syncTableToAll();
    }

    public static void handlePass(ServerPlayer player) {
        if (gameState != 2) return;
        int pIdx = tablePlayers.indexOf(player);
        if (pIdx != turnIndex || pIdx == lastWinnerIndex) return;

        try { CardSoundHelper.playPassSound(player); } catch (Exception e) {}

        broadcast("§7" + player.getName().getString() + " 不要");
        turnIndex = (turnIndex + 1) % 3;
        timeOutCounter = 0;

        if(turnIndex == lastWinnerIndex) {
            currentPlayedCards.clear();
            lastHandType = null;
            broadcast("§e无人接牌，请出牌。");
        }
        syncTableToAll();
    }

    private static void applyPunishment() {
        for (ServerPlayer p : tablePlayers) {
            boolean isPlayerLandlord = (currentLandlord != null && p.getUUID().equals(currentLandlord.getUUID()));

            ServerPlayer target = p;
            if (!(p instanceof FakePlayer)) {
                ServerPlayer freshPlayer = p.server.getPlayerList().getPlayer(p.getUUID());
                if (freshPlayer != null) {
                    target = freshPlayer;
                }
            }

            if (lastLandlordWon) {
                // 地主赢了 -> 农民受苦
                if (!isPlayerLandlord && !(target instanceof FakePlayer)) {
                    if (target.gameMode.getGameModeForPlayer() == GameType.CREATIVE) {
                        target.setGameMode(GameType.SURVIVAL);
                        target.sendSystemMessage(Component.literal("§c[系统] 强制切换为生存模式！"));
                    }

                    target.removeAllEffects();
                    target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 8 * 20, 1));
                    target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 8 * 20, 0));
                    target.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 5 * 20, 0));
                    target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 5 * 20, 0));
                }
            } else {
                // 农民赢了 -> 地主戴高帽
                if (isPlayerLandlord) {
                    ItemStack shameHat = new ItemStack(ExampleMod.SHAME_HAT.get());
                    shameHat.setHoverName(Component.literal("§f§l只是运气不好"));
                    target.setItemSlot(EquipmentSlot.HEAD, shameHat);
                }
            }
        }
    }

    public static void onTick() {
        if (gameState == 0) return;

        if (gameState == 3) {
            if (punishmentTimer > 0) {
                punishmentTimer--;

                if (punishmentTimer % 20 == 0) {
                    applyPunishment();
                    broadcast("§e惩罚剩余: " + (punishmentTimer / 20) + "s...");
                }

            } else {
                broadcast("§a惩罚结束，装备已归还。");
                resetGame();
                syncTableToAll();
            }
            return;
        }

        if (!tablePlayers.isEmpty()) {
            ServerPlayer currentP = tablePlayers.get(turnIndex % tablePlayers.size());
            if (currentP instanceof FakePlayer) {
                botThinkTime++;
                if (botThinkTime > 30) { runBotAI(currentP); botThinkTime = 0; }
            }
        }
        timeOutCounter++;
        if (timeOutCounter > 600) { handleTimeOut(); timeOutCounter = 0; }
    }

    private static void backupInventory(ServerPlayer player) {
        if (player instanceof FakePlayer) return;
        if (inventoryBackups.containsKey(player.getUUID())) return;

        gameModeBackups.put(player.getUUID(), player.gameMode.getGameModeForPlayer());

        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            items.add(player.getInventory().getItem(i).copy());
            player.getInventory().setItem(i, ItemStack.EMPTY);
        }
        inventoryBackups.put(player.getUUID(), items);
    }

    private static void restoreInventory(ServerPlayer p) {
        if (p instanceof FakePlayer) return;

        ServerPlayer player = p;
        ServerPlayer fresh = p.server.getPlayerList().getPlayer(p.getUUID());
        if (fresh != null) player = fresh;

        player.removeAllEffects();

        if (gameModeBackups.containsKey(player.getUUID())) {
            player.setGameMode(gameModeBackups.get(player.getUUID()));
            gameModeBackups.remove(player.getUUID());
        }

        if (inventoryBackups.containsKey(player.getUUID())) {
            List<ItemStack> original = inventoryBackups.get(player.getUUID());
            for (int i = 0; i < original.size(); i++) {
                if (i < player.getInventory().getContainerSize()) {
                    player.getInventory().setItem(i, original.get(i));
                }
            }
            inventoryBackups.remove(player.getUUID());

            player.inventoryMenu.broadcastChanges();
            player.containerMenu.broadcastChanges();
            player.closeContainer();
            player.sendSystemMessage(Component.literal("§a装备已归还。"));
        }
    }

    private static void runBotAI(ServerPlayer bot) {
        if (gameState == 1) handlePlayerGrabLandlord(bot, true);
        else if (gameState == 2) {
            if (turnIndex == lastWinnerIndex) {
                for (int i=9; i<36; i++) {
                    if (!bot.getInventory().getItem(i).isEmpty()) {
                        List<Integer> l = new ArrayList<>(); l.add(i);
                        handlePlayCard(bot, l); return;
                    }
                }
            } else handlePass(bot);
        }
    }

    private static void handleTimeOut() {
        if(tablePlayers.isEmpty()) return;
        ServerPlayer p = tablePlayers.get(turnIndex % tablePlayers.size());
        if(p instanceof FakePlayer) return;

        broadcast("§c玩家 " + p.getName().getString() + " 超时托管！");
        if(gameState == 1) handlePlayerGrabLandlord(p, false);
        else if (gameState == 2) {
            if(turnIndex == lastWinnerIndex) {
                for (int i=9; i<36; i++) {
                    if (!p.getInventory().getItem(i).isEmpty()) {
                        List<Integer> l = new ArrayList<>(); l.add(i);
                        handlePlayCard(p, l); return;
                    }
                }
            } else handlePass(p);
        }
    }

    private static void resetGame() {
        for (ServerPlayer p : tablePlayers) {
            if (inventoryBackups.containsKey(p.getUUID())) restoreInventory(p);
        }
        gameState = 0; isGameStarted = false; tablePlayers.clear(); readyMap.clear();
        winnerName = ""; currentLandlord = null; currentPlayedCards.clear(); currentBottomCards.clear();
        punishmentTimer = 0;
    }

    public static void syncTableToAll() {
        if(tablePlayers.isEmpty()) return;

        String lName = (currentLandlord==null) ? "" : currentLandlord.getName().getString();
        String tName = "";
        if(gameState != 0 && gameState != 3 && !tablePlayers.isEmpty()) {
            tName = tablePlayers.get(turnIndex % tablePlayers.size()).getName().getString();
        }

        List<String> names = new ArrayList<>();
        List<Boolean> readies = new ArrayList<>();
        List<Integer> handCounts = new ArrayList<>();
        String owner = tablePlayers.get(0).getName().getString();

        for(ServerPlayer p : tablePlayers) {
            names.add(p.getName().getString());
            readies.add(readyMap.getOrDefault(p.getStringUUID(), false));
            int count = 0;
            for(int i=9; i<36; i++) if(!p.getInventory().getItem(i).isEmpty()) count++;
            handCounts.add(count);
        }

        PacketSyncTable packet = new PacketSyncTable(
                gameState, lName, tName, turnIndex != lastWinnerIndex,
                currentPlayedCards, lastWinnerIndex, currentBottomCards,
                names, readies, handCounts, owner, winnerName
        );

        for(ServerPlayer p : tablePlayers) {
            if(!(p instanceof FakePlayer)) PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(()->p), packet);
        }
    }

    public static void sortPlayerHand(ServerPlayer p) {
        if(p instanceof FakePlayer) return;
        List<ItemStack> list = new ArrayList<>();
        for(int i=9; i<36; i++) {
            ItemStack s = p.getInventory().getItem(i);
            if(!s.isEmpty()) { list.add(s); p.getInventory().setItem(i, ItemStack.EMPTY); }
        }
        list.sort((a,b) -> Integer.compare(PokerRules.getValue(b), PokerRules.getValue(a)));
        for(int i=0; i<list.size(); i++) p.getInventory().setItem(9+i, list.get(i));
        p.inventoryMenu.broadcastChanges();
    }

    private static boolean isHandEmpty(ServerPlayer p) {
        for(int i=9; i<36; i++) if(!p.getInventory().getItem(i).isEmpty()) return false;
        return true;
    }

    private static void broadcast(String m) {
        for(ServerPlayer p : tablePlayers) if(!(p instanceof FakePlayer)) p.sendSystemMessage(Component.literal(m));
    }

    private static void addAllCardsToDeck(List<ItemStack> deck) {
        deck.add(new ItemStack(ExampleMod.CARD_SPADE_3.get())); deck.add(new ItemStack(ExampleMod.CARD_SPADE_4.get())); deck.add(new ItemStack(ExampleMod.CARD_SPADE_5.get())); deck.add(new ItemStack(ExampleMod.CARD_SPADE_6.get())); deck.add(new ItemStack(ExampleMod.CARD_SPADE_7.get())); deck.add(new ItemStack(ExampleMod.CARD_SPADE_8.get())); deck.add(new ItemStack(ExampleMod.CARD_SPADE_9.get())); deck.add(new ItemStack(ExampleMod.CARD_SPADE_10.get())); deck.add(new ItemStack(ExampleMod.CARD_SPADE_JACK.get())); deck.add(new ItemStack(ExampleMod.CARD_SPADE_QUEEN.get())); deck.add(new ItemStack(ExampleMod.CARD_SPADE_KING.get())); deck.add(new ItemStack(ExampleMod.CARD_SPADE_ACE.get())); deck.add(new ItemStack(ExampleMod.CARD_SPADE_TWO.get()));
        deck.add(new ItemStack(ExampleMod.CARD_HEART_3.get())); deck.add(new ItemStack(ExampleMod.CARD_HEART_4.get())); deck.add(new ItemStack(ExampleMod.CARD_HEART_5.get())); deck.add(new ItemStack(ExampleMod.CARD_HEART_6.get())); deck.add(new ItemStack(ExampleMod.CARD_HEART_7.get())); deck.add(new ItemStack(ExampleMod.CARD_HEART_8.get())); deck.add(new ItemStack(ExampleMod.CARD_HEART_9.get())); deck.add(new ItemStack(ExampleMod.CARD_HEART_10.get())); deck.add(new ItemStack(ExampleMod.CARD_HEART_JACK.get())); deck.add(new ItemStack(ExampleMod.CARD_HEART_QUEEN.get())); deck.add(new ItemStack(ExampleMod.CARD_HEART_KING.get())); deck.add(new ItemStack(ExampleMod.CARD_HEART_ACE.get())); deck.add(new ItemStack(ExampleMod.CARD_HEART_TWO.get()));
        deck.add(new ItemStack(ExampleMod.CARD_CLUB_3.get())); deck.add(new ItemStack(ExampleMod.CARD_CLUB_4.get())); deck.add(new ItemStack(ExampleMod.CARD_CLUB_5.get())); deck.add(new ItemStack(ExampleMod.CARD_CLUB_6.get())); deck.add(new ItemStack(ExampleMod.CARD_CLUB_7.get())); deck.add(new ItemStack(ExampleMod.CARD_CLUB_8.get())); deck.add(new ItemStack(ExampleMod.CARD_CLUB_9.get())); deck.add(new ItemStack(ExampleMod.CARD_CLUB_10.get())); deck.add(new ItemStack(ExampleMod.CARD_CLUB_JACK.get())); deck.add(new ItemStack(ExampleMod.CARD_CLUB_QUEEN.get())); deck.add(new ItemStack(ExampleMod.CARD_CLUB_KING.get())); deck.add(new ItemStack(ExampleMod.CARD_CLUB_ACE.get())); deck.add(new ItemStack(ExampleMod.CARD_CLUB_TWO.get()));
        deck.add(new ItemStack(ExampleMod.CARD_DIAMOND_3.get())); deck.add(new ItemStack(ExampleMod.CARD_DIAMOND_4.get())); deck.add(new ItemStack(ExampleMod.CARD_DIAMOND_5.get())); deck.add(new ItemStack(ExampleMod.CARD_DIAMOND_6.get())); deck.add(new ItemStack(ExampleMod.CARD_DIAMOND_7.get())); deck.add(new ItemStack(ExampleMod.CARD_DIAMOND_8.get())); deck.add(new ItemStack(ExampleMod.CARD_DIAMOND_9.get())); deck.add(new ItemStack(ExampleMod.CARD_DIAMOND_10.get())); deck.add(new ItemStack(ExampleMod.CARD_DIAMOND_JACK.get())); deck.add(new ItemStack(ExampleMod.CARD_DIAMOND_QUEEN.get())); deck.add(new ItemStack(ExampleMod.CARD_DIAMOND_KING.get())); deck.add(new ItemStack(ExampleMod.CARD_DIAMOND_ACE.get())); deck.add(new ItemStack(ExampleMod.CARD_DIAMOND_TWO.get()));
        deck.add(new ItemStack(ExampleMod.BLACK_JOKER.get())); deck.add(new ItemStack(ExampleMod.RED_JOKER.get()));
    }
}