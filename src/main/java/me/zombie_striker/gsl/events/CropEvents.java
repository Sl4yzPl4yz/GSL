package me.zombie_striker.gsl.events;

import me.zombie_striker.gsl.crops.CropType;
import me.zombie_striker.gsl.utils.ComponentBuilder;
import me.zombie_striker.gsl.utils.StringUtil;
import me.zombie_striker.gsl.world.GSLChunk;
import me.zombie_striker.gsl.world.GSLCube;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class CropEvents implements Listener {

    @EventHandler
    public void onPlant(BlockPlaceEvent event) {
        CropType cropType = CropType.getCropTypeByMaterial(event.getBlock().getType());
        if (cropType != null) {
            GSLChunk gslChunk = GSLChunk.getGSLChunk(event.getBlock().getChunk());
            GSLCube gslCube = gslChunk.getCubes()[(event.getBlock().getY() - GSLChunk.BLOCK_Y_OFFSET) / 16];
            if (gslCube == null) {
                gslCube = new GSLCube();
                gslChunk.getCubes()[(event.getBlock().getY() - GSLChunk.BLOCK_Y_OFFSET) / 16] = gslCube;
            }
            int x = event.getBlock().getX() % 16;
            if (event.getBlock().getX() < 0)
                x = Math.abs((-event.getBlock().getX()) % 16 - 15);
            int z = event.getBlock().getZ() % 16;
            if (event.getBlock().getZ() < 0)
                z = Math.abs((-event.getBlock().getZ()) % 16 - 15);

            int y = event.getBlock().getY() - GSLChunk.BLOCK_Y_OFFSET;

            gslCube.getPlantDate()[x][y][z] = System.currentTimeMillis();
        }
    }

    @EventHandler
    public void onGrow(BlockGrowEvent event) {
        CropType cropType = CropType.getCropTypeByMaterial(event.getBlock().getType());
        if (cropType != null) {
            GSLChunk gslChunk = GSLChunk.getGSLChunk(event.getBlock().getChunk());
            GSLCube gslCube = gslChunk.getCubes()[(event.getBlock().getY() - GSLChunk.BLOCK_Y_OFFSET) / 16];
            if (gslCube == null) {
                gslCube = new GSLCube();
                gslChunk.getCubes()[(event.getBlock().getY() - GSLChunk.BLOCK_Y_OFFSET) / 16] = gslCube;
            }
            int x = event.getBlock().getX() % 16;
            if (event.getBlock().getX() < 0)
                x = Math.abs((-event.getBlock().getX()) % 16 - 15);
            int z = event.getBlock().getZ() % 16;
            if (event.getBlock().getZ() < 0)
                z = Math.abs((-event.getBlock().getZ()) % 16 - 15);

            int y = (event.getBlock().getY() - GSLChunk.BLOCK_Y_OFFSET)%16;

            long time = gslCube.getPlantDate()[x][y][z];
            long growtime = (long) (cropType.getDefaultWaitingTime() * (1000 * 60 * 60));

            if(growtime<0) {
                event.setCancelled(true);
                return;
            }boolean directSunlight = cropType.requiresSunlight();
            if(directSunlight) {
                Block highest = event.getBlock().getWorld().getHighestBlockAt(event.getBlock().getLocation());
                while (!highest.equals(event.getBlock())) {
                    if (!highest.getType().isOccluding()) {
                        highest = highest.getRelative(BlockFace.DOWN);
                    } else {
                        directSunlight = false;
                    }
                }
                if (!directSunlight) {
                    event.setCancelled(true);
                }
            }

            int stage = (int) (((double) (System.currentTimeMillis() - time)) / growtime);

            Ageable ageable = (Ageable) event.getBlock().getBlockData();
            ageable.setAge(stage);
            event.getBlock().setBlockData(ageable);
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        GSLChunk gslChunk = GSLChunk.getGSLChunk(event.getBlock().getChunk());
        GSLCube gslCube = gslChunk.getCubes()[(event.getBlock().getY() - GSLChunk.BLOCK_Y_OFFSET) / 16];
        if (gslCube != null) {
            int x = event.getBlock().getX() % 16;
            if (event.getBlock().getX() < 0)
                x = Math.abs((-event.getBlock().getX()) % 16 - 15);
            int z = event.getBlock().getZ() % 16;
            if (event.getBlock().getZ() < 0)
                z = Math.abs((-event.getBlock().getZ()) % 16 - 15);

            int y = (event.getBlock().getY() - GSLChunk.BLOCK_Y_OFFSET)%16;
            gslCube.getPlantDate()[x][y][z] = -1;
        }

    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.OFF_HAND)
            return;
        CropType cropType = CropType.getCropTypeByMaterial(event.getClickedBlock().getType());
        if (cropType != null) {
            GSLChunk gslChunk = GSLChunk.getGSLChunk(event.getClickedBlock().getChunk());
            GSLCube gslCube = gslChunk.getCubes()[(event.getClickedBlock().getY() - GSLChunk.BLOCK_Y_OFFSET) / 16];
            if (gslCube == null) {
                gslCube = new GSLCube();
                gslChunk.getCubes()[(event.getClickedBlock().getY() - GSLChunk.BLOCK_Y_OFFSET) / 16] = gslCube;
            }
            int x = event.getClickedBlock().getX() % 16;
            if (event.getClickedBlock().getX() < 0)
                x = Math.abs((-event.getClickedBlock().getX()) % 16 - 15);
            int z = event.getClickedBlock().getZ() % 16;
            if (event.getClickedBlock().getZ() < 0)
                z = Math.abs((-event.getClickedBlock().getZ()) % 16 - 15);

            int y = (event.getClickedBlock().getY() - GSLChunk.BLOCK_Y_OFFSET)%16;

            long time = gslCube.getPlantDate()[x][y][z];
            long growtime = (long) (cropType.getDefaultWaitingTime() * (1000 * 60 * 60));

            boolean directSunlight = cropType.requiresSunlight();
            if(directSunlight){
               Block highest = event.getClickedBlock().getWorld().getHighestBlockAt(event.getClickedBlock().getLocation());
               while(!highest.equals(event.getClickedBlock())){
                   if(!highest.getType().isOccluding()){
                       highest = highest.getRelative(BlockFace.DOWN);
                   }else{
                       directSunlight=false;
                   }
               }
               if(!directSunlight) {
                   ComponentBuilder cb = new ComponentBuilder("This crop is in the shade and will not grow!", ComponentBuilder.RED);
                   event.getPlayer().sendMessage(cb.build());
                   return;
               }
            }else{

            }

            if (growtime > System.currentTimeMillis() - time) {
                ComponentBuilder cb = new ComponentBuilder("It will take ", ComponentBuilder.GREEN).append(StringUtil.formatTime(growtime - (System.currentTimeMillis() - time)), ComponentBuilder.WHITE).append(" till this crop is fully grown.", ComponentBuilder.GREEN);
                event.getPlayer().sendMessage(cb.build());
            }
        }
    }

    @EventHandler
    public void onFertilize(BlockFertilizeEvent event){
        CropType cropType = CropType.getCropTypeByMaterial(event.getBlock().getType());
        if(cropType!=null){
            event.setCancelled(true);
        }
    }
}