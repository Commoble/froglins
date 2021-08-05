package commoble.froglins.data;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import commoble.froglins.Froglins;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData;

public class FroglinSpawnEntry
{
	public static final Codec<FroglinSpawnEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("weight").forGetter(FroglinSpawnEntry::getWeight),
			Codec.INT.fieldOf("min").forGetter(FroglinSpawnEntry::getMin),
			Codec.INT.fieldOf("max").forGetter(FroglinSpawnEntry::getMax),
			BiomeSet.CODEC.fieldOf("include").forGetter(FroglinSpawnEntry::getInclude),
			NullableFieldCodec.makeDefaultableField("exclude", BiomeSet.CODEC, BiomeSet.EMPTY).forGetter(FroglinSpawnEntry::getExclude),
			NullableFieldCodec.makeDefaultableField("require", BiomeSet.CODEC, BiomeSet.EMPTY).forGetter(FroglinSpawnEntry::getRequire)
		).apply(instance, FroglinSpawnEntry::new));
	
	private final int weight;	public int getWeight() {return this.weight;}
	private final int min;	public int getMin() {return this.min;}
	private final int max;	public int getMax() {return this.max;}
	private final BiomeSet include;	public BiomeSet getInclude() {return this.include;}
	private final BiomeSet exclude;	public BiomeSet getExclude() {return this.exclude;}
	private final BiomeSet require;	public BiomeSet getRequire() {return this.require;}
	private final Cache cache = new Cache();
	
	public FroglinSpawnEntry(int weight, int min, int max, BiomeSet include, BiomeSet exclude, BiomeSet require)
	{
		this.weight = weight;
		this.min = min;
		this.max = max;
		this.include = include;
		this.exclude = exclude;
		this.require = require;
	}
	
	public void addToBiomeIfPermitted(ResourceKey<Biome> biome, List<SpawnerData> spawners)
	{
		if (this.canBeAddedToBiome(biome))
		{
			spawners.add(new SpawnerData(Froglins.INSTANCE.froglin, this.weight, this.min, this.max));
		}
	}
	
	public boolean canBeAddedToBiome(ResourceKey<Biome> biome)
	{
		return this.cache.getAllowedBiomes().contains(biome);
	}
	
	class Cache
	{
		private Set<ResourceKey<Biome>> cachedBiomesToSpawnIn = null;
		
		public Set<ResourceKey<Biome>> getAllowedBiomes()
		{
			if (this.cachedBiomesToSpawnIn == null)
			{
				this.cachedBiomesToSpawnIn = makeUpdatedCache(FroglinSpawnEntry.this);
			}
			return this.cachedBiomesToSpawnIn;
		}
	}
	
	@Nonnull
	private static Set<ResourceKey<Biome>> makeUpdatedCache(FroglinSpawnEntry entry)
	{
		Set<ResourceKey<Biome>> excludes = entry.getExclude().getBiomes();
		Set<ResourceKey<Biome>> requires = entry.getRequire().getBiomes();
		Set<ResourceKey<Biome>> includes = entry.getInclude().getBiomes();
		
		Set<ResourceKey<Biome>> biomesToSpawnIn = includes.stream()
			.filter(biome -> !excludes.contains(biome))
			.filter(biome -> requires.isEmpty() || requires.contains(biome))
			.collect(Collectors.toSet());
		
		return biomesToSpawnIn;
	}
}
