package commoble.froglins;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import commoble.froglins.data.BiomeSet;
import commoble.froglins.data.FroglinSpawnEntry;
import commoble.froglins.util.ConfigHelper;
import commoble.froglins.util.ConfigHelper.ConfigValueListener;
import commoble.froglins.util.TomlConfigOps;
import net.minecraftforge.common.ForgeConfigSpec;

public class CommonConfig
{
	private static final Logger LOGGER = LogManager.getLogger();
	
	public final ConfigValueCache<List<FroglinSpawnEntry>> spawns;
	
	public CommonConfig(ForgeConfigSpec.Builder builder, ConfigHelper.Subscriber subscriber)
	{
		builder.push("spawning");
		
		builder.comment(
			"This is a list of spawner data that will used to add spawn entries to biomes as they load.",
			"CHANGES TO THIS CONFIG REQUIRE A SERVER RESTART TO TAKE EFFECT",
			"Each entry has the following fields:",
			"weight -- Spawning weight in the biome; higher number == spawn more often relative to other mobs (zombies have 100, usually)",
			"min -- Minimum group size to attempt to spawn (typically 1)",
			"max -- Maximum group size to attempt to spawn (typically 4)",
			"include -- A mandatory list of biome identifiers (more info below)",
			"exclude -- An optional list of biome identifiers",
			"require -- An optional list of biome identifiers",
			"For each biome defined in the include list, this spawn entry will be added to that biome as they load, UNLESS:",
			"  the biome is in the exclude list, or the require list is non-empty and the biome is not in the require list",
			"A biome identifier can either be a resource location identifying a single biome (e.g. minecraft:swamp)",
			"OR a Forge BiomeDictionary identifier identifying a set of biomes, in all caps (e.g. WET)",
			"A list of the default BiomeDictionary keys is available here:",
			"https://github.com/MinecraftForge/MinecraftForge/blob/1.16.x/src/main/java/net/minecraftforge/common/BiomeDictionary.java",
			"Be aware that adding multiple spawn entries that permit spawning in the same biome will cause a spawn entry",
			"  to be added to that biome twice, likely causing froglins to spawn more often"
			);
		
		this.spawns = makeConfigValueCache(builder, subscriber, "entries", SPAWN_LIST_CODEC, DEFAULT_SPAWN_ENTRIES);
		
		builder.pop();
	}
	
	public static <T> ConfigValueCache<T> makeConfigValueCache(ForgeConfigSpec.Builder builder, ConfigHelper.Subscriber subscriber,
		String name,
		Codec<T> codec,
		T defaultObject)
	{
		DataResult<Object> encodeResult = codec.encodeStart(TomlConfigOps.INSTANCE, defaultObject);
		Object encodedObject = encodeResult.getOrThrow(false, s -> LOGGER.error("Unable to encode default value: ", s));
		ConfigValueListener<Object> listener = subscriber.subscribe(builder.define(name, encodedObject));
		return new ConfigValueCache<>(listener, codec, defaultObject, encodedObject);
	}
	
	static class ConfigValueCache<T> implements Supplier<T>
	{
		
		private @Nonnull final ConfigValueListener<Object> listener;
		private @Nonnull final Codec<T> codec;
		private @Nonnull Object cachedObject;
		private @Nonnull T parsedObject;
		private @Nonnull T defaultObject;
		public ConfigValueCache(ConfigValueListener<Object> listener, Codec<T> codec, T defaultObject, Object encodedDefaultObject)
		{
			this.listener = listener;
			this.codec = codec;
			this.defaultObject = defaultObject;
			this.parsedObject = defaultObject;
			this.cachedObject = encodedDefaultObject;
		}

		@Override
		@Nonnull
		public T get()
		{
			Object freshObject = this.listener.get();
			if (!Objects.equals(this.cachedObject, freshObject))
			{
				this.cachedObject = freshObject;
				this.parsedObject = this.getReparsedObject(freshObject);
			}
			return this.parsedObject;
		}
		
		private T getReparsedObject(Object obj)
		{
			DataResult<T> parseResult = this.codec.parse(TomlConfigOps.INSTANCE, obj);
			return parseResult.get().map(
				result -> result,
				failure ->
				{
					LOGGER.error("Config failure: Using default config value due to parsing error", failure.message());
					return this.defaultObject;
				});
		}
	}
	
	public static final Codec<List<FroglinSpawnEntry>> SPAWN_LIST_CODEC = FroglinSpawnEntry.CODEC.listOf();
	static final List<FroglinSpawnEntry> DEFAULT_SPAWN_ENTRIES = Lists.newArrayList(
		new FroglinSpawnEntry(50,1,4,
			new BiomeSet(ImmutableList.of("SWAMP")),
			new BiomeSet(ImmutableList.of("OCEAN", "DRY", "COLD")),
			new BiomeSet(ImmutableList.of("OVERWORLD"))),
		new FroglinSpawnEntry(15,1,4,
			new BiomeSet(ImmutableList.of("RIVER")),
			new BiomeSet(ImmutableList.of("OCEAN", "DRY", "COLD", "SWAMP")),
			new BiomeSet(ImmutableList.of("OVERWORLD"))),
		new FroglinSpawnEntry(50,1,4,
			new BiomeSet(ImmutableList.of("WET")),
			new BiomeSet(ImmutableList.of("OCEAN", "DRY", "COLD", "SWAMP", "RIVER")),
			new BiomeSet(ImmutableList.of("OVERWORLD"))));
	
	
}
