package com.quequiere.cityplugin.listeners;

import java.awt.event.ItemListener;
import java.util.List;
import java.util.Optional;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.MobSpawner;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.block.tileentity.carrier.TileEntityCarrier;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.hanging.Hanging;
import org.spongepowered.api.entity.hanging.ItemFrame;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.tileentity.TargetTileEntityEvent;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.event.item.inventory.UseItemStackEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.item.inventory.lens.impl.minecraft.ContainerChestInventoryLens;
import org.spongepowered.common.mixin.core.tileentity.MixinTileEntityLockable;

import com.quequiere.cityplugin.CityPlugin;
import com.quequiere.cityplugin.object.CityPermEnum;
import com.quequiere.cityplugin.object.Resident;

public class PhysicBlockListener
{

	@Listener
	public void on(InteractEntityEvent event, @Root Player p)
	{

		if (event.getTargetEntity() instanceof Hanging)
		{
			Resident r = Resident.fromPlayerId(p.getUniqueId());

			if (!r.getCache().hasPerm(event.getTargetEntity().getLocation(), CityPermEnum.SWITH))
			{
				event.setCancelled(true);
				CityPlugin.sendMessage("You can't swith with frame here !", TextColors.RED, p);
				return;
			}

		}

	}

	@Listener
	public void interact(final InteractBlockEvent event, @Root Player p)
	{

		Optional<Location<World>> location = event.getTargetBlock().getLocation();

		if (event.getTargetBlock() == null)
		{

			return;
		}

		if (event instanceof InteractBlockEvent.Primary)
		{

			return;
		}

		if (!location.isPresent())
		{

			return;
		}

		Resident r = Resident.fromPlayerId(p.getUniqueId());

		Location<World> loc = event.getTargetBlock().getLocation().get();

		if (event instanceof InteractBlockEvent.Secondary)
		{

			Optional<TileEntity> optiel = location.get().getTileEntity();

			if (optiel.isPresent())
			{

				TileEntity te = optiel.get();
				if (te instanceof TileEntityCarrier || te instanceof MobSpawner)
				{
					if (!r.getCache().hasPerm(loc, CityPermEnum.SWITH))
					{
						event.setCancelled(true);
						CityPlugin.sendMessage("You can't swith and interact with inventory here !", TextColors.RED, p);
						return;
					}
				}

			}
			
			Optional<ItemStack> i = p.getItemInHand(((InteractBlockEvent.Secondary) event).getHandType());
			if(i.isPresent())
			{
				ItemStack is = i.get();
				if(is.getItem().getClass().toString().contains("ItemHangingEntity"))
				{
					
					if (!r.getCache().hasPerm(loc, CityPermEnum.BUILD))
					{
						event.setCancelled(true);
						CityPlugin.sendMessage("You can't build with hanging entity here !", TextColors.RED, p);
						return;
					}
				}
				
			}

			return;
		}
		else
		{
			// System.out.println("pa6");
		}

		if (!r.getCache().hasPerm(loc, CityPermEnum.SWITH))
		{
			event.setCancelled(true);
			CityPlugin.sendMessage("You can't swith here !", TextColors.RED, p);
			return;
		}

	}

	@Listener
	public void onChangeBlock(final ChangeBlockEvent event)
	{

		Optional<Player> optionalPlayer = event.getCause().first(Player.class);
		if (!optionalPlayer.isPresent())
		{
			return;
		}

		Player p = optionalPlayer.get();
		Resident r = Resident.fromPlayerId(p.getUniqueId());

		for (Transaction<BlockSnapshot> transaction : event.getTransactions())
		{
			Location<World> loc = transaction.getOriginal().getLocation().get();

			if (event instanceof ChangeBlockEvent.Place)
			{
				if (!r.getCache().hasPerm(loc, CityPermEnum.BUILD))
				{
					event.setCancelled(true);
					if(!transaction.getOriginal().getState().getType().equals(BlockTypes.GRASS))
					{
						CityPlugin.sendMessage("You can't build here !", TextColors.RED, p);
					}			
					return;
				}
			}
			else if (event instanceof ChangeBlockEvent.Break)
			{
				if (!r.getCache().hasPerm(loc, CityPermEnum.DESTROY))
				{
					event.setCancelled(true);
					CityPlugin.sendMessage("You can't destroy here !", TextColors.RED, p);
					return;
				}
			}
			else if (event instanceof ChangeBlockEvent.Modify)
			{
				if (!r.getCache().hasPerm(loc, CityPermEnum.SWITH))
				{
					event.setCancelled(true);
					CityPlugin.sendMessage("You can't switch here !", TextColors.RED, p);
					return;
				}
			}

		}

	}

}
