package net.commoble.froglins;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

public record ServerConfig(
	IntValue froglinFullnessFromKill,
	IntValue froglinDigFrequency,
	IntValue froglinEggFrequency,
	BooleanValue persistentFroglinsLayPersistentFroglinEggs,
	BooleanValue playersPlacePersistentFroglinEggs)
{
	
	public static ServerConfig create(ModConfigSpec.Builder builder)
	{
		return new ServerConfig(
		builder.push("Froglin Behaviour")
			.comment("Time in ticks after a froglin kills something before it becomes hungry again")
			.translation("froglins.froglin_fullness_from_kill")
			.defineInRange("froglin_fullness_from_kill", 18000, 0, Integer.MAX_VALUE),
		builder
			.comment("How often (every n ticks) a digging froglin attempts to dig downwards. Increasing this value reduces the rate at which froglins check the blockstates below them")
			.translation("froglins.froglin_dig_frequency")
			.defineInRange("froglin_dig_frequency", 20, 1, Integer.MAX_VALUE),
		builder
			.comment("Average frequency (roughly every n ticks) at which a well-fed froglin attempts to lay an egg in water")
			.translation("froglins.froglin_egg_frequency")
			.defineInRange("froglin_egg_frequency", 100, 1, Integer.MAX_VALUE),
		
		builder.pop().push("Froglin Eggs")
			.comment("Froglins marked as persistent will lay eggs that hatch into persistent froglins")
			.translation("froglins.persistent_froglins_lay_persistent_froglin_eggs")
			.define("persistent_froglins_lay_persistent_froglin_eggs", false),
		builder
			.comment("Froglin eggs placed by players hatch into persistent froglins")
			.translation("froglins.players_place_persistent_froglin_eggs")
			.define("players_place_persistent_froglin_eggs", false));
	}
}
