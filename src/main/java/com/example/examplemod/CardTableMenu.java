package com.example.examplemod;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class CardTableMenu extends AbstractContainerMenu {

    public CardTableMenu(int id, Inventory inventory) {
        super(ExampleMod.CARD_TABLE_MENU.get(), id);
        layoutPlayerInventory(inventory);
    }

    public CardTableMenu(int id, Inventory inventory, FriendlyByteBuf data) {
        this(id, inventory);
    }

    private void layoutPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++) this.addSlot(new Slot(inventory, col + row * 9 + 9, -10000, -10000));
        for (int col = 0; col < 9; col++) this.addSlot(new Slot(inventory, col, -10000, -10000));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }

    @Override
    public boolean stillValid(Player player) { return true; }

    // ★★★ 核心保留：防止容器关闭时乱动背包，配合 GameLogic 使用 ★★★
    @Override
    public void removed(Player player) {
        super.removed(player);
    }

    // ★★★ 核心修复：隔离客户端屏幕更新逻辑 ★★★
    public void updateData(boolean isFull, int state,
                           String lName, String turnName, boolean pass,
                           java.util.List<ItemStack> bottom, java.util.List<String> pNames, java.util.List<Boolean> pReadies, java.util.List<Integer> hCounts,
                           String owner, java.util.List<ItemStack> playedCards, int lastIdx, String winner) {

        // 使用 DistExecutor 确保只有客户端执行屏幕更新逻辑
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHandler.updateScreen(
                isFull, state, lName, turnName, pass, bottom, pNames, pReadies, hCounts, owner, playedCards, lastIdx, winner
        ));
    }

    // 内部类隔离客户端代码
    private static class ClientHandler {
        public static void updateScreen(boolean isFull, int state, String lName, String turnName, boolean pass,
                                        java.util.List<ItemStack> bottom, java.util.List<String> pNames, java.util.List<Boolean> pReadies, java.util.List<Integer> hCounts,
                                        String owner, java.util.List<ItemStack> playedCards, int lastIdx, String winner) {

            // 只有这里才引用 net.minecraft.client
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.screen instanceof CardTableScreen screen) {
                if (isFull) {
                    screen.updateGameInfo(state, lName, turnName, pass, bottom, pNames, pReadies, hCounts, owner, winner);
                }
                screen.updateDesk(playedCards, lastIdx, bottom);
            }
        }
    }
}