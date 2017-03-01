package com.quequiere.cityplugin.command;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.text.LiteralText.Builder;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import com.quequiere.cityplugin.CityPlugin;
import com.quequiere.cityplugin.Tools;
import com.quequiere.cityplugin.object.City;
import com.quequiere.cityplugin.object.CityChunk;
import com.quequiere.cityplugin.object.Resident;

public class CityChunkCommand implements CommandCallable
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
		Optional<Chunk> cho = Tools.getChunk(p.getLocation());
		
		if(!cho.isPresent())
		{
			CityPlugin.sendMessage("We can't find the chunk where you are!", TextColors.RED, p);
			return CommandResult.success();
		}
		
		Chunk ch = cho.get();
		
		

		City city = City.getCityFromChunk(ch);

		if (city == null)
		{
			CityPlugin.sendMessage("This is Wilderness!", TextColors.DARK_GREEN, p);
			return CommandResult.success();

		}

		CityChunk cc = city.getChunck(ch);

		if (args.length == 0 || args.length == 1 && args[0].equals(""))
		{
			displayChunk(p, r, cc);
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

			if (subc.equals(SubCommand.sell))
			{
				if (args.length < 2)
				{
					CityPlugin.sendMessage("You need to give a price!", TextColors.RED, p);
				}
				else
				{
					int price = 0;
					try
					{
						price = Integer.parseInt(args[1]);
					}
					catch (NumberFormatException e)
					{
						CityPlugin.sendMessage("Invalid price!", TextColors.RED, p);
						return CommandResult.success();
					}

					if (city.hasAssistantPerm(r) || cc.isOwner(r.getId()))
					{
						cc.setSellPrice(price);
						CityPlugin.sendMessage("Sell price modified!", TextColors.GREEN, p);
					}
					else
					{
						CityPlugin.sendMessage("You need to be assistant or owner to do that!", TextColors.RED, p);
					}

				}
			}
			else if (subc.equals(SubCommand.help))
			{
				displayHelp(p);
			}
			else
			{
				CityPlugin.sendMessage("Please contact the plugin developer!", TextColors.RED, p);
			}

		}

		return CommandResult.success();

	}

	public enum SubCommand
	{
		help, sell
	};

	public static void displayChunk(Player p, Resident r, CityChunk cc)
	{
		City c = cc.getCity();
		Builder builder = Text.builder("");
		boolean canModify = false;

		if (c.hasAssistantPerm(r))
		{
			canModify = true;
		}

		@SuppressWarnings("unused")
		TextColor itemColor;
		@SuppressWarnings("unused")
		String itemString;
		ArrayList<Object> objects = new ArrayList<>();

		builder.append(Text.of(TextColors.GOLD, "Land owned by " + c.getName() + ".\n"));

		if (cc.getResident() != null)
		{
			builder.append(Text.of(TextColors.DARK_GREEN, "Resident: "));

			User u = Tools.getUser(cc.getResident());

			builder.append(Text.of(TextColors.GREEN, u.getName() + "\n"));
			cc.displayPerm(p, r, builder, canModify);
		}
		else
		{
			builder.append(Text.of(TextColors.DARK_GREEN, "You are not a resident here, use /c for more info.\n"));
		}

		if (cc.getSellPrice() > 0)
		{
			builder.append(Text.of(TextColors.DARK_GREEN, "Sell price: "));
			builder.append(Text.of(TextColors.GREEN, cc.getSellPrice() + ""));

			builder.append(Text.of(TextColors.GRAY, " ["));

			objects.clear();

			objects.add(TextActions.executeCallback(source -> {

				if (c.hasResident(r.getId()))
				{

					Account originaccount = null;
					if (cc.getResident() == null)
					{
						originaccount=CityPlugin.economyService.getOrCreateAccount(c.getNameEconomy()).get();
					}
					else
					{
						originaccount=CityPlugin.economyService.getOrCreateAccount(cc.getResident()).get();
					}

					Account newaccount = CityPlugin.economyService.getOrCreateAccount(c.getNameEconomy()).get();

					TransactionResult transactionResult = originaccount.transfer(newaccount, CityPlugin.economyService.getDefaultCurrency(), new BigDecimal(cc.getSellPrice()), Cause.of(NamedCause.source(p)));

					if (transactionResult.getResult() != ResultType.SUCCESS)
					{
						CityPlugin.sendMessage("Transaction failed!", TextColors.RED, p);
					}
					else
					{
						CityPlugin.sendMessage("Transaction sucessful!", TextColors.GREEN, p);
						cc.setSellPrice(-1);
						cc.setResident(r.getId());
						CityPlugin.sendMessage("You bought this land!", TextColors.GREEN, p);
					}

				}
				else
				{
					CityPlugin.sendMessage("You need to be resident of the city to do that!", TextColors.RED, p);
				}

			}));

			objects.add(TextActions.showText(Text.of("Buy this land")));
			objects.add(TextColors.AQUA);
			objects.add("Buy");

			builder.append(Text.of(objects.toArray()));

			builder.append(Text.of(TextColors.GRAY, "]"));
			builder.append(Text.of(TextColors.GRAY, "\n"));

		}
		else
		{
			builder.append(Text.of(TextColors.DARK_GREEN, "Sell price: "));
			builder.append(Text.of(TextColors.GREEN, "Not for sale\n"));
		}

		builder.append(Text.of(TextColors.DARK_GREEN, "Use /cc help for more info."));

		p.sendMessage(builder.toText());

	}

	public static void displayHelp(Player p)
	{

		CityPlugin.sendMessage("List of possibilities: ", TextColors.RED, p);
		for (SubCommand sc : SubCommand.values())
		{
			CityPlugin.sendMessage("/cc " + sc.name(), TextColors.RED, p);
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
