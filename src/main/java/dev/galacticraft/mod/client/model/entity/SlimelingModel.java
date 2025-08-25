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

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.Entity;

public class SlimelingModel<T extends Entity> extends EntityModel<T> {
    private final ModelPart main_body;
    private final ModelPart body1;
    private final ModelPart body2;
    private final ModelPart body3;
    private final ModelPart body_head;
    private final ModelPart head1;
    private int color = -1;

    public SlimelingModel(ModelPart root) {
        this.main_body = root.getChild("main_body");
        this.body1 = this.main_body.getChild("body1");
        this.body2 = this.body1.getChild("body2");
        this.body3 = this.body2.getChild("body3");
        this.body_head = this.main_body.getChild("body_head");
        this.head1 = this.body_head.getChild("head1");
    }

    public static LayerDefinition createBodyLayer() {
        var meshdefinition = new MeshDefinition();
        var partdefinition = meshdefinition.getRoot();

        var main_body = partdefinition.addOrReplaceChild("main_body", CubeListBuilder.create().texOffs(82, 0).addBox(-5.0F, -5.0F, -5.0F, 10.0F, 10.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 19.0F, -4.0F, 0.0F, 3.1416F, 0.0F));

        var body1 = main_body.addOrReplaceChild("body1", CubeListBuilder.create().texOffs(48, 0).addBox(-4.0F, -2.0F, -10.0F, 9.0F, 7.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.5F, 0.0F, 0.0F));

        var body2 = body1.addOrReplaceChild("body2", CubeListBuilder.create().texOffs(20, 0).addBox(-3.0F, 0.0F, -14.0F, 7.0F, 5.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        var body3 = body2.addOrReplaceChild("body3", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, 2.0F, -16.0F, 5.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        var body_head = main_body.addOrReplaceChild("body_head", CubeListBuilder.create().texOffs(122, 0).addBox(-12.5F, -14.0F, 10.0F, 9.0F, 9.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, 5.0F, -12.0F));

        var head1 = body_head.addOrReplaceChild("head1", CubeListBuilder.create().texOffs(156, 0).addBox(-2.5F, -14.0F, 1.0F, 7.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(-8.5F, -5.0F, 12.0F));

        return LayerDefinition.create(meshdefinition, 256, 128);
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        //        this.tail1.rotateAngleY = Mth.cos(par1 * 0.6662F) * 0.2F * par2;
        //        this.tail2.rotateAngleY = Mth.cos(par1 * 0.6662F) * 0.2F * par2;
        //        this.tail3.rotateAngleY = Mth.cos(par1 * 0.6662F) * 0.2F * par2;
        //        this.tail1.offsetZ = Mth.cos(0.5F * par1 * 0.6662F) * 0.2F * par2;
        //        this.tail2.offsetZ = Mth.cos(0.5F * par1 * 0.6662F) * 0.2F * par2;
        //        this.tail3.offsetZ = Mth.cos(0.5F * par1 * 0.6662F) * 0.2F * par2;
        //        this.neck.offsetZ = -Mth.cos(0.5F * par1 * 0.6662F) * 0.1F * par2;
        //        this.head.offsetZ = -Mth.cos(0.5F * par1 * 0.6662F) * 0.1F * par2;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.main_body.render(poseStack, vertexConsumer, packedLight, packedOverlay, this.color);
    }

    public void setColor(int tint) {
        this.color = tint;
    }
}