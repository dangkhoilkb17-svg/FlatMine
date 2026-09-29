package dev.flatmine.server;

import dev.flatmine.FlatMine;
import dev.flatmine.common.Cuboid;
import java.util.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public final class MiningJobManager {
    private static final Map<UUID, MiningJob> JOBS = new HashMap<>();

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(s -> {
            Iterator<Map.Entry<UUID, MiningJob>> it = JOBS.entrySet().iterator();
            while (it.hasNext()) {
                var e = it.next();
                if (e.getValue().tick()) it.remove();
            }
        });
    }

    public static void startJob(ServerPlayerEntity p, ServerWorld w, Cuboid c, float speedMultiplier, boolean destroyDrops) {
        cancelJob(p);
        JOBS.put(p.getUuid(), new MiningJob(p, w, c, speedMultiplier, destroyDrops));
    }

    public static void cancelJob(ServerPlayerEntity p) {
        MiningJob j = JOBS.remove(p.getUuid());
        if (j != null) {
            j.cancel();
            clearSelection(p);
        }
    }

    public static boolean isRunning(ServerPlayerEntity p) {
        return JOBS.containsKey(p.getUuid());
    }

    public static void clearSelection(ServerPlayerEntity p) {
        FlatMine.clearSelectionState(p);
    }

    public static boolean processMiningTick(ServerPlayerEntity player, ServerWorld world, Queue<BlockPos> queue, float speedMultiplier, boolean destroyDrops) {
        ItemStack tool = player.getMainHandStack();

        if (player.isCreative() && destroyDrops) {
            return destroyQueue(world, queue, 32 * speedMultiplier, player);
        }

        if (!isPickaxeOrShovel(tool)) {
            player.sendMessage(Text.literal("§c[FlatMine] Bị hủy: Bạn phải cầm Cúp hoặc Xẻng để tiếp tục đào!"), true);
            clearSelection(player);
            return true;
        }

        int targetBreaks = Math.max(1, Math.round(32 * speedMultiplier));
        int brokenCount = 0;

        while (!queue.isEmpty() && brokenCount < targetBreaks) {
            BlockPos pos = queue.poll();
            var state = world.getBlockState(pos);
            if (state.isAir() || state.getHardness(world, pos) < 0) continue;

            BlockEntity blockEntity = world.getBlockEntity(pos);

            boolean canHarvestForDrop = !state.isToolRequired() || tool.isSuitableFor(state);
            ItemStack toolForDrops = tool.copy();

            if (destroyDrops) {
                if (blockEntity instanceof Inventory inv) inv.clear();
                world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS | Block.FORCE_STATE);
            } else {
                world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
            }

            if (!player.isCreative() && tool.isDamageable()) {
                boolean wrongTool = !tool.isSuitableFor(state);

                if (state.getHardness(world, pos) != 0.0F) {
                    if (wrongTool) {
                        tool.damage(2, player, net.minecraft.entity.EquipmentSlot.MAINHAND);
                    } else {
                        tool.postMine(world, state, pos, player);
                    }
                }

                if (tool.isEmpty()) {
                    player.sendMessage(Text.literal("§c[FlatMine] Công cụ của bạn đã vỡ!"), true);
                }
            }

            if (!destroyDrops && canHarvestForDrop) {
                Block.dropStacks(state, world, pos, blockEntity, player, toolForDrops);
                state.onStacksDropped(world, pos, toolForDrops, true);
            }

            brokenCount++;
        }

        if (queue.isEmpty()) {
            player.sendMessage(Text.literal("§a[FlatMine] Đã dọn dẹp xong khu vực!"), true);
            clearSelection(player);
            return true;
        }
        return false;
    }

    private static boolean destroyQueue(ServerWorld world, Queue<BlockPos> queue, float target, ServerPlayerEntity player) {
        int brokenCount = 0;
        int targetBreaks = Math.max(1, Math.round(target));

        while (!queue.isEmpty() && brokenCount < targetBreaks) {
            BlockPos pos = queue.poll();
            var state = world.getBlockState(pos);
            if (state.isAir() || state.getHardness(world, pos) < 0) continue;

            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof Inventory inv) inv.clear();
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS | Block.FORCE_STATE);
            brokenCount++;
        }

        if (queue.isEmpty()) {
            player.sendMessage(Text.literal("§a[FlatMine] Đã tiêu hủy xong khu vực!"), true);
            clearSelection(player);
            return true;
        }
        return false;
    }

    private static boolean isPickaxeOrShovel(ItemStack stack) {
        if (stack.isEmpty()) return false;
        String name = stack.getItem().getTranslationKey();
        return name.contains("pickaxe") || name.contains("shovel");
    }

    private MiningJobManager() {}
}
