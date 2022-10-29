package commoble.froglins.client;

import commoble.froglins.Froglins;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class ClientEvents
{
	
	public static void subscribeClientEvents(IEventBus modBus, IEventBus forgeBus)
	{
		// subscribe client-only events
		// client-only classes like Minecraft can be safely referred to in this class
		modBus.addListener(ClientEvents::onRegisterLayerDefinitions);
		modBus.addListener(ClientEvents::onRegisterRenderers);
		modBus.addListener(ClientEvents::onRegisterItemColors);
	}
	
	private static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event)
	{
		event.registerLayerDefinition(FroglinRenderer.FROGLIN_MODEL_LAYER, FroglinModel::createBodyLayer);
	}
	
	private static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event)
	{
		event.registerEntityRenderer(Froglins.INSTANCE.froglinEntityType.get(), FroglinRenderer::new);
	}
	
	private static void onRegisterItemColors(RegisterColorHandlersEvent.Item event)
	{
		event.register((stack,tint) -> tint > 0 ? -1 : 0x032f00, Froglins.INSTANCE.healthinessTonicItem.get());
	}
}