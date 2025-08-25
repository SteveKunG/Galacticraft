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

package dev.galacticraft.mod.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.galacticraft.mod.Constant;
import dev.galacticraft.mod.client.model.entity.SlimelingModel;
import dev.galacticraft.mod.client.render.entity.model.GCEntityModelLayer;
import dev.galacticraft.mod.content.entity.Slimeling;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;

public class SlimelingRenderer extends MobRenderer<Slimeling, SlimelingModel<Slimeling>> {
    public SlimelingRenderer(EntityRendererProvider.Context context) {
        super(context, new SlimelingModel<>(context.bakeLayer(GCEntityModelLayer.SLIMELING)), 0.3f);
    }

    @Override
    public ResourceLocation getTextureLocation(Slimeling entity) {
        return Constant.id(Constant.EntityTexture.SLIMELING);
    }

    @Override
    public void render(Slimeling entity, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
        var color = entity.getColor();
        this.model.setColor(FastColor.ARGB32.colorFromFloat(1.0f, color.x(), color.y(), color.z()));
        super.render(entity, yaw, partialTick, poseStack, multiBufferSource, packedLight);
    }
}