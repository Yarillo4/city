package com.quequiere.cityplugin.listeners;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.quequiere.cityplugin.CityPlugin;
import com.quequiere.cityplugin.object.City;
import com.quequiere.cityplugin.object.Resident;

public class ChatListener {

	@Listener
	public void onMessage(MessageChannelEvent.Chat event, @Root Player player) {
		if (CityPlugin.generalConfig.isCityNameInChat()) {
			Resident r = Resident.fromPlayerId(player.getUniqueId());
			City c = r.getCity();
			if (c != null) {
				event.setMessage(Text.builder().append(Text.of(c.isPrivateCity()?TextColors.GRAY:TextColors.DARK_GREEN, "[" + c.getCustomName() + "] "))
						.append(event.getMessage()).build());
			}
		}

	}
}
