package net.commoble.froglins.client;

import net.commoble.froglins.Froglins;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;	

@EventBusSubscriber(modid=Froglins.MODID)
public class ClientProxy
{
	@SubscribeEvent
	public static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event)
	{
		event.registerLayerDefinition(FroglinRenderer.FROGLIN_MODEL_LAYER, FroglinModel::createBodyLayer);
	}
	
	@SubscribeEvent
	public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event)
	{
		event.registerEntityRenderer(Froglins.FROGLIN.get(), FroglinRenderer::new);
	}
}