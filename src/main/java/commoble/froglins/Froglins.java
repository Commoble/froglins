package commoble.froglins;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import commoble.froglins.client.ClientEvents;
import commoble.froglins.data.FroglinSpawnEntry;
import commoble.froglins.util.ConfigHelper;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.SpawnPlacements.Type;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.decoration.Motive;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.brewing.BrewingRecipe;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.crafting.NBTIngredient;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

@Mod(Froglins.MODID)
public class Froglins
{
	public static final String MODID = "froglins"; // use this same string everywhere you need a modid	
	public static Froglins INSTANCE;
	
	public static final Logger LOGGER = LogManager.getLogger();
	
	public static final Tag<Block> DIGGABLE_TAG = BlockTags.bind("froglins:diggable");
	public static final Tag<EntityType<?>> EDIBLE_FISH_TAG = EntityTypeTags.createOptional(new ResourceLocation("froglins:edible_fish"));
	public static final Tag<EntityType<?>> EDIBLE_ANIMALS_TAG = EntityTypeTags.createOptional(new ResourceLocation("froglins:edible_animals"));
	
	public final ServerConfig serverConfig;
	public final CommonConfig commonConfig;
	
	public final RegistryObject<FroglinEggBlock> froglinEggBlock;
	public final RegistryObject<SpawnEggItem> froglinSpawnEggItem;
	public final RegistryObject<Item> froglinEyeItem;
	public final RegistryObject<BlockItem> froglinEggItem;
	public final RegistryObject<HealthinessTonicItem> healthinessTonicItem;
	public final RegistryObject<EntityType<FroglinEntity>> froglinEntityType;
	public final RegistryObject<Potion> frogsMightPotion;
	public final RegistryObject<Potion> longFrogsMightPotion;
	public final RegistryObject<Potion> strongFrogsMightPotion;
	public final RegistryObject<Potion> frogChampionPotion;
	public final RegistryObject<Potion> longFrogChampionPotion;
	public final RegistryObject<Potion> strongFrogChampionPotion;
	
	public final RegistryObject<SoundEvent> froglinSoundAmbient;
	public final RegistryObject<SoundEvent> froglinSoundAngry;
	public final RegistryObject<SoundEvent> froglinSoundAttack;
	public final RegistryObject<SoundEvent> froglinSoundDeath;
	public final RegistryObject<SoundEvent> froglinSoundHurt;
	public final RegistryObject<SoundEvent> froglinSoundStep;
	
	
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
		DeferredRegister<EntityType<?>> entityTypes = registerRegister(modBus, ForgeRegistries.ENTITIES);
		DeferredRegister<MobEffect> effects = registerRegister(modBus, ForgeRegistries.MOB_EFFECTS);
		DeferredRegister<Potion> potions = registerRegister(modBus, ForgeRegistries.POTIONS);
		DeferredRegister<Motive> paintings = registerRegister(modBus, ForgeRegistries.PAINTING_TYPES);
		DeferredRegister<SoundEvent> sounds = registerRegister(modBus, ForgeRegistries.SOUND_EVENTS);
		
		this.froglinEntityType = entityTypes.register(Names.FROGLIN, () ->
			EntityType.Builder.of(FroglinEntity::new, MobCategory.MONSTER)
				.build(new ResourceLocation(MODID, Names.FROGLIN).toString()));
		
		// register objects via deferred registers
		this.froglinEggBlock = blocks.register(Names.FROGLIN_EGG,
			() -> new FroglinEggBlock(
				BlockBehaviour.Properties.of(Material.WATER_PLANT)
					.noOcclusion()
					.noCollission()
					.randomTicks()
					.instabreak()
					.sound(SoundType.SLIME_BLOCK)));
		
		this.froglinEggItem = items.register(Names.FROGLIN_EGG, () ->
			new BlockItem(this.froglinEggBlock.get(), new Item.Properties().tab(CreativeModeTab.TAB_BREWING)));
		
		this.froglinSpawnEggItem = items.register(Names.FROGLIN_SPAWN_EGG, () ->
			new ForgeSpawnEggItem(this.froglinEntityType, 0x001e00, 0xbdcbd8, new Item.Properties().tab(CreativeModeTab.TAB_MISC)));
		
