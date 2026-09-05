package dev.flatmine.common;
import net.minecraft.util.math.BlockPos;
public final class MiningCursor {
 private final Cuboid c; private int x,z,y; private boolean done;
 public MiningCursor(Cuboid c){this.c=c; x=c.minX(); z=c.minZ(); y=c.maxY();}
 public boolean done(){return done;}
 public BlockPos current(){return done?null:new BlockPos(x,y,z);}
 public void advance(){ if(done)return; if(++z<=c.maxZ())return; z=c.minZ(); if(++x<=c.maxX())return; x=c.minX(); if(--y>=c.minY())return; done=true; }
}
