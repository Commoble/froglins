## 21.11.0
* Updated to MC 1.21.11 / Neoforge 21.1.13-beta
* Improved saturation from eating froglin eyes
* Renamed Healthiness Tonic to Frogade (id is now froglins:frogade). It now stacks.
* Froglin spawn egg now has unique texture consistent with other spawn eggs
* Frog's Might potion is now greener


## 1.19.2-3.0.0.0
* Froglin spawn biomes are now configured via datapacks via forge biome modifiers instead of config files
* Froglin spawns in swamps and wetlands are configured via data/froglins/forge/biome_modifier/common_froglin_spawns.json, which by default spawns froglins in any overworld biome in the froglins:common_froglin_spawns biome tag, but not in the froglins:no_common_froglin_spawns biome tag
* Froglin spawns in rivers are configured via data/froglins/forge/biome_modifier/rare_froglin_spawns.json, which by default spawns froglins in any overworld biome in the froglins:rare_froglin_spawns biome tag, but not in the froglins:no_froglin_spawns biome tag
* Fixed a bug where consuming a healthiness tonic while inventory was full would not produce a glass bottle
* Moved the froglins:healthiness_tonic_curable_effects mob effect tag from tags/mob_effects to tags/mob_effect as minecraft loads mob effect tags itself now and that's where it loads them from

## 1.17.1-2.0.0.2
* Added datapack support to Healthiness Tonics. The effects curable by the tonic are now defined by tags at data/froglins/tags/mob_effects/healthiness_tonic_curable_effects.json
* Added integration with Absolem Jackdaw's Bad to the Bone mod; tonics now cure arthritis, bad knees, bad vision, and back pain if both mods are present

## 1.17.1-2.0.0.1
* Fixed invalid criteria in some advancements causing them to be awarded immediately (affects brew_tonic and froglin_rancher)

## 1.17.1-2.0.0.0
* Updated to 1.17.1. This update to froglins makes some changes to the way data is saved; if you try to update a save from 1.16.5 or older to this version, your save may not work properly.
* The "persistant" blockstate property in froglin egg blocks has been renamed to "persistent", as it was previously not spelled correctly.
* The config values persistant_froglins_lay_persistant_froglin_eggs and players_place_persistant_froglin_eggs have been renamed to persistent_froglins_lay_persistent_froglin_eggs and players_place_persistent_froglin_eggs respectively, as previously they were not spelled correctly.
* Froglins now have their own sound events. They still reuse vanilla sounds at the moment, but they can be overridden by resource packs.