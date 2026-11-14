package com.example.examplemod;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.LivingEntity;

public class ShameHatModel extends HumanoidModel<LivingEntity> {

    public ShameHatModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition partdefinition = meshdefinition.getRoot();

        // ★★★ 关键步骤 1：把默认的 "head" 替换成空节点 ★★★
        // 这样原来的那个8x8x8的方块头就消失了
        PartDefinition head = partdefinition.addOrReplaceChild("head",
                CubeListBuilder.create(),
                PartPose.ZERO);

        // ★★★ 关键步骤 2：把默认的 "hat" (外层) 也替换成空节点 ★★★
        // 这就是导致你头变白的罪魁祸首之一！把它也删了！
        partdefinition.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);

        // ★★★ 3. 开始堆叠我们的10层高帽 ★★★
        // 从 Y = -8.0F (头顶) 开始往上堆

        // Layer 1: 底座
        head.addOrReplaceChild("h1", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-5.0F, -10.0F, -5.0F, 10.0F, 2.0F, 10.0F), PartPose.ZERO);

        // Layer 2
        head.addOrReplaceChild("h2", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-4.5F, -12.0F, -4.5F, 9.0F, 2.0F, 9.0F), PartPose.ZERO);

        // Layer 3
        head.addOrReplaceChild("h3", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-4.0F, -15.0F, -4.0F, 8.0F, 3.0F, 8.0F), PartPose.ZERO);

        // Layer 4
        head.addOrReplaceChild("h4", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-3.5F, -19.0F, -3.5F, 7.0F, 4.0F, 7.0F), PartPose.ZERO);

        // Layer 5
        head.addOrReplaceChild("h5", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-3.0F, -24.0F, -3.0F, 6.0F, 5.0F, 6.0F), PartPose.ZERO);

        // Layer 6
        head.addOrReplaceChild("h6", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-2.5F, -30.0F, -2.5F, 5.0F, 6.0F, 5.0F), PartPose.ZERO);

        // Layer 7
        head.addOrReplaceChild("h7", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-2.0F, -38.0F, -2.0F, 4.0F, 8.0F, 4.0F), PartPose.ZERO);

        // Layer 8
        head.addOrReplaceChild("h8", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-1.5F, -48.0F, -1.5F, 3.0F, 10.0F, 3.0F), PartPose.ZERO);

        // Layer 9
        head.addOrReplaceChild("h9", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-1.0F, -60.0F, -1.0F, 2.0F, 12.0F, 2.0F), PartPose.ZERO);

        // Layer 10
        head.addOrReplaceChild("h10", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-0.5F, -76.0F, -0.5F, 1.0F, 16.0F, 1.0F), PartPose.ZERO);

        return LayerDefinition.create(meshdefinition, 64, 64);
    }
}