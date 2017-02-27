package com.quequiere.cityplugin.object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import com.quequiere.cityplugin.CityPlugin;

import com.quequiere.cityplugin.Tools;

public class PlayerCache
{
	public static ArrayList<PlayerCache> cache = new ArrayList<PlayerCache>();
	private City city;
	private UUID id;
	private ArrayList<City> invitation = new ArrayList<City>();
	private long lastTpCity = 0;
	private boolean displayMap= false;
	private HashMap<CityPermEnum, Long> advertMessage = new HashMap<CityPermEnum, Long>();
	private boolean adminBypass=false;

	private HashMap<Chunk, HashMap<CityPermEnum, Boolean>> cachePerm;

	private PlayerCache(UUID id)
	{
		this.id = id;
		this.initializeCache();
		cache.add(this);
	}
	
	
	
	public boolean isAdminBypass() {
		return adminBypass;
	}



	public void setAdminBypass(boolean adminBypass) {
		this.reloadPerm();
		this.adminBypass = adminBypass;
	}



	public boolean canDisplayMessage(CityPermEnum perm)
	{
		boolean b = false;
		long now = System.currentTimeMillis();
	
	
		
		if(advertMessage.containsKey(perm))
		{
			
			long last = advertMessage.get(perm);
			long diff = now-last;
			
			if(diff>CityPlugin.generalConfig.getAntiSpamAdvertMessageInMs())
			{
				b=true;
				advertMessage.remove(perm);
				advertMessage.put(perm, now);
			}
		}
		else
		{
			b=true;
			advertMessage.put(perm, now);
		}
		
	
		return b;
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

	
	public boolean isDisplayMap()
	{
		return displayMap;
	}


	public void setDisplayMap(boolean displayMap)
	{
		this.displayMap = displayMap;
	}


	public long getLastTpCity() {
		return lastTpCity;
	}


	public void setLastTpCity(long lastTpCity) {
		this.lastTpCity = lastTpCity;
	}


	public void initializeCache()
	{
		this.setAdminBypass(false);
		this.reloadCity();
		this.reloadPerm();
	}

	public void reloadPerm()
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
			CityWorld cw = CityWorld.getByName(c.getWorld().getName());
			this.loadPermFromWorld(c, c.getWorld(), CityPermRankEnum.outsider,cw);
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
			if(this.isAdminBypass())
			{
				b=true;
			}
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
			if(this.isAdminBypass())
			{
				b=true;
			}
			local1.put(perm, b);
		}

		this.getCachePerm().put(c, local1);

	}

	private void loadPermFromWorld(Chunk c,World w, CityPermRankEnum rank,CityWorld cw)
	{

		HashMap<CityPermEnum, Boolean> local1 = new HashMap<CityPermEnum, Boolean>();
		for (CityPermEnum perm : CityPermEnum.values())
		{
			boolean b = cw.canDoAction(perm, rank);
			if(this.isAdminBypass())
			{
				b=true;
			}
			local1.put(perm, b);
		}
		this.getCachePerm().put(c, local1);

	}
	
	

	public ArrayList<City> getInvitation() {
		return invitation;
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
