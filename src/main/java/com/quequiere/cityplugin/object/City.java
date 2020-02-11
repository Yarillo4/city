package com.quequiere.cityplugin.object;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.quequiere.cityplugin.CityPlugin;
import com.quequiere.cityplugin.Tools;
import com.quequiere.cityplugin.datamanip.LocationDeserializer;
import com.quequiere.cityplugin.datamanip.LocationSerializer;
import com.quequiere.cityplugin.dynmap.CityDynmapAdaptator;
import com.quequiere.cityplugin.listeners.JoinListener;
import com.quequiere.cityplugin.object.tool.PermissibleZone;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

public class City extends PermissibleZone
{
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

	private String customName = "";

	private int bonusClaim = 0;
	private UUID economyUUID;
	
	private boolean privateCity = false;

	private City(String name, Resident mayor, Chunk c)
	{
		this.name = name;

		if (Sponge.getServer().getPlayer(mayor.getId()).isPresent())
		{
			this.spawn = mayor.getPlayer().getLocation();
		}

		this.initCityPerm();
		this.initCityBooleanPerm();
		this.citychunk.add(new CityChunk(c));

		this.addResident(mayor);
		mayor.setRank(CityRankEnum.mayor);
		this.setPlayerTaxe(CityPlugin.generalConfig.getDefaultPlayerTaxOnCity());
		
		
		new java.util.Timer().schedule( 
		        new java.util.TimerTask() {
		            @Override
		            public void run() {
		            	System.out.println("Initialize dynmaplink !");
		            	CityDynmapAdaptator.init(null);
		            }
		        }, 
		        10000 
		);
		
		

	}

	public static City tryCreateCity(String name, Player p, boolean importFileBypass, User mayor, Optional<Chunk> locationo,boolean privateCity)
	{

		if (!importFileBypass)
		{

			if (name.length() > CityPlugin.generalConfig.getCityNameLenght())
			{
				CityPlugin.sendMessage("The maximum length of a custom city name is " + CityPlugin.generalConfig.getCityNameLenght(), TextColors.RED, p);
				return null;
			}
			
			BigDecimal price = null;
			if(privateCity)
			{
				price =  CityPlugin.generalConfig.getPrivatecityprice();
			}
			else
			{
				price =  CityPlugin.generalConfig.getCityCreateCost();
			}

			Account account = CityPlugin.economyService.getOrCreateAccount(p.getUniqueId()).get();
			TransactionResult transactionResult = account.withdraw(CityPlugin.economyService.getDefaultCurrency(), price, Sponge.getCauseStackManager().getCurrentCause());

			if (transactionResult.getResult() != ResultType.SUCCESS)
			{
				CityPlugin.sendMessage("You can't afford that! You need: " + "$" + price, TextColors.RED, p);
				return null;
			}

		}

		if (!locationo.isPresent())
		{
			CityPlugin.sendMessage("We can't find the chunk chere you are standing up", TextColors.RED, p);
			return null;
		}

		Chunk location = locationo.get();

		City named = getCityByName(name);
		if (named != null)
		{
			CityPlugin.sendMessage(named.getName() + " already exists! Please choose a different name!", TextColors.RED, p);
			return null;
		}

		if (City.getCityFromChunk(location) != null)
		{
			CityPlugin.sendMessage("This territory has already been claimed!", TextColors.RED, p);
			return null;
		}

		if (!importFileBypass && hasOtherCityInRadius(null, location))
		{
			CityPlugin.sendMessage("You cannot claim this close to another city.", TextColors.RED, p);
			return null;
		}

		Resident r = Resident.fromPlayerId(mayor.getUniqueId());
		City c = new City(name, r, location);
		
		if(privateCity)
		{
			c.setPrivateCity(true);
		}
			
		loaded.add(c);
		c.save();
		r.getCache().initializeCache();
		c.updatePermission();

		if(privateCity)
		{
			Sponge.getGame().getServer().getBroadcastChannel().send(Text.builder("The private city " + name + " has been created!").color(TextColors.GREEN).build());
		}
		else
		{
			Sponge.getGame().getServer().getBroadcastChannel().send(Text.builder("The city " + name + " has been created!").color(TextColors.GREEN).build());
		}
		

		return c;

	}
	
	

	public boolean isPrivateCity()
	{
		return privateCity;
	}

	public void setPrivateCity(boolean privateCity)
	{
		this.privateCity = privateCity;
	}

	public static City tryCreateCity(String name, Player p,boolean privateCity)
	{
		return tryCreateCity(name, p, false, p, Tools.getChunk(p.getLocation()),privateCity);
	}

