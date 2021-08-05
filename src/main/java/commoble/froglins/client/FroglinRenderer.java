package commoble.froglins.client;

import commoble.froglins.FroglinEntity;
import commoble.froglins.Froglins;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

import static commoble.froglins.client.ClientEvents.FROGLIN;

public class FroglinRenderer extends MobRenderer<FroglinEntity, FroglinModel>
{
	public static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation(Froglins.MODID, "textures/entity/froglin.png");

//	public static final FroglinModel MODEL = new FroglinModel(0F);

	public FroglinRenderer(EntityRendererProvider.Context p_174304_) {
		super(p_174304_, new FroglinModel(p_174304_.bakeLayer(FROGLIN)), 0.5F);
	}

//	public FroglinRenderer(EntityRenderDispatcher renderManagerIn)
//	{
//		super(renderManagerIn, MODEL, 0.5F);
//	}

	@Override
	public ResourceLocation getTextureLocation(FroglinEntity entity)
	{
		return TEXTURE_LOCATION;
	}

}
