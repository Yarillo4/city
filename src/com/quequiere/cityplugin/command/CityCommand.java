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
import org.spongepowered.api.text.LiteralText.Builder;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.text.action.SpongeCallbackHolder;

import com.quequiere.cityplugin.CityPlugin;
import com.quequiere.cityplugin.Tools;
import com.quequiere.cityplugin.object.City;
import com.quequiere.cityplugin.object.CityPermEnum;
import com.quequiere.cityplugin.object.CityPermRankEnum;
import com.quequiere.cityplugin.object.Resident;

public class CityCommand implements CommandCallable
{

	@Override
	public CommandResult process(CommandSource src, String arg) throws CommandException
	{

		if (!(src instanceof Player))
		{
			return CommandResult.empty();
		}

		String args[] = arg.split(" ");

		Player p = (Player) src;
		Resident r = Resident.fromPlayerId(p.getUniqueId());
		City c = r.getCity();

		if (args.length == 0 || args.length == 1 && args[0].equals(""))
		{
			if (c == null)
			{
				displayHelp(p);
			}
			else
			{
				displayCity(p, r, c);
			}
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

			if (subc.equals(SubCommand.create))
			{
				if (c != null)
				{
					CityPlugin.sendMessage("You are in a city, so you can't do that !", TextColors.RED, p);
				}
				else
				{

					if (args.length < 2)
					{
						CityPlugin.sendMessage("You need to give a name to the city !", TextColors.RED, p);
					}
					else
					{
						CityPlugin.sendMessage("Try to create city ...", TextColors.GREEN, p);
						City newc = City.tryCreateCity(args[1], p);
						if (newc == null)
						{
							CityPlugin.sendMessage("Something goes wrong, city has not been created !", TextColors.RED, p);
						}
						else
						{
							CityPlugin.sendMessage("City created !", TextColors.GREEN, p);
						}
					}
				}
			}
			else if (subc.equals(SubCommand.join))
			{
				if (c != null)
				{
					CityPlugin.sendMessage("You are in a city, so you can't do that !", TextColors.RED, p);
				}
				else
				{

					if (args.length < 2)
					{
						CityPlugin.sendMessage("You need to give a name of a city !", TextColors.RED, p);
					}
					else
					{
						String name = args[1];
						City named = City.getCityByName(name);
						if (named == null)
						{
							CityPlugin.sendMessage("This city doesn't exist !", TextColors.RED, p);
						}
						else
						{
							if (named.isOpenJoin())
							{
								named.addResident(r);
								CityPlugin.sendMessage("You joined the city !", TextColors.GREEN, p);
							}
							else
							{
								CityPlugin.sendMessage("You need to be invited to join this city !", TextColors.RED, p);
							}
						}
					}
				}
			}
			else if (subc.equals(SubCommand.claim))
			{
				if (c == null)
				{
					CityPlugin.sendMessage("You need to be in a city to do that !", TextColors.RED, p);
				}
				else
				{
					if (!c.hasAssistantPerm(r))
					{
						CityPlugin.sendMessage("You need assistant perm to do that !", TextColors.RED, p);
					}
					else
					{
						CityPlugin.sendMessage("Starting claim process ...", TextColors.GREEN, p);
						c.tryToClaimHere(p);
					}
				}
			}
			else if (subc.equals(SubCommand.leave))
			{
				if (c == null)
				{
					CityPlugin.sendMessage("You need to be in a city to do that !", TextColors.RED, p);
				}
				else if (c.hasMayorPerm(r))
				{
					CityPlugin.sendMessage("You are the mayor, you can't leave your city !", TextColors.RED, p);
				}
				else
				{
					c.removeResident(p.getUniqueId());
					r.getCache().initializeCache();
					CityPlugin.sendMessage("You leaved the city !", TextColors.GREEN, p);
				}
			}
			else if (subc.equals(SubCommand.help))
			{
				displayHelp(p);
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
		create, claim, leave, help, join
	};

	public static void displayCity(Player p, Resident r, City c)
	{
		Builder builder = Text.builder("");
		boolean canModify = false;

		if (c.hasAssistantPerm(r))
		{
			canModify = true;
		}

		TextColor itemColor;
		String itemString;
		ArrayList<Object> objects = new ArrayList<>();

		builder.append(Text.of(TextColors.GOLD, "_____________[ " + c.getName() + " ]_____________\n"));

		// --------------------------------------------------------------------------------------------

		builder.append(Text.of(TextColors.DARK_GREEN, "City chunks: "));
		builder.append(Text.of(TextColors.GREEN, c.getClaimedChunk().size() + " / XX\n"));

		// --------------------------------------------------------------------------------------------

		DisplayPlayerListCallBack.displayPlayerList(builder, p, r, c);

		// --------------------------------------------------------------------------------------------

		itemColor = c.isOpenJoin() ? TextColors.GREEN : TextColors.RED;
		itemString = c.isOpenJoin() ? "Open" : "Invitation only";

		builder.append(Text.of(TextColors.DARK_GREEN, "Join: "));
		builder.append(Text.of(TextColors.GRAY, "["));

		objects.clear();
		if (canModify)
			objects.add(TextActions.executeCallback(source -> {
				c.setOpenJoin(!c.isOpenJoin());
				displayCity(p, r, c);
			}));

		objects.add(TextActions.showText(Text.of("Determine if players can directly join the city without invitation")));
		objects.add(itemColor);
		objects.add(itemString);

		builder.append(Text.of(objects.toArray()));

		builder.append(Text.of(TextColors.GRAY, "]"));
		builder.append(Text.of(TextColors.GRAY, "\n"));

		// --------------------------------------------------------------------------------------------

		c.displayPerm(p, r, builder,canModify);

		// --------------------------------------------------------------------------------------------

		p.sendMessage(builder.toText());
	}

	public static void displayHelp(Player p)
	{

		CityPlugin.sendMessage("List of possibilities: ", TextColors.RED, p);
		for (SubCommand sc : SubCommand.values())
		{
			CityPlugin.sendMessage("/city " + sc.name(), TextColors.RED, p);
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
