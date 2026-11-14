package com.example.examplemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CardTableScreen extends AbstractContainerScreen<CardTableMenu> {

    // 缩放比例 1.6 倍
    private static final float CARD_SCALE = 1.6f;
    // 标准间距 (牌少的时候)
    private static final float STD_SPACING = 20.0f;

    private final Set<Integer> selectedSlots = new HashSet<>();
    private List<ItemStack> bottomCards = new ArrayList<>();
    private List<ItemStack> playedCards = new ArrayList<>();

    private int gameState = 0;
    private int lastPlayerIdx = -1;

    private String landlordName = "";
    private String currentTurnName = "";
    private boolean canPass = false;
    private String winnerInfo = "";

    private List<String> lobbyPlayers = new ArrayList<>();
    private List<Boolean> lobbyReadies = new ArrayList<>();
    private List<Integer> handCounts = new ArrayList<>();
    private String ownerName = "";

    private Button btnRunAway, btnReady, btnGrab, btnPass, btnPlay, btnNoCard, btnRestart, btnAddBot;
    private Button btnChatToggle;
    private List<Button> chatButtons = new ArrayList<>();
    private boolean showChatMenu = false;

    public CardTableScreen(CardTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 500;
        this.imageHeight = 300;
    }

    public void updateGameInfo(int state, String lName, String turnName, boolean pass, List<ItemStack> bottom,
                               List<String> pNames, List<Boolean> pReadies, List<Integer> hCounts, String owner, String winner) {
        this.gameState = state;
        this.landlordName = lName;
        this.bottomCards = bottom;
        this.currentTurnName = turnName;
        this.canPass = pass;
        this.lobbyPlayers = pNames;
        this.lobbyReadies = pReadies;
        this.handCounts = hCounts;
        this.ownerName = owner;
        this.winnerInfo = winner;
        updateButtonVisibility();
    }

    public void updateDesk(List<ItemStack> played, int lastIdx, List<ItemStack> bottom) {
        this.playedCards = played;
        this.lastPlayerIdx = lastIdx;
        this.bottomCards = bottom;
        updateButtonVisibility();
    }

    @Override
    protected void init() {
        super.init();
        int cx = this.width / 2;
        int cy = this.height / 2;
        int btnY = this.height - 110;

        btnRunAway = addRenderableWidget(Button.builder(Component.literal("离开"), b -> {
            PacketHandler.INSTANCE.sendToServer(new PacketRunAway());
            this.onClose();
        }).bounds(10, 10, 40, 20).build());

        btnReady = addRenderableWidget(Button.builder(Component.literal("准备"), b -> PacketHandler.INSTANCE.sendToServer(new PacketPlayerReady())).bounds(cx - 40, cy + 50, 80, 20).build());
        btnAddBot = addRenderableWidget(Button.builder(Component.literal("加机器人"), b -> PacketHandler.INSTANCE.sendToServer(new PacketAddBot())).bounds(cx - 50, cy - 20, 100, 20).build());

        btnGrab = addRenderableWidget(Button.builder(Component.literal("抢地主"), b -> PacketHandler.INSTANCE.sendToServer(new PacketCallLandlord(true))).bounds(cx - 60, cy + 10, 50, 20).build());
        btnPass = addRenderableWidget(Button.builder(Component.literal("不抢"), b -> PacketHandler.INSTANCE.sendToServer(new PacketCallLandlord(false))).bounds(cx + 10, cy + 10, 50, 20).build());

        btnPlay = addRenderableWidget(Button.builder(Component.literal("出 牌"), b -> {
            PacketHandler.INSTANCE.sendToServer(new PacketPlayCard(new ArrayList<>(selectedSlots)));
            selectedSlots.clear();
        }).bounds(cx - 85, btnY, 80, 24).build());

        btnNoCard = addRenderableWidget(Button.builder(Component.literal("不 要"), b -> PacketHandler.INSTANCE.sendToServer(new PacketPass())).bounds(cx + 5, btnY, 80, 24).build());

        btnRestart = addRenderableWidget(Button.builder(Component.literal("再来一局"), b -> PacketHandler.INSTANCE.sendToServer(new PacketPlayerReady())).bounds(cx - 40, cy + 20, 80, 20).build());

        btnChatToggle = addRenderableWidget(Button.builder(Component.literal("语音"), b -> {
            showChatMenu = !showChatMenu;
            updateChatButtons();
        }).bounds(this.width - 60, this.height - 40, 50, 20).build());

        initChatButtons();
        updateButtonVisibility();
    }

    private void initChatButtons() {
        chatButtons.clear();
        String[] texts = {"快点啊", "打得不错", "决战到天亮", "配合得好", "再见", "失误了"};
        String[] sounds = {"msgchatmsg03", "msgchatmsg04", "msgchatmsg08", "msgchatmsg02", "msgchatmsg11", "msgchatmsg12"};
        for(int i=0; i<texts.length; i++) {
            String s = sounds[i];
            Button b = addRenderableWidget(Button.builder(Component.literal(texts[i]), btn -> {
                PacketHandler.INSTANCE.sendToServer(new PacketVoiceMessage(s));
                showChatMenu = false; updateChatButtons();
            }).bounds(this.width - 150, this.height - 200 + (i * 22), 85, 20).build());
            b.visible = false;
            chatButtons.add(b);
        }
    }
    private void updateChatButtons() { for (Button b : chatButtons) b.visible = showChatMenu; }

    private void updateButtonVisibility() {
        if (minecraft.player == null) return;
        String myName = minecraft.player.getName().getString();

        btnReady.visible = false; btnAddBot.visible = false;
        btnGrab.visible = false; btnPass.visible = false;
        btnPlay.visible = false; btnNoCard.visible = false; btnRestart.visible = false;
        if(btnChatToggle != null) btnChatToggle.visible = true;
        if(btnRunAway != null) btnRunAway.visible = true;

        if (gameState == 3) {
            btnRestart.visible = true;
        }
        else if (gameState == 2) {
            if (myName.equals(currentTurnName)) {
                btnPlay.visible = true;
                btnNoCard.visible = canPass;
            }
        }
        else if (gameState == 1) {
            if (myName.equals(currentTurnName)) {
                btnGrab.visible = true;
                btnPass.visible = true;
            }
        }
        else {
            int myIdx = getMyIndex();
            boolean isReady = (myIdx != -1 && myIdx < lobbyReadies.size()) ? lobbyReadies.get(myIdx) : false;
            boolean isOwner = myName.equals(ownerName);

            btnReady.visible = true;
            btnReady.setMessage(Component.literal(isReady ? "§a已准备" : "§c点击准备"));

            if (isOwner) btnAddBot.visible = lobbyPlayers.size() < 3;
        }
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        renderCustomBg(g);

        if (lobbyPlayers.isEmpty() && bottomCards.isEmpty()) {
            g.drawCenteredString(this.font, "连接中...", this.width / 2, this.height / 2, 0xFFFFFF);
            super.render(g, mx, my, pt);
            return;
        }

        int cx = this.width / 2;
        int cy = this.height / 2;

        if (gameState == 0) {
            drawLobby(g, cx, cy);
        } else {
            drawGameScene(g, cx, cy);
            drawMyHand(g, cx);
        }

        for (net.minecraft.client.gui.components.events.GuiEventListener child : this.children()) {
            if (child instanceof Button btn && btn.visible) btn.render(g, mx, my, pt);
        }
        this.renderTooltip(g, mx, my);
    }

    private void renderCustomBg(GuiGraphics graphics) {
        int w = this.width;
        int h = this.height;
        graphics.fill(0, 0, w, h, 0xFF4A3218);
        graphics.fill(10, 10, w - 10, h - 10, 0xFF005522);
    }

    private void drawLobby(GuiGraphics graphics, int cx, int cy) {
        graphics.drawCenteredString(this.font, "=== 斗地主大厅 (" + lobbyPlayers.size() + "/3) ===", cx, cy - 80, 0xFFFF00);
        graphics.drawCenteredString(this.font, "§7满3人且全部准备后自动开始", cx, cy - 65, 0xAAAAAA);

        int[][] pos = { {cx, cy + 60}, {cx + 150, cy - 40}, {cx - 150, cy - 40} };
        int myIdx = getMyIndex();
        if (myIdx == -1) myIdx = 0;

        for (int i = 0; i < 3; i++) {
            int realIdx = (myIdx + i) % 3;
            int x = pos[i][0];
            int y = pos[i][1];
            graphics.fill(x - 40, y - 40, x + 40, y + 40, 0x40000000);
            graphics.renderOutline(x - 40, y - 40, 80, 80, 0xFFFFFFFF);

            if (realIdx < lobbyPlayers.size()) {
                String name = lobbyPlayers.get(realIdx);
                boolean ready = (realIdx < lobbyReadies.size()) ? lobbyReadies.get(realIdx) : false;
                boolean isOwner = name.equals(ownerName);

                String displayName = name.equals(getMyName()) ? "我" : name;
                graphics.drawCenteredString(this.font, displayName, x, y - 10, 0xFFFFFF);
                if (isOwner) graphics.drawCenteredString(this.font, "§6[房主]", x, y - 25, 0xFFFFFF);

                graphics.pose().pushPose();
                graphics.pose().translate(x, y + 15, 0);
                graphics.pose().scale(2.0f, 2.0f, 1f);
                graphics.drawCenteredString(this.font, ready ? "✔" : "...", 0, 0, ready ? 0x00FF00 : 0xAAAAAA);
                graphics.pose().popPose();
            } else {
                graphics.drawCenteredString(this.font, "等待加入", x, y, 0x888888);
            }
        }
    }

    private void drawGameScene(GuiGraphics graphics, int cx, int cy) {
        // 底牌
        if (!bottomCards.isEmpty()) {
            graphics.fill(cx - 55, 5, cx + 55, 60, 0x60000000);
            boolean showFront = (gameState == 2 || gameState == 3);
            if (showFront) {
                graphics.pose().pushPose();
                graphics.pose().translate(cx - 45, 20, 0);
                graphics.pose().scale(CARD_SCALE, CARD_SCALE, 1.0f);
                for (int i = 0; i < bottomCards.size(); i++) graphics.renderItem(bottomCards.get(i), i * 20, 0);
                graphics.pose().popPose();
            } else {
                graphics.fill(cx - 35, 22, cx - 19, 38, 0xFFDD3333);
                graphics.fill(cx - 7, 22, cx + 9, 38, 0xFFDD3333);
                graphics.fill(cx + 21, 22, cx + 37, 38, 0xFFDD3333);
                graphics.drawCenteredString(this.font, "底牌", cx, 45, 0xFFAAAA);
            }
        }

        // 出牌
        if (!playedCards.isEmpty() && lastPlayerIdx != -1) {
            int myIdx = getMyIndex();
            if (myIdx == -1) myIdx = 0;
            int relativePos = (lastPlayerIdx - myIdx + 3) % 3;

            int startX = cx - (playedCards.size() * 10);
            int drawY = cy;

            if (relativePos == 1) { startX = cx + 120; drawY = cy - 20; }
            else if (relativePos == 2) { startX = cx - 180; drawY = cy - 20; }

            graphics.pose().pushPose();
            graphics.pose().translate(startX, drawY, 0);
            graphics.pose().scale(CARD_SCALE, CARD_SCALE, 1.0f);

            // 出牌也应用简单的挤牌逻辑 (如果太多的话)
            float spacing = 18f;
            if (playedCards.size() > 10) spacing = 12f;

            for (int i = 0; i < playedCards.size(); i++) {
                graphics.renderItem(playedCards.get(i), (int)(i * spacing), 0);
            }
            graphics.pose().popPose();
        }

        if (!winnerInfo.isEmpty()) {
            graphics.pose().pushPose();
            graphics.pose().translate(cx, cy - 30, 0);
            graphics.pose().scale(3.0f, 3.0f, 1f);
            graphics.drawCenteredString(this.font, winnerInfo + " 赢了!", 0, 0, 0xFFD700);
            graphics.pose().popPose();
        } else {
            if (!currentTurnName.isEmpty()) {
                String turnTxt = currentTurnName.equals(getMyName()) ? ">>> 轮到你了 <<<" : "等待 " + currentTurnName + "...";
                graphics.drawCenteredString(this.font, turnTxt, cx, cy - 50, 0xFFFF00);
            }
        }

        int myIdx = getMyIndex();
        if (myIdx == -1) myIdx = 0;
        int rightIdx = (myIdx + 1) % 3;
        int leftIdx = (myIdx + 2) % 3;
        drawAvatar(graphics, rightIdx, cx + 180, cy - 60);
        drawAvatar(graphics, leftIdx, cx - 180, cy - 60);
    }

    private void drawAvatar(GuiGraphics graphics, int idx, int x, int y) {
        if (idx >= lobbyPlayers.size()) return;
        String name = lobbyPlayers.get(idx);
        boolean isLandlord = name.equals(landlordName);
        graphics.fill(x - 40, y - 25, x + 40, y + 25, 0x80000000);
        graphics.drawCenteredString(this.font, name, x, y - 15, 0xFFFFFF);
        if (!landlordName.isEmpty()) {
            String role = isLandlord ? "地主" : "农民";
            int color = isLandlord ? 0xFF5555 : 0x55FF55;
            graphics.drawCenteredString(this.font, role, x, y + 5, color);
        }
        if (idx < handCounts.size()) {
            graphics.drawCenteredString(this.font, "剩 " + handCounts.get(idx) + " 张", x, y + 15, 0xAAAAAA);
        }
    }

    // ★★★ 核心修复：智能挤牌算法 ★★★
    private void drawMyHand(GuiGraphics graphics, int cx) {
        if (minecraft.player == null) return;
        Inventory inv = minecraft.player.getInventory();
        List<Integer> handSlots = new ArrayList<>();
        for(int i=9; i<36; i++) {
            if (!inv.getItem(i).isEmpty()) handSlots.add(i);
        }

        if (handSlots.isEmpty()) return;

        // 1. 计算可用屏幕宽度 (转为缩放后的空间)
        // 留出左右各 40 像素边距
        float availableWidth = (this.width - 80) / CARD_SCALE;

        // 2. 计算需要的宽度 (按标准间距 20)
        // (N-1)*间距 + 最后一张牌宽(16)
        float totalWidthNeeded = (handSlots.size() - 1) * STD_SPACING + 16;

        // 3. 决定间距
        float spacing = STD_SPACING;
        if (totalWidthNeeded > availableWidth) {
            // 挤牌公式：(总宽 - 最后一张牌) / (张数 - 1)
            spacing = (availableWidth - 16) / (float)(handSlots.size() - 1);
        }

        // 4. 重新计算总宽 (用于居中)
        float finalTotalWidth = (handSlots.size() - 1) * spacing + 16;
        int yBase = this.height - 60;

        graphics.pose().pushPose();
        graphics.pose().translate(cx, yBase, 0);
        graphics.pose().scale(CARD_SCALE, CARD_SCALE, 1.0f);
        graphics.pose().translate(-finalTotalWidth / 2.0f, 0, 0);

        for (int i = 0; i < handSlots.size(); i++) {
            int slotId = handSlots.get(i);
            int drawY = 0;
            if (selectedSlots.contains(slotId)) drawY -= 10;
            graphics.renderItem(inv.getItem(slotId), (int)(i * spacing), drawY);

            // 选中高亮
            if (selectedSlots.contains(slotId)) {
                graphics.fill((int)(i * spacing), drawY, (int)(i * spacing) + 16, drawY + 16, 0x80FFFF00);
            }
        }
        graphics.pose().popPose();

        if (!landlordName.isEmpty()) {
            boolean amILandlord = getMyName().equals(landlordName);
            graphics.drawCenteredString(this.font, amILandlord ? "我是地主" : "我是农民", cx, this.height - 95, amILandlord ? 0xFF5555 : 0x55FF55);
        }
    }

    // ★★★ 核心修复：点击判定也同步使用挤牌逻辑 ★★★
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (net.minecraft.client.gui.components.events.GuiEventListener child : this.children()) {
            if (child.mouseClicked(mouseX, mouseY, button)) return true;
        }
        if (gameState == 2) {
            Inventory inv = minecraft.player.getInventory();
            List<Integer> handSlots = new ArrayList<>();
            for(int i=9; i<36; i++) if(!inv.getItem(i).isEmpty()) handSlots.add(i);

            if (handSlots.isEmpty()) return false;

            // 必须使用完全一样的算法计算间距和起始点
            int cx = this.width / 2;
            float availableWidth = (this.width - 80) / CARD_SCALE;
            float totalWidthNeeded = (handSlots.size() - 1) * STD_SPACING + 16;

            float spacing = STD_SPACING;
            if (totalWidthNeeded > availableWidth) {
                spacing = (availableWidth - 16) / (float)(handSlots.size() - 1);
            }

            float finalTotalWidth = (handSlots.size() - 1) * spacing + 16;
            float screenStartX = cx - (finalTotalWidth / 2.0f) * CARD_SCALE;
            int yBase = this.height - 60;

            // 倒序检测 (从最上层的牌开始)
            for (int i = handSlots.size() - 1; i >= 0; i--) {
                int slotId = handSlots.get(i);

                float cardX = screenStartX + (i * spacing * CARD_SCALE);
                float cardY = yBase;
                if (selectedSlots.contains(slotId)) cardY -= (10 * CARD_SCALE);

                float cardW = 16 * CARD_SCALE;
                float cardH = 16 * CARD_SCALE;

                if (mouseX >= cardX && mouseX <= cardX + cardW && mouseY >= cardY && mouseY <= cardY + cardH) {
                    if (selectedSlots.contains(slotId)) selectedSlots.remove(slotId);
                    else { selectedSlots.add(slotId); minecraft.player.playSound(SoundEvents.UI_BUTTON_CLICK.get(), 0.5f, 1.0f); }
                    return true;
                }
            }
        }
        return false;
    }

    @Override protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1) {}
    private String getMyName() { return minecraft.player != null ? minecraft.player.getName().getString() : ""; }
    private int getMyIndex() {
        String name = getMyName();
        for (int i = 0; i < lobbyPlayers.size(); i++) if (lobbyPlayers.get(i).equals(name)) return i;
        return -1;
    }
}