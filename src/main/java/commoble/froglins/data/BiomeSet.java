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

import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;

public class BiomeSet
{
	static final Logger LOGGER = LogManager.getLogger();
	static final BiomeSet EMPTY = new BiomeSet(ImmutableList.of());
	
	static final Codec<BiomeSet> CODEC = Codec.STRING.listOf()
		.xmap(BiomeSet::new, BiomeSet::getRaws);
	

	private final @Nonnull List<String> raws; public List<String> getRaws() {return this.raws;}
	private @Nullable Set<RegistryKey<Biome>> biomes;
	
	public BiomeSet(@Nonnull List<String> raws)
	{
		this.raws = raws;
	}
	
	@Nonnull
	public Set<RegistryKey<Biome>> getBiomes()
	{
		if (this.biomes == null)
		{
			this.biomes = getBiomePredicate(this.raws);
		}
		return this.biomes;
	}
	
	static Set<RegistryKey<Biome>> getBiomePredicate(List<String> strings)
	{
		Set<RegistryKey<Biome>> biomes = new HashSet<>();
		for (String s : strings)
		{
			String biomeDictKey = s.toUpperCase();
			if (biomeDictKey.equals(s)) // valid biomedict format
			{
				BiomeDictionary.Type type = BiomeDictionary.Type.getType(biomeDictKey);
				Set<RegistryKey<Biome>> biomeDictBiomes = BiomeDictionary.getBiomes(type);
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
					biomes.add(RegistryKey.getOrCreateKey(Registry.BIOME_KEY, biomeLocation));
				}
				catch(ResourceLocationException e)
				{
					LOGGER.error("Error parsing froglin spawn configs: bad resource location", e);
				}
			}
		}
		return biomes;
	}
		
	static List<String> getBiomeNames(Set<RegistryKey<Biome>> biomes)
	{
		return biomes.stream()
			.map(key -> key.getLocation().toString())
			.collect(Collectors.toList());
	}
}
