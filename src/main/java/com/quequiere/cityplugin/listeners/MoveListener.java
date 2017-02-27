package com.quequiere.cityplugin.listeners;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3d;
import com.quequiere.cityplugin.CityPlugin;
import com.quequiere.cityplugin.Tools;
import com.quequiere.cityplugin.object.City;
import com.quequiere.cityplugin.object.CityChunk;
import com.quequiere.cityplugin.object.Resident;
import com.quequiere.cityplugin.visualizer.MapCityChunkVisualizer;

public class MoveListener
{

	@Listener
	public void moovePlayer(final MoveEntityEvent event, @Getter("getTargetEntity")
	final Player p)
	{

		final Location<World> previousLocation = event.getFromTransform().getLocation();
		final Location<World> newLocation = event.getToTransform().getLocation();

		final Vector3d rotationFrom = event.getFromTransform().getRotation();
		final Vector3d torationto = event.getToTransform().getRotation();

		int originalY = rotationFrom.getFloorY() % 360;
		int toY = torationto.getFloorY() % 360;

		if (!Tools.getPlayerDirection(originalY).equals(Tools.getPlayerDirection(toY)))
		{
			Resident r = Resident.fromPlayerId(p.getUniqueId());
			if (r.getCache().isDisplayMap())
			{
				MapCityChunkVisualizer.updatedisplay(p, toY, newLocation);
			}
		}

		if (previousLocation.getChunkPosition().equals(newLocation.getChunkPosition()))
		{
			return;
		}

		Resident r = Resident.fromPlayerId(p.getUniqueId());
		if (r.getCache().isDisplayMap())
		{
			MapCityChunkVisualizer.updatedisplay(p, toY, newLocation);
		}

		Chunk previousChunk = Tools.getChunk(previousLocation.getChunkPosition().getX(), previousLocation.getChunkPosition().getZ(), previousLocation.getExtent());
		Chunk newChunk = Tools.getChunk(newLocation.getChunkPosition().getX(), newLocation.getChunkPosition().getZ(), newLocation.getExtent());

		City previousCity = City.getCityFromChunk(previousChunk);
		City newCity = City.getCityFromChunk(newChunk);

		if (previousCity != null && newCity == null)
		{
			CityPlugin.sendMessageWithoutPrefix("Now leaving " + previousCity.getName() + "!", TextColors.GOLD, p);
		}
		else if (newCity != null && previousCity == null || newCity != null && previousCity != null && !newCity.equals(previousCity))
		{
			CityPlugin.sendMessageWithoutPrefix("______________[ Welcome to " + newCity.getName() + " ]______________", TextColors.GOLD, p);
		}

		if (newCity != null)
		{
			CityChunk newcc = newCity.getChunck(newChunk);
			CityChunk oldcc = newCity.getChunck(previousChunk);

			if (newcc != null && newcc.getResident() != null)
			{
				User u = Tools.getUser(newcc.getResident());
				CityPlugin.sendMessageWithoutPrefix("~~~ " + u.getName() + "'s' Land ~~~", TextColors.DARK_GREEN, p);

				if (newcc.getSellPrice() > 0)
				{
					CityPlugin.sendMessage("This land is for sale, use /cc for more info.", TextColors.AQUA, p);
				}

			}
			else if (newcc.getSellPrice() > 0)
			{
				CityPlugin.sendMessage("This land is for sale, use /cc for more info.", TextColors.AQUA, p);
			}
			else if (newcc != null && oldcc != null && newcc.getResident() == null && oldcc.getResident() != null)
			{
				CityPlugin.sendMessageWithoutPrefix("~~~ City Land ~~~", TextColors.GREEN, p);
			}
		}

	}

}
