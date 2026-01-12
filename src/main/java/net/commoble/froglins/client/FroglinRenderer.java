package net.commoble.froglins.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.commoble.froglins.FroglinEntity;
import net.commoble.froglins.Froglins;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class FroglinRenderer extends HumanoidMobRenderer<FroglinEntity, FroglinRenderState, FroglinModel>
{
	public static final Identifier TEXTURE_LOCATION = Froglins.id("textures/entity/froglin.png");
	public static final ModelLayerLocation FROGLIN_MODEL_LAYER = new ModelLayerLocation(
		Froglins.FROGLIN.getId(), "main");

	public FroglinRenderer(EntityRendererProvider.Context context)
	{
		super(context, new FroglinModel(context.bakeLayer(FROGLIN_MODEL_LAYER)), 0.5F);
	}

	@Override
	public Identifier getTextureLocation(FroglinRenderState froglin)
	{
		return TEXTURE_LOCATION;
	}

	@Override
	public FroglinRenderState createRenderState()
	{
		return new FroglinRenderState();
	}

	@Override
	protected void setupRotations(FroglinRenderState froglin, PoseStack poseStack, float uhh, float uhhhhh)
	{
		super.setupRotations(froglin, poseStack, uhh, uhhhhh);
        float f = froglin.swimAmount;
        if (f > 0.0F) {
            float f1 = -10.0F - froglin.xRot;
            float f2 = Mth.lerp(f, 0.0F, f1);
            poseStack.rotateAround(Axis.XP.rotationDegrees(f2), 0.0F, froglin.boundingBoxHeight / 2.0F / uhhhhh, 0.0F);
        }
	}

	
}
