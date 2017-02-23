package com.quequiere.cityplugin.command;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
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
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import com.quequiere.cityplugin.CityPlugin;
import com.quequiere.cityplugin.object.City;
import com.quequiere.cityplugin.object.CityChunk;
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
			else if (subc.equals(SubCommand.info))
			{
					if (args.length < 2)
					{
						CityPlugin.sendMessage("You need to give a name to the city !", TextColors.RED, p);
					}
					else
					{
						City ci = City.getCityByName(args[1]);
						if(ci!=null)
						{
							displayCity(p, r, ci);
						}
						else
						{
							CityPlugin.sendMessage("Can't find this city", TextColors.RED, p);
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
			else if (subc.equals(SubCommand.destroy))
			{
				if (c == null)
				{
					CityPlugin.sendMessage("You need to be in a city to do that", TextColors.RED, p);
				}
				else
				{

					if (args.length < 2)
					{
						CityPlugin.sendMessage("add 'confirm' to this command to destroy definitly your city", TextColors.RED, p);
					}
					else
					{
						if(c.hasMayorPerm(r))
						{
							String s = args[1];

							if(s.equalsIgnoreCase("confirm"))
							{
								CityPlugin.sendMessage("Start city destroy process ", TextColors.GREEN, p);
								c.destroy();
							}
						}
						else
						{
							CityPlugin.sendMessage("You need to be mayor to do that !", TextColors.RED, p);
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
			else if (subc.equals(SubCommand.teleport))
			{
				if (c == null)
				{
					CityPlugin.sendMessage("You need to be in a city to do that !", TextColors.RED, p);
				}
				else
				{
					p.setLocation(c.getSpawn());
					CityPlugin.sendMessage("You has been teleported to the city !", TextColors.GREEN, p);
				}
			}
			else if (subc.equals(SubCommand.unclaim))
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
						CityPlugin.sendMessage("Starting unclaim process ...", TextColors.GREEN, p);
						
						CityChunk cc = c.getChunck(r.getChunk());
						
						if(cc!=null)
						{
							c.unclaimChunk(cc);
							CityPlugin.sendMessage("Unclaimed chunk !", TextColors.GREEN, p);
						}
						else
						{
							CityPlugin.sendMessage("This is not a city chunk !", TextColors.RED, p);
						}
						
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
			else if (subc.equals(SubCommand.setteleport))
			{
				if (c == null)
				{
					CityPlugin.sendMessage("You need to be in a city to do that !", TextColors.RED, p);
				}
				else if (c.hasAssistantPerm(r))
				{
					c.setSpawn(p.getLocation());
					CityPlugin.sendMessage("Teleport location set !", TextColors.GREEN, p);
				}
				else
				{
					CityPlugin.sendMessage("You need to be assistant to do that !", TextColors.RED, p);
					return CommandResult.success();
				}
			}
			else if (subc.equals(SubCommand.settax))
			{
				if (c == null)
				{
					CityPlugin.sendMessage("You need to be in a city to do that !", TextColors.RED, p);
				}
				else
				{

					if (args.length < 2)
					{
						CityPlugin.sendMessage("You need to give an amount !", TextColors.RED, p);
					}
					else
					{
						String name = args[1];
						double amount = 0;
						try
						{
							amount=Double.parseDouble(name);
							
							if(amount<=0)
							{
								CityPlugin.sendMessage("Invalid amount", TextColors.RED, p);
								return CommandResult.success();
							}
						}
						catch(NumberFormatException e)
						{
							CityPlugin.sendMessage("Format error !", TextColors.RED, p);
							return CommandResult.success();
						}
						
						if(c.hasMayorPerm(r))
						{
							c.setPlayerTaxe(amount);
							CityPlugin.sendMessage("Tax set !", TextColors.GREEN, p);
						}
						else
						{
							CityPlugin.sendMessage("You need to be mayor to do that", TextColors.RED, p);
							return CommandResult.success();
						}
						
					}
				}
			}
			else if (subc.equals(SubCommand.deposit))
			{
				if (c == null)
				{
					CityPlugin.sendMessage("You need to be in a city to do that !", TextColors.RED, p);
				}
				else
				{

					if (args.length < 2)
					{
						CityPlugin.sendMessage("You need to give an amount !", TextColors.RED, p);
					}
					else
					{
						String name = args[1];
						double amount = 0;
						try
						{
							amount=Double.parseDouble(name);
							
							if(amount<=0)
							{
								CityPlugin.sendMessage("Invalid amount", TextColors.RED, p);
								return CommandResult.success();
							}
						}
						catch(NumberFormatException e)
						{
							CityPlugin.sendMessage("Format error !", TextColors.RED, p);
							return CommandResult.success();
						}
						
						
						Account paccount = CityPlugin.economyService.getOrCreateAccount(p.getUniqueId()).get();
						Account caccount = CityPlugin.economyService.getOrCreateAccount(c.getNameEconomy()).get();
						
						TransactionResult transactionResult = paccount.transfer(caccount, CityPlugin.economyService.getDefaultCurrency(), new BigDecimal(amount), Cause.of(NamedCause.source(p)));
						
						if (transactionResult.getResult() != ResultType.SUCCESS)
						{
							CityPlugin.sendMessage("Transaction failed !", TextColors.RED, p);
						}
						else
						{
							CityPlugin.sendMessage("Transaction sucess !", TextColors.GREEN, p);
						}
						
					}
				}
			}
			else if (subc.equals(SubCommand.withdraw))
			{
				if (c == null)
				{
					CityPlugin.sendMessage("You need to be in a city to do that !", TextColors.RED, p);
				}
				else
				{
					
					if(!c.hasAssistantPerm(r))
					{
						CityPlugin.sendMessage("You need assistant perm to do that", TextColors.RED, p);
						return CommandResult.success();
					}

					if (args.length < 2)
					{
						CityPlugin.sendMessage("You need to give an amount !", TextColors.RED, p);
					}
					else
					{
						String name = args[1];
						double amount = 0;
						try
						{
							amount=Double.parseDouble(name);
							
							if(amount<=0)
							{
								CityPlugin.sendMessage("Invalid amount", TextColors.RED, p);
								return CommandResult.success();
							}
						}
						catch(NumberFormatException e)
						{
							CityPlugin.sendMessage("Format error !", TextColors.RED, p);
							return CommandResult.success();
						}
						
						
						Account paccount = CityPlugin.economyService.getOrCreateAccount(p.getUniqueId()).get();
						Account caccount = CityPlugin.economyService.getOrCreateAccount(c.getNameEconomy()).get();
						
						TransactionResult transactionResult = caccount.transfer(paccount, CityPlugin.economyService.getDefaultCurrency(), new BigDecimal(amount), Cause.of(NamedCause.source(p)));
						
						if (transactionResult.getResult() != ResultType.SUCCESS)
						{
							CityPlugin.sendMessage("Transaction failed !", TextColors.RED, p);
						}
						else
						{
							CityPlugin.sendMessage("Transaction sucess !", TextColors.GREEN, p);
						}
						
					}
				}
			}
			else if (subc.equals(SubCommand.credit))
			{
				CityPlugin.sendMessage("__________[ Credit ]__________", TextColors.GREEN, p);
				CityPlugin.sendMessage("Dev by quequiere [FR]", TextColors.GREEN, p);
				CityPlugin.sendMessage("Skype support: quequierebego", TextColors.GREEN, p);
				CityPlugin.sendMessage("For pixelsky-mc.com", TextColors.GREEN, p);
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
		create, claim, leave, help, join,destroy,unclaim,deposit,info,withdraw,teleport,credit,settax,setteleport
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
		builder.append(Text.of(TextColors.GREEN, c.getClaimedChunk().size() + " / "+c.getMaxChunk()+"\n"));
		
		// --------------------------------------------------------------------------------------------

		Account account = CityPlugin.economyService.getOrCreateAccount(c.getNameEconomy()).get();
		BigDecimal balance =  account.getBalance(CityPlugin.economyService.getDefaultCurrency());
		
		builder.append(Text.of(TextColors.DARK_GREEN, "Bank: "));
		builder.append(Text.of(TextColors.RED, balance +" $"));

		builder.append(Text.of(TextColors.DARK_GREEN, " Daily chunk price: "));
		builder.append(Text.of(TextColors.GREEN,CityPlugin.generalConfig.getChunkDailyCostBase()  +" $"));
		
		builder.append(Text.of(TextColors.DARK_GREEN, " Daily city cost: "));
		builder.append(Text.of(TextColors.RED,c.getDailyCost()  +" $"));
		
		builder.append(Text.of("\n"));
		
		// --------------------------------------------------------------------------------------------
				
		builder.append(Text.of(TextColors.DARK_GREEN, "Remaining day before destruction: "));
		BigDecimal days = balance.divide(c.getDailyCost());
		days=days.setScale(2, RoundingMode.DOWN);
		builder.append(Text.of(TextColors.RED,days+" days"));
		
		if(days.compareTo(new BigDecimal(5))<1)
		{
			builder.append(Text.of(TextColors.RED,"\nEmmergency alert, deposite funds with '/c deposit' or city could be destroyed in few days"));
		}
		
		builder.append(Text.of("\n"));
		
		// --------------------------------------------------------------------------------------------

		builder.append(Text.of(TextColors.DARK_GREEN, "Player tax: "));
		builder.append(Text.of(TextColors.GREEN, c.getPlayerTaxe()+" "));
		
		
		itemColor = c.isRemovePlayerTax()? TextColors.GREEN : TextColors.RED;
		itemString = c.isRemovePlayerTax() ? "Remove if can't pay" : "Can stay in city";

		builder.append(Text.of(TextColors.DARK_GREEN, "Pay statment: "));
		builder.append(Text.of(TextColors.GRAY, "["));

		objects.clear();
		if (canModify)
			objects.add(TextActions.executeCallback(source -> {
				if(!c.hasAssistantPerm(r))
					return;
				c.setRemovePlayerTax(!c.isRemovePlayerTax());
				displayCity(p, r, c);
			}));

		objects.add(TextActions.showText(Text.of("Determine if players are removed of the city if they can't pay daily player tax")));
		objects.add(itemColor);
		objects.add(itemString);

		builder.append(Text.of(objects.toArray()));

		builder.append(Text.of(TextColors.GRAY, "]"));
		builder.append(Text.of(TextColors.GRAY, "\n"));

		
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
				if(!c.hasAssistantPerm(r))
					return;
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
