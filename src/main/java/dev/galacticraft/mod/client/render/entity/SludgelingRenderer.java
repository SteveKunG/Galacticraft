package dev.galacticraft.mod.client.render.entity;

import dev.galacticraft.mod.content.entity.Sludgeling;
import net.minecraft.client.model.SilverfishModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class SludgelingRenderer extends MobRenderer<Sludgeling, SilverfishModel<Sludgeling>> {
    private static final ResourceLocation SILVERFISH_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/silverfish.png");

    public SludgelingRenderer(EntityRendererProvider.Context context) {
        super(context, new SilverfishModel<>(context.bakeLayer(ModelLayers.SILVERFISH)), 0.3F);
    }

    @Override
    protected float getFlipDegrees(Sludgeling entity) {
        return 180.0F;
    }

    @Override
    public ResourceLocation getTextureLocation(Sludgeling entity) {
        return SILVERFISH_LOCATION;
    }
}
