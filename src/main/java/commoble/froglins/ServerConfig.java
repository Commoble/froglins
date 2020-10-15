package commoble.froglins;

import commoble.froglins.util.ConfigHelper;
import commoble.froglins.util.ConfigHelper.ConfigValueListener;
import net.minecraftforge.common.ForgeConfigSpec;

public class ServerConfig
{
	public final ConfigValueListener<Integer> froglinFullnessFromKill;
	public final ConfigValueListener<Integer> froglinDigFrequency;
	public final ConfigValueListener<Integer> froglinEggFrequency;
	
	public final ConfigValueListener<Boolean> persistantFroglinsLayPersistantFroglinEggs;
	public final ConfigValueListener<Boolean> playersPlacePersistantFroglinEggs;
	
	public ServerConfig(ForgeConfigSpec.Builder builder, ConfigHelper.Subscriber subscriber)
	{
		builder.push("Froglin Behaviour");
		
		this.froglinFullnessFromKill = subscriber.subscribe(builder
			.comment("Time in ticks after a froglin kills something before it becomes hungry again")
			.translation("froglins.froglin_fullness_from_kill")
			.defineInRange("froglin_fullness_from_kill", 18000, 0, Integer.MAX_VALUE));
		this.froglinDigFrequency = subscriber.subscribe(builder
			.comment("How often (every n ticks) a digging froglin attempts to dig downwards. Increasing this value reduces the rate at which froglins check the blockstates below them")
			.translation("froglins.froglin_dig_frequency")
			.defineInRange("froglin_dig_frequency", 20, 1, Integer.MAX_VALUE));
		this.froglinEggFrequency = subscriber.subscribe(builder
			.comment("Average frequency (roughly every n ticks) at which a well-fed froglin attempts to lay an egg in water")
			.translation("froglins.froglin_egg_frequency")
			.defineInRange("froglin_egg_frequency", 100, 1, Integer.MAX_VALUE));
		
		builder.pop();
		
		
		builder.push("Froglin Eggs");
		
		this.persistantFroglinsLayPersistantFroglinEggs = subscriber.subscribe(builder
			.comment("Froglins marked as persistant will lay eggs that hatch into persistant froglins")
			.translation("froglins.persistant_froglins_lay_persistant_froglin_eggs")
			.define("persistant_froglins_lay_persistant_froglin_eggs", false));
		this.playersPlacePersistantFroglinEggs = subscriber.subscribe(builder
			.comment("Froglin eggs placed by players hatch into persistant froglins")
			.translation("froglins.players_place_persistant_froglin_eggs")
			.define("players_place_persistant_froglin_eggs", false));
		
		builder.pop();
	}
}