	public static boolean hasOtherCityInRadius(City reference, Chunk c)
	{
		int chunkseparator = CityPlugin.generalConfig.getCityChunkseparator();

		for (int x = -chunkseparator; x <= chunkseparator; x++)
		{
			for (int z = -chunkseparator; z <= chunkseparator; z++)
			{

				int xc = c.getPosition().getX() + x;
				int zc = c.getPosition().getZ() + z;

				Optional<Chunk> toseeo = Tools.getChunk(xc, zc, c.getWorld());

				if (!toseeo.isPresent())
				{
					System.out.println("City can't find a chunk for method 1 radius: " + xc + " / " + zc + " / " + c.getWorld());
					return true;
				}

				Chunk tosee = toseeo.get();

				if (!tosee.equals(c))
				{

					Optional<Chunk> currentCo = Tools.getChunk(xc, zc, c.getWorld());

					if (!currentCo.isPresent())
					{
						System.out.println("City can't find a chunk for method 2 radius: " + xc + " / " + zc + " / " + c.getWorld());
						return true;
					}

					City ctarget = City.getCityFromChunk(currentCo.get());

					if (ctarget != null && !ctarget.equals(reference))
					{
						return true;
					}
				}

			}
		}

		return false;
	}

	public static City getCityByName(String name)
	{
		for (City c : loaded)
		{
			if (c.getName().equalsIgnoreCase(name))
			{
				return c;
			}
		}

		return null;
	}

	public int getMaxChunk()
	{
		return CityPlugin.generalConfig.getChunkPerPlayer() * this.getResidents().size() + this.getBonusClaim();
	}

	public int getBonusClaim()
	{
		return bonusClaim;
	}

	public void setBonusClaim(int bonusClaim)
	{
		this.bonusClaim = bonusClaim;
		this.save();
	}

	public void addResident(Resident r)
	{
		r.setRank(CityRankEnum.resident);
		if (r.getId() == null)
		{
			System.out.println("fatal error");
			return;
		}
		this.residents.add(r.getId());
		r.getCache().initializeCache();

		for (Player p : Sponge.getServer().getOnlinePlayers())
		{
			if (this.hasResident(p.getUniqueId()))
			{

				if (r.getPlayer() != null)
				{
					CityPlugin.sendMessage(r.getPlayer().getName() + " joined the city !", TextColors.GREEN, p);
				}
				else
				{
					CityPlugin.sendMessage(r.getId() + " joined the city !", TextColors.GREEN, p);
				}

			}

		}

		this.save();
	}

	public UUID getEconomyUUID()
	{
		return economyUUID;
	}

	public void setEconomyUUID(UUID economyUUID)
	{
		this.economyUUID = economyUUID;
		this.save();
	}

	public UUID getNameEconomy()
	{

		if (this.getEconomyUUID() == null)
		{
			UUID id = Tools.getnerateCustomUUID();
			System.out.println("City generate a new UUID for city " + this.getName());
			this.setEconomyUUID(id);
		}

		return this.getEconomyUUID();
	}

	public boolean hasResident(UUID id)
	{
		for (UUID rid : this.getResidents())
		{
			if (rid.equals(id))
			{
				return true;
			}
		}

		return false;
	}

	public ArrayList<UUID> getResidents()
	{
		return residents;
	}

	public static ArrayList<City> getLoaded()
	{
		return loaded;
	}

	public String getName()
	{
		return name;
	}

	public Location<World> getSpawn()
	{
		return spawn;
	}

	public void setSpawn(Location<World> spawn)
	{
		this.spawn = spawn;
		this.save();
	}

	public boolean isOpenJoin()
	{
		return openJoin;
	}

	public void setOpenJoin(boolean openJoin)
	{
		this.openJoin = openJoin;
		this.save();
	}

	public boolean hasAssistantPerm(Resident r)
	{

		if (this.hasResident(r.getId()))
		{
			if (r.getRank().equals(CityRankEnum.mayor) || r.getRank().equals(CityRankEnum.assistant))
			{
				return true;
			}
		}

		return false;
	}

	public boolean hasMayorPerm(Resident r)
	{
		if (this.hasResident(r.getId()))
		{
			if (r.getRank().equals(CityRankEnum.mayor))
			{
				return true;
			}

		}

		return false;
	}

	public void removeResident(UUID id)
	{
		this.getResidents().remove(id);
		Resident.fromPlayerId(id).setRank(CityRankEnum.resident);

		for (CityChunk cc : this.getClaimedChunk())
		{
			if (cc != null && cc.getResident() != null && cc.getResident().equals(id))
			{
				cc.setResident(null);
			}
		}

		this.save();
	}

	public static void reloadAll()
	{
		loaded.clear();

		if (!folder.exists())
		{
			folder.mkdir();
		}

		for (String fname : folder.list())
		{
			String toload = fname.replace(".json", "");

			City c = loadCityFromFile(toload);
			if (c != null)
			{
				loaded.add(c);
				System.out.println("[City] Loaded city: " + c.getName());
			}
			else
			{
				System.out.println("Error while loading city: " + toload);
			}
		}

	}

