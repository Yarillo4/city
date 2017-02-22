package com.quequiere.cityplugin.object;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.text.Text;
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

	@SuppressWarnings("unused")
	private Empire empire;

	private boolean openJoin = false;
	private double playerTax = 0;
	private boolean removePlayerTax = false;
	
	private City(String name, Resident mayor) {
		this.name = name;
		this.spawn = mayor.getPlayer().getLocation();
		this.initCityPerm();
		this.citychunk.add(new CityChunk(mayor.getChunk()));

		this.addResident(mayor);
		mayor.setRank(CityRankEnum.mayor);

	}

	public static City tryCreateCity(String name, Player p) {
		
		
		Account account = CityPlugin.economyService.getOrCreateAccount(p.getUniqueId()).get();
		TransactionResult transactionResult = account.withdraw(CityPlugin.economyService.getDefaultCurrency(), CityPlugin.generalConfig.getCityCreateCost(), Cause.of(NamedCause.source(p)));
		
		if (transactionResult.getResult() != ResultType.SUCCESS)
		{
			CityPlugin.sendMessage("No enought money in your account ! You need: "+ CityPlugin.generalConfig.getCityCreateCost()+CityPlugin.economyService.getDefaultCurrency().getSymbol(), TextColors.RED, p);
			return null;
		}
		
		
		
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
	
	public int getMaxChunk()
	{
		return CityPlugin.generalConfig.getChunkPerPlayer()*this.getResidents().size();
	}

	public void addResident(Resident r) {
		r.setRank(CityRankEnum.resident);
		this.residents.add(r.getId());
		r.getCache().initializeCache();
		this.save();
	}
	
	public String getNameEconomy()
	{
		return "City_"+this.getName();
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

		if(!folder.exists())
		{
			folder.mkdir();
		}
		
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
		
		if(this.getClaimedChunk().size()>=this.getMaxChunk())
		{
			CityPlugin.sendMessage("You need more player in your city to claim more chunk !", TextColors.RED, p);
			return;
		}
		
		
		Account account = CityPlugin.economyService.getOrCreateAccount(this.getNameEconomy()).get();
		TransactionResult transactionResult = account.withdraw(CityPlugin.economyService.getDefaultCurrency(), CityPlugin.generalConfig.getChunkClaimCost(), Cause.of(NamedCause.source(p)));
		
		if (transactionResult.getResult() != ResultType.SUCCESS)
		{
			CityPlugin.sendMessage("No enought money in the city's bank account.", TextColors.RED, p);
			CityPlugin.sendMessage("Use /c deposite to add fund. You need "+CityPlugin.generalConfig.getChunkClaimCost()+CityPlugin.economyService.getDefaultCurrency().getSymbol(), TextColors.RED, p);
			return ;
		}
		

		CityChunk cc = new CityChunk(target);
		citychunk.add(cc);
		this.save();
		cc.updatePermission();
		CityPlugin.sendMessage("New chunk claimed !", TextColors.GREEN, p);

	}
	
	public void destroy()
	{
		for(UUID id:this.getResidents())
		{
			this.removeResident(id);
		}
		
		@SuppressWarnings("unchecked")
		ArrayList<CityChunk> list = (ArrayList<CityChunk>) this.getClaimedChunk().clone();
		
		for(CityChunk cc:list)
		{
			this.unclaimChunk(cc);
		}
		
		loaded.remove(this);
		
		File f = new File(folder.getAbsolutePath() + "/" + name + ".json");
		f.delete();
		
		Sponge.getGame().getServer().getBroadcastChannel().send(Text.builder(this.getName()+ " has been successfuly destroyed").color(TextColors.GRAY).build());
	}
	
	public void unclaimChunk(CityChunk cc)
	{
		this.citychunk.remove(cc);
		this.cityoutpost.remove(cc);
		cc.initialize();
		
		for (Player p : Sponge.getServer().getOnlinePlayers())
		{
			Resident r = Resident.fromPlayerId(p.getUniqueId());
			r.getCache().clearChunkPerm(cc.getChunk());
		}
		this.save();
	}
	
	public BigDecimal getDailyCost()
	{
		return CityPlugin.generalConfig.getChunkDailyCostBase().multiply(new BigDecimal(this.getClaimedChunk().size()));
	}
	
	

	public BigDecimal getPlayerTaxe() {
		return new BigDecimal(this.playerTax);
	}

	public void setPlayerTaxe(double playerTaxe) {
		this.playerTax = playerTaxe;
		this.save();
	}
	

	public boolean isRemovePlayerTax() {
		return removePlayerTax;
	}

	public void setRemovePlayerTax(boolean removePlayerTax) {
		this.removePlayerTax = removePlayerTax;
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
