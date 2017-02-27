package com.quequiere.cityplugin.object;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.quequiere.cityplugin.datamanip.LocationDeserializer;
import com.quequiere.cityplugin.datamanip.LocationSerializer;
import com.quequiere.cityplugin.object.tool.PermissibleZone;

public class CityWorld  extends PermissibleZone{

	private static ArrayList<CityWorld> loaded = new  ArrayList<CityWorld>();
	private static File folder = new File("./config/city/cityworlds/");
	private String name;

	public CityWorld(String name)
	{
		this.name=name;
	}

	public String getName() {
		return name;
	}

	@Override
	public void updatePermission() {
		this.save();

		for (Player p : Sponge.getServer().getOnlinePlayers())
		{
			Resident r = Resident.fromPlayerId(p.getUniqueId());
			r.getCache().reloadPerm();
		}

	}

	public static CityWorld getByName(String name)
	{
		for(CityWorld cw:loaded)
		{
			if(name.equals(cw.getName()))
			{
				return cw;
			}
		}

		return null;
	}

	public static void loadWorld(String name)
	{
		if(getByName(name)!=null)
		{
			loaded.remove(getByName(name));
		}
		System.out.println("[City] New CityWorld loaded.");
		CityWorld cw = loadCityFromFile(name);
		loaded.add(cw);
	}

	private static CityWorld loadCityFromFile(String name) {
		if (!folder.exists()) {
			folder.mkdirs();
		}

		File f = new File(folder.getAbsolutePath() + "/" + name + ".json");
		CityWorld c = null;
		if (f.exists()) {
			BufferedReader br = null;
			try {
				br = new BufferedReader(new FileReader(f));
				StringBuilder sb = new StringBuilder();
				String line = br.readLine();
				while (line != null) {
					sb.append(line);
					sb.append(System.lineSeparator());
					line = br.readLine();
				}
				String everything = sb.toString();
				c = fromJson(everything);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}

		if(c==null)
		{
			c=new CityWorld(name);
			c.setPermission(CityPermEnum.BUILD, CityPermRankEnum.outsider, true);
			c.setPermission(CityPermEnum.DESTROY, CityPermRankEnum.outsider, true);
			c.setPermission(CityPermEnum.SWITCH, CityPermRankEnum.outsider, true);
			c.setPermission(CityPermEnum.USEITEM, CityPermRankEnum.outsider, true);
			c.save();
		}

		return c;
	}

	public void save() {

		BufferedWriter writer = null;
		try {

			if (!folder.exists()) {
				folder.mkdirs();
			}

			File file = new File(folder.getAbsolutePath() + "/" + this.getName() + ".json");
			if (!file.exists()) {
				file.createNewFile();
			}
			writer = new BufferedWriter(new FileWriter(file));

			writer.write(this.toJson());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				writer.close();
			} catch (Exception e) {
			}
		}

	}

	public String toJson() {
		Gson gson = new GsonBuilder().setPrettyPrinting()
				.create();
		return gson.toJson(this);
	}

	private static CityWorld fromJson(String s) {
		Gson gson = new GsonBuilder().create();
		return gson.fromJson(s, CityWorld.class);
	}


}
