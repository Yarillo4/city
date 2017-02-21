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
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.quequiere.cityplugin.CityPlugin;
import com.quequiere.cityplugin.Tools;
import com.quequiere.cityplugin.datamanip.LocationDeserializer;
import com.quequiere.cityplugin.datamanip.LocationSerializer;
import com.quequiere.cityplugin.object.tool.PermissibleZone;

public class City extends PermissibleZone {
	private static ArrayList<City> loaded = new ArrayList<City>();
	private static File folder = new File("./config/city/citys/");

	private String name;

	private ArrayList<CityChunk> citychunk = new ArrayList<CityChunk>();
	private ArrayList<CityChunk> cityoutpost = new ArrayList<CityChunk>();
	private Location<World> spawn;

	private ArrayList<UUID> residents = new ArrayList<UUID>();

	private Empire empire;

	private boolean openJoin = false;

	private City(String name, Resident mayor) {
		this.name = name;
		this.spawn = mayor.getPlayer().getLocation();
		this.initCityPerm();
		this.citychunk.add(new CityChunk(mayor.getChunk()));

		this.addResident(mayor);
		mayor.setRank(CityRankEnum.mayor);

	}

	public static City tryCreateCity(String name, Player p) {
		City named = getCityByName(name);
		if (named != null) {
			CityPlugin.sendMessage(named.getName() + " city already exist !", TextColors.RED, p);
			return null;
		}

		if (City.getCityFromChunk(Tools.getChunk(p.getLocation())) != null) {
			CityPlugin.sendMessage("You can't create city on a claimed territory !", TextColors.RED, p);
			return null;
		}

		Resident r = Resident.fromPlayerId(p.getUniqueId());
		City c = new City(name, r);
		loaded.add(c);
		c.save();
		r.getCache().initializeCache();
		c.updatePermission();
		return c;
	}

	public static City getCityByName(String name) {
		for (City c : loaded) {
			if (c.getName().equalsIgnoreCase(name)) {
				return c;
			}
		}

		return null;
	}

	public void addResident(Resident r) {
		r.setRank(CityRankEnum.resident);
		this.residents.add(r.getId());
		r.getCache().initializeCache();
		this.save();
	}

	public boolean hasResident(UUID id) {
		for (UUID rid : this.getResidents()) {
			if (rid.equals(id)) {
				return true;
			}
		}

		return false;
	}

	public ArrayList<UUID> getResidents() {
		return residents;
	}

	public static ArrayList<City> getLoaded() {
		return loaded;
	}

	public String getName() {
		return name;
	}

	public Location<World> getSpawn() {
		return spawn;
	}

	public boolean isOpenJoin() {
		return openJoin;
	}

	public void setOpenJoin(boolean openJoin) {
		this.openJoin = openJoin;
		this.save();
	}

	public boolean hasAssistantPerm(Resident r) {
		if (r.getRank().equals(CityRankEnum.mayor) || r.getRank().equals(CityRankEnum.assistant)) {
			return true;
		}

		return false;
	}

	public boolean hasMayorPerm(Resident r) {
		if (r.getRank().equals(CityRankEnum.mayor)) {
			return true;
		}

		return false;
	}

	public void removeResident(UUID id) {
		this.getResidents().remove(id);
		Resident.fromPlayerId(id).setRank(CityRankEnum.resident);
		this.save();
	}

	public static void reloadAll() {
		loaded.clear();

		for (String fname : folder.list()) {
			String toload = fname.replace(".json", "");

			City c = loadCityFromFile(toload);
			if (c != null) {
				loaded.add(c);
				System.out.println("[City] Loaded city: " + c.getName());
			} else {
				System.out.println("Error while loading city: " + toload);
			}
		}

	}

	private static City loadCityFromFile(String name) {
		if (!folder.exists()) {
			folder.mkdirs();
		}

		File f = new File(folder.getAbsolutePath() + "/" + name + ".json");
		City c = null;
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

	public void tryToClaimHere(Player p) {
		Resident r = Resident.fromPlayerId(p.getUniqueId());
		Chunk target = r.getChunk();
		City c = City.getCityFromChunk(target);

		if (c != null) {
			CityPlugin.sendMessage("This chunk is already claimed by " + c.getName(), TextColors.RED, p);
			return;
		}

		citychunk.add(new CityChunk(target));
		this.save();
		CityPlugin.sendMessage("New chunk claimed !", TextColors.GREEN, p);

	}

	public ArrayList<CityChunk> getClaimedChunk() {
		ArrayList<CityChunk> list = new ArrayList<CityChunk>();
		for (CityChunk cc : this.citychunk) {
			list.add(cc);
		}

		for (CityChunk cc : this.cityoutpost) {
			list.add(cc);
		}
		return list;
	}

	public CityChunk getChunck(Chunk c) {
		for (CityChunk cc : this.getClaimedChunk()) {
			if (cc.isEquals(c)) {
				return cc;
			}
		}
		return null;
	}

	public static City getCityFromChunk(Chunk c) {
		for (City ci : loaded) {
			if (ci.getChunck(c)!=null) {
				return ci;
			}
		}
		return null;
	}

	public String toJson() {
		Gson gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(Location.class, new LocationSerializer())
				.create();
		return gson.toJson(this);
	}

	private static City fromJson(String s) {
		Gson gson = new GsonBuilder().registerTypeAdapter(Location.class, new LocationDeserializer()).create();
		return gson.fromJson(s, City.class);
	}

	@Override
	public void updatePermission() {
		this.save();

		for (Player p : Sponge.getServer().getOnlinePlayers()) {
			Resident r = Resident.fromPlayerId(p.getUniqueId());

			for (CityChunk cc : this.getClaimedChunk()) {
				r.getCache().clearChunkPerm(cc.getChunk());
			}

		}

	}

}
