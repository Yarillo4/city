package com.quequiere.cityplugin.datamanip;

import java.lang.reflect.Type;

import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class LocationSerializer implements JsonSerializer<Location<World>>
{

	@Override
	public JsonElement serialize(Location<World> src, Type typeOfSrc, JsonSerializationContext arg2)
	{
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		JsonElement withoutTransientFields = new JsonObject();

		JsonArray list = new JsonArray();
		list.add(gson.toJsonTree(toString(src)));
		withoutTransientFields.getAsJsonObject().add("locationSerialized", list);
		return withoutTransientFields;
	}

	public static String toString(Location<World> l)
	{
		if (l.getExtent() instanceof World)
		{
			World w = (World) l.getExtent();
			String s = l.getBlockX() + ":" + l.getBlockY() + ":" + l.getBlockZ() + ":" + w.getName();
			return s;
		}
		else
		{
			System.out.println("ERROR SERIALIZER with extend extend: " + l.getExtent().getClass());
		}

		return null;

	}

}
