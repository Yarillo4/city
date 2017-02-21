package com.quequiere.cityplugin.object;

import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Chunk;

import com.quequiere.cityplugin.Tools;
import com.quequiere.cityplugin.object.tool.PermissibleZone;

public class CityChunk extends PermissibleZone
{
	private int x, z;
	private String world;
	private UUID resident = null;
	private double sellPrice = -1;

	private transient City city;

	public CityChunk(Chunk c)
	{
		this.x = c.getPosition().getX();
		this.z = c.getPosition().getZ();
		this.world = c.getWorld().getName();
	}

	public boolean isEquals(Chunk c)
	{
		if (c.getWorld().getName().equals(this.world))
		{
			if (c.getPosition().getX() == this.x && c.getPosition().getZ() == this.z)
			{
				return true;
			}
		}

		return false;
	}
	
	public boolean isOwner(UUID id)
	{
		if(this.getResident()==null)
			return false;
		
		return this.getResident().equals(id) ? true:false;
	}

	public UUID getResident()
	{
		return resident;
	}
	
	

	public void setResident(UUID resident)
	{
		this.resident = resident;
		this.updatePermission();
	}

	public Chunk getChunk()
	{
		return Tools.getChunk(this.x, this.z, Tools.getWorldByName(this.world));
	}

	public City getCity()
	{
		if (this.city == null)
		{
			this.city = City.getCityFromChunk(this.getChunk());
		}

		return this.city;
	}

	@Override
	public void updatePermission()
	{
		this.save();

		for (Player p : Sponge.getServer().getOnlinePlayers())
		{
			Resident r = Resident.fromPlayerId(p.getUniqueId());

			r.getCache().clearChunkPerm(this.getChunk());

		}

	}

	public double getSellPrice()
	{
		return sellPrice;
	}
	
	public void save()
	{
		this.getCity().save();
	}

	public void setSellPrice(double sellPrice)
	{
		this.sellPrice = sellPrice;
		this.save();
	}
	
	

}
