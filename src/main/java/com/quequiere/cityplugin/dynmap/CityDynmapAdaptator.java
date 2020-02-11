package com.quequiere.cityplugin.dynmap;

import com.quequiere.cityplugin.object.City;
import com.quequiere.cityplugin.object.CityChunk;
import org.dynmap.DynmapCore;
import org.dynmap.forge.DynmapMod;
import org.dynmap.forge.DynmapPlugin;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;
import org.dynmap.utils.TileFlags;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.lang.reflect.Field;
import java.util.*;

public class CityDynmapAdaptator
{
	public static DynmapCore core;

	enum direction
	{
		XPLUS, ZPLUS, XMINUS, ZMINUS
	};

	static int townblocksize = 16;
	static private Map<String, AreaMarker> resareas = new HashMap<String, AreaMarker>();
	static private Map<String, Marker> resmark = new HashMap<String, Marker>();
	static MarkerSet set;

	public static void init(String cityName)
	{

		DynmapPlugin pl = DynmapMod.plugin;
		Field f = pl.getClass().getDeclaredFields()[0];
		f.setAccessible(false);

		try
		{
			Object o = f.get(pl);
			core = (DynmapCore) o;
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}

		set = core.getMarkerAPI().createMarkerSet("city.markersetdeux", "Cities", null, false);
		
		if(set==null)
		{
			set = core.getMarkerAPI().getMarkerSet("city.markersetdeux");
			//System.out.println("try bypass: "+set);
		}
	

		Map<String, AreaMarker> newmap = new HashMap<String, AreaMarker>(); 
		Map<String, Marker> newmark = new HashMap<String, Marker>(); 
		for (City c : City.getLoaded())
		{
			if(cityName!=null)
			{
				if(cityName.equals(c.getName()))
				{
					handleTown(c, newmap, newmark);
					break;
				}
			}
			else
			{
				handleTown(c, newmap, newmark);
			}
			
		}

		resareas = newmap;
		resmark = newmark;

	}

	private static int floodFillTarget(TileFlags src, TileFlags dest, int x, int y)
	{
		int cnt = 0;
		ArrayDeque<int[]> stack = new ArrayDeque<int[]>();
		stack.push(new int[] { x, y });

		while (stack.isEmpty() == false)
		{
			int[] nxt = stack.pop();
			x = nxt[0];
			y = nxt[1];
			if (src.getFlag(x, y))
			{ /* Set in src */
				src.setFlag(x, y, false); /* Clear source */
				dest.setFlag(x, y, true); /* Set in destination */
				cnt++;
				if (src.getFlag(x + 1, y))
					stack.push(new int[] { x + 1, y });
				if (src.getFlag(x - 1, y))
					stack.push(new int[] { x - 1, y });
				if (src.getFlag(x, y + 1))
					stack.push(new int[] { x, y + 1 });
				if (src.getFlag(x, y - 1))
					stack.push(new int[] { x, y - 1 });
			}
		}
		return cnt;
	}

