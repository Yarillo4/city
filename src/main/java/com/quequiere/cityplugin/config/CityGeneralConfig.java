package com.quequiere.cityplugin.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

import org.spongepowered.api.world.Chunk;

import com.google.common.base.Optional;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.quequiere.cityplugin.CityPlugin;
import com.quequiere.cityplugin.listeners.JoinListener;
import com.quequiere.cityplugin.object.City;
import com.quequiere.cityplugin.object.CityPermBooleanEnum;
import com.quequiere.cityplugin.object.CityWorld;
import com.quequiere.cityplugin.object.PlayerCache;

public class CityGeneralConfig {

	private static File folder = new File("./config/city/");

	private int lastTaxCheck=0;

	private int chunkPerPlayer = 5;
	private double chunkDailyCostBase = 20;
	private double chunkClaimCost = 500;
	private double outpostClaimCost = 40000;
	private double cityCreateCost = 50000;
	private int cityChunkseparator = 5;
	private int cityNameLenght = 10;
	private int customCityNameLenght = 3;
	private int maxPlayerTaxOnCity = 9999;
	private int defaultPlayerTaxOnCity =0;
	private int teleportCityCooldownInSeconds=60;
	private boolean cityNameInChat = true;
	private long antiSpamAdvertMessageInMs=1000;
	private boolean disableCollideMessage = false;
	private boolean dissociatePixelmonEntityFromCreatureSpawn = true;
	private boolean allowBreeding = true;
	private double privatecityprice = 5000;

	private HashMap<CityPermBooleanEnum, ForcableConfig> serverOverridePerm;
	
	private ArrayList<String> whitelistDestroy;
	

	
	
	public ArrayList<String> getWhitelistDestroy() {
		
		if(whitelistDestroy==null)
		{
			whitelistDestroy=new ArrayList<String>();
			this.save();
		}
		
		return whitelistDestroy;
	}

	private void initOverrideBooleanPerms()
	{
		serverOverridePerm = new HashMap<CityPermBooleanEnum, ForcableConfig>();
		for(CityPermBooleanEnum per:CityPermBooleanEnum.values())
		{
			if(per.equals(CityPermBooleanEnum.pvp))
			{
				serverOverridePerm.put(per, new ForcableConfig(false,false));
			}
			else
			{
				serverOverridePerm.put(per, new ForcableConfig(false,true));
			}
			
		}
		
		this.save();
	}

	private HashMap<CityPermBooleanEnum, ForcableConfig> getServerOverridePerm()
	{
		if(serverOverridePerm==null)
		{
			this.initOverrideBooleanPerms();
		}
		else
		{
			//si ajout d'une perm dans les configs via dev et que non initialisee en jeu
			for(CityPermBooleanEnum perms:CityPermBooleanEnum.values())
			{
				if(!serverOverridePerm.containsKey(perms))
				{
					serverOverridePerm.put(perms, new ForcableConfig(false,true));
				}
			}
		}
		return serverOverridePerm;
	}
	

	
	public Optional<Boolean> isActivePerm(CityPermBooleanEnum cityPermBoolean)
	{
		if(!this.getServerOverridePerm().get(cityPermBoolean).forceThisPermOnServer)
		{
			return Optional.absent();
		}
		else
		{
			return Optional.of(this.getServerOverridePerm().get(cityPermBoolean).valueOfPerm);
		}
		
	}
	
	
	public BigDecimal getPrivatecityprice() {
		return new BigDecimal(privatecityprice);
	}



	public boolean isAllowBreeding() {
		return allowBreeding;
	}

	public boolean isdissociatePixelmonEntityFromCreatureSpawn()
	{
		return dissociatePixelmonEntityFromCreatureSpawn;
	}

	public int getChunkPerPlayer() {
		return chunkPerPlayer;
	}

	public boolean isDisableCollideMessage() {
		return disableCollideMessage;
	}





	public long getAntiSpamAdvertMessageInMs() {
		return antiSpamAdvertMessageInMs;
	}




	public BigDecimal getOutpostClaimCost() {
		return new BigDecimal(this.outpostClaimCost);
	}




	public boolean isCityNameInChat() {
		return cityNameInChat;
	}




	public int getCityNameLenght() {
		return cityNameLenght;
	}




	public int getTeleportCityCooldownInSeconds() {
		return teleportCityCooldownInSeconds;
	}




	public int getLastTaxCheck() {
		return lastTaxCheck;
	}

	public void setLastTaxCheck(int lastTaxCheck) {
		this.lastTaxCheck = lastTaxCheck;
		this.save();
	}



	public int getDefaultPlayerTaxOnCity()
	{
		return defaultPlayerTaxOnCity;
	}


	public int getMaxPlayerTaxOnCity()
	{
		return maxPlayerTaxOnCity;
	}


	public int getCustomCityNameLenght()
	{
		return customCityNameLenght;
	}


	public int getCityChunkseparator()
	{
		return cityChunkseparator;
	}


	public BigDecimal getChunkClaimCost() {
		return new BigDecimal(chunkClaimCost);
	}

	public BigDecimal getCityCreateCost() {
		return new BigDecimal(cityCreateCost);
	}


	public BigDecimal getChunkDailyCostBase() {
		return new BigDecimal(chunkDailyCostBase);
	}

	public static void loadConfig()
	{
		CityPlugin.generalConfig=loadConfigFile();
		JoinListener.chunkPerms= new HashMap<Chunk, HashMap<CityPermBooleanEnum,Boolean>>();
		
		for(PlayerCache pc:PlayerCache.cache)
		{
			pc.initializeCache();
		}
		
	}


	private static CityGeneralConfig loadConfigFile() {
		if (!folder.exists()) {
			folder.mkdirs();
		}

		File f = new File(folder.getAbsolutePath() + "/" + "config" + ".json");
		CityGeneralConfig c = null;
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
			System.out.println("[CITY] Created a new config!");
			c = new CityGeneralConfig();
			c.save();
		}

		//initialize si nexiste pas
		c.getServerOverridePerm();
		
		c.save();

		return c;
	}

	public void save() {

		BufferedWriter writer = null;
		try {

			if (!folder.exists()) {
				folder.mkdirs();
			}

			File file = new File(folder.getAbsolutePath() + "/" + "config" + ".json");
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

	private static CityGeneralConfig fromJson(String s) {
		Gson gson = new GsonBuilder().create();
		return gson.fromJson(s, CityGeneralConfig.class);
	}

}
