package dev.flatmine.server;
import net.minecraft.registry.RegistryKey; import net.minecraft.util.math.BlockPos; import net.minecraft.world.World;
public final class SelectionState { public RegistryKey<World> dimension; public BlockPos first, second; public void clear(){dimension=null;first=null;second=null;} }
