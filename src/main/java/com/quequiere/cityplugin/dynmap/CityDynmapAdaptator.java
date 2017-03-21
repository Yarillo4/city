package com.quequiere.cityplugin.dynmap;

import java.lang.reflect.Field;

import org.dynmap.DynmapCore;
import org.dynmap.forge.DynmapMod;
import org.dynmap.forge.DynmapPlugin;
import org.dynmap.markers.MarkerSet;

public class CityDynmapAdaptator
{
	public static DynmapCore core;
	
	public static void init()
	{
		
		DynmapPlugin pl = DynmapMod.plugin;
		Field f = pl.getClass().getDeclaredFields()[0];
		f.setAccessible(true);
		
		
		try
		{
			Object o = f.get(pl);
			core=(DynmapCore) o;
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
	}

	public static void test()
	{
		MarkerSet m = core.getMarkerAPI().createMarkerSet("worldtest", "machin", null, false);
		m.setMarkerSetLabel("Coucou");
		
		
	}
}
