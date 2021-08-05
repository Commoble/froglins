package commoble.froglins.client;

import commoble.froglins.Froglins;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientEvents
{
	public static void subscribeClientEvents(IEventBus modBus, IEventBus forgeBus)
	{
		// subscribe client-only events
		// client-only classes like Minecraft can be safely referred to in this class
		modBus.addListener(ClientEvents::onClientSetup);
		modBus.addListener(ClientEvents::onRegisterItemColors);
	}
  
	public static void onClientSetup(FMLClientSetupEvent event)
	{
		RenderingRegistry.registerEntityRenderingHandler(Froglins.INSTANCE.froglin, FroglinRenderer::new);
	}
	
	public static void onRegisterItemColors(ColorHandlerEvent.Item event)
	{
		event.getItemColors().register((stack,tint) -> tint > 0 ? -1 : 0x032f00, Froglins.INSTANCE.healthinessTonicItem.get());
	}
}