package dev.flatmine.server;

import dev.flatmine.common.Cuboid;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public final class MiningJob {
    private final ServerPlayerEntity player;
    private final ServerWorld world;
    private final Queue<BlockPos> queue;
    private final boolean destroyDrops;

    public MiningJob(ServerPlayerEntity p, ServerWorld w, Cuboid c, boolean destroyDrops) {
        this.player = p;
        this.world = w;
        this.destroyDrops = destroyDrops;
        this.queue = new LinkedList<>();

        BlockPos minPos = new BlockPos(c.minX(), c.minY(), c.minZ());
        BlockPos maxPos = new BlockPos(c.maxX(), c.maxY(), c.maxZ());

        // BƯỚC 1: LẤY TOÀN BỘ CÁC BLOCK BỎ VÀO DANH SÁCH
        List<BlockPos> list = new ArrayList<>();
        for (BlockPos pos : BlockPos.iterate(minPos, maxPos)) {
            if (!world.getBlockState(pos).isAir() && world.isInBuildLimit(pos)) {
                list.add(pos.toImmutable());
            }
        }

        // BƯỚC 2: SẮP XẾP TỪ Y CAO XUỐNG Y THẤP (Để đào cuốn chiếu từ trên xuống)
        list.sort((p1, p2) -> Integer.compare(p2.getY(), p1.getY()));
        
        // BƯỚC 3: ĐƯA VÀO HÀNG ĐỢI
        queue.addAll(list);
    }

    public boolean tick() {
        if (player.isRemoved() || player.getWorld() != world || !player.interactionManager.isSurvivalLike()) {
            MiningJobManager.clearSelection(player); // Xóa viền sáng nếu người chơi chết hoặc đổi map
            return true; 
        }
        
        return MiningJobManager.processMiningTick(player, world, queue, 1.0f, destroyDrops);
    }

    public void cancel() {
        queue.clear();
    }
}