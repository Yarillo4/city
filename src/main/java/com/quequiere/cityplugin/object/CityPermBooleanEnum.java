package com.quequiere.cityplugin.object;

public enum CityPermBooleanEnum
{
	pvp("PVP","Toggle pvp"),
	interactEntityLiving("InteractEntity","Interact with entity"),
	spawnCreature("Creature","Toggle spawn of creature like animals, villager ..."), //Animals,Villager EntityAgeable
	spawnMob("Mob","Toggle spawn of mobs like Creeper, Zombie ..."),
	naturalSpawn("naturalSpawn","Toggle the possibilie for entities to spawn naturaly in this zone."); 
	
	public String overText;
	public String displayName;
	
	CityPermBooleanEnum(String displayName,String overText)
	{
		this.displayName=displayName;
		this.overText=overText;
	}
	
}
