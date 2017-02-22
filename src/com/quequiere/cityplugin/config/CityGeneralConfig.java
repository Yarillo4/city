package com.quequiere.cityplugin.config;

import java.math.BigDecimal;

import com.quequiere.cityplugin.CityPlugin;

public class CityGeneralConfig {
	
	private int chunkPerPlayer = 5;
	private double chunkDailyCostBase = 20;
	private double chunkClaimCost = 500;
	private double cityCreateCost = 50000;
	
	
	public int getChunkPerPlayer() {
		return chunkPerPlayer;
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
		CityPlugin.generalConfig=new CityGeneralConfig();
	}
	
	public void save()
	{
		
	}

}