	private static City loadCityFromFile(String name)
	{
		if (!folder.exists())
		{
			folder.mkdirs();
		}

		File f = new File(folder.getAbsolutePath() + "/" + name + ".json");
		City c = null;
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
				c = fromJson(everything);
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

		return c;
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

			File file = new File(folder.getAbsolutePath() + "/" + this.getName() + ".json");
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

	public void setNoMayor()
	{
		for (UUID id : this.getResidents())
		{
			Resident r = Resident.fromPlayerId(id);
			if (r.getRank().equals(CityRankEnum.mayor))
			{
				r.setRank(CityRankEnum.assistant);
			}
		}
	}

	public Optional<Resident> getMayor()
	{
		for (UUID id : this.getResidents())
		{
			Resident r = Resident.fromPlayerId(id);
			if (r.getRank().equals(CityRankEnum.mayor))
			{
				return Optional.of(r);
			}
		}

		return Optional.empty();
	}

	public void forceClaimImport(Chunk target)
	{

		City c = City.getCityFromChunk(target);

		if (c != null)
		{
			System.out.println("chunk already claimed, can't import !");
			return;
		}

		CityChunk cc = new CityChunk(target);
		citychunk.add(cc);
		this.save();
		cc.updatePermission();

	}

	public void tryToClaimHere(Player p, boolean outpost)
	{
		Resident r = Resident.fromPlayerId(p.getUniqueId());
		Optional<Chunk> targeto = r.getChunk();

		if (!targeto.isPresent())
		{
			CityPlugin.sendMessage("Can't find the chunk where you are standing up 2", TextColors.RED, p);
			return;
		}

		Chunk target = targeto.get();
		City c = City.getCityFromChunk(target);

		if (c != null)
		{
			CityPlugin.sendMessage("This chunk is already claimed by " + c.getName(), TextColors.RED, p);
			return;
		}

		if (this.getClaimedChunk().size() >= this.getMaxChunk())
		{
			CityPlugin.sendMessage("You need more player in your city to claim more chunk !", TextColors.RED, p);
			return;
		}

		boolean pass = false;
		// check proximity
		for (int x = -1; x <= 1; x++)
		{
			for (int z = -1; z <= 1; z++)
			{

				int xc = target.getPosition().getX() + x;
				int zc = target.getPosition().getZ() + z;

				Optional<Chunk> toseeo = Tools.getChunk(xc, zc, target.getWorld());

				if (!toseeo.isPresent())
				{
					System.out.println("Can't find chunk on " + xc + " / " + zc);
					continue;
				}

				Chunk tosee = toseeo.get();

				if (!tosee.equals(target))
				{

					City ctarget = City.getCityFromChunk(tosee);

					if (ctarget != null && ctarget.equals(r.getCity()))
					{
						pass = true;
						break;
					}
				}

			}
		}

		if (!pass && !outpost)
		{
			CityPlugin.sendMessage("Claims need to be accroch to the city, or create an outpost", TextColors.RED, p);
			return;
		}

		if (hasOtherCityInRadius(this, target))
		{
			CityPlugin.sendMessage("You cant claim here, need more space between city !", TextColors.RED, p);
			return;
		}

		Account account = CityPlugin.economyService.getOrCreateAccount(this.getNameEconomy()).get();
		TransactionResult transactionResult = account.withdraw(CityPlugin.economyService.getDefaultCurrency(), outpost ? CityPlugin.generalConfig.getOutpostClaimCost() : CityPlugin.generalConfig.getChunkClaimCost(), Sponge.getCauseStackManager().getCurrentCause());

		if (transactionResult.getResult() != ResultType.SUCCESS)
		{
			CityPlugin.sendMessage("No enought money in the city's bank account.", TextColors.RED, p);
			CityPlugin.sendMessage("Use /c deposit to add fund. You need " + (outpost ? CityPlugin.generalConfig.getOutpostClaimCost() : CityPlugin.generalConfig.getChunkClaimCost()) + " $", TextColors.RED, p);
			return;
		}

		CityChunk cc = new CityChunk(target);
		citychunk.add(cc);
		this.save();
		cc.updatePermission();
		CityPlugin.sendMessage("New chunk claimed !", TextColors.GREEN, p);
		CityDynmapAdaptator.init(this.getName());
	}

	public void destroy()
	{
		@SuppressWarnings("unchecked")
		ArrayList<UUID> listr = (ArrayList<UUID>) this.getResidents().clone();
		for (UUID id : listr)
		{
			this.removeResident(id);
		}

		@SuppressWarnings("unchecked")
		ArrayList<CityChunk> list = (ArrayList<CityChunk>) this.getClaimedChunk().clone();

		for (CityChunk cc : list)
		{
			this.unclaimChunk(cc);
		}

		loaded.remove(this);

		File f = new File(folder.getAbsolutePath() + "/" + name + ".json");
		f.delete();

		Sponge.getGame().getServer().getBroadcastChannel().send(Text.builder(this.getName() + " has been successfuly destroyed").color(TextColors.GRAY).build());
		
		
		CityDynmapAdaptator.clear();
		
		new java.util.Timer().schedule( 
		        new java.util.TimerTask() {
		            @Override
		            public void run() {
		            	System.out.println("Initialize dynmaplink !");
		            	CityDynmapAdaptator.init(null);
		            }
		        }, 
		        30000 
		);
		
		
		
		
		
	}

	public void unclaimChunk(CityChunk cc)
	{
		this.citychunk.remove(cc);
		this.cityoutpost.remove(cc);
		cc.initialize();

		for (Player p : Sponge.getServer().getOnlinePlayers())
		{
			Resident r = Resident.fromPlayerId(p.getUniqueId());

			Optional<Chunk> cco = cc.getChunk();
			if (cco.isPresent())
			{
				r.getCache().clearChunkPerm(cco.get());
			}
			else
			{
				System.out.println("City can't find chunk while unclaiming and update perm");
			}

		}
		this.save();
		CityDynmapAdaptator.init(this.getName());
	}

	public BigDecimal getTaxDailyCost()
	{

		if (!this.getMayor().isPresent())
		{
			return new BigDecimal(0);
		}

		return CityPlugin.generalConfig.getChunkDailyCostBase().multiply(new BigDecimal(this.getClaimedChunk().size()));
	}

	public BigDecimal getPlayerTaxe()
	{
		return new BigDecimal(this.playerTax);
	}

	public void setPlayerTaxe(double playerTaxe)
	{
		this.playerTax = playerTaxe;
		this.save();
	}

	public boolean isRemovePlayerTax()
	{
		return removePlayerTax;
	}

	public void setRemovePlayerTax(boolean removePlayerTax)
	{
		this.removePlayerTax = removePlayerTax;
	}

	public ArrayList<CityChunk> getClaimedChunk()
	{
		ArrayList<CityChunk> list = new ArrayList<CityChunk>();
		for (CityChunk cc : this.citychunk)
		{
			list.add(cc);
		}

		for (CityChunk cc : this.cityoutpost)
		{
			list.add(cc);
		}
		return list;
	}

	public CityChunk getChunck(Chunk c)
	{
		for (CityChunk cc : this.getClaimedChunk())
		{
			if (cc.isEquals(c))
			{
				return cc;
			}
		}
		return null;
	}

	public static City getCityFromChunk(Chunk c)
	{
		for (City ci : loaded)
		{
			if (ci.getChunck(c) != null)
			{
				return ci;
			}
		}
		return null;
	}

	public String toJson()
	{
		Gson gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(Location.class, new LocationSerializer()).create();
		return gson.toJson(this);
	}

	private static City fromJson(String s)
	{
		Gson gson = new GsonBuilder().registerTypeAdapter(Location.class, new LocationDeserializer()).create();
		return gson.fromJson(s, City.class);
	}

	@Override
	public void updatePermission()
	{
		this.save();

		for (Player p : Sponge.getServer().getOnlinePlayers())
		{
			Resident r = Resident.fromPlayerId(p.getUniqueId());

			for (CityChunk cc : this.getClaimedChunk())
			{

				Optional<Chunk> cco = cc.getChunk();

				if (cco.isPresent())
				{
					r.getCache().clearChunkPerm(cco.get());
				}
				else
				{
					System.out.println("City can't find chunk while update perm of city");
				}

			}

		}

		for (CityChunk cc : this.getClaimedChunk())
		{

			Optional<Chunk> cco = cc.getChunk();
			if (cco.isPresent())
			{
				if (JoinListener.chunkPerms.containsKey(cc.getChunk().get()))
				{
					JoinListener.chunkPerms.remove(cc.getChunk().get());
				}
			}
			else
			{
				System.out.println("City can't find chunk while update perm of city 2");
			}

		}

	}

	public String getCustomName()
	{
		if (customName == null || customName.equals(""))
		{
			int size = CityPlugin.generalConfig.getCustomCityNameLenght();

			if (this.getName().length() < size)
			{
				size = this.getName().length();
			}

			String nametoret = this.getName().substring(0, size).toUpperCase();
			return nametoret;

		}

		return this.customName.toUpperCase();
	}

	public void setCustomName(String customName)
	{
		this.customName = customName;
		this.save();
	}


}
