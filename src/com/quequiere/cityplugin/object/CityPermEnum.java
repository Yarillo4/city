package com.quequiere.cityplugin.object;

public enum CityPermEnum
{
	BUILD("Build","Determine if %r can build here"),
	DESTROY("Destroy","Determine if %r can destroy here"),
	SWITH("Switch","Determine if %r can swith blocks"),
	USEITEM("Use Item","Determine if %r can use item here");
	
	public String value;
	private String desc;
		
	CityPermEnum(String n,String desc)
	{
		this.value=n;
		this.desc = desc;
	}
	
	public String getDescription(CityPermRankEnum r)
	{
		return this.desc.replaceAll("%r", r.getDescName());
	}

}
