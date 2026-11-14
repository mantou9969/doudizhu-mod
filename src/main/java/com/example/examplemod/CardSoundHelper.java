package com.example.examplemod;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import java.util.List;
import java.util.Random;

public class CardSoundHelper {
    private static final Random RANDOM = new Random();

    // 播放出牌音效
    public static void playCardSound(Player player, List<ItemStack> cards) {
        if (cards == null || cards.isEmpty()) return;

        PokerRules.HandResult result = PokerRules.evaluate(cards);
        String soundName = "";

        switch (result.type) {
            case SINGLE: // 1xx
                soundName = String.format("1%02d", getFileIndex(result.primaryValue));
                break;
            case PAIR:   // 2xx
                soundName = String.format("2%02d", getFileIndex(result.primaryValue));
                break;
            case TRIPLE: // 3xx (三张)
            case TRIPLE_ONE: // 三带一
                soundName = String.format("3%02d", getFileIndex(result.primaryValue));
                break;
            case TRIPLE_PAIR: // 三带一对
                soundName = "sandaiyidui";
                break;
            case BOMB:
                soundName = "zhadan";
                break;
            case ROCKET:
                soundName = "wangzha";
                break;
            case STRAIGHT:
                soundName = "shunzi";
                break;
            case CONSECUTIVE_PAIRS:
                soundName = "liandui";
                break;
            case PLANE:
                soundName = "feiji";
                break;
            case FOUR_WITH_TWO:
                // 简单判断：6张是四带二单，8张是四带两对
                if (cards.size() == 8) soundName = "sidailiangdui";
                else soundName = "sidaier";
                break;
            default: return;
        }
        playSoundByName(player, soundName);
    }

    // ★★★ 新增：剩余牌数报警音效 ★★★
    public static void playRemainingSound(Player player, int count) {
        if (count == 1) {
            playSoundByName(player, "baojing1"); // 我就剩一张牌了
        } else if (count == 2) {
            playSoundByName(player, "baojing2"); // 我就剩两张牌了
        }
    }

    // 播放"不要" (随机 buyao1 - buyao4)
    public static void playPassSound(Player player) {
        int idx = RANDOM.nextInt(4) + 1;
        playSoundByName(player, "buyao" + idx);
    }

    // 播放叫地主
    public static void playOrderSound(Player player, boolean want) {
        playSoundByName(player, want ? "order" : "noorder");
    }

    // 通用播放方法
    public static void playSoundByName(Player player, String name) {
        SoundEvent sound = ModSounds.getSound(name);
        if (sound != null) {
            // 音量 1.5，语速 1.0
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    sound, SoundSource.PLAYERS, 1.5F, 1.0F);
        }
    }

    // 核心映射 (保持你的原版逻辑)
    // 逻辑值: 3=3 ... 10=10, 11=J, 12=Q, 13=K, 14=A, 15=2, 16=小王, 17=大王
    // 文件名: 3=03 ... 10=10, 11=11, 12=12, 13=13, 01=A, 02=2, 14=小王, 15=大王
    private static int getFileIndex(int logicValue) {
        if (logicValue == 14) return 1;  // A -> 101
        if (logicValue == 15) return 2;  // 2 -> 102
        if (logicValue == 16) return 14; // 小王 -> 114
        if (logicValue == 17) return 15; // 大王 -> 115
        return logicValue; // 3-13 保持不变
    }
}