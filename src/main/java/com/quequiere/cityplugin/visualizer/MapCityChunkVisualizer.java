package com.quequiere.cityplugin.visualizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scoreboard.Score;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.scoreboard.critieria.Criteria;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlots;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Text.Builder;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3d;
import com.quequiere.cityplugin.Tools;
import com.quequiere.cityplugin.object.City;
import com.quequiere.cityplugin.object.CityChunk;
import com.quequiere.cityplugin.object.Resident;

public class MapCityChunkVisualizer
{

	public static void updatedisplay(Player p,int dir,Location<World> currentloc)
	{

		Scoreboard scoreboard = Scoreboard.builder().build();

		List<Score> lines = new ArrayList<Score>();

		Objective obj = Objective.builder().name("EasyScoreboard").criterion(Criteria.DUMMY).displayName(Text.of(TextColors.GOLD, "▘▘▘ Chunk map ▘▘▘")).build();

		Score c = null;
		TextColor color = null;



		Direction d = Tools.getPlayerDirection(dir);

		c = obj.getOrCreateScore(Text.of(TextColors.GOLD, "        " + d.name()));
		c.setScore(50);

		int score = 49;
		int step = -3;

		for (Text t : generateChunkDisplay(p, d,currentloc))
		{
			int val = Math.abs(step);
			if (step < 0)
			{
				color = TextColors.GREEN;
			}
			else if (step == 0)
			{
				color = TextColors.WHITE;
			}
			else
			{
				color = TextColors.AQUA;
			}
			c = obj.getOrCreateScore(Text.of(color, val, "  ", TextColors.RESET, t));
			c.setScore(score);
			score--;
			step++;
		}
		
		c = obj.getOrCreateScore(Text.of(TextColors.AQUA, "█ - To buy"));
		c.setScore(42);
		c = obj.getOrCreateScore(Text.of(TextColors.GREEN, "█ - Your cc"));
		c.setScore(41);
		c = obj.getOrCreateScore(Text.of(TextColors.GOLD, "█ - City cc"));
		c.setScore(40);
		c = obj.getOrCreateScore(Text.of(TextColors.YELLOW, "█ - Player cc"));
		c.setScore(39);
		c = obj.getOrCreateScore(Text.of(TextColors.GRAY, "▒ - Wild"));
		c.setScore(38);
		c = obj.getOrCreateScore(Text.of(TextColors.RED, "▓ - Other city"));
		c.setScore(37);

		scoreboard.addObjective(obj);
		scoreboard.updateDisplaySlot(obj, DisplaySlots.SIDEBAR);

		p.setScoreboard(scoreboard); 

	}
	// c = obj.getOrCreateScore(Text.of(color, val," " ,TextColors.RESET,
	// "██▒\u2062▒▒▒"));

	private static ArrayList<Text> generateChunkDisplay(Player p, Direction playerDirection,Location<World> currentloc)
	{
		Resident r = Resident.fromPlayerId(p.getUniqueId());
		HashMap<Direction, Direction> nextDirection = new HashMap<Direction, Direction>();
		nextDirection.put(Direction.NORTH, Direction.EAST);
		nextDirection.put(Direction.WEST, Direction.SOUTH);
		nextDirection.put(Direction.SOUTH, Direction.WEST);
		nextDirection.put(Direction.EAST, Direction.NORTH);

		ArrayList<Text> list = new ArrayList<Text>();
		Chunk ref = Tools.getChunk(p.getLocation());
		Location<Chunk> loc = Tools.getChunkLocation(currentloc);
		
		Direction toapply = null;
		
		if(playerDirection.equals(Direction.NORTH) || playerDirection.equals(Direction.SOUTH))
		{
			toapply=playerDirection.getOpposite();
		}
		else
		{
			toapply=playerDirection;
		}

		for (int z = -3; z <= 3; z++)
		{
			Location<Chunk> localTemp = Tools.addDirection(loc,toapply , z);

			Builder b = Text.builder();
			for (int x = -3; x <= 3; x++)
			{
				Location<Chunk> target = Tools.addDirection(localTemp, nextDirection.get(playerDirection), x);
				
				Chunk targetChunk = Tools.getChunk(target.getBlockX(), target.getBlockZ(), p.getWorld());
				City c = City.getCityFromChunk(targetChunk);
				
				String code = null;
				TextColor color = null;
				
				if (c == null)
				{
					if(target.getExtent().equals(targetChunk))
					{
						code = "\u2062";
					}
					else
					{
						code = "▒";
					}
					color=TextColors.GRAY;
				}
				else
				{
					if (c.equals(r.getCity()))
					{
						if(target.getExtent().equals(targetChunk))
						{
							code = "\u2062";
						}
						else
						{
							code = "█";
						}
						
						CityChunk cc = c.getChunck(targetChunk);
						
						if(cc.getSellPrice()>0)
						{
							color=TextColors.AQUA;
						}
						else
						{
							if(cc.getResident()==null)
							{
								color=TextColors.GOLD;
							}
							else
							{
								if(cc.getResident().equals(p.getUniqueId()))
								{
									color=TextColors.GREEN;
								}
								else
								{
									color=TextColors.YELLOW;
								}
							}
						}
	
					}
					else
					{
						code="▓";
						color=TextColors.RED;
					}
				}
				
				b.append(Text.of(color, code));

			}
			
			list.add(b.build());

		}

		return list;
	}

}
