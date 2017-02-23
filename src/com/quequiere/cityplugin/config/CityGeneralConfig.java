package com.quequiere.cityplugin.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.quequiere.cityplugin.CityPlugin;

public class CityGeneralConfig {
	
	private static File folder = new File("./config/city/");
	
	private int lastTaxCheck=0;
	
	private int chunkPerPlayer = 5;
	private double chunkDailyCostBase = 20;
	private double chunkClaimCost = 500;
	private double cityCreateCost = 50000;
	private int cityChunkseparator = 5;
	private int customCityNameLenght = 3;
	private int maxPlayerTaxOnCity = 9999;
	private int defaultPlayerTaxOnCity =0;

	
	public int getChunkPerPlayer() {
		return chunkPerPlayer;
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
			System.out.println("[CITY] Generate a new config file !");
			c = new CityGeneralConfig();
			c.save();
		}
		
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
