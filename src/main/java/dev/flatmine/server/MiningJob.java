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

        List<BlockPos> list = new ArrayList<>();
        for (BlockPos pos : BlockPos.iterate(minPos, maxPos)) {
            if (!world.getBlockState(pos).isAir() && world.isInBuildLimit(pos)) {
                list.add(pos.toImmutable());
            }
        }

        list.sort((p1, p2) -> Integer.compare(p2.getY(), p1.getY()));
        queue.addAll(list);
    }

    public boolean tick() {
        if (player.isRemoved() || player.getWorld() != world) {
            MiningJobManager.clearSelection(player);
            return true;
        }

        // Creative chỉ được dùng chế độ tiêu hủy. Không chạy harvest/drop/durability mechanics.
        if (player.isCreative() && !destroyDrops) {
            MiningJobManager.clearSelection(player);
            return true;
        }

        // Survival-like mới được chạy cơ chế đào bình thường.
        if (!player.isCreative() && !player.interactionManager.isSurvivalLike()) {
            MiningJobManager.clearSelection(player);
            return true;
        }

        return MiningJobManager.processMiningTick(player, world, queue, 1.0f, destroyDrops);
    }

    public void cancel() {
        queue.clear();
    }
}