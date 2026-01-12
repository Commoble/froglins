package net.commoble.froglins;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.commoble.froglins.util.ConfigHelper;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.OffsetType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.brewing.BrewingRecipe;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(Froglins.MODID)
public class Froglins
{
	public static final String MODID = "froglins"; // use this same string everywhere you need a modid
	
	public static final Logger LOGGER = LogManager.getLogger();
	
	public static final TagKey<Block> DIGGABLE_BLOCKS_TAG = TagKey.create(Registries.BLOCK, id("diggable"));
	public static final TagKey<EntityType<?>> EDIBLE_FISH_TAG = TagKey.create(Registries.ENTITY_TYPE, id("edible_fish"));
	public static final TagKey<EntityType<?>> EDIBLE_ANIMALS_TAG = TagKey.create(Registries.ENTITY_TYPE, id("edible_animals"));
	public static final TagKey<MobEffect> CURABLE_AILMENTS_TAG = TagKey.create(Registries.MOB_EFFECT, id("frogade_curable_effects"));
	public static final TagKey<Item> FROGADE_CATALYSTS = TagKey.create(Registries.ITEM, id("frogade_catalysts"));
	
	public static final ServerConfig SERVERCONFIG = ConfigHelper.register(MODID, ModConfig.Type.SERVER, ServerConfig::create);
	
	// create and register deferred registers
	private static final DeferredRegister<ConsumeEffect.Type<?>> CONSUME_EFFECTS = defreg(Registries.CONSUME_EFFECT_TYPE);
	private static final DeferredRegister.Blocks BLOCKS = defreg(DeferredRegister::createBlocks);
	private static final DeferredRegister.Items ITEMS = defreg(DeferredRegister::createItems);
	private static final DeferredRegister.Entities ENTITY_TYPES = defreg(DeferredRegister::createEntities);
	private static final DeferredRegister<MobEffect> EFFECTS = defreg(Registries.MOB_EFFECT);
	private static final DeferredRegister<Potion> POTIONS = defreg(Registries.POTION);
	private static final DeferredRegister<SoundEvent> SOUNDS = defreg(Registries.SOUND_EVENT);
	
	public static final DeferredHolder<ConsumeEffect.Type<?>, ConsumeEffect.Type<FrogadeConsumeEffect>> FROGADE_CONSUME_EFFECT = CONSUME_EFFECTS.register(Names.FROGADE, () -> new ConsumeEffect.Type<>(FrogadeConsumeEffect.CODEC, FrogadeConsumeEffect.STREAM_CODEC));
	
	public static final DeferredHolder<EntityType<?>, EntityType<FroglinEntity>> FROGLIN = ENTITY_TYPES.registerEntityType(
		Names.FROGLIN,
		FroglinEntity::new,
		MobCategory.MONSTER);
	
	public static final DeferredBlock<FroglinEggBlock> FROGLIN_EGG_BLOCK = registerBlockItem(Names.FROGLIN_EGG,
		FroglinEggBlock::new,
		props -> props
			.mapColor(MapColor.WATER)
			.offsetType(OffsetType.XYZ)
			.noOcclusion()
			.noCollision()
			.randomTicks()
			.instabreak()
			.sound(SoundType.SLIME_BLOCK),
		BlockItem::new,
		UnaryOperator.identity());
	
