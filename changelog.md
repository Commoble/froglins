## 1.17.1-2.0.0.0
* Updated to 1.17.1. This update to froglins makes some changes to the way data is saved; if you try to update a save from 1.16.5 or older to this version, your save may not work properly.
* The "persistant" blockstate property in froglin egg blocks has been renamed to "persistent", as it was previously not spelled correctly.
* Some changes to config files have been made:
  * All fields previously in the server config file are now found in the common config file.
    * (You can find the common config file at yourminecraftfolder/config/froglins-common.toml)
    * As the common config file is shared by all save folders, this will prevent your config values from being reset each time you make a new world.
  * The config values persistant_froglins_lay_persistant_froglin_eggs and players_place_persistant_froglin_eggs have been renamed to persistent_froglins_lay_persistent_froglin_eggs and players_place_persistent_froglin_eggs respectively, as previously they were not spelled correctly.
