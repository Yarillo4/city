package com.quequiere.cityplugin.object;

public enum CityPermRankEnum
{
	admin("A","you should not see that"),
	resident("R","resident of the city"),
	outsider("O","external players"),
	empire("E","member of the empire");
	
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
