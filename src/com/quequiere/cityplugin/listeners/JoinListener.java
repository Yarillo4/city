package com.quequiere.cityplugin.listeners;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.quequiere.cityplugin.CityPlugin;
import com.quequiere.cityplugin.object.City;
import com.quequiere.cityplugin.object.Resident;


public class JoinListener
{
	
	
	@Listener
	public void onConnectTaxCheck(ClientConnectionEvent.Join event)
	{
		int day =CityPlugin.generalConfig.getLastTaxCheck();
		Calendar cal = Calendar.getInstance();
		int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
		
		if(day!=dayOfMonth)
		{
			CityPlugin.generalConfig.setLastTaxCheck(dayOfMonth);
			 System.out.println("[CITY] New day of tax !");
			 Sponge.getGame().getServer().getBroadcastChannel().send(Text.builder("New day of tax for City !").color(TextColors.GREEN).build());
			 
			 for(City c:City.getLoaded())
			 {
					Account account = CityPlugin.economyService.getOrCreateAccount(c.getNameEconomy()).get();
					TransactionResult transactionResult = account.withdraw(CityPlugin.economyService.getDefaultCurrency(), c.getDailyCost(), Cause.of(NamedCause.source(event)));
					
					if (transactionResult.getResult() != ResultType.SUCCESS)
					{
						Sponge.getGame().getServer().getBroadcastChannel().send(Text.builder(c.getName()+ " has been destroyed !").color(TextColors.RED).build());
						c.destroy();
						continue ;
					}
					
					ArrayList<UUID> residentToRemove = new ArrayList<>();
					
					for(UUID id:c.getResidents())
					{
						Account raccount = CityPlugin.economyService.getOrCreateAccount(id).get();
						TransactionResult rtransactionResult = account.withdraw(CityPlugin.economyService.getDefaultCurrency(), c.getPlayerTaxe(), Cause.of(NamedCause.source(event)));
						
						if (transactionResult.getResult() != ResultType.SUCCESS && c.isRemovePlayerTax())
						{
							residentToRemove.add(id);
						}
					}
					
					for(UUID id:residentToRemove)
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