		RegistryObject<HealthinessEffect> healthinessEffect = effects.register(Names.HEALTHINESS, () ->
			new HealthinessEffect(MobEffectCategory.BENEFICIAL, 0x032f00));
		
		this.froglinEyeItem = items.register(Names.FROGLIN_EYE, () ->
			new Item(
				new Item.Properties()
					.tab(CreativeModeTab.TAB_BREWING)
					.food(new FoodProperties.Builder()
						.fast()
						.nutrition(1)
						.saturationMod(0.1F)
						.build()
						)));
		
		this.healthinessTonicItem = items.register(Names.HEALTHINESS_TONIC, () ->
			new HealthinessTonicItem(
				new Item.Properties()
					.tab(CreativeModeTab.TAB_BREWING)
					.stacksTo(1)
					.craftRemainder(Items.GLASS_BOTTLE)
					.food(
						new FoodProperties.Builder()
							.effect(() -> new MobEffectInstance(healthinessEffect.get(), 1), 1F)
							.alwaysEat()
							.build()
							)));
		
		RegistryObject<MobEffect> frogsMightEffect = effects.register(Names.FROGS_MIGHT, () ->
			new PublicEffect(MobEffectCategory.BENEFICIAL, 0xd4f6bc)
				.addAttributeModifier(ForgeMod.SWIM_SPEED.get(), "1c2a2b4d-7c8e-473c-a6c3-d68af3d47704", 0.4F, AttributeModifier.Operation.MULTIPLY_TOTAL));
		
		// name args in Potion constructor is used for translation key
		// convention is to use the same name for normal/long/strong potions
		String frogsMightTranslationKey = makePotionTranslationKey(Names.FROGS_MIGHT);
		this.frogsMightPotion = potions.register(Names.FROGS_MIGHT, () ->
			new Potion(frogsMightTranslationKey,
				new MobEffectInstance(frogsMightEffect.get(), 3600)));
		this.longFrogsMightPotion = potions.register(Names.LONG_FROGS_MIGHT, () ->
			new Potion(frogsMightTranslationKey,
				new MobEffectInstance(frogsMightEffect.get(), 9600)));
		this.strongFrogsMightPotion = potions.register(Names.STRONG_FROGS_MIGHT, () ->
			new Potion(frogsMightTranslationKey,
				new MobEffectInstance(frogsMightEffect.get(), 1800, 1)));
		
		String frogChampionTranslationKey = makePotionTranslationKey(Names.FROG_CHAMPION);
		this.frogChampionPotion = potions.register(Names.FROG_CHAMPION, () ->
			new Potion(frogChampionTranslationKey,
				new MobEffectInstance(MobEffects.JUMP, 1800, 2),
				new MobEffectInstance(frogsMightEffect.get(), 1800)));
		this.longFrogChampionPotion = potions.register(Names.LONG_FROG_CHAMPION, () ->
			new Potion(frogChampionTranslationKey,
				new MobEffectInstance(MobEffects.JUMP, 4800, 2),
				new MobEffectInstance(frogsMightEffect.get(), 4800)));
		this.strongFrogChampionPotion = potions.register(Names.STRONG_FROG_CHAMPION, () ->
			new Potion(frogChampionTranslationKey,
				new MobEffectInstance(MobEffects.JUMP, 900, 3),
				new MobEffectInstance(frogsMightEffect.get(), 900, 1)));
		
		paintings.register(Names.FROGLIN, () -> new Motive(32,32));
		
		this.froglinSoundAmbient = registerSound(sounds, Names.FROGLIN_SOUND_AMBIENT);
		this.froglinSoundAngry = registerSound(sounds, Names.FROGLIN_SOUND_ANGRY);
		this.froglinSoundAttack = registerSound(sounds, Names.FROGLIN_SOUND_ATTACK);
		this.froglinSoundDeath = registerSound(sounds, Names.FROGLIN_SOUND_DEATH);
		this.froglinSoundHurt = registerSound(sounds, Names.FROGLIN_SOUND_HURT);
		this.froglinSoundStep = registerSound(sounds, Names.FROGLIN_SOUND_STEP);
		
