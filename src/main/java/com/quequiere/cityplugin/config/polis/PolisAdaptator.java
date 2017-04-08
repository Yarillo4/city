package com.quequiere.cityplugin.config.polis;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3i;
import com.quequiere.cityplugin.CityPlugin;
import com.quequiere.cityplugin.Tools;
import com.quequiere.cityplugin.object.City;
import com.quequiere.cityplugin.object.Resident;

import io.github.hsyyid.polis.Polis;
import io.github.hsyyid.polis.config.ClaimsConfig;
import io.github.hsyyid.polis.config.Configs;
import io.github.hsyyid.polis.utils.ConfigManager;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;

public class PolisAdaptator
{
	public static void importPolisConfigs(Player p )
	{
		Polis polis = Polis.getPolis();
		Optional<UserStorageService> userStorage = Sponge.getServiceManager().provide(UserStorageService.class);

		Iterator arg0 = ConfigManager.getTeams().iterator();

		while (arg0.hasNext())
		{
			Object t = arg0.next();
			String teamName = String.valueOf(t);

			if(City.getCityByName(teamName)!=null)
			{
				CityPlugin.sendMessage("Failed to import "+teamName+" from Polis because the City already exists!", TextColors.RED, p);
				continue;
			}

			System.out.println("Try to import config for team: "+teamName);

			String leaderName = ConfigManager.getLeader(teamName);
			BigDecimal bank = ConfigManager.getBalance(teamName);
			ArrayList<String> members = ConfigManager.getMembers(teamName);
			ArrayList<Chunk> clist = importClaimConfigs(teamName);

			if(clist.size()<=0)
			{
				CityPlugin.sendMessage("Failed to import "+teamName+" from Polis because no chunks are claimed!", TextColors.RED, p);
				continue;
			}

			Optional<User> mayor=null;
			try
			{
				mayor =  userStorage.get().get(UUID.fromString(leaderName));

			}
			catch(IllegalArgumentException e)
			{
				CityPlugin.sendMessage("Fail to import "+teamName+" from Polis can't resolve player UUID:"+leaderName, TextColors.RED, p);
				continue;
			}


			if(!mayor.isPresent())
			{
				CityPlugin.sendMessage("Fail to import "+teamName+" from Polis cause mayor uuid doesn't exist ...", TextColors.RED, p);
				continue;
			}

			City c = City.tryCreateCity(teamName, p, true, mayor.get(), Optional.of(clist.get(0)),false);

			if(c!=null)
			{
				CityPlugin.sendMessage("Successfuly import "+teamName+" from Polis.", TextColors.GREEN, p);
			}
			else
			{
				CityPlugin.sendMessage("Fail to import "+teamName+" from Polis something goes wrong.", TextColors.RED, p);
			}

			for(String r:members)
			{
				Optional<User> mem=null;
				try
				{
					mem =  userStorage.get().get(UUID.fromString(r));
					if(mem.isPresent())
					{
						Resident resident = Resident.fromPlayerId(mem.get().getUniqueId());
						if(resident!=null)
						{
							c.addResident(resident);
						}

					}

				}
				catch(IllegalArgumentException e)
				{
					CityPlugin.sendMessage("Fail to import a player for "+teamName+" from Polis.", TextColors.RED, p);
					continue;
				}
			}

			Account account = CityPlugin.economyService.getOrCreateAccount(c.getNameEconomy()).get();
			account.setBalance(CityPlugin.economyService.getDefaultCurrency(), bank, Cause.of(NamedCause.source(p)));

			for(Chunk ch:clist)
			{
				c.forceClaimImport(ch);
			}


		}
	}

	private static ArrayList<Chunk> importClaimConfigs(String teamname)
	{

		ClaimsConfig claimsConfig = ClaimsConfig.getConfig();
		ArrayList<Chunk> list = new ArrayList<Chunk>();



		for (Object worldUUIDObject : Configs.getConfig(claimsConfig).getNode(new Object[] { "claims" ,teamname }).getChildrenMap().keySet())
		{
			String worldUUIDNname = (String) worldUUIDObject;

			for (Object chunkXObject : Configs.getConfig(claimsConfig).getNode(new Object[] { "claims",teamname ,worldUUIDNname }).getChildrenMap().keySet())
			{
				Integer chunkX = Integer.parseInt((String) chunkXObject);

				for (Object chunkZObject : Configs.getConfig(claimsConfig).getNode(new Object[] { "claims",teamname,worldUUIDNname ,String.valueOf(chunkX) }).getChildrenMap().keySet())
				{
					Integer chunkZ = Integer.parseInt((String) chunkZObject);
					CommentedConfigurationNode valueNode = Configs.getConfig(claimsConfig).getNode((Object[]) ("claims." + teamname + "." + worldUUIDNname + "." + String.valueOf(chunkX) + "." + String.valueOf(chunkZ)).split("\\."));
					boolean value = valueNode.getBoolean();
					if (value)
					{
						Optional<World> worldo = Sponge.getServer().getWorld(UUID.fromString(worldUUIDNname));

						if (worldo.isPresent())
						{
							System.out.println("Find a new claimed chunk for team: " + teamname + " on world " + worldo.get().getName() + " pos: " + chunkX + "/" + chunkZ);
							try
							{
								Optional<Chunk> c = Tools.getChunk(chunkX, chunkZ, worldo.get());
								if(c.isPresent())
								{
									list.add(c.get());
								}
								else
								{
									System.out.println("Error while loading chunk");
								}

							}
							catch(NoSuchElementException e)
							{
								System.out.println("Error while loading chunk ....");
							}


						}
						else
						{
							System.out.println("Error, missing world for team "+teamname+" for world uuid "+worldUUIDNname);
						}

					}

				}
			}

		}

		return list;

	}

}
