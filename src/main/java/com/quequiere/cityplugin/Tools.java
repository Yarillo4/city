package com.quequiere.cityplugin;

import java.util.Optional;
import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class Tools
{
	public static Chunk getChunk(int chunkX,int chunkZ,World w)
	{
		Chunk chunk = w.getChunk(chunkX, 0, chunkZ).get();
		return chunk;
	}
	
	
	public static Chunk getChunk(Location<World> l)
	{
		int chunkX = l.getBlockPosition().getX() >> 4;
		int chunkZ = l.getBlockPosition().getZ() >> 4;
		return getChunk(chunkX,chunkZ,l.getExtent());
	}

	public static World getWorldByName(String name)
	{
		return Sponge.getServer().getWorld(name).get();
	}
	public static User getUser(UUID uuid)
	{
		Optional<Player> onlinePlayer = Sponge.getServer().getPlayer(uuid);

		if (onlinePlayer.isPresent())
		{
			return onlinePlayer.get();
		}

		Optional<UserStorageService> userStorage = Sponge.getServiceManager().provide(UserStorageService.class);

		return userStorage.get().get(uuid).get();
	}
}
