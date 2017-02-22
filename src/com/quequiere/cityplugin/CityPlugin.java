package com.quequiere.cityplugin;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

import com.quequiere.cityplugin.command.CityChunkCommand;
import com.quequiere.cityplugin.command.CityCommand;
import com.quequiere.cityplugin.listeners.JoinListener;
import com.quequiere.cityplugin.listeners.MoveListener;
import com.quequiere.cityplugin.listeners.PhysicBlockListener;
import com.quequiere.cityplugin.object.City;

@Plugin(id = "city", name = "City", version = "0.01")
public class CityPlugin
{

	public static CityPlugin plugin;
	public static PluginContainer container;

	@Listener
	public void preInit(GamePreInitializationEvent e)
	{
		plugin = this;
		container = Sponge.getPluginManager().fromInstance(CityPlugin.plugin).get();

		this.registerListener();
		this.registerCommand();
	}

	private void registerListener()
	{
		Sponge.getEventManager().registerListeners(this, new PhysicBlockListener());
		Sponge.getEventManager().registerListeners(this, new JoinListener());
		Sponge.getEventManager().registerListeners(this, new MoveListener());
	}

	private void registerCommand()
	{
		CommandManager cmdService = Sponge.getCommandManager();
		cmdService.register(plugin, new CityCommand(), "city", "c");
		cmdService.register(plugin, new CityChunkCommand(), "citychunk", "cc");
	}

	@Listener
	public void onGamePostInit(GameStartedServerEvent e)
	{
		City.reloadAll();
	}
	
	public static void sendMessageWithoutPrefix(String message, TextColor color, Player p)
	{
		p.sendMessage(Text.of(message));
	}

	public static void sendMessage(String message, TextColor color, Player p)
	{
		p.sendMessage(Text.of(color,"[City] "+message));
	}
}
