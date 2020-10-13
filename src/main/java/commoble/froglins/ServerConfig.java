package commoble.froglins;

import commoble.froglins.util.ConfigHelper;
import commoble.froglins.util.ConfigHelper.ConfigValueListener;
import net.minecraftforge.common.ForgeConfigSpec;

public class ServerConfig
{
	public final ConfigValueListener<Integer> froglinFullnessFromKill;
	public final ConfigValueListener<Integer> froglinDigFrequency;
	
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
		
		builder.pop();
	}
}
