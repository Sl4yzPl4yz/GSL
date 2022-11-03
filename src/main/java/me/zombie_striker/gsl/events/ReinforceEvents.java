package me.zombie_striker.gsl.events;

import me.zombie_striker.gsl.materials.MaterialType;
import me.zombie_striker.gsl.namelayers.NameLayer;
import me.zombie_striker.gsl.reinforcement.ReinforcementMaterial;
import me.zombie_striker.gsl.states.PlayerState;
import me.zombie_striker.gsl.states.PlayerStatesManager;
import me.zombie_striker.gsl.states.ReinforcementState;
import me.zombie_striker.gsl.utils.ComponentBuilder;
import me.zombie_striker.gsl.utils.InventoryUtil;
import me.zombie_striker.gsl.world.GSLChunk;
import me.zombie_striker.gsl.world.GSLCube;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class ReinforceEvents implements Listener {

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        PlayerState ps = PlayerStatesManager.getState(event.getPlayer().getUniqueId(), ReinforcementState.class);
        if (ps != null) {
            NameLayer nl = ((ReinforcementState) ps).getNameLayer();
            MaterialType reinforcematerial = ((ReinforcementState) ps).getReinforceGroup();

            GSLChunk gslChunk = GSLChunk.getGSLChunk(event.getBlock().getChunk());
            GSLCube gslCube = gslChunk.getCubes()[(event.getBlock().getY() - GSLChunk.BLOCK_Y_OFFSET) / 16];
            if (gslCube == null) {
                gslCube = new GSLCube();
                gslChunk.getCubes()[(event.getBlock().getY() - GSLChunk.BLOCK_Y_OFFSET) / 16]=gslCube;
            }
                int x = event.getBlock().getX() % 16;
                if (event.getBlock().getX() < 0)
                    x = Math.abs((-event.getBlock().getX()) % 16 - 15);
                int z = event.getBlock().getZ() % 16;
                if (event.getBlock().getZ() < 0)
                    z = Math.abs((-event.getBlock().getZ()) % 16 - 15);

                int y = event.getBlock().getY() - GSLChunk.BLOCK_Y_OFFSET;

                ReinforcementMaterial rm = ReinforcementMaterial.getReinforcementMaterialData(reinforcematerial);

                gslCube.getNamelayers()[x][y][z] = nl;
                gslCube.getPlaced()[x][y][z] = true;
                gslCube.getDurability()[x][y][z] = rm.getDurability();
                gslCube.getReinforcedBy()[x][y][z] = rm.getType();
                InventoryUtil.removeOneOf(rm.getType(),event.getPlayer().getInventory());
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
            if(gslCube.getDurability()[x][y][z]>0) {
                if (gslCube.getNamelayers()[x][y][z] != null && !gslCube.getNamelayers()[x][y][z].getMemberranks().containsKey(event.getPlayer().getUniqueId())) {
                    gslCube.getDurability()[x][y][z]=gslCube.getDurability()[x][y][z]-1;
                    if(gslCube.getDurability()[x][y][z]>0){
                        event.setCancelled(true);
                        return;
                    }
                }else{
                    MaterialType mt = gslCube.getReinforcedBy()[x][y][z];
                    ItemStack is = mt.toItemStack();
                    int first = event.getPlayer().getInventory().firstEmpty();
                    if(first!=-1){
                        event.getPlayer().getWorld().dropItem(event.getPlayer().getLocation(),is);
                    }else{
                        event.getPlayer().getInventory().addItem(is);
                    }
                }
                gslCube.getNamelayers()[x][y][z]=null;
                gslCube.getReinforcedBy()[x][y][z]=null;
                gslCube.getDurability()[x][y][z]=-1;
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event){
        if(event.getHand()== EquipmentSlot.OFF_HAND)
            return;
        PlayerState ps = PlayerStatesManager.getState(event.getPlayer().getUniqueId(), ReinforcementState.class);
        if (ps != null) {
            NameLayer nl = ((ReinforcementState) ps).getNameLayer();
            MaterialType reinforcematerial = ((ReinforcementState) ps).getReinforceGroup();
            ReinforcementMaterial rm = ReinforcementMaterial.getReinforcementMaterialData(reinforcematerial);


            GSLChunk gslChunk = GSLChunk.getGSLChunk(event.getClickedBlock().getChunk());
            GSLCube gslCube = gslChunk.getCubes()[(event.getClickedBlock().getY() - GSLChunk.BLOCK_Y_OFFSET) / 16];
            if (gslCube == null) {
                gslCube = new GSLCube();
                gslChunk.getCubes()[(event.getClickedBlock().getY() - GSLChunk.BLOCK_Y_OFFSET) / 16]=gslCube;
            }
            int x = event.getClickedBlock().getX() % 16;
            if (event.getClickedBlock().getX() < 0)
                x = Math.abs((-event.getClickedBlock().getX()) % 16 - 15);
            int z = event.getClickedBlock().getZ() % 16;
            if (event.getClickedBlock().getZ() < 0)
                z = Math.abs((-event.getClickedBlock().getZ()) % 16 - 15);
            int y = (event.getClickedBlock().getY() - GSLChunk.BLOCK_Y_OFFSET)%16;

            if(gslCube.getNamelayers()[x][y][z]==null) {
                gslCube.getNamelayers()[x][y][z] = nl;
                gslCube.getReinforcedBy()[x][y][z] = reinforcematerial;
                gslCube.getDurability()[x][y][z] = rm.getDurability();
                ItemStack inhand = event.getPlayer().getInventory().getItemInMainHand();
                if(inhand.getAmount()==1) {
                    inhand = null;
                }else{
                    inhand.setAmount(inhand.getAmount()-1);
                }
                event.getPlayer().getInventory().setItemInMainHand(inhand);
            }else{
                if(gslCube.getNamelayers()[x][y][z].getMemberranks().containsKey(event.getPlayer().getUniqueId())){
                    ItemStack inhand = event.getPlayer().getInventory().getItemInMainHand();
                    if(inhand.getAmount()==1) {
                        inhand = null;
                    }else{
                        inhand.setAmount(inhand.getAmount()-1);
                    }
                    event.getPlayer().getInventory().setItemInMainHand(inhand);

                    if(gslCube.getReinforcedBy()[x][y][z]!=null)
                    event.getPlayer().getInventory().addItem(gslCube.getReinforcedBy()[x][y][z].toItemStack());

                    gslCube.getNamelayers()[x][y][z] = nl;
                    gslCube.getReinforcedBy()[x][y][z] = reinforcematerial;
                    gslCube.getDurability()[x][y][z] = rm.getDurability();
                }else{
                    event.getPlayer().sendMessage(new ComponentBuilder("You cannot reinforce this block, as it is already reinforced to a foreign group.",ComponentBuilder.RED).build());
                }
            }

        }
    }
}