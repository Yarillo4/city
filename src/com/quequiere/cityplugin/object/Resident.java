package com.quequiere.cityplugin.object;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.quequiere.cityplugin.Tools;

public class Resident
{
	private static File folder = new File("./config/city/resident/");

	private static ArrayList<Resident> loaded = new ArrayList<Resident>();
	
	private UUID id;
	private transient PlayerCache cache;
	private CityRankEnum rank = CityRankEnum.resident;

	private Resident(UUID id)
	{
		this.id = id;
	}

	public UUID getId()
	{
		return id;
	}
	
	public void remove()
	{
		loaded.remove(this);
	}

	public Player getPlayer()
	{
		return Sponge.getServer().getPlayer(this.getId()).get();
	}

	public Chunk getChunk()
	{
		Chunk chunk = Tools.getChunk(this.getPlayer().getLocation());
		return chunk;
	}

	public City getCity()
	{
		return this.getCache().getCity();
	}
	
	
	public CityRankEnum getRank()
	{
		if(rank==null)
		{
			this.setRank(CityRankEnum.resident);
		}
		
		return rank;
	}

	public void setRank(CityRankEnum rank)
	{
		this.rank = rank;
		this.getCache().initializeCache();
		this.save();
	}

	public PlayerCache getCache()
	{
		if (cache == null)
		{
			cache = PlayerCache.generateCache(id);
		}
		return cache;
	}

	private static Resident loadresident(UUID id)
	{
		if (!folder.exists())
		{
			folder.mkdirs();
		}

		File f = new File(folder.getAbsolutePath() + "/" + id.toString() + ".json");
		Resident r = null;
		if (f.exists())
		{
			BufferedReader br = null;
			try
			{
				br = new BufferedReader(new FileReader(f));
				StringBuilder sb = new StringBuilder();
				String line = br.readLine();
				while (line != null)
				{
					sb.append(line);
					sb.append(System.lineSeparator());
					line = br.readLine();
				}
				String everything = sb.toString();
				r = fromJson(everything);
			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			finally
			{
				try
				{
					br.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}

		}
		else
		{
			r = new Resident(id);
			r.save();
			loaded.add(r);
		}

		return r;
	}

	public void save()
	{

		BufferedWriter writer = null;
		try
		{

			if (!folder.exists())
			{
				folder.mkdirs();
			}

			File file = new File(folder.getAbsolutePath() + "/" + this.getId().toString() + ".json");
			if (!file.exists())
			{
				file.createNewFile();
			}
			writer = new BufferedWriter(new FileWriter(file));

			writer.write(this.toJson());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				writer.close();
			}
			catch (Exception e)
			{
			}
		}

	}

	public String toJson()
	{
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return gson.toJson(this);
	}

	private static Resident fromJson(String s)
	{
		Gson gson = new GsonBuilder().create();
		return gson.fromJson(s, Resident.class);
	}

	public static Resident fromPlayerId(UUID id)
	{
		for (Resident r : loaded)
		{
			if (id.equals(r.id))
			{
				return r;
			}
		}

		Resident r = loadresident(id);
		loaded.add(r);
		return r;
	}
}
