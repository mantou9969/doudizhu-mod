package com.example.examplemod;

import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PokerRules {

    // 牌型枚举
    public enum HandType {
        UNKNOWN,            // 乱出
        SINGLE,             // 单张
        PAIR,               // 对子
        TRIPLE,             // 三张 (不带)
        TRIPLE_ONE,         // 三带一 (带单张)
        TRIPLE_PAIR,        // 三带二 (带对子)
        BOMB,               // 炸弹
        ROCKET,             // 王炸
        STRAIGHT,           // 顺子 (5张起)
        CONSECUTIVE_PAIRS,  // 连对 (3连对起)
        PLANE,              // 飞机 (带翅膀或不带)
        FOUR_WITH_TWO       // 四带二
    }

    public static class HandResult {
        public HandType type;
        public int primaryValue; // 用于比大小的核心值 (顺子就是起始值，三带一就是三张那个值)
        public int length;       // 用于顺子/连对/飞机的长度判定

        public HandResult(HandType type, int val) {
            this.type = type;
            this.primaryValue = val;
            this.length = 0;
        }

        public HandResult(HandType type, int val, int len) {
            this.type = type;
            this.primaryValue = val;
            this.length = len;
        }
    }

    // ★★★ 核心：牌型判断 ★★★
    public static HandResult evaluate(List<ItemStack> cards) {
        if (cards == null || cards.isEmpty()) return new HandResult(HandType.UNKNOWN, 0);

        List<Integer> values = new ArrayList<>();
        for (ItemStack stack : cards) values.add(getValue(stack));
        Collections.sort(values); // 从小到大排序

        int size = values.size();
        int vMin = values.get(0);
        int vMax = values.get(size - 1);

        // --- 1. 特殊牌型：王炸 ---
        if (size == 2 && values.contains(16) && values.contains(17)) {
            return new HandResult(HandType.ROCKET, 17);
        }

        // --- 2. 统计每种点数出现的次数 ---
        Map<Integer, Integer> counts = new HashMap<>();
        for (int v : values) counts.put(v, counts.getOrDefault(v, 0) + 1);

        // 找出出现4次、3次、2次、1次的牌有哪些
        List<Integer> c4 = new ArrayList<>();
        List<Integer> c3 = new ArrayList<>();
        List<Integer> c2 = new ArrayList<>();
        List<Integer> c1 = new ArrayList<>();

        for (Map.Entry<Integer, Integer> entry : counts.entrySet()) {
            if (entry.getValue() == 4) c4.add(entry.getKey());
            else if (entry.getValue() == 3) c3.add(entry.getKey());
            else if (entry.getValue() == 2) c2.add(entry.getKey());
            else if (entry.getValue() == 1) c1.add(entry.getKey());
        }
        Collections.sort(c4); Collections.sort(c3); Collections.sort(c2); Collections.sort(c1);

        // --- 3. 炸弹 (4张) ---
        if (size == 4 && !c4.isEmpty()) return new HandResult(HandType.BOMB, c4.get(0));

        // --- 4. 单张 ---
        if (size == 1) return new HandResult(HandType.SINGLE, vMin);

        // --- 5. 对子 ---
        if (size == 2 && !c2.isEmpty()) return new HandResult(HandType.PAIR, c2.get(0));

        // --- 6. 三张 (3不带) ---
        if (size == 3 && !c3.isEmpty()) return new HandResult(HandType.TRIPLE, c3.get(0));

        // --- 7. 三带一 (3+1=4张) ---
        if (size == 4 && !c3.isEmpty()) return new HandResult(HandType.TRIPLE_ONE, c3.get(0));

        // --- 8. 三带二 (3+2=5张，带的是对子) ---
        if (size == 5 && !c3.isEmpty() && !c2.isEmpty()) return new HandResult(HandType.TRIPLE_PAIR, c3.get(0));

        // --- 9. 四带二 (4+1+1=6张 或 4+2+2=8张) ---
        if (!c4.isEmpty()) {
            // 四带两单
            if (size == 6) return new HandResult(HandType.FOUR_WITH_TWO, c4.get(0));
            // 四带两对
            if (size == 8 && c2.size() >= 2) return new HandResult(HandType.FOUR_WITH_TWO, c4.get(0));
        }

        // --- 10. 顺子 (5张起，不能有2和王) ---
        if (size >= 5 && c1.size() == size) {
            // 顺子不能包含2(15), 小王(16), 大王(17)
            if (vMax < 15 && (vMax - vMin) == (size - 1)) {
                return new HandResult(HandType.STRAIGHT, vMin, size);
            }
        }

        // --- 11. 连对 (6张起，偶数，3对起，不能有2和王) ---
        if (size >= 6 && size % 2 == 0 && c2.size() == (size / 2)) {
            // 检查连对是否连续
            boolean isConsecutive = true;
            for (int i = 0; i < c2.size() - 1; i++) {
                if (c2.get(i + 1) - c2.get(i) != 1) {
                    isConsecutive = false;
                    break;
                }
            }
            if (isConsecutive && c2.get(c2.size() - 1) < 15) {
                return new HandResult(HandType.CONSECUTIVE_PAIRS, c2.get(0), size / 2);
            }
        }

        // --- 12. 飞机 (两个以上连续的三张) ---
        if (!c3.isEmpty() && c3.size() >= 2) {
            // 检查三张部分是否连续 (比如 333 444)
            boolean planeConsecutive = true;
            for (int i = 0; i < c3.size() - 1; i++) {
                if (c3.get(i + 1) - c3.get(i) != 1) {
                    planeConsecutive = false;
                    break;
                }
            }
            if (planeConsecutive && c3.get(c3.size()-1) < 15) {
                // 飞机不带 (333 444 = 6张)
                if (size == c3.size() * 3)
                    return new HandResult(HandType.PLANE, c3.get(0), c3.size());

                // 飞机带单 (333 444 + 5 6 = 8张)
                if (size == c3.size() * 4)
                    return new HandResult(HandType.PLANE, c3.get(0), c3.size());

                // 飞机带对 (333 444 + 55 66 = 10张)
                if (size == c3.size() * 5 && c2.size() == c3.size())
                    return new HandResult(HandType.PLANE, c3.get(0), c3.size());
            }
        }

        return new HandResult(HandType.UNKNOWN, 0);
    }

    // ★★★ 核心：比大小 ★★★
    public static boolean canBeat(HandResult prev, HandResult current) {
        if (prev == null || prev.type == HandType.UNKNOWN) return true;
        if (current.type == HandType.UNKNOWN) return false;

        // 1. 王炸无敌
        if (current.type == HandType.ROCKET) return true;
        if (prev.type == HandType.ROCKET) return false;

        // 2. 炸弹管普通牌
        if (current.type == HandType.BOMB && prev.type != HandType.BOMB) return true;

        // 3. 同类型比较
        if (current.type == prev.type) {
            // 如果是顺子、连对、飞机，长度必须一致
            if (current.type == HandType.STRAIGHT ||
                    current.type == HandType.CONSECUTIVE_PAIRS ||
                    current.type == HandType.PLANE) {
                if (current.length != prev.length) return false;
            }

            // 比数值
            return current.primaryValue > prev.primaryValue;
        }

        return false;
    }

    // 获取数值
    public static int getValue(ItemStack stack) {
        String name = stack.getItem().getDescriptionId();
        if (name.contains("red_joker")) return 17;
        if (name.contains("black_joker")) return 16;
        if (name.contains("two")) return 15;
        if (name.contains("ace")) return 14;
        if (name.contains("king")) return 13;
        if (name.contains("queen")) return 12;
        if (name.contains("jack")) return 11;
        if (name.contains("10")) return 10;
        if (name.contains("9")) return 9;
        if (name.contains("8")) return 8;
        if (name.contains("7")) return 7;
        if (name.contains("6")) return 6;
        if (name.contains("5")) return 5;
        if (name.contains("4")) return 4;
        if (name.contains("3")) return 3;
        return 0;
    }
}