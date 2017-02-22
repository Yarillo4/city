package com.quequiere.cityplugin.object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.quequiere.cityplugin.Tools;

public class PlayerCache
{
	public static ArrayList<PlayerCache> cache = new ArrayList<PlayerCache>();
	private City city;
	private UUID id;

	private HashMap<Chunk, HashMap<CityPermEnum, Boolean>> cachePerm;

	private PlayerCache(UUID id)
	{
		this.id = id;
		this.initializeCache();
		cache.add(this);
	}

	public static PlayerCache generateCache(UUID id)
	{
		for (PlayerCache c : cache)
		{
			if (c.id.equals(id))
			{
				c.initializeCache();
				return c;
			}
		}

		PlayerCache cache = new PlayerCache(id);
		return cache;
	}

	public void initializeCache()
	{
		this.reloadCity();
		this.reloadPerm();
	}

	private void reloadPerm()
	{
		cachePerm = new HashMap<Chunk, HashMap<CityPermEnum, Boolean>>();
	}

	public HashMap<Chunk, HashMap<CityPermEnum, Boolean>> getCachePerm()
	{
		if (this.cachePerm == null)
		{
			this.reloadPerm();
		}
		return cachePerm;
	}

	private void reloadCity()
	{
		for (City c : City.getLoaded())
		{
			if (c.hasResident(this.id))
			{
				this.city = c;
				return;
			}
		}

		this.city = null;
	}

	public City getCity()
	{
		return this.city;
	}

	private void loadPermForChunk(Chunk c)
	{
		City targetCity = City.getCityFromChunk(c);

		if (targetCity == null)
		{
			this.loadPermFromWorld(c, c.getWorld(), CityPermRankEnum.outsider);
		}
		else
		{

			CityChunk cc = targetCity.getChunck(c);

			if (cc.getResident() == null)
			{

				if (targetCity.hasResident(this.id))
				{
					if (targetCity.hasAssistantPerm(Resident.fromPlayerId(this.id)))
					{
						loadPermCity(c, targetCity, CityPermRankEnum.admin);
					}
					else
					{
						loadPermCity(c, targetCity, CityPermRankEnum.resident);
					}

				}
				else
				{
					loadPermCity(c, targetCity, CityPermRankEnum.outsider);
				}

			}
			else
			{

				if (cc.getResident().equals(this.id))
				{
					loadPermChunkCity(c, cc, CityPermRankEnum.admin);
					// A CHANGER EN TANT QUE ADMIN
				}
				else
				{

					if (targetCity.hasResident(this.id))
					{
						if (targetCity.hasAssistantPerm(Resident.fromPlayerId(this.id)))
						{
							loadPermChunkCity(c, cc, CityPermRankEnum.admin);
						}
						else
						{
							loadPermChunkCity(c, cc, CityPermRankEnum.resident);
						}

					}
					else
					{

						loadPermChunkCity(c, cc, CityPermRankEnum.outsider);
					}
				}

			}

		}

	}

	private void loadPermChunkCity(Chunk c, CityChunk cc, CityPermRankEnum rank)
	{
		HashMap<CityPermEnum, Boolean> local1 = new HashMap<CityPermEnum, Boolean>();
		for (CityPermEnum perm : CityPermEnum.values())
		{
			boolean b = cc.canDoAction(perm, rank); 
			local1.put(perm, b);
		}
		this.getCachePerm().put(c, local1);

	}

	private void loadPermCity(Chunk c, City ci, CityPermRankEnum rank)
	{

		HashMap<CityPermEnum, Boolean> local1 = new HashMap<CityPermEnum, Boolean>();

		for (CityPermEnum perm : CityPermEnum.values())
		{
			boolean b = ci.canDoAction(perm, rank);
			local1.put(perm, b);
		}

		this.getCachePerm().put(c, local1);

	}

	private void loadPermFromWorld(Chunk c, World w, CityPermRankEnum rank)
	{

		System.out.println("this need to be dev2");

		HashMap<CityPermEnum, Boolean> local1 = new HashMap<CityPermEnum, Boolean>();
		for (CityPermEnum perm : CityPermEnum.values())
		{
			boolean b = false;
			local1.put(perm, b);
		}
		this.getCachePerm().put(c, local1);

	}

	public boolean hasPerm(Location<World> loc, CityPermEnum perm)
	{
		Chunk c = Tools.getChunk(loc);

		if (!cachePerm.containsKey(c))
		{
			this.loadPermForChunk(c);
		}

		return cachePerm.get(c).get(perm);
	}

	public void clearChunkPerm(Chunk c)
	{
		this.getCachePerm().remove(c);
	}

}
