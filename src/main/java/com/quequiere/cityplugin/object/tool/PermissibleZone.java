package com.quequiere.cityplugin.object.tool;

import java.util.ArrayList;
import java.util.HashMap;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.LiteralText.Builder;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import com.quequiere.cityplugin.CityPlugin;
import com.quequiere.cityplugin.command.CityChunkCommand;
import com.quequiere.cityplugin.command.CityCommand;
import com.quequiere.cityplugin.command.CityWorldCommand;
import com.quequiere.cityplugin.object.City;
import com.quequiere.cityplugin.object.CityChunk;
import com.quequiere.cityplugin.object.CityPermBooleanEnum;
import com.quequiere.cityplugin.object.CityPermEnum;
import com.quequiere.cityplugin.object.CityPermRankEnum;
import com.quequiere.cityplugin.object.CityWorld;
import com.quequiere.cityplugin.object.Resident;

public abstract class PermissibleZone
{

	private HashMap<CityPermEnum, HashMap<CityPermRankEnum, Boolean>> cityPerm;
	private HashMap<CityPermBooleanEnum, Boolean> cityBooleanPerm;

	public void initCityBooleanPerm()
	{
		cityBooleanPerm = new HashMap<CityPermBooleanEnum, Boolean>();
		 for(CityPermBooleanEnum perm:CityPermBooleanEnum.values())
		 {
			 if(perm.equals(CityPermBooleanEnum.pvp))
			 {
				 cityBooleanPerm.put(perm, false);
			 }
			 else
			 {
				 cityBooleanPerm.put(perm, true);
			 }
			 
		 }	
		 
		 this.updatePermission();
	}
	
	

	public HashMap<CityPermBooleanEnum, Boolean> getCityBooleanPerm()
	{
		if(cityBooleanPerm==null)
		{
			this.initCityBooleanPerm();
			this.updatePermission();
		}
		return cityBooleanPerm;
	}



	public void initCityPerm()
	{
		this.cityPerm = new HashMap<CityPermEnum, HashMap<CityPermRankEnum, Boolean>>();
		for (CityPermEnum perm : CityPermEnum.values())
		{
			HashMap<CityPermRankEnum, Boolean> permtypes = new HashMap<CityPermRankEnum, Boolean>();

			for (CityPermRankEnum permrank : CityPermRankEnum.values())
			{
				if(permrank.equals(CityPermRankEnum.admin))
					continue;
				permtypes.put(permrank, false);
			}

			this.cityPerm.put(perm, permtypes);
		}
		
		
		this.updatePermission();
	}
	
	

	public HashMap<CityPermEnum, HashMap<CityPermRankEnum, Boolean>> getCityPerm()
	{
		if (this.cityPerm == null)
		{
			this.initCityPerm();
			this.updatePermission();
		}
		return this.cityPerm;
	}

	public boolean canDoAction(CityPermEnum perm, CityPermRankEnum rank)
	{

		if (rank.equals(CityPermRankEnum.admin))
		{
			return true;
		}

		HashMap<CityPermRankEnum, Boolean> map = this.getCityPerm().get(perm);
		
		if(map==null ||  map.get(rank) == null)
		{
			this.initCityPerm();
			this.updatePermission();
			map = this.getCityPerm().get(perm);
		}
		
		return map.get(rank);
	}

	public boolean isActive(CityPermBooleanEnum citypermboolean)
	{
		return this.getCityBooleanPerm().get(citypermboolean);
	}
	
	public void setBooleanPerm(CityPermBooleanEnum citypermboolean, boolean value)
	{
		this.getCityBooleanPerm().remove(citypermboolean);
		this.getCityBooleanPerm().put(citypermboolean, value);
		this.updatePermission();
	}
	

	public void setPermission(CityPermEnum perm, CityPermRankEnum rank, boolean value)
	{
		HashMap<CityPermRankEnum, Boolean> map = this.getCityPerm().get(perm);
		map.remove(rank);
		map.put(rank, value);
		this.updatePermission();
	}

	public abstract void updatePermission();

	public void displayPerm(Player p, Resident r, Builder builder, boolean canModify)
	{
		ArrayList<Object> objects = new ArrayList<>();
		
		for (CityPermBooleanEnum boolPerm : CityPermBooleanEnum.values())
		{
			boolean can = this.isActive(boolPerm);
			objects.clear();
			if (canModify)
				objects.add(TextActions.executeCallback(source -> {
					boolean old = this.isActive(boolPerm);
					
					if (this instanceof City)
					{
						City c = (City) this;
						if(c.hasAssistantPerm(r))
						{
							this.setBooleanPerm(boolPerm, !old);
							CityCommand.displayCity(p, r, (City) this);
						}
					}
					else if (this instanceof CityChunk)
					{
						this.setBooleanPerm(boolPerm, !old);
						CityChunkCommand.displayChunk(p, r, (CityChunk) this); 
					}
					else if (this instanceof CityWorld)
					{
						this.setBooleanPerm(boolPerm, !old);
						CityWorldCommand.displayWorld(p, CityWorld.getByName(p.getWorld().getName()));
					}
					else
					{
						CityPlugin.sendMessage("Error, not dev part 245", TextColors.RED, p);
					}

				}));
			
			
			objects.add(TextActions.showText(Text.of(boolPerm.overText)));
			objects.add(Text.of(TextColors.DARK_GREEN,boolPerm.displayName,": ",TextColors.GRAY,"["));
			objects.add(can ? TextColors.GREEN : TextColors.RED);
			objects.add(can ? "ON" : "OFF");
			objects.add(Text.of(TextColors.GRAY,"] "));
		
			builder.append(Text.of(objects.toArray()));
			
		}
		
		objects.clear();

		 
		builder.append(Text.of(TextColors.DARK_GREEN, "\nPermission: "));

		for (CityPermEnum perm : CityPermEnum.values())
		{
			builder.append(Text.of(TextColors.GREEN, perm.value + ": "));

			for (CityPermRankEnum rank : CityPermRankEnum.values())
			{
				if (rank.equals(CityPermRankEnum.admin))
					continue;

				boolean can = this.canDoAction(perm, rank);

				objects.clear();
				if (canModify)
					objects.add(TextActions.executeCallback(source -> {
						boolean old = this.canDoAction(perm, rank);
						
						if (this instanceof City)
						{
							City c = (City) this;
							if(c.hasAssistantPerm(r))
							{
								this.setPermission(perm, rank, !old);
								CityCommand.displayCity(p, r, (City) this);
							}
							
						}
						else if (this instanceof CityChunk)
						{
							this.setPermission(perm, rank, !old);
							CityChunkCommand.displayChunk(p, r, (CityChunk) this); 
						}
						else if (this instanceof CityWorld)
						{
							this.setPermission(perm, rank, !old);
							CityWorldCommand.displayWorld(p, CityWorld.getByName(p.getWorld().getName()));
						}
						else
						{
							CityPlugin.sendMessage("Error, not dev part 245", TextColors.RED, p);
						}

					}));
				objects.add(TextActions.showText(Text.of(perm.getDescription(rank))));
				objects.add(can ? TextColors.GREEN : TextColors.GRAY);
				objects.add(can ? rank.letter : "-");
				builder.append(Text.of(objects.toArray()));

			}

			builder.append(Text.of(" "));
		}

		builder.append(Text.of("\n"));
	}
	

}
