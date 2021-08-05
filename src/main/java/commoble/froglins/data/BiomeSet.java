package commoble.froglins.data;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;

public class BiomeSet
{
	static final Logger LOGGER = LogManager.getLogger();
	static final BiomeSet EMPTY = new BiomeSet(ImmutableList.of());
	
	static final Codec<BiomeSet> CODEC = Codec.STRING.listOf()
		.xmap(BiomeSet::new, BiomeSet::getRaws);
	

	private final @Nonnull List<String> raws; public List<String> getRaws() {return this.raws;}
	private @Nullable Set<ResourceKey<Biome>> biomes;
	
	public BiomeSet(@Nonnull List<String> raws)
	{
		this.raws = raws;
	}
	
	@Nonnull
	public Set<ResourceKey<Biome>> getBiomes()
	{
		if (this.biomes == null)
		{
			this.biomes = getBiomePredicate(this.raws);
		}
		return this.biomes;
	}
	
	static Set<ResourceKey<Biome>> getBiomePredicate(List<String> strings)
	{
		Set<ResourceKey<Biome>> biomes = new HashSet<>();
		for (String s : strings)
		{
			String biomeDictKey = s.toUpperCase();
			if (biomeDictKey.equals(s)) // valid biomedict format
			{
				BiomeDictionary.Type type = BiomeDictionary.Type.getType(biomeDictKey);
				Set<ResourceKey<Biome>> biomeDictBiomes = BiomeDictionary.getBiomes(type);
				if (biomeDictBiomes.isEmpty())
				{
					LOGGER.error("Error parsing froglin spawn configs: No biomes registered to biomedict key {}", biomeDictKey);
				}
				else
				{
					biomes.addAll(biomeDictBiomes);
				}
			}
			else
			{
				try
				{
					ResourceLocation biomeLocation = new ResourceLocation(s); // can throw RL exception
					biomes.add(ResourceKey.create(Registry.BIOME_REGISTRY, biomeLocation));
				}
				catch(ResourceLocationException e)
				{
					LOGGER.error("Error parsing froglin spawn configs: bad resource location", e);
				}
			}
		}
		return biomes;
	}
		
	static List<String> getBiomeNames(Set<ResourceKey<Biome>> biomes)
	{
		return biomes.stream()
			.map(key -> key.location().toString())
			.collect(Collectors.toList());
	}
}
