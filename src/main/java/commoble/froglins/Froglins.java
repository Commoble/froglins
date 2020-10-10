package commoble.froglins;

import java.util.function.BiConsumer;

import commoble.froglins.client.ClientEvents;
import commoble.froglins.util.ConfigHelper;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes;
import net.minecraft.entity.item.PaintingType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

@Mod(Froglins.MODID)
public class Froglins
{
	public static final String MODID = "froglins"; // use this same string everywhere you need a modid	
	public static Froglins INSTANCE;
	
	public static final ITag<Block> DIGGABLE_TAG = BlockTags.makeWrapperTag("froglins:diggable");
	
	public final ServerConfig serverConfig;
	
	// forge registry order doesn't currently work well with spawn eggs
	// make so we have to make the froglin entity type before the egg item
	public final EntityType<FroglinEntity> froglin = EntityType.Builder.create(FroglinEntity::new, EntityClassification.MONSTER)
		.build(new ResourceLocation(MODID, Names.FROGLIN).toString());

	public Froglins() // invoked by forge due to @Mod
	{
		INSTANCE = this;

		ModLoadingContext modContext = ModLoadingContext.get();
		FMLJavaModLoadingContext fmlContext = FMLJavaModLoadingContext.get();

		// mod bus has modloading init events and registry events
		IEventBus modBus = fmlContext.getModEventBus();
		// forge bus is for server starting events and in-game events
		IEventBus forgeBus = MinecraftForge.EVENT_BUS;
		
		this.serverConfig = ConfigHelper.register(modContext, fmlContext, ModConfig.Type.SERVER, ServerConfig::new);
		
		// create and register deferred registers
		DeferredRegister<Item> items = registerRegister(modBus, ForgeRegistries.ITEMS);
		DeferredRegister<PaintingType> paintings = registerRegister(modBus, ForgeRegistries.PAINTING_TYPES);
		
		// register objects via deferred registers
		items.register(Names.FROGLIN_SPAWN_EGG, () ->
			new SpawnEggItem(this.froglin, 0x001e00, 0xbdcbd8, new Item.Properties().group(ItemGroup.MISC)));
		paintings.register(Names.FROGLIN, () -> new PaintingType(32,32));
		
		// manually register entity types
		modBus.addGenericListener(EntityType.class, this::onRegisterEntityTypes);
		
		// other event listeners
		modBus.addListener(this::onCommonSetup);

		// add listeners to clientjar events separately
		if (FMLEnvironment.dist == Dist.CLIENT)
		{
			ClientEvents.subscribeClientEvents(modBus, forgeBus);
		}
	}
	
	public void onRegisterEntityTypes(RegistryEvent.Register<EntityType<?>> event)
	{
		BiConsumer<String,EntityType<?>> registrator = getRegistrator(event.getRegistry());
		registrator.accept(Names.FROGLIN, this.froglin);
	}
	
	public void onCommonSetup(FMLCommonSetupEvent event)
	{
		event.enqueueWork(this::afterCommonSetup);
	}
	
	// runs on the main thread after common setup event
	// stuff can safely be put into vanilla maps here
	public void afterCommonSetup()
	{
		GlobalEntityTypeAttributes.put(this.froglin, FroglinEntity.createAttributes().create());
	}
	
	public static <T extends IForgeRegistryEntry<T>> BiConsumer<String,T> getRegistrator(IForgeRegistry<T> registry)
	{
		return (name,thing) -> register(name, thing, registry);
	}
	
	public static <T extends IForgeRegistryEntry<T>> void register(String name, T thing, IForgeRegistry<T> registry)
	{
		thing.setRegistryName(new ResourceLocation(MODID, name));
		registry.register(thing);
	}
	
	// creates a DeferredRegister and subscribes it to the mod bus
	public static <T extends IForgeRegistryEntry<T>> DeferredRegister<T> registerRegister(IEventBus modBus, IForgeRegistry<T> registry)
	{
		DeferredRegister<T> register = DeferredRegister.create(registry, MODID);
		register.register(modBus);
		return register;
	}
}