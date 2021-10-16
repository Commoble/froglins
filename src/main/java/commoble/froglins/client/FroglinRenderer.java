package commoble.froglins.client;

import commoble.froglins.FroglinEntity;
import commoble.froglins.Froglins;
import commoble.froglins.Names;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class FroglinRenderer extends MobRenderer<FroglinEntity, FroglinModel>
{
	public static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation(Froglins.MODID, "textures/entity/froglin.png");
	public static final ModelLayerLocation FROGLIN_MODEL_LAYER = new ModelLayerLocation(
		new ResourceLocation(Froglins.MODID, Names.FROGLIN), "main");

	public FroglinRenderer(EntityRendererProvider.Context context)
	{
		super(context, new FroglinModel(context.bakeLayer(FROGLIN_MODEL_LAYER)), 0.5F);
	}

	@Override
	public ResourceLocation getTextureLocation(FroglinEntity entity)
	{
		return TEXTURE_LOCATION;
	}

}
