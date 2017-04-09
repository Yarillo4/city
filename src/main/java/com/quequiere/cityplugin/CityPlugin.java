package com.quequiere.cityplugin;

import java.util.Optional;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColor;

import com.google.inject.Inject;
import com.quequiere.cityplugin.bstats.Metrics;
import com.quequiere.cityplugin.command.CityAdminCommand;
import com.quequiere.cityplugin.command.CityChunkCommand;
import com.quequiere.cityplugin.command.CityCommand;
import com.quequiere.cityplugin.command.CityDynmapCommand;
import com.quequiere.cityplugin.command.CityWorldCommand;
import com.quequiere.cityplugin.config.CityGeneralConfig;
import com.quequiere.cityplugin.dynmap.CityDynmapAdaptator;
import com.quequiere.cityplugin.listeners.ChatListener;
import com.quequiere.cityplugin.listeners.JoinListener;
import com.quequiere.cityplugin.listeners.MoveListener;
import com.quequiere.cityplugin.listeners.PhysicBlockListener;
import com.quequiere.cityplugin.object.City;

@Plugin(id = "city", name = "City", version = PluginInfo.VERSION, description = "A chunk based land protection system that allows for players to set up complex claims and create civilizations.", url = "https://forums.spongepowered.org/t/city-claim/", authors = { "quequiere" })
public class CityPlugin
{

	public static CityPlugin plugin;
	public static PluginContainer container;
	public static CityGeneralConfig generalConfig;
	public static EconomyService economyService;

	@Inject
	private Metrics metrics;

	@Listener
	public void preInit(GamePreInitializationEvent e)
	{
		plugin = this;
		container = Sponge.getPluginManager().fromInstance(CityPlugin.plugin).get();

		this.loadConfig();

		this.registerListener();
		this.registerCommand();
	}

	private void loadConfig()
	{
		CityGeneralConfig.loadConfig();
	}

	private void registerListener()
	{
		Sponge.getEventManager().registerListeners(this, new PhysicBlockListener());
		Sponge.getEventManager().registerListeners(this, new JoinListener());
		Sponge.getEventManager().registerListeners(this, new MoveListener());
		Sponge.getEventManager().registerListeners(this, new ChatListener());
	}

	private void registerCommand()
	{
		CommandManager cmdService = Sponge.getCommandManager();
		cmdService.register(plugin, new CityCommand(), "city", "c");
		cmdService.register(plugin, new CityChunkCommand(), "citychunk", "cc");
		cmdService.register(plugin, new CityWorldCommand(), "cityworld", "cw");
		cmdService.register(plugin, new CityAdminCommand(), "cityadmin", "ca");
		cmdService.register(plugin, new CityDynmapCommand(), "citydynmap", "cd");
	}

	@Listener
	public void onGamePostInit(GameStartedServerEvent event)
	{
		Optional<EconomyService> econService = Sponge.getServiceManager().provide(EconomyService.class);

		if (econService.isPresent())
		{
			economyService = econService.get();
		}
		else
		{
			System.out.println("FATAL ERROR WITH CITY PLUGIN, NO ECONOMY SERVICE !!!!!!!!!!!");
		}

		City.reloadAll();
		
		boolean dynmapFind = true;
		try {
		    Class.forName("org.dynmap.DynmapCore");
		    System.out.println("Dynmap system finded !");
		} catch(Exception e) {
			System.out.println("Dynmap system not finded.");
			dynmapFind= false;
		}
		
		if(dynmapFind)
		{
			new java.util.Timer().schedule( 
			        new java.util.TimerTask() {
			            @Override
			            public void run() {
			            	System.out.println("Initialize dynmaplink !");
			            	CityDynmapAdaptator.init(null);
			            }
			        }, 
			        15000 
			);
		}

		
		

	}

	public static void sendMessageWithoutPrefix(String message, TextColor color, Player p)
	{
		p.sendMessage(Text.of(color, message));
	}

	public static void sendMessage(String message, TextColor color, Player p)
	{
		p.sendMessage(Text.of(color, "[City] " + message));
	}

	public static void sendMessage(String message, TextColor color, Object desc, Player p)
	{
		p.sendMessage(Text.of(color, TextActions.showText(Text.of(desc)), "[City] " + message));
	}

	public static boolean hasPerm(Player p, String perm)
	{
		if (p.hasPermission(perm))
			return true;

		return false;
	}
}
