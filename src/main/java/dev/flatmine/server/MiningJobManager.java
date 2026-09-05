package dev.flatmine.server;

import dev.flatmine.common.Cuboid;
import dev.flatmine.network.FlatMinePayloads;
import java.util.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EquipmentSlot;
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

    public static void startJob(ServerPlayerEntity p, ServerWorld w, Cuboid c, boolean destroyDrops) {
        cancelJob(p);
        JOBS.put(p.getUuid(), new MiningJob(p, w, c, destroyDrops));
    }

    public static void cancelJob(ServerPlayerEntity p) {
        MiningJob j = JOBS.remove(p.getUuid());
        if (j != null) { j.cancel(); clearSelection(p); }
    }

    public static boolean isRunning(ServerPlayerEntity p) { return JOBS.containsKey(p.getUuid()); }

    public static void clearSelection(ServerPlayerEntity p) {
        if (p.networkHandler != null) {
            ServerPlayNetworking.send(p, new FlatMinePayloads.Status(0, BlockPos.ORIGIN, BlockPos.ORIGIN, 0, 0));
        }
    }

    public static boolean processMiningTick(ServerPlayerEntity player, ServerWorld world, Queue<BlockPos> queue, float speedMultiplier, boolean destroyDrops) {
        ItemStack tool = player.getMainHandStack();

        // Creative: chỉ chạy chế độ tiêu hủy, không kiểm tra tool, harvest, drop hay durability.
        if (player.isCreative() && destroyDrops) {
            return destroyQueue(world, queue, 32 * speedMultiplier, true, player);
        }

        // Mọi chế độ khác phải là Survival-like và có cúp/xẻng để dùng mining mechanics.
        if (!isPickaxeOrShovel(tool)) {
            player.sendMessage(Text.literal("§c[FlatMine] Bị hủy: Bạn phải cầm Cúp hoặc Xẻng để tiếp tục đào!"), true);
            clearSelection(player);
            return true;
        }

        int targetBreaks = Math.round(32 * speedMultiplier);
        int brokenCount = 0;

        while (!queue.isEmpty() && brokenCount < targetBreaks) {
            BlockPos pos = queue.poll();
            var state = world.getBlockState(pos);
            if (state.isAir() || state.getHardness(world, pos) < 0) continue;

            BlockEntity blockEntity = world.getBlockEntity(pos);

            /*
             * Survival mining mechanics:
             * - FlatMine phá block độc lập với tốc độ/loại tool.
             * - Drop vẫn dùng điều kiện harvest + loot của Vanilla 1.21.1.
             * - Đủ điều kiện harvest và có item/block drop -> -1 durability.
             * - Không đủ điều kiện hoặc không có item/block drop -> -2 durability.
             * - Block không yêu cầu tool vẫn có thể drop bình thường với cúp/xẻng.
             */
            boolean canHarvestForDrop = !state.isToolRequired() || tool.isSuitableFor(state);
            List<ItemStack> drops = canHarvestForDrop
                    ? Block.getDroppedStacks(state, world, pos, blockEntity, player, tool)
                    : Collections.emptyList();
            boolean hasDrop = !drops.isEmpty();

            if (!player.isCreative() && tool.isDamageable()) {
                tool.damage(hasDrop ? 1 : 2, player, EquipmentSlot.MAINHAND);
                if (tool.isEmpty()) {
                    player.sendMessage(Text.literal("§c[FlatMine] Công cụ của bạn đã vỡ!"), true);
                    clearSelection(player);
                    return true;
                }
            }

            if (destroyDrops) {
                if (blockEntity instanceof Inventory inv) inv.clear();
                world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS | Block.FORCE_STATE);
            } else {
                if (canHarvestForDrop) {
                    Block.dropStacks(state, world, pos, blockEntity, player, tool);
                    state.onStacksDropped(world, pos, tool, true);
                }
                world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
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

    private static boolean destroyQueue(ServerWorld world, Queue<BlockPos> queue, float target, boolean force, ServerPlayerEntity player) {
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
