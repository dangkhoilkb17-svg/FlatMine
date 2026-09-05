package dev.flatmine.server;
import net.minecraft.block.*; import net.minecraft.block.entity.BlockEntity; import net.minecraft.server.world.ServerWorld; import net.minecraft.util.math.BlockPos;
public final class InteractiveBlocks {
 private InteractiveBlocks(){}
 public static boolean isInteractive(ServerWorld world, BlockPos pos){
  BlockState s=world.getBlockState(pos); Block b=s.getBlock(); BlockEntity be=world.getBlockEntity(pos);
  if(be!=null && s.createScreenHandlerFactory(world,pos)!=null) return true;
  return b instanceof CraftingTableBlock || b instanceof DoorBlock || b instanceof TrapdoorBlock || b instanceof FenceGateBlock || b instanceof ButtonBlock || b instanceof LeverBlock || b instanceof BedBlock || b instanceof NoteBlock || b instanceof BellBlock || b instanceof CakeBlock || b instanceof ComposterBlock;
 }
}
