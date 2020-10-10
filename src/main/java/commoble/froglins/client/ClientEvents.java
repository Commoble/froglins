package commoble.froglins.client;

import commoble.froglins.Froglins;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientEvents
{
  public static void subscribeClientEvents(IEventBus modBus, IEventBus forgeBus)
  {
    // subscribe client-only events
    // client-only classes like Minecraft can be safely referred to in this class
    modBus.addListener(ClientEvents::onClientSetup);
  }
  
  public static void onClientSetup(FMLClientSetupEvent event)
  {
	  RenderingRegistry.registerEntityRenderingHandler(Froglins.INSTANCE.froglin, FroglinRenderer::new);
  }
}