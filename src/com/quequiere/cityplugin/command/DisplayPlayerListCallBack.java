package com.quequiere.cityplugin.command;

import java.util.ArrayList;
import java.util.UUID;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.LiteralText.Builder;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import com.quequiere.cityplugin.CityPlugin;
import com.quequiere.cityplugin.Tools;
import com.quequiere.cityplugin.object.City;
import com.quequiere.cityplugin.object.CityRankEnum;
import com.quequiere.cityplugin.object.Resident;

public class DisplayPlayerListCallBack
{
	public static void displayPlayerList(Builder builder, Player p, Resident r, City c)
	{

		builder.append(Text.of(TextColors.DARK_GREEN, "Residents: "));
		builder.append(Text.of(TextColors.GREEN, c.getResidents().size()));

		final boolean canModify = c.hasAssistantPerm(r);

		ArrayList<Object> objects = new ArrayList<>();

		objects.add(TextActions.executeCallback(source -> {

			Builder localbuilder = Text.builder("");
			diplayPlayerList(builder, p, r, c, localbuilder, canModify);

			p.sendMessage(localbuilder.toText());

		}));

		objects.add(TextActions.showText(Text.of("See more info about residents")));
		objects.add(TextColors.GRAY);
		objects.add(" [+]\n");
		builder.append(Text.of(objects.toArray()));
	}

	public static void diplayPlayerList(Builder builder, Player p, Resident r, City c, Builder localbuilder, boolean canModify)
	{
		localbuilder.append(Text.of(TextColors.GRAY, "____________[ Player List ]____________\n"));

		for (UUID id : c.getResidents())
		{
			User u = Tools.getUser(id);
			displayPlayersOption(builder, u, localbuilder, canModify, p, r, c);
		}
	}

	private static void displayPlayersOption(Builder builder, User u, Builder localbuilder, boolean canModify, Player p, Resident r, City c)
	{

		Resident target = Resident.fromPlayerId(u.getUniqueId());
		ArrayList<Object> objects = new ArrayList<>();

		if (c.hasMayorPerm(target))
		{
			localbuilder.append(Text.of(TextActions.showText(Text.of("This is the mayor of the city")),TextColors.DARK_RED, "[M] "));
		}
		else if (c.hasAssistantPerm(target))
		{
			localbuilder.append(Text.of(TextActions.showText(Text.of("This is a mayor assistant")),TextColors.RED, "[A] "));
		}
		else
		{
			localbuilder.append(Text.of(TextActions.showText(Text.of("This is a regular resident")),TextColors.AQUA, "[R] "));
		}

		localbuilder.append(Text.of(TextColors.GOLD, u.getName() + " "));

		if (!c.hasResident(target.getId()))
		{
			CityPlugin.sendMessage("You can't do that", TextColors.RED, p);
			return;
		}

		if (c.hasMayorPerm(r) && !c.hasMayorPerm(target))
		{
			objects.clear();
			objects.add(TextActions.executeCallback(source -> {
				
				if(!c.hasMayorPerm(r))
					return;
				
				r.setRank(CityRankEnum.assistant);
				target.setRank(CityRankEnum.mayor);
				Builder l = Text.builder("");
				diplayPlayerList(localbuilder, p, r, c, l, canModify);
				p.sendMessage(l.toText());
			}));

			objects.add(TextActions.showText(Text.of("Give your mayor rank to this player")));
			objects.add(TextColors.DARK_RED);
			objects.add("[M] ");

			localbuilder.append(Text.of(objects.toArray()));
		}

		if (c.hasAssistantPerm(r))
		{
			if (!c.hasAssistantPerm(target))
			{
				objects.clear();
				objects.add(TextActions.executeCallback(source -> {
					
					if(!c.hasAssistantPerm(r))
						return;
					
					target.setRank(CityRankEnum.assistant);
					Builder l = Text.builder("");
					diplayPlayerList(localbuilder, p, r, c, l, canModify);
					p.sendMessage(l.toText());
				}));

				objects.add(TextActions.showText(Text.of("Give the assistant rank to this player.")));
				objects.add(TextColors.AQUA);
				objects.add("[A] ");

				localbuilder.append(Text.of(objects.toArray()));
			}

			if (!c.hasMayorPerm(target) && c.hasAssistantPerm(target))
			{
				objects.clear();
				objects.add(TextActions.executeCallback(source -> {
					
					if(!c.hasAssistantPerm(r))
						return;
					
					target.setRank(CityRankEnum.resident);
					Builder l = Text.builder("");
					diplayPlayerList(localbuilder, p, r, c, l, canModify);
					p.sendMessage(l.toText());
				}));

				objects.add(TextActions.showText(Text.of("Remove this player of assistants.")));
				objects.add(TextColors.RED);
				objects.add("[A-] ");

				localbuilder.append(Text.of(objects.toArray()));
			}

			if (!c.hasAssistantPerm(target) || c.hasMayorPerm(r) && c.hasMayorPerm(target) && !c.hasMayorPerm(target))
			{
				objects.clear();
				objects.add(TextActions.executeCallback(source -> {
					
					if(!c.hasAssistantPerm(r))
						return;
					
					c.removeResident(target.getId());
					Builder l = Text.builder("");
					diplayPlayerList(localbuilder, p, r, c, l, canModify);
					p.sendMessage(l.toText());
				}));

				objects.add(TextActions.showText(Text.of("Remove this player from your city !")));
				objects.add(TextColors.RED);
				objects.add("[-] ");

				localbuilder.append(Text.of(objects.toArray()));
			}

		}

		localbuilder.append(Text.of("\n"));

	}
}
