package commoble.froglins.client;

import commoble.froglins.Froglins;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientEvents
{

	public static ModelLayerLocation FROGLIN = new ModelLayerLocation( new ResourceLocation("froglins:froglin"), "froglin");


	public static void subscribeClientEvents(IEventBus modBus, IEventBus forgeBus)
	{
		// subscribe client-only events
		// client-only classes like Minecraft can be safely referred to in this class
		modBus.addListener(ClientEvents::registerLayer);
		modBus.addListener(ClientEvents::registerRenderers);
		modBus.addListener(ClientEvents::onRegisterItemColors);

	}

	public static void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event){
		event.registerLayerDefinition(FROGLIN, FroglinModel::createLayer);

	}

	public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event){
		event.registerEntityRenderer(Froglins.INSTANCE.froglin, FroglinRenderer::new);
	}

	// no longer how models register.
//	public static void onClientSetup(FMLClientSetupEvent event)
//	{
//		RenderingRegistry.registerEntityRenderingHandler(Froglins.INSTANCE.froglin, FroglinRenderer::new);
//	}
	
	public static void onRegisterItemColors(ColorHandlerEvent.Item event)
	{
		event.getItemColors().register((stack,tint) -> tint > 0 ? -1 : 0x032f00, Froglins.INSTANCE.healthinessTonicItem.get());
	}
}