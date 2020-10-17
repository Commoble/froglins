package commoble.froglins;

import java.util.Set;
import java.util.function.BiConsumer;

import commoble.froglins.client.ClientEvents;
import commoble.froglins.util.ConfigHelper;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntitySpawnPlacementRegistry.PlacementType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes;
import net.minecraft.entity.item.PaintingType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Food;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.potion.Effects;
import net.minecraft.potion.Potion;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.MobSpawnInfo.Spawners;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.ForgeTagHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.RegistryObject;
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
	public static final ITag<Item> REDSTONE_DUST_TAG = ItemTags.makeWrapperTag("forge:dusts/redstone");
	public static final ITag<Item> GLOWSTONE_DUST_TAG = ItemTags.makeWrapperTag("forge:dusts/glowstone");
	public static final ITag<EntityType<?>> EDIBLE_FISH_TAG = EntityTypeTags.createOptional(new ResourceLocation("froglins:edible_fish"));
	public static final ITag<EntityType<?>> EDIBLE_ANIMALS_TAG = EntityTypeTags.createOptional(new ResourceLocation("froglins:edible_animals"));
	
	public final ServerConfig serverConfig;
	public final CommonConfig commonConfig;
	
	// forge registry order doesn't currently work well with spawn eggs
	// make so we have to make the froglin entity type before the egg item
	public final EntityType<FroglinEntity> froglin = EntityType.Builder.create(FroglinEntity::new, EntityClassification.MONSTER)
		.build(new ResourceLocation(MODID, Names.FROGLIN).toString());
	
	public final RegistryObject<FroglinEggBlock> froglinEggBlock;
	public final RegistryObject<HealthinessTonicItem> healthinessTonicItem;
	public final RegistryObject<Potion> frogsMightPotion;
	public final RegistryObject<Potion> longFrogsMightPotion;
	public final RegistryObject<Potion> strongFrogsMightPotion;
	public final RegistryObject<Potion> frogChampionPotion;
	public final RegistryObject<Potion> longFrogChampionPotion;
	public final RegistryObject<Potion> strongFrogChampionPotion;

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
		this.commonConfig = ConfigHelper.register(modContext, fmlContext, ModConfig.Type.COMMON, CommonConfig::new);
		
		// create and register deferred registers
		DeferredRegister<Block> blocks = registerRegister(modBus, ForgeRegistries.BLOCKS);
		DeferredRegister<Item> items = registerRegister(modBus, ForgeRegistries.ITEMS);
		DeferredRegister<Effect> effects = registerRegister(modBus, ForgeRegistries.POTIONS);
		DeferredRegister<Potion> potions = registerRegister(modBus, ForgeRegistries.POTION_TYPES);
		DeferredRegister<PaintingType> paintings = registerRegister(modBus, ForgeRegistries.PAINTING_TYPES);
		
		// register objects via deferred registers
		this.froglinEggBlock = blocks.register(Names.FROGLIN_EGG,
			() -> new FroglinEggBlock(
				AbstractBlock.Properties.create(Material.OCEAN_PLANT)
					.notSolid()
					.doesNotBlockMovement()
					.tickRandomly()
					.zeroHardnessAndResistance()
					.sound(SoundType.SLIME)));
		
		items.register(Names.FROGLIN_SPAWN_EGG, () ->
			new SpawnEggItem(this.froglin, 0x001e00, 0xbdcbd8, new Item.Properties().group(ItemGroup.MISC)));
		
		items.register(Names.FROGLIN_EGG, () ->
			new BlockItem(this.froglinEggBlock.get(), new Item.Properties().group(ItemGroup.BREWING)));
		
		RegistryObject<HealthinessEffect> healthinessEffect = effects.register(Names.HEALTHINESS, () ->
			new HealthinessEffect(EffectType.BENEFICIAL, 0x032f00));
		
		items.register(Names.FROGLIN_EYE, () ->
			new Item(
				new Item.Properties()
					.group(ItemGroup.BREWING)
					.food(new Food.Builder()
						.fastToEat()
						.hunger(1)
						.saturation(0.1F)
						.build()
						)));
		
		this.healthinessTonicItem = items.register(Names.HEALTHINESS_TONIC, () ->
			new HealthinessTonicItem(
				new Item.Properties()
					.group(ItemGroup.BREWING)
					.maxStackSize(3)
					.food(
						new Food.Builder()
							.effect(() -> new EffectInstance(healthinessEffect.get()), 1F)
							.setAlwaysEdible()
							.build()
							)));
		
		RegistryObject<Effect> frogsMightEffect = effects.register(Names.FROGS_MIGHT, () ->
			new PublicEffect(EffectType.BENEFICIAL, 0xd4f6bc)
				.addAttributesModifier(ForgeMod.SWIM_SPEED.get(), "1c2a2b4d-7c8e-473c-a6c3-d68af3d47704", 0.4F, AttributeModifier.Operation.MULTIPLY_TOTAL));
		
		// name args in Potion constructor is used for translation key
		// convention is to use the same name for normal/long/strong potions
		String frogsMightTranslationKey = makePotionTranslationKey(Names.FROGS_MIGHT);
		this.frogsMightPotion = potions.register(Names.FROGS_MIGHT, () ->
			new Potion(frogsMightTranslationKey,
				new EffectInstance(frogsMightEffect.get(), 3600)));
		this.longFrogsMightPotion = potions.register(Names.LONG_FROGS_MIGHT, () ->
			new Potion(frogsMightTranslationKey,
				new EffectInstance(frogsMightEffect.get(), 9600)));
		this.strongFrogsMightPotion = potions.register(Names.STRONG_FROGS_MIGHT, () ->
			new Potion(frogsMightTranslationKey,
				new EffectInstance(frogsMightEffect.get(), 1800, 1)));
		
		String frogChampionTranslationKey = makePotionTranslationKey(Names.FROG_CHAMPION);
		this.frogChampionPotion = potions.register(Names.FROG_CHAMPION, () ->
			new Potion(frogChampionTranslationKey,
				new EffectInstance(Effects.JUMP_BOOST, 1800, 2),
				new EffectInstance(frogsMightEffect.get(), 1800)));
		this.longFrogChampionPotion = potions.register(Names.LONG_FROG_CHAMPION, () ->
			new Potion(frogChampionTranslationKey,
				new EffectInstance(Effects.JUMP_BOOST, 4800, 2),
				new EffectInstance(frogsMightEffect.get(), 4800)));
		this.strongFrogChampionPotion = potions.register(Names.STRONG_FROG_CHAMPION, () ->
			new Potion(frogChampionTranslationKey,
				new EffectInstance(Effects.JUMP_BOOST, 900, 3),
				new EffectInstance(frogsMightEffect.get(), 900, 1)));
		
		paintings.register(Names.FROGLIN, () -> new PaintingType(32,32));
		
		// manually register entity types
		modBus.addGenericListener(EntityType.class, this::onRegisterEntityTypes);
		
		// other event listeners
		modBus.addListener(this::onCommonSetup);
		
		forgeBus.addListener(EventPriority.HIGH, this::addThingsToBiomeOnBiomeLoad);

		// add listeners to clientjar events separately
		if (FMLEnvironment.dist == Dist.CLIENT)
		{
			ClientEvents.subscribeClientEvents(modBus, forgeBus);
		}
	}
	
	public static String makePotionTranslationKey(String potionName)
	{
		return MODID + "." + potionName;
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
		EntitySpawnPlacementRegistry.register(this.froglin, PlacementType.IN_WATER, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, FroglinEntity::canRandomlySpawn);
		BrewingRecipeRegistry.addRecipe(new PotionToItemRecipe(
			Items.POTION,
			potionTag("froglins:healthiness_bases"),
			itemTag("froglins:healthiness_catalysts"),
			this.healthinessTonicItem.get()));
		BrewingRecipeRegistry.addRecipe(new PotionUpgradeRecipe(
			potionTag("froglins:frogs_might_bases"),
			itemTag("froglins:frogs_might_catalysts"),
			this.frogsMightPotion.get()));
		BrewingRecipeRegistry.addRecipe(new PotionUpgradeRecipe(
			potionTag("froglins:frogs_might"),
			REDSTONE_DUST_TAG,
			this.longFrogsMightPotion.get()));
		BrewingRecipeRegistry.addRecipe(new PotionUpgradeRecipe(
			potionTag("froglins:frogs_might"),
			GLOWSTONE_DUST_TAG,
			this.longFrogsMightPotion.get()));
		BrewingRecipeRegistry.addRecipe(new PotionUpgradeRecipe(
			potionTag("froglins:frog_champion_bases"),
			itemTag("froglins:frog_champion_catalysts"),
			this.frogChampionPotion.get()));
		BrewingRecipeRegistry.addRecipe(new PotionUpgradeRecipe(
			potionTag("froglins:frog_champion"),
			REDSTONE_DUST_TAG,
			this.longFrogChampionPotion.get()));
		BrewingRecipeRegistry.addRecipe(new PotionUpgradeRecipe(
			potionTag("froglins:frog_champion"),
			GLOWSTONE_DUST_TAG,
			this.longFrogChampionPotion.get()));
	}
	
	public void addThingsToBiomeOnBiomeLoad(BiomeLoadingEvent event)
	{
		RegistryKey<Biome> key = RegistryKey.getOrCreateKey(Registry.BIOME_KEY, event.getName());
		
		Set<BiomeDictionary.Type> types = BiomeDictionary.getTypes(key);
		
		// avoid cold, dry, or ocean biomes
		if (!types.contains(BiomeDictionary.Type.COLD) && !types.contains(BiomeDictionary.Type.OCEAN) && !types.contains(BiomeDictionary.Type.DRY))
		{
			// only spawn in overworld for now
			if (types.contains(BiomeDictionary.Type.OVERWORLD))
			{
				int weight =
					types.contains(BiomeDictionary.Type.SWAMP) ? 50
						: types.contains(BiomeDictionary.Type.RIVER) ? 15
						: types.contains(BiomeDictionary.Type.WET) ? 50
						: 5;

				event.getSpawns()
					.getSpawner(EntityClassification.MONSTER)
					.add(new Spawners(this.froglin, weight, 1, 4));
			}
		}
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
	
	public static ITag<Item> itemTag(String id)
	{
		return ItemTags.createOptional(new ResourceLocation(id));
	}
	
	public static ITag<Potion> potionTag(String id)
	{
		return ForgeTagHandler.createOptionalTag(ForgeRegistries.POTION_TYPES, new ResourceLocation(id));
	}
}