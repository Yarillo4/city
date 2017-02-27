package com.quequiere.cityplugin.listeners;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.quequiere.cityplugin.CityPlugin;
import com.quequiere.cityplugin.object.City;
import com.quequiere.cityplugin.object.CityWorld;
import com.quequiere.cityplugin.object.Resident;


public class JoinListener
{
	@Listener
	public void worldLoad(LoadWorldEvent event)
	{
		CityWorld.loadWorld(event.getTargetWorld().getName());
	}

	@Listener
	public void onConnectTaxCheck(ClientConnectionEvent.Join event)
	{
		int day =CityPlugin.generalConfig.getLastTaxCheck();
		Calendar cal = Calendar.getInstance();
		int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);

		if(day!=dayOfMonth)
		{
			CityPlugin.generalConfig.setLastTaxCheck(dayOfMonth);
			 System.out.println("[CITY] A new tax day has arrived! All cities have paid taxes!");
			 Sponge.getGame().getServer().getBroadcastChannel().send(Text.builder("A new tax day has arrived! All cities have paid taxes!").color(TextColors.GREEN).build());

			 @SuppressWarnings("unchecked")
			ArrayList<City> cites = (ArrayList<City>) City.getLoaded().clone();

			 for(City c:cites)
			 {
					Account account = CityPlugin.economyService.getOrCreateAccount(c.getNameEconomy()).get();
					TransactionResult transactionResult = account.withdraw(CityPlugin.economyService.getDefaultCurrency(), c.getTaxDailyCost(), Cause.of(NamedCause.source(event)));

					if (transactionResult.getResult() != ResultType.SUCCESS)
					{
						Sponge.getGame().getServer().getBroadcastChannel().send(Text.builder(c.getName()+ " could not afford tax today and has fallen!").color(TextColors.RED).build());
						c.destroy();
						continue ;
					}

					ArrayList<UUID> residentToRemove = new ArrayList<>();

					for(UUID id:c.getResidents())
					{
						Account raccount = CityPlugin.economyService.getOrCreateAccount(id).get();
						TransactionResult rtransactionResult = raccount.withdraw(CityPlugin.economyService.getDefaultCurrency(), c.getPlayerTaxe(), Cause.of(NamedCause.source(event)));

						if (rtransactionResult.getResult() != ResultType.SUCCESS && c.isRemovePlayerTax())
						{
							if(!c.hasAssistantPerm(Resident.fromPlayerId(id)))
							{
								residentToRemove.add(id);
							}

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
