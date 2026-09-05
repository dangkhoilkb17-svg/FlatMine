package dev.flatmine.common;
import static org.junit.jupiter.api.Assertions.*;import java.util.*;import net.minecraft.util.math.BlockPos;import org.junit.jupiter.api.Test;
class MiningOrderTest {
 @Test void freeSelectionAndReverseAreEqual(){Cuboid a=Cuboid.of(new BlockPos(10,60,10),new BlockPos(100,120,200));Cuboid b=Cuboid.of(new BlockPos(100,120,200),new BlockPos(10,60,10));assertEquals(a,b);assertEquals(91,a.sizeX());assertEquals(61,a.sizeY());assertEquals(191,a.sizeZ());}
 @Test void entireLayerBeforeDescending(){Cuboid c=Cuboid.of(new BlockPos(0,60,0),new BlockPos(1,62,1));MiningCursor q=new MiningCursor(c);List<BlockPos> p=new ArrayList<>();while(!q.done()){p.add(q.current());q.advance();}assertEquals(12,p.size());assertTrue(p.subList(0,4).stream().allMatch(x->x.getY()==62));assertTrue(p.subList(4,8).stream().allMatch(x->x.getY()==61));assertTrue(p.subList(8,12).stream().allMatch(x->x.getY()==60));}
}