	public static final DeferredHolder<MobEffect, MobEffect> FROGS_MIGHT_EFFECT = EFFECTS.register(Names.FROGS_MIGHT, () ->
		new PublicEffect(MobEffectCategory.BENEFICIAL, 2086401)
			.addAttributeModifier(NeoForgeMod.SWIM_SPEED, id(Names.FROGS_MIGHT), 0.4F, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

	
	public static final DeferredHolder<Item, SpawnEggItem> FROGLIN_SPAWN_EGG_ITEM = ITEMS.registerItem(Names.FROGLIN_SPAWN_EGG,
		SpawnEggItem::new,
		props -> props.spawnEgg(FROGLIN.get()));
	
	public static final DeferredHolder<Item, Item> FROGLIN_EYE_ITEM = ITEMS.registerItem(Names.FROGLIN_EYE,
		Item::new,
		props -> props
			.food(
				new FoodProperties.Builder()
					.nutrition(1)
					.build(),
				Consumables.defaultFood()
					.consumeSeconds(0.8F)
					.build()));
	
	public static final DeferredHolder<Item, FrogadeItem> FROGADE_ITEM = ITEMS.registerItem(Names.FROGADE,
		FrogadeItem::new,
		props -> props
			.usingConvertsTo(Items.GLASS_BOTTLE)
			.craftRemainder(Items.GLASS_BOTTLE)
			.food(
				new FoodProperties.Builder()
					.alwaysEdible()
					.build(),
				Consumables.defaultDrink().consumeSeconds(0.8F).onConsume(FrogadeConsumeEffect.INSTANCE).build()));
	public static final String FROGS_MIGHT_TRANSLATION_KEY = makePotionTranslationKey(Names.FROGS_MIGHT);
	public static final String FROG_CHAMPION_TRANSLATION_KEY = makePotionTranslationKey(Names.FROG_CHAMPION);
	
	public static final DeferredHolder<Potion, Potion> FROGS_MIGHT_POTION = POTIONS.register(Names.FROGS_MIGHT, () ->
		new Potion(FROGS_MIGHT_TRANSLATION_KEY,
			new MobEffectInstance(FROGS_MIGHT_EFFECT, 3600)));
	public static final DeferredHolder<Potion, Potion> LONG_FROGS_MIGHT_POTION = POTIONS.register(Names.LONG_FROGS_MIGHT, () ->
		new Potion(FROGS_MIGHT_TRANSLATION_KEY,
			new MobEffectInstance(FROGS_MIGHT_EFFECT, 9600)));
	public static final DeferredHolder<Potion, Potion> STRONG_FROGS_MIGHT_POTION = POTIONS.register(Names.STRONG_FROGS_MIGHT, () ->
		new Potion(FROGS_MIGHT_TRANSLATION_KEY,
			new MobEffectInstance(FROGS_MIGHT_EFFECT, 1800, 1)));
	public static final DeferredHolder<Potion, Potion> FROG_CHAMPION_POTION = POTIONS.register(Names.FROG_CHAMPION, () ->
		new Potion(FROG_CHAMPION_TRANSLATION_KEY,
			new MobEffectInstance(MobEffects.JUMP_BOOST, 1800, 2),
			new MobEffectInstance(FROGS_MIGHT_EFFECT, 1800)));
	public static final DeferredHolder<Potion, Potion> LONG_FROG_CHAMPION_POTION = POTIONS.register(Names.LONG_FROG_CHAMPION, () ->
		new Potion(FROG_CHAMPION_TRANSLATION_KEY,
			new MobEffectInstance(MobEffects.JUMP_BOOST, 4800, 2),
			new MobEffectInstance(FROGS_MIGHT_EFFECT, 4800)));
	public static final DeferredHolder<Potion, Potion> STRONG_FROG_CHAMPION_POTION = POTIONS.register(Names.STRONG_FROG_CHAMPION, () ->
		new Potion(FROG_CHAMPION_TRANSLATION_KEY,
			new MobEffectInstance(MobEffects.JUMP_BOOST, 900, 3),
			new MobEffectInstance(FROGS_MIGHT_EFFECT, 900, 1)));
	
	public static final DeferredHolder<SoundEvent, SoundEvent> FROGLIN_SOUND_AMBIENT = registerSound(SOUNDS, Names.FROGLIN_SOUND_AMBIENT);
	public static final DeferredHolder<SoundEvent, SoundEvent> FROGLIN_SOUND_ANGRY = registerSound(SOUNDS, Names.FROGLIN_SOUND_ANGRY);
	public static final DeferredHolder<SoundEvent, SoundEvent> FROGLIN_SOUND_ATTACK = registerSound(SOUNDS, Names.FROGLIN_SOUND_ATTACK);
	public static final DeferredHolder<SoundEvent, SoundEvent> FROGLIN_SOUND_DEATH = registerSound(SOUNDS, Names.FROGLIN_SOUND_DEATH);
	public static final DeferredHolder<SoundEvent, SoundEvent> FROGLIN_SOUND_HURT = registerSound(SOUNDS, Names.FROGLIN_SOUND_HURT);
	public static final DeferredHolder<SoundEvent, SoundEvent> FROGLIN_SOUND_STEP = registerSound(SOUNDS, Names.FROGLIN_SOUND_STEP);
	
	
	public Froglins(IEventBus modBus) // invoked by forge due to @Mod
	{
		// forge bus is for server starting events and in-game events
		IEventBus forgeBus = NeoForge.EVENT_BUS;
		
		// other event listeners
		modBus.addListener(this::onRegisterAttributes);
		modBus.addListener(this::onBuildCreativeModeTabContents);
		modBus.addListener(this::onRegisterSpawnPlacements);
		
		forgeBus.addListener(this::onRegisterBrewingRecipes);
	}
	
	public static String makePotionTranslationKey(String potionName)
	{
		return MODID + "." + potionName;
	}
	
	private void onRegisterAttributes(EntityAttributeCreationEvent event)
	{
		event.put(FROGLIN.get(), FroglinEntity.createAttributes().build());
	}
	
	private void onBuildCreativeModeTabContents(BuildCreativeModeTabContentsEvent event)
	{
		var tabKey = event.getTabKey();
		if (tabKey == CreativeModeTabs.INGREDIENTS)
		{
			event.accept(FROGLIN_EGG_BLOCK.get().asItem());
			event.accept(FROGLIN_EYE_ITEM.get());
		}
		else if (tabKey == CreativeModeTabs.FOOD_AND_DRINKS)
		{
			event.accept(FROGADE_ITEM.get());
		}
		else if (tabKey == CreativeModeTabs.SPAWN_EGGS)
		{
			event.accept(FROGLIN_SPAWN_EGG_ITEM.get());
		}
	}
	
	private void onRegisterSpawnPlacements(RegisterSpawnPlacementsEvent event)
	{
		event.register(FROGLIN.get(), SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, FroglinEntity::canRandomlySpawn, RegisterSpawnPlacementsEvent.Operation.OR);
	}
	
	private void onRegisterBrewingRecipes(RegisterBrewingRecipesEvent event)
	{
		Item froglinEggItem = FROGLIN_EGG_BLOCK.asItem();
		event.getBuilder().addRecipe(new BrewingRecipe(
			DataComponentIngredient.of(false, () -> DataComponents.POTION_CONTENTS, new PotionContents(Potions.AWKWARD), Items.POTION),
			Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(FROGADE_CATALYSTS)),
			new ItemStack(FROGADE_ITEM.get())));
		
		event.getBuilder().addMix(Potions.AWKWARD, froglinEggItem, FROGS_MIGHT_POTION);
		event.getBuilder().addMix(FROGS_MIGHT_POTION, Items.REDSTONE, LONG_FROGS_MIGHT_POTION);
		event.getBuilder().addMix(FROGS_MIGHT_POTION, Items.GLOWSTONE_DUST, STRONG_FROGS_MIGHT_POTION);
		event.getBuilder().addMix(Potions.STRONG_LEAPING, froglinEggItem, FROG_CHAMPION_POTION);
		event.getBuilder().addMix(FROG_CHAMPION_POTION, Items.REDSTONE, LONG_FROG_CHAMPION_POTION);
		event.getBuilder().addMix(FROGS_MIGHT_POTION, Items.GLOWSTONE_DUST, STRONG_FROG_CHAMPION_POTION);
	}
	
	// creates a DeferredRegister and subscribes it to the mod bus
	private static <T> DeferredRegister<T> defreg(ResourceKey<Registry<T>> registryKey)
	{
		IEventBus modBus = ModList.get().getModContainerById(MODID).get().getEventBus();
		DeferredRegister<T> register = DeferredRegister.create(registryKey, MODID);
		register.register(modBus);
		return register;
	}
	
	private static <R extends DeferredRegister<?>> R defreg(Function<String,R> defregFactory)
	{
		IEventBus modBus = ModList.get().getModContainerById(MODID).get().getEventBus();
		R register = defregFactory.apply(MODID);
		register.register(modBus);
		return register;
	}
	
	private static <BLOCK extends Block, ITEM extends BlockItem> DeferredBlock<BLOCK> registerBlockItem(
		String name,
		Function<BlockBehaviour.Properties, BLOCK> blockFactory,
		UnaryOperator<BlockBehaviour.Properties> blockPropsBuilder,
		BiFunction<? super BLOCK, Item.Properties, ITEM> itemFactory,
		UnaryOperator<Item.Properties> itemPropsBuilder)
	{
		DeferredBlock<BLOCK> blockHolder = BLOCKS.registerBlock(name, blockFactory, blockPropsBuilder);
		ITEMS.registerItem(name, props -> itemFactory.apply(blockHolder.get(), props.useBlockDescriptionPrefix()), itemPropsBuilder);
		return blockHolder;
	}
	
	private static DeferredHolder<SoundEvent, SoundEvent> registerSound(DeferredRegister<SoundEvent> sounds, String name)
	{
		return sounds.register(name, () -> SoundEvent.createVariableRangeEvent(id(name)));
	}
	
	public static Identifier id(String path)
	{
		return Identifier.fromNamespaceAndPath(MODID, path);
	}
}