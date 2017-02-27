package com.quequiere.cityplugin;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.GameProfileManager;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3d;

public class Tools
{
	public static Chunk getChunk(int chunkX,int chunkZ,World w)
	{
		Chunk chunk = w.getChunk(chunkX, 0, chunkZ).get();
		return chunk;
	}

	public static UUID getnerateCustomUUID()
	{
		UUID newUID = UUID.randomUUID();
		GameProfileManager profileManager = Sponge.getServer().getGameProfileManager();
		CompletableFuture<GameProfile> futureGameProfile = profileManager.get(newUID);
		System.out.println("Generated uuid: "+futureGameProfile.isDone());
		return newUID;
	}

	public static Direction getPlayerDirection(int degrees)
	{

		degrees = Math.abs(degrees);
		try
		{
			Direction[] cardinalDirections = { Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.EAST };
			int index = (int) Math.floor((degrees *4F)/360+0.5D) &3;

			Direction d = cardinalDirections[Math.abs(index)];
			return d;
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			System.out.println("ArrrayIndexOutOfBounds, degree is out of bounds: "+degrees);
			return Direction.NORTH;
		}


	}

	public static Location<Chunk> addDirection(Location<Chunk> c,Direction d,int count)
	{
		Location<Chunk> toret = c.copy();
		Direction di = d;

		if(count<0)
		{
			di=d.getOpposite();
		}


		for(int x=0;x<Math.abs(count);x++)
		{
			toret=toret.getRelative(di);
		}
		return toret;
	}

	public static Location<Chunk> getChunkLocation(Location<World> l)
	{
		int chunkX = l.getBlockPosition().getX() >> 4;
		int chunkZ = l.getBlockPosition().getZ() >> 4;
		return getChunk(l).getLocation(chunkX, 0, chunkZ);
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
