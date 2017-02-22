package com.quequiere.cityplugin.listeners;

import java.util.Optional;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.item.inventory.UseItemStackEvent;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.quequiere.cityplugin.CityPlugin;
import com.quequiere.cityplugin.object.CityPermEnum;
import com.quequiere.cityplugin.object.Resident;

public class PhysicBlockListener {

	@Listener
	public void useItem(final UseItemStackEvent.Start event) {
		
		Optional<Player> optionalPlayer = event.getCause().first(Player.class);
		if (!optionalPlayer.isPresent()) {
			return;
		}

		Player p = optionalPlayer.get();
		Resident r = Resident.fromPlayerId(p.getUniqueId());

		Location<World> loc = p.getLocation();

		if (!r.getCache().hasPerm(loc, CityPermEnum.SWITH)) {
			event.setCancelled(true);
			CityPlugin.sendMessage("You can't use item here !", TextColors.RED, p);
			return;
		}

	}

	@Listener
	public void interact(final InteractBlockEvent event, @Root Player p) {

		Optional<Location<World>> location = event.getTargetBlock().getLocation();

		if (!location.isPresent()) {
			return;
		}

		Resident r = Resident.fromPlayerId(p.getUniqueId());

		Location<World> loc = event.getTargetBlock().getLocation().get();

		if (!r.getCache().hasPerm(loc, CityPermEnum.SWITH)) {
			event.setCancelled(true);
			CityPlugin.sendMessage("You can't swith here !", TextColors.RED, p);
			return;
		}

	}

	@Listener
	public void onChangeBlock(final ChangeBlockEvent event) {
		Optional<Player> optionalPlayer = event.getCause().first(Player.class);
		if (!optionalPlayer.isPresent()) {
			return;
		}

		Player p = optionalPlayer.get();
		Resident r = Resident.fromPlayerId(p.getUniqueId());

		for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
			Location<World> loc = transaction.getOriginal().getLocation().get();

			if (event instanceof ChangeBlockEvent.Place) {
				if (!r.getCache().hasPerm(loc, CityPermEnum.BUILD)) {
					event.setCancelled(true);
					CityPlugin.sendMessage("You can't build here !", TextColors.RED, p);
					return;
				}
			} else if (event instanceof ChangeBlockEvent.Break) {
				if (!r.getCache().hasPerm(loc, CityPermEnum.DESTROY)) {
					event.setCancelled(true);
					CityPlugin.sendMessage("You can't destroy here !", TextColors.RED, p);
					return;
				}
			}

		}

	}

}
