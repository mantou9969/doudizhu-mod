package com.example.examplemod;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class ShameHatItem extends ArmorItem {

    private static final ResourceLocation WHITE_TEXTURE = new ResourceLocation("minecraft", "textures/block/white_wool.png");

    public ShameHatItem() {
        super(ArmorMaterials.LEATHER, ArmorItem.Type.HELMET, new Properties());
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private ShameHatModel model;

            @Override
            public @Nullable HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                if (model == null) {
                    model = new ShameHatModel(ShameHatModel.createBodyLayer().bakeRoot());
                }

                // 同步头部动作
                model.head.copyFrom(original.head);

                // ★★★ 必须加这几行！强制隐藏默认的帽子层 ★★★
                // 如果不写这个，游戏会默认渲染一个白色的方块套在你头上
                model.hat.visible = false;

                // 顺便把身体其他部位也隐藏了，防止变成白色雪人
                model.body.visible = false;
                model.rightArm.visible = false;
                model.leftArm.visible = false;
                model.rightLeg.visible = false;
                model.leftLeg.visible = false;

                return model;
            }
        });
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return WHITE_TEXTURE.toString();
    }
}