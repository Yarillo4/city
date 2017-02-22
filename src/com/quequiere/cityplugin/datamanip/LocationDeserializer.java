package com.quequiere.cityplugin.datamanip;

import java.lang.reflect.Type;
import java.util.Optional;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class LocationDeserializer implements JsonDeserializer<Location<World>>
{

	@Override
	public Location<World> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
	{
		//System.out.println("Location deserialize....");

		JsonObject jsonO = json.getAsJsonObject();
		JsonElement tagElement = jsonO.get("locationSerialized");
		Gson g = new Gson();
		JsonArray nbtListJson = g.fromJson(tagElement.toString(), JsonArray.class);
		String nbtStringValue = nbtListJson.get(0).toString().replace("\"", "");

		String args[] = nbtStringValue.split(":");
		Integer x = Integer.parseInt(args[0]);
		Integer y = Integer.parseInt(args[1]);
		Integer z = Integer.parseInt(args[2]);
		Optional<World> w = Sponge.getGame().getServer().getWorld(args[3]);
		
		if(!w.isPresent())
		{
			System.out.println("cant find world: "+args[3]);
		}
		
		Location<World> l = new Location<World>(w.get(),x,y,z);
		
		return l;
	}

}
