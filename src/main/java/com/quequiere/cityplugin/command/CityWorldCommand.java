package com.quequiere.cityplugin.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.LiteralText.Builder;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import com.quequiere.cityplugin.CityPlugin;
import com.quequiere.cityplugin.object.CityWorld;

public class CityWorldCommand implements CommandCallable
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
		
		String perm = "city.cityworld";
		if(!CityPlugin.hasPerm(p, perm))
		{
			CityPlugin.sendMessage("You need "+perm+" perm to do that !", TextColors.RED, p);
			return CommandResult.success();
		}
		

		CityWorld cw = CityWorld.getByName(p.getWorld().getName());

		if (cw == null)
		{
			CityPlugin.sendMessage("Can't find this world :o ! This is an error !", TextColors.RED, p);
			return CommandResult.success();

		}

		if (args.length == 0 || args.length == 1 && args[0].equals(""))
		{
			displayWorld(p, cw);
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
			else
			{
				CityPlugin.sendMessage("Dev error, you shouldn't be here !", TextColors.RED, p);
			}

		}

		return CommandResult.success();

	}

	public enum SubCommand
	{
		help
	};

	public static void displayWorld(Player p, CityWorld cw)
	{

		Builder builder = Text.builder("");

		// ATTENTION A CODE EN FONCTION DUNE PERM !
		boolean canModify = true;

		@SuppressWarnings("unused")
		TextColor itemColor;
		@SuppressWarnings("unused")
		String itemString;
		ArrayList<Object> objects = new ArrayList<>();

		builder.append(Text.of(TextColors.GOLD, "_____________[ World " + cw.getName() + " ]_____________\n"));

		cw.displayPerm(p, null, builder, canModify);

		builder.append(Text.of(TextColors.DARK_GREEN, "Use /cw help for more info "));

		p.sendMessage(builder.toText());

	}

	public static void displayHelp(Player p)
	{

		CityPlugin.sendMessage("List of possibilities: ", TextColors.RED, p);
		for (SubCommand sc : SubCommand.values())
		{
			CityPlugin.sendMessage("/cw " + sc.name(), TextColors.RED, p);
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