	private static void handleTown(City town, Map<String, AreaMarker> newmap, Map<String, Marker> newmark)
	{
		String name = town.getName();
		double[] x = null;
		double[] z = null;
		int poly_index = 0; /* Index of polygon for given town */

		/* Handle areas */
		List<CityChunk> blocks = town.getClaimedChunk();
		if (blocks.isEmpty())
			return;
		/* Build popup */
		String desc = town.getName();

		HashMap<String, TileFlags> blkmaps = new HashMap<String, TileFlags>();
		LinkedList<CityChunk> nodevals = new LinkedList<CityChunk>();
		World curworld = null;
		TileFlags curblks = null;
		boolean vis = false;
		/* Loop through blocks: set flags on blockmaps for worlds */
		for (CityChunk b : blocks)
		{

			if (b.getWorld() != curworld)
			{ /* Not same world */
				String wname = b.getWorld().getName();
				vis = true;
				if (vis)
				{ /* Only accumulate for visible areas */
					curblks = blkmaps.get(wname); /* Find existing */
					if (curblks == null)
					{
						curblks = new TileFlags();
						blkmaps.put(wname, curblks); /* Add fresh one */
					}
				}
				curworld = b.getWorld();
			}
			if (vis)
			{
				curblks.setFlag(b.getX(), b.getZ(),
						true); /* Set flag for block */
				nodevals.addLast(b);
			}
		}
		/* Loop through until we don't find more areas */
		while (nodevals != null)
		{
			LinkedList<CityChunk> ournodes = null;
			LinkedList<CityChunk> newlist = null;
			TileFlags ourblks = null;
			int minx = Integer.MAX_VALUE;
			int minz = Integer.MAX_VALUE;
			for (CityChunk node : nodevals)
			{
				int nodex = node.getX();
				int nodez = node.getZ();

				if (ourblks == null)
				{ /* If not started, switch to world for this block first */
					if (node.getWorld() != curworld)
					{
						curworld = node.getWorld();
						curblks = blkmaps.get(curworld.getName());
					}
				}
				/*
				 * If we need to start shape, and this block is not part of one
				 * yet
				 */
				if ((ourblks == null) && curblks.getFlag(nodex, nodez))
				{
					ourblks = new TileFlags(); /* Create map for shape */
					ournodes = new LinkedList<CityChunk>();
					floodFillTarget(curblks, ourblks, nodex,
							nodez); /* Copy shape */
					ournodes.add(node); /* Add it to our node list */
					minx = nodex;
					minz = nodez;
				}
				/* If shape found, and we're in it, add to our node list */
				else if ((ourblks != null) && (node.getWorld() == curworld) && (ourblks.getFlag(nodex, nodez)))
				{
					ournodes.add(node);
					if (nodex < minx)
					{
						minx = nodex;
						minz = nodez;
					}
					else if ((nodex == minx) && (nodez < minz))
					{
						minz = nodez;
					}
				}
				else
				{ /* Else, keep it in the list for the next polygon */
					if (newlist == null)
						newlist = new LinkedList<CityChunk>();
					newlist.add(node);
				}
			}
			nodevals = newlist; /* Replace list (null if no more to process) */
			if (ourblks != null)
			{
				/*
				 * Trace outline of blocks - start from minx, minz going to x+
				 */
				int init_x = minx;
				int init_z = minz;
				int cur_x = minx;
				int cur_z = minz;
				direction dir = direction.XPLUS;
				ArrayList<int[]> linelist = new ArrayList<int[]>();
				linelist.add(new int[] { init_x, init_z }); // Add start point
				while ((cur_x != init_x) || (cur_z != init_z) || (dir != direction.ZMINUS))
				{
					switch (dir) {
					case XPLUS: /* Segment in X+ direction */
						if (!ourblks.getFlag(cur_x + 1, cur_z))
						{ /* Right turn? */
							linelist.add(new int[] { cur_x + 1,
									cur_z }); /* Finish line */
							dir = direction.ZPLUS; /* Change direction */
						}
						else if (!ourblks.getFlag(cur_x + 1, cur_z - 1))
						{ /* Straight? */
							cur_x++;
						}
						else
						{ /* Left turn */
							linelist.add(new int[] { cur_x + 1,
									cur_z }); /* Finish line */
							dir = direction.ZMINUS;
							cur_x++;
							cur_z--;
						}
						break;
					case ZPLUS: /* Segment in Z+ direction */
						if (!ourblks.getFlag(cur_x, cur_z + 1))
						{ /* Right turn? */
							linelist.add(new int[] { cur_x + 1,
									cur_z + 1 }); /* Finish line */
							dir = direction.XMINUS; /* Change direction */
						}
						else if (!ourblks.getFlag(cur_x + 1, cur_z + 1))
						{ /* Straight? */
							cur_z++;
						}
						else
						{ /* Left turn */
							linelist.add(new int[] { cur_x + 1,
									cur_z + 1 }); /* Finish line */
							dir = direction.XPLUS;
							cur_x++;
							cur_z++;
						}
						break;
					case XMINUS: /* Segment in X- direction */
						if (!ourblks.getFlag(cur_x - 1, cur_z))
						{ /* Right turn? */
							linelist.add(new int[] { cur_x,
									cur_z + 1 }); /* Finish line */
							dir = direction.ZMINUS; /* Change direction */
						}
						else if (!ourblks.getFlag(cur_x - 1, cur_z + 1))
						{ /* Straight? */
							cur_x--;
						}
						else
						{ /* Left turn */
							linelist.add(new int[] { cur_x,
									cur_z + 1 }); /* Finish line */
							dir = direction.ZPLUS;
							cur_x--;
							cur_z++;
						}
						break;
					case ZMINUS: /* Segment in Z- direction */
						if (!ourblks.getFlag(cur_x, cur_z - 1))
						{ /* Right turn? */
							linelist.add(new int[] { cur_x,
									cur_z }); /* Finish line */
							dir = direction.XPLUS; /* Change direction */
						}
						else if (!ourblks.getFlag(cur_x - 1, cur_z - 1))
						{ /* Straight? */
							cur_z--;
						}
						else
						{ /* Left turn */
							linelist.add(new int[] { cur_x,
									cur_z }); /* Finish line */
							dir = direction.XMINUS;
							cur_x--;
							cur_z--;
						}
						break;
					}
				}
				/* Build information for specific area */
				String polyid = town.getName() + "__" + poly_index;

				int sz = linelist.size();
				x = new double[sz];
				z = new double[sz];
				for (int i = 0; i < sz; i++)
				{
					int[] line = linelist.get(i);
					x[i] = (double) line[0] * (double) townblocksize;
					z[i] = (double) line[1] * (double) townblocksize;
				}
				/* Find existing one */
				AreaMarker m = resareas.remove(polyid); /* Existing area? */
				if (m == null)
				{

					m = set.createAreaMarker(polyid, name, false, curworld.getName(), x, z, false);
					if (m == null)
					{
						System.out.println("Error adding area maker " + polyid);

						return;
					}
				}
				else
				{
					m.setCornerLocations(x, z); /* Replace corner locations */
					m.setLabel(name); /* Update label */
				}
				m.setDescription(desc); /* Set popup */

				addStyle(m);

				/* Add to map */
				newmap.put(polyid, m);
				poly_index++;
			}
		}

		/* Now, add marker for home block */
		Location<World> blk = null;
		try
		{
			blk = town.getSpawn();
		}
		catch (Exception ex)
		{
			System.out.println("getHomeBlock exception " + ex);
		}
		if ((blk != null) && blocks.size()>0)
		{
			String markid = town.getName() + "__home";

			MarkerIcon ico = core.getMarkerAPI().getMarkerIcon(MarkerIcon.DEFAULT);
			if (ico != null)
			{
				Marker home = resmark.remove(markid);
				// double xx = townblocksize*blk.getX() + (townblocksize/2);
				// double zz = townblocksize*blk.getZ() + (townblocksize/2);
				double xx = blk.getX();
				double zz = blk.getZ();
				if (home == null)
				{
					home = set.createMarker(markid, name + " [home]", blk.getExtent().getName(), xx, 64, zz, ico, false);
					if (home == null)
						return;
				}
				else
				{
					home.setLocation(blk.getExtent().getName(), xx, 64, zz);
					home.setLabel(name + " [home]"); /* Update label */
					home.setMarkerIcon(ico);
				}
				home.setDescription(desc); /* Set popup */
				newmark.put(markid, home);
			}
		}
		
	
	}
	
	public static void clear()
	{
		set.deleteMarkerSet();
	}

	private static void addStyle(AreaMarker m)
	{

		m.setLineStyle(1, 0, 0);

		m.setFillStyle(0.35, 0xFF0000);
		m.setRangeY(2, 5);
		m.setBoostFlag(true);
	}

}
