package com.quequiere.cityplugin.object;

public enum CityPermRankEnum
{
	admin("A","Administrator"),
	resident("R","Resident"),
	outsider("O","Outsider"),
	empire("E","Member of Empire");

	public String letter;
	private String descName;

	CityPermRankEnum(String letter,String descname)
	{
		this.letter=letter;
		this.descName=descname;
	}

	public String getDescName()
	{
		return this.descName;
	}

}
