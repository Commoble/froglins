package commoble.froglins.client;

import commoble.froglins.FroglinEntity;
import commoble.froglins.Froglins;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;

public class FroglinRenderer extends MobRenderer<FroglinEntity, FroglinModel>
{
	public static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation(Froglins.MODID, "textures/entity/froglin.png");
	
	public static final FroglinModel MODEL = new FroglinModel(0F);

	public FroglinRenderer(EntityRendererManager renderManagerIn)
	{
		super(renderManagerIn, MODEL, 0.5F);
	}

	@Override
	public ResourceLocation getTextureLocation(FroglinEntity entity)
	{
		return TEXTURE_LOCATION;
	}

}
