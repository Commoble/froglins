package commoble.froglins;

import commoble.froglins.util.ConfigHelper;
import commoble.froglins.util.ConfigHelper.ConfigValueListener;
import net.minecraftforge.common.ForgeConfigSpec;

public class ServerConfig
{
	public final ConfigValueListener<Integer> froglinDespawnDelay;
	public final ConfigValueListener<Integer> froglinDigFrequency;
	
	public ServerConfig(ForgeConfigSpec.Builder builder, ConfigHelper.Subscriber subscriber)
	{
		builder.push("Froglin Behaviour");
		
		this.froglinDespawnDelay = subscriber.subscribe(builder
			.comment("Idle time in ticks before a froglin is allowed to randomly despawn (600 ticks for normal mobs). Resets if froglin attacks or is damaged")
			.translation("froglins.froglin_despawn_delay")
			.defineInRange("froglin_despawn_delay", 24000, 0, Integer.MAX_VALUE));
		this.froglinDigFrequency = subscriber.subscribe(builder
			.comment("How often (every n ticks) a digging froglin attempts to dig downwards. Increasing this value reduces the rate at which froglins check the blockstates below them")
			.translation("froglins.froglin_dig_frequency")
			.defineInRange("froglin_dig_frequency", 20, 1, Integer.MAX_VALUE));
		
		builder.pop();
	}
}
