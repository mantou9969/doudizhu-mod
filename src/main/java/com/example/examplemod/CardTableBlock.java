package com.example.examplemod;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class CardTableBlock extends Block {

    public CardTableBlock() {
        // 定义方块属性：硬度2.5
        super(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5f));
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            if (player instanceof ServerPlayer serverPlayer) {
                // ★★★ 核心修复：玩家右键时，强制加入大厅 ★★★
                // 这行代码执行后，您的名字才会出现在大厅里，准备按钮才会生效！
                GameLogic.joinTable(serverPlayer);

                // 打开 GUI
                NetworkHooks.openScreen(serverPlayer, new MenuProvider() {
                    @Override
                    public Component getDisplayName() {
                        return Component.literal("斗地主牌桌");
                    }

                    @Nullable
                    @Override
                    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
                        return new CardTableMenu(id, inventory);
                    }
                }, pos);
            }
        }
        return InteractionResult.SUCCESS;
    }
}