package com.quequiere.cityplugin.object;

public enum CityPermEnum
{
	BUILD("Build","Can %r build here"),
	DESTROY("Destroy","Can %r destroy here"),
	SWITCH("Switch","Can %r switch items here"),
	USEITEM("Use Item","Can %r use items here");

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