		// other event listeners
		modBus.addListener(this::onRegisterAttributes);
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
	
	private void onRegisterAttributes(EntityAttributeCreationEvent event)
	{
		event.put(this.froglinEntityType.get(), FroglinEntity.createAttributes().build());
	}
	
	private void onCommonSetup(FMLCommonSetupEvent event)
	{
		event.enqueueWork(this::afterCommonSetup);
	}
	
	// runs on the main thread after common setup event
	// stuff can safely be put into vanilla maps here
	private void afterCommonSetup()
	{
		SpawnPlacements.register(this.froglinEntityType.get(), Type.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, FroglinEntity::canRandomlySpawn);
		
		// add spawn egg behaviours to dispenser
//		DefaultDispenseItemBehavior spawnEggBehavior = new DefaultDispenseItemBehavior()
//		{
//			/**
//			 * Dispense the specified stack, play the dispense sound and spawn particles.
//			 */
//			@Override
//			public ItemStack execute(BlockSource source, ItemStack stack)
//			{
//				Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);
//				EntityType<?> entitytype = ((SpawnEggItem) stack.getItem()).getType(stack.getTag());
//				entitytype.spawn(source.getLevel(), stack, (Player) null, source.getPos().relative(direction), MobSpawnType.DISPENSER, direction != Direction.UP, false);
//				stack.shrink(1);
//				return stack;
//			}
//		};
		
//		DispenserBlock.registerBehavior(this.froglinSpawnEggItem.get(), spawnEggBehavior);
		
		BrewingRecipeRegistry.addRecipe(potionToItemRecipe(
			Items.POTION,
			Potions.AWKWARD,
			this.froglinEyeItem.get(),
			this.healthinessTonicItem.get()));
		PotionBrewing.addMix(Potions.AWKWARD, this.froglinEggItem.get(), this.frogsMightPotion.get());
		PotionBrewing.addMix(this.frogsMightPotion.get(), Items.REDSTONE, this.longFrogsMightPotion.get());
		PotionBrewing.addMix(this.frogsMightPotion.get(), Items.GLOWSTONE_DUST, this.strongFrogsMightPotion.get());
		PotionBrewing.addMix(Potions.STRONG_LEAPING, this.froglinEggItem.get(), this.frogChampionPotion.get());
		PotionBrewing.addMix(this.frogChampionPotion.get(), Items.REDSTONE, this.longFrogChampionPotion.get());
		PotionBrewing.addMix(this.frogsMightPotion.get(), Items.GLOWSTONE_DUST, this.strongFrogChampionPotion.get());
	}
	
	private void addThingsToBiomeOnBiomeLoad(BiomeLoadingEvent event)
	{
		ResourceKey<Biome> key = ResourceKey.create(Registry.BIOME_REGISTRY, event.getName());
		List<SpawnerData> spawners = event.getSpawns().getSpawner(MobCategory.MONSTER);
		
		for (FroglinSpawnEntry entry : this.commonConfig.spawns.get())
		{
			entry.addToBiomeIfPermitted(key, spawners);
		}
	}
	
	// creates a DeferredRegister and subscribes it to the mod bus
	private static <T extends IForgeRegistryEntry<T>> DeferredRegister<T> registerRegister(IEventBus modBus, IForgeRegistry<T> registry)
	{
		DeferredRegister<T> register = DeferredRegister.create(registry, MODID);
		register.register(modBus);
		return register;
	}
	
	private static BrewingRecipe potionToItemRecipe(Item inputPotionItem, Potion inputPotion, Item catalyst, Item output)
	{
		Ingredient inputPotionIngredient = NBTIngredient.of(PotionUtils.setPotion(new ItemStack(inputPotionItem), inputPotion));
		Ingredient catalystIngredient = Ingredient.of(catalyst);
		ItemStack result = new ItemStack(output);
		return new BrewingRecipe(inputPotionIngredient, catalystIngredient, result);
	}
	
	private static RegistryObject<SoundEvent> registerSound(DeferredRegister<SoundEvent> sounds, String name)
	{
		return sounds.register(name, () -> new SoundEvent(new ResourceLocation(MODID, name)));
	}
}