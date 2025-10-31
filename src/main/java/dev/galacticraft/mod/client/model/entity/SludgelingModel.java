/*
 * Copyright (c) 2019-2025 Team Galacticraft
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.galacticraft.mod.client.model.entity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.Entity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

public class SludgelingModel<T extends Entity> extends EntityModel<T> {
    private final ModelPart body;
    private final ModelPart tail1;
    private final ModelPart tail2;
    private final ModelPart tail3;
    private final ModelPart tail4;

    public SludgelingModel(ModelPart root) {
        this.body = root.getChild("body");
        this.tail1 = this.body.getChild("tail1");
        this.tail2 = this.body.getChild("tail2");
        this.tail3 = this.body.getChild("tail3");
        this.tail4 = this.body.getChild("tail4");
    }

    public static LayerDefinition createBodyLayer() {
        var meshdefinition = new MeshDefinition();
        var partdefinition = meshdefinition.getRoot();

        var body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(4, 0).addBox(-0.5F, -1.0F, -2.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.5F, 24.0F, -2.0F));

        var tail1 = body.addOrReplaceChild("tail1", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -0.75F, 0.0F));

        var tail2 = body.addOrReplaceChild("tail2", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 2.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -0.5F, 0.0F));

        var tail3 = body.addOrReplaceChild("tail3", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.25F, 3.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -0.5F, 0.0F));

        var tail4 = body.addOrReplaceChild("tail4", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.5F, 4.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -0.5F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
//        this.tail1.rotateAngleY = MathHelper.cos(f2 * 0.3F + 0 * 0.15F * (float) Math.PI) * (float) Math.PI * 0.025F * (1 + Math.abs(0 - 2));
//        this.tail2.rotateAngleY = MathHelper.cos(f2 * 0.3F + 1 * 0.15F * (float) Math.PI) * (float) Math.PI * 0.025F * (1 + Math.abs(1 - 2));
//        this.tail3.rotateAngleY = MathHelper.cos(f2 * 0.3F + 2 * 0.15F * (float) Math.PI) * (float) Math.PI * 0.025F * (1 + Math.abs(1 - 2));
//        this.tail4.rotateAngleY = MathHelper.cos(f2 * 0.3F + 3 * 0.15F * (float) Math.PI) * (float) Math.PI * 0.025F * (1 + Math.abs(1 - 2));
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}