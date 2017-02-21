package com.quequiere.cityplugin.listeners;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import com.quequiere.cityplugin.object.Resident;

public class JoinListener
{

	@Listener
	public void spawnInfo(ClientConnectionEvent.Join event)
	{
		Player p = event.getTargetEntity();
		Resident.fromPlayerId(p.getUniqueId()).getCache().initializeCache();
	}
	
	@Listener
	public void spawnInfo(ClientConnectionEvent.Disconnect event)
	{
		Player p = event.getTargetEntity();
		Resident.fromPlayerId(p.getUniqueId()).remove();
	}
}
