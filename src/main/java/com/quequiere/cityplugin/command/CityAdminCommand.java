package com.quequiere.cityplugin.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import com.quequiere.cityplugin.CityPlugin;
import com.quequiere.cityplugin.PluginInfo;
import com.quequiere.cityplugin.config.CityGeneralConfig;
import com.quequiere.cityplugin.config.polis.PolisAdaptator;
import com.quequiere.cityplugin.object.City;
import com.quequiere.cityplugin.object.CityRankEnum;
import com.quequiere.cityplugin.object.CityWorld;
import com.quequiere.cityplugin.object.Resident;

public class CityAdminCommand implements CommandCallable
{
	
	public static ArrayList<Player> bindedWhitelistDestroy = new ArrayList<Player>();

	@Override
	public CommandResult process(CommandSource src, String arg) throws CommandException
	{

		if (!(src instanceof Player))
		{
			return CommandResult.empty();
		}

		String args[] = arg.split(" ");

		Player p = (Player) src;

		String perm = "city.cityadmin";
		if (!CityPlugin.hasPerm(p, perm))
		{
			CityPlugin.sendMessage("You need the " + perm + " node to do that !", TextColors.RED, p);
			return CommandResult.success();
		}

		CityWorld cw = CityWorld.getByName(p.getWorld().getName());

		if (cw == null)
		{
			CityPlugin.sendMessage("Cannot find this CityWorld!", TextColors.RED, p);
			return CommandResult.success();

		}

		if (args.length == 0 || args.length == 1 && args[0].equals(""))
		{
			displayHelp(p);
		}
		else
		{
			SubCommand subc = null;

			try
			{
				subc = SubCommand.valueOf(args[0]);
			}
			catch (IllegalArgumentException e)
			{
				displayHelp(p);
				return CommandResult.success();
			}

			if (subc.equals(SubCommand.help))
			{
				displayHelp(p);
			}
			else if (subc.equals(SubCommand.polisimport))
			{
				try
				{
					Class.forName("io.github.hsyyid.polis.Polis");
				}
				catch (ClassNotFoundException e)
				{
					CityPlugin.sendMessage("This command needs polis to work!", TextColors.RED, p);
					return CommandResult.success();
				}

				if (City.getLoaded().size() <= 0)
				{
					PolisAdaptator.importPolisConfigs(p);
				}
				else
				{
					CityPlugin.sendMessage("Sorry, you only can run this command on a server with cities!", TextColors.RED, p);
				}

			}
			else if (subc.equals(SubCommand.addbonusclaim))
			{

				if (args.length < 3)
				{
					CityPlugin.sendMessage("You need to give a name to the city and a claim bonus to add!", TextColors.RED, p);
				}
				else
				{
					City c = City.getCityByName(args[1]);


					if (c == null)
					{
						CityPlugin.sendMessage("We can't find city:" + args[1], TextColors.RED, p);
					}
					else
					{
						try
						{
							int nb = Integer.parseInt(args[2]);
							c.setBonusClaim(c.getBonusClaim()+nb);
							CityPlugin.sendMessage("Bonus claim added and now is set to " + args[1], TextColors.GREEN, p);
						}
						catch(NumberFormatException e)
						{
							CityPlugin.sendMessage("You need to give an integer: " + args[2], TextColors.RED, p);
						}
					}
				}
			}

			else if (subc.equals(SubCommand.setbonusclaim))
			{

				if (args.length < 3)
				{
					CityPlugin.sendMessage("You need to give a name to the city and a claim bonus!", TextColors.RED, p);
				}
				else
				{
					City c = City.getCityByName(args[1]);


					if (c == null)
					{
						CityPlugin.sendMessage("We can't find city:" + args[1], TextColors.RED, p);
					}
					else
					{
						try
						{
							int nb = Integer.parseInt(args[2]);
							c.setBonusClaim(nb);
							CityPlugin.sendMessage("Bonus claim set " + args[1], TextColors.GREEN, p);
						}
						catch(NumberFormatException e)
						{
							CityPlugin.sendMessage("You need to give an integer: " + args[2], TextColors.RED, p);
						}
					}
				}
			}

			else if (subc.equals(SubCommand.setmayor))
			{

				if (args.length < 3)
				{
					CityPlugin.sendMessage("You need to give a name to the city and a playername or 'none' to set as admin city (don't pay tax)!", TextColors.RED, p);
				}
				else
				{
					City c = City.getCityByName(args[1]);
					Optional<UserStorageService> userStorage = Sponge.getServiceManager().provide(UserStorageService.class);


					if(args[2].equals("none"))
					{
						c.setNoMayor();
						CityPlugin.sendMessage("This is an admin city now.", TextColors.GREEN, p);
						return CommandResult.success();
					}


					Optional<User> uo = null;
					try
					{
						uo = userStorage.get().get(args[2]);
					}
					catch(IllegalArgumentException e)
					{
						CityPlugin.sendMessage("Can't find player !", TextColors.RED, p);
						return CommandResult.success();
					}


					if (c == null)
					{
						CityPlugin.sendMessage("We can't find city:" + args[1], TextColors.RED, p);
					}
					else if (!uo.isPresent())
					{
						CityPlugin.sendMessage("We can't find player:" + args[2], TextColors.RED, p);
					}
					else
					{
						// Resident r= Resident.fromPlayerId();
						for (City scan : City.getLoaded())
						{
							if (scan.hasResident(uo.get().getUniqueId()) && !scan.equals(c))
							{
								CityPlugin.sendMessage("This player is on an other city. Need to be in the target city or without city", TextColors.RED, p);
								return CommandResult.success();
							}
						}

						Resident r = Resident.fromPlayerId(uo.get().getUniqueId());

						if (r == null)
						{
							CityPlugin.sendMessage("Player should be online to do that !", TextColors.RED, p);
							return CommandResult.success();
						}

						if (!c.hasResident(r.getId()))
						{
							c.addResident(r);
						}

						for (UUID tr : c.getResidents())
						{
							Resident trr = Resident.fromPlayerId(tr);
							if (trr.getRank().equals(CityRankEnum.mayor))
							{
								trr.setRank(CityRankEnum.assistant);
							}
						}

						r.setRank(CityRankEnum.mayor);
						CityPlugin.sendMessage("Player is now mayor", TextColors.GREEN, p);
						c.save();

					}
				}
			}
			else if (subc.equals(SubCommand.adminbypass))
			{
				Resident r = Resident.fromPlayerId(p.getUniqueId());
				r.getCache().setAdminBypass(!r.getCache().isAdminBypass());
				CityPlugin.sendMessage("Admin bypass is now set to "+r.getCache().isAdminBypass(), TextColors.GREEN, p);
			}
			else if (subc.equals(SubCommand.whitelistdestroy))
			{
				if(bindedWhitelistDestroy.contains(p))
				{
					bindedWhitelistDestroy.remove(p);
					CityPlugin.sendMessage("Bind removed !", TextColors.GREEN, p);
				}
				else
				{
					bindedWhitelistDestroy.add(p);
					CityPlugin.sendMessage("Now click on the block to add it on the whitelist destroy", TextColors.GREEN, p);
				}
			}
			else if (subc.equals(SubCommand.reloadConf))
			{
				CityGeneralConfig.loadConfig();
				CityPlugin.sendMessage("Configuration reloaded !", TextColors.GREEN, p);
			}

			else
			{
				CityPlugin.sendMessage("Dev error, you shouldn't be here !", TextColors.RED, p);
			}

		}

		return CommandResult.success();

	}

	public enum SubCommand
	{
		help, polisimport,setmayor,setbonusclaim,adminbypass,addbonusclaim,reloadConf,whitelistdestroy
	};

	public static void displayHelp(Player p)
	{

		CityPlugin.sendMessage("City plugin " + PluginInfo.VERSION, TextColors.GREEN, p);
		CityPlugin.sendMessage("List of possibilities: ", TextColors.RED, p);
		for (SubCommand sc : SubCommand.values())
		{
			CityPlugin.sendMessage("/ca " + sc.name(), TextColors.RED, p);
		}
	}

	@Override
	public List<String> getSuggestions(CommandSource arg0, String arg1, Location<World> arg2) throws CommandException
	{
		return null;
	}

	@Override
	public boolean testPermission(CommandSource arg0)
	{
		return false;
	}

	@Override
	public Optional<Text> getShortDescription(CommandSource arg0)
	{
		return null;
	}

	@Override
	public Optional<Text> getHelp(CommandSource arg0)
	{
		return null;
	}

	@Override
	public Text getUsage(CommandSource arg0)
	{
		return null;
	}

}
