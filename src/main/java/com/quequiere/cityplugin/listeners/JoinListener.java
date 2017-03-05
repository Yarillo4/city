package com.quequiere.cityplugin.listeners;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.Ambient;
import org.spongepowered.api.entity.living.animal.Animal;
import org.spongepowered.api.entity.living.monster.Monster;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Chunk;

import com.quequiere.cityplugin.CityPlugin;
import com.quequiere.cityplugin.Tools;
import com.quequiere.cityplugin.object.City;
import com.quequiere.cityplugin.object.CityChunk;
import com.quequiere.cityplugin.object.CityPermBooleanEnum;
import com.quequiere.cityplugin.object.CityWorld;
import com.quequiere.cityplugin.object.Resident;
import com.quequiere.cityplugin.object.tool.PermissibleZone;

public class JoinListener
{

	public static HashMap<Chunk, HashMap<CityPermBooleanEnum,Boolean>> chunkPerms = new HashMap<Chunk, HashMap<CityPermBooleanEnum,Boolean>>();
	
	@Listener
	public void worldLoad(SpawnEntityEvent event)
	{
		for (Entity e : event.getEntities())
		{
			
			if(e.getClass().toString().contains("entities.pixelmon.EntityPixelmon"))
			{
				// !!!! For pixelmon need to cancel event, because cause lags if is checked
				if(CityPlugin.generalConfig.isdissociatePixelmonEntityFromCreatureSpawn()||true)
				{
					return;
				}
			}
			
			Optional<Chunk> co = Tools.getChunk(e.getLocation());
			if (co.isPresent())
			{
				Chunk c = co.get();

				if(!chunkPerms.containsKey(c))
				{
					loadBooleanPermForChunk(c);
				}
					
				if(e instanceof Monster)
				{
					if(!chunkPerms.get(c).get(CityPermBooleanEnum.spawnMob))
					{
						event.setCancelled(true);
					}
				}
				else if(e instanceof Animal || e instanceof Ambient)
				{
					if(!chunkPerms.get(c).get(CityPermBooleanEnum.spawnCreature))
					{
						event.setCancelled(true);
					}
				}
				
			}

		}
	}
	
	public static void loadBooleanPermForChunk(Chunk c)
	{
		PermissibleZone pz = null;
		City targetCity = City.getCityFromChunk(c);
		if(targetCity==null)
		{
			pz = CityWorld.getByName(c.getWorld().getName());
		}
		else
		{
			CityChunk cc = targetCity.getChunck(c);
			
			if (cc.getResident() == null)
			{
				pz=targetCity;
			}
			else
			{
				pz=cc;
			}
		}
		
		HashMap<CityPermBooleanEnum,Boolean> map = new HashMap<CityPermBooleanEnum,Boolean>();
		for(CityPermBooleanEnum perms:CityPermBooleanEnum.values())
		{
			if(CityPlugin.generalConfig.isActivePerm(perms).isPresent())
			{
				map.put(perms, CityPlugin.generalConfig.isActivePerm(perms).get());
			}
			else
			{
				map.put(perms, pz.isActive(perms));
			}
			
			
		}
		chunkPerms.put(c, map);
	}
	

	@Listener
	public void worldLoad(LoadWorldEvent event)
	{
		CityWorld.loadWorld(event.getTargetWorld().getName());
	}

	@Listener
	public void onConnectTaxCheck(ClientConnectionEvent.Join event)
	{
		int day = CityPlugin.generalConfig.getLastTaxCheck();
		Calendar cal = Calendar.getInstance();
		int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);

		if (day != dayOfMonth)
		{
			CityPlugin.generalConfig.setLastTaxCheck(dayOfMonth);
			System.out.println("[CITY] A new tax day has arrived! All cities have paid taxes!");
			Sponge.getGame().getServer().getBroadcastChannel().send(Text.builder("A new tax day has arrived! All cities have paid taxes!").color(TextColors.GREEN).build());

			@SuppressWarnings("unchecked")
			ArrayList<City> cites = (ArrayList<City>) City.getLoaded().clone();

			for (City c : cites)
			{
				Account account = CityPlugin.economyService.getOrCreateAccount(c.getNameEconomy()).get();
				TransactionResult transactionResult = account.withdraw(CityPlugin.economyService.getDefaultCurrency(), c.getTaxDailyCost(), Cause.of(NamedCause.source(event)));

				if (transactionResult.getResult() != ResultType.SUCCESS)
				{
					Sponge.getGame().getServer().getBroadcastChannel().send(Text.builder(c.getName() + " could not afford tax today and has fallen!").color(TextColors.RED).build());
					c.destroy();
					continue;
				}

				ArrayList<UUID> residentToRemove = new ArrayList<>();

				for (UUID id : c.getResidents())
				{
					Account raccount = CityPlugin.economyService.getOrCreateAccount(id).get();
					TransactionResult rtransactionResult = raccount.withdraw(CityPlugin.economyService.getDefaultCurrency(), c.getPlayerTaxe(), Cause.of(NamedCause.source(event)));

					if (rtransactionResult.getResult() != ResultType.SUCCESS && c.isRemovePlayerTax())
					{
						if (!c.hasAssistantPerm(Resident.fromPlayerId(id)))
						{
							residentToRemove.add(id);
						}

					}
				}

				for (UUID id : residentToRemove)
				{
					c.removeResident(id);
				}

			}

		}
	}

	@Listener
	public void spawnInfo(ClientConnectionEvent.Join event)
	{
		Player p = event.getTargetEntity();
		Resident.fromPlayerId(p.getUniqueId()).getCache().initializeCache();
	}

	@Listener
	public void spawnInfo(ClientConnectionEvent.Disconnect event)
	{
		Player p = event.getTargetEntity();
		Resident.fromPlayerId(p.getUniqueId()).remove();
	}
}
