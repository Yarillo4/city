package com.quequiere.cityplugin.listeners;

import java.util.Optional;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.MobSpawner;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.block.tileentity.carrier.TileEntityCarrier;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.hanging.Hanging;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.vehicle.minecart.Minecart;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.entity.CollideEntityEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import com.quequiere.cityplugin.CityPlugin;
import com.quequiere.cityplugin.object.CityPermEnum;
import com.quequiere.cityplugin.object.Resident;

public class PhysicBlockListener
{
	
	@Listener
	public void on(CollideEntityEvent event, @Root Player p)
	{

		Resident r = Resident.fromPlayerId(p.getUniqueId());
		for(Entity e:event.getEntities())
		{
			if (!r.getCache().hasPerm(e.getLocation(), CityPermEnum.SWITH))
			{
				event.setCancelled(true);
				if(r.getCache().canDisplayMessage(CityPermEnum.SWITH))
				CityPlugin.sendMessage("You can't swith with here: collide entity.", TextColors.RED, p);
				return;
			}
		}
	
	}

	@Listener
	public void on(InteractEntityEvent event, @Root Player p)
	{	
		if (event.getTargetEntity() instanceof Hanging)
		{
			Resident r = Resident.fromPlayerId(p.getUniqueId());

			if (!r.getCache().hasPerm(event.getTargetEntity().getLocation(), CityPermEnum.SWITH))
			{
				event.setCancelled(true);
				if(r.getCache().canDisplayMessage(CityPermEnum.SWITH))
				CityPlugin.sendMessage("You can't swith with frame here !", TextColors.RED, p);
				return;
			}

		}
		else if (event.getTargetEntity() instanceof Minecart)
		{
			Resident r = Resident.fromPlayerId(p.getUniqueId());

			if (!r.getCache().hasPerm(event.getTargetEntity().getLocation(), CityPermEnum.SWITH))
			{
				event.setCancelled(true);
				if(r.getCache().canDisplayMessage(CityPermEnum.SWITH))
				CityPlugin.sendMessage("You can't swith with minecart here !", TextColors.RED, p);
				return;
			}
		}
		else if (event.getTargetEntity() instanceof ArmorStand)
		{
			Resident r = Resident.fromPlayerId(p.getUniqueId());

			if (!r.getCache().hasPerm(event.getTargetEntity().getLocation(), CityPermEnum.SWITH))
			{
				event.setCancelled(true);
				if(r.getCache().canDisplayMessage(CityPermEnum.SWITH))
				CityPlugin.sendMessage("You can't swith with armorstand here !", TextColors.RED, p);
				return;
			}
		}
		else if (event.getTargetEntity() instanceof Living)
		{
			Resident r = Resident.fromPlayerId(p.getUniqueId());

			if (!r.getCache().hasPerm(event.getTargetEntity().getLocation(), CityPermEnum.USEITEM))
			{
				event.setCancelled(true);
				if(r.getCache().canDisplayMessage(CityPermEnum.USEITEM))
				CityPlugin.sendMessage("You can't use item here: interactWithLivingEntity", TextColors.RED, p);
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

		if (!location.isPresent())
		{
			return;
		}

		Resident r = Resident.fromPlayerId(p.getUniqueId());

		Location<World> loc = event.getTargetBlock().getLocation().get();

		// special fix for pixelmonmod fossil cleaner
		if (event instanceof InteractBlockEvent.Primary )
		{
			if( loc.getTileEntity().isPresent())
			{
				if (!r.getCache().hasPerm(loc, CityPermEnum.DESTROY))
				{
					event.setCancelled(true);
					if(r.getCache().canDisplayMessage(CityPermEnum.DESTROY))
					CityPlugin.sendMessage("You can't destroy tile entity here !", TextColors.RED, p);
					return;
				}
			}
			else
			{
				return;
			}
			
			
		}

		if (event instanceof InteractBlockEvent.Secondary)
		{

			Optional<TileEntity> optiel = location.get().getTileEntity();
			
			if (optiel.isPresent())
			{
				TileEntity te = optiel.get();
				if(te instanceof TileEntity)
				{
					if (!r.getCache().hasPerm(loc, CityPermEnum.USEITEM))
					{
						event.setCancelled(true);
						if(r.getCache().canDisplayMessage(CityPermEnum.USEITEM))
						CityPlugin.sendMessage("You can't use item here: tileEntity", TextColors.RED, p);
						return;
					}
				}

			}
			
			
			//special fix for apricorntree
			Optional<TileEntity> downTile = location.get().getRelative(Direction.DOWN).getTileEntity();
			if(downTile.isPresent() && downTile.get().getClass().getName().contains("TileEntityApricornTree"))
			{
				if (!r.getCache().hasPerm(loc, CityPermEnum.USEITEM))
				{
					event.setCancelled(true);
					if(r.getCache().canDisplayMessage(CityPermEnum.USEITEM))
					CityPlugin.sendMessage("You can't use item here: apricornTree", TextColors.RED, p);
					return;
				}
			}
			

			Optional<ItemStack> i = p.getItemInHand(HandTypes.MAIN_HAND);
			if (i.isPresent())
			{
				ItemStack is = i.get();
				if (is.getItem().getClass().toString().contains("ItemHangingEntity"))
				{

					if (!r.getCache().hasPerm(loc, CityPermEnum.BUILD))
					{
						event.setCancelled(true);
						if(r.getCache().canDisplayMessage(CityPermEnum.BUILD))
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
			if(r.getCache().canDisplayMessage(CityPermEnum.SWITH))
			CityPlugin.sendMessage("You can't switch here !", TextColors.RED, p);
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
					if (!transaction.getOriginal().getState().getType().equals(BlockTypes.GRASS))
					{
						if(r.getCache().canDisplayMessage(CityPermEnum.BUILD))
						CityPlugin.sendMessage("You can't build here !", TextColors.RED,event.toString(), p);
					}
					return;
				}
			}
			else if (event instanceof ChangeBlockEvent.Break)
			{
				if (!r.getCache().hasPerm(loc, CityPermEnum.DESTROY))
				{
					event.setCancelled(true);
					if(r.getCache().canDisplayMessage(CityPermEnum.DESTROY))
					CityPlugin.sendMessage("You can't destroy here !", TextColors.RED, p);
					return;
				}
			}
			else if (event instanceof ChangeBlockEvent.Modify)
			{
				if (!r.getCache().hasPerm(loc, CityPermEnum.SWITH))
				{
					if (loc.getTileEntity().isPresent())
					{
						//CityPlugin.sendMessage("Special dev fix, report is abnormal", TextColors.GRAY, p);
						return;
					}
					else
					{
						event.setCancelled(true);
						if(r.getCache().canDisplayMessage(CityPermEnum.SWITH))
						CityPlugin.sendMessage("You can't switch here: modify !", TextColors.RED, p);
						return;
					}

				}
			}

		}

	}

}
