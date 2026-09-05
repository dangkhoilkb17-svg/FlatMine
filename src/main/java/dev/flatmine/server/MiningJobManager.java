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
             * Kết hợp cơ chế phá/harvest và drop của Vanilla:
             *
             * - Nếu block không yêu cầu tool, cúp/xẻng vẫn có thể harvest và nhận drop.
             *   Ví dụ: cúp + Dirt hoặc xẻng + Dirt đều drop Dirt.
             * - Nếu block yêu cầu tool, phải dùng đúng loại và đủ tier thông qua
             *   ItemStack.isSuitableFor().
             *   Ví dụ: cúp đá + Iron Ore -> drop; cúp gỗ + Iron Ore -> không drop;
             *   cúp sắt + Diamond Ore -> không drop.
             * - Tính drop trước khi destroyDrops để destroyDrops không làm thay đổi
             *   quy tắc độ bền.
             */
            boolean canHarvest = !state.isToolRequired() || tool.isSuitableFor(state);
            List<ItemStack> drops = canHarvest
                    ? Block.getDroppedStacks(state, world, pos, blockEntity, player, tool)
                    : Collections.emptyList();
            boolean hasDrop = !drops.isEmpty();

            // Có drop hợp lệ theo Vanilla -> -1 độ bền; không có -> -2.
            if (!player.isCreative() && tool.isDamageable()) {
                tool.damage(hasDrop ? 1 : 2, player, EquipmentSlot.MAINHAND);
                if (tool.isEmpty()) {
                    player.sendMessage(Text.literal("§c[FlatMine] Công cụ của bạn đã vỡ!"), true);
                    clearSelection(player);
                    return true;
                }
            }

            // destroyDrops chỉ quyết định drop có xuất hiện hay không.
            if (destroyDrops) {
                if (blockEntity instanceof Inventory inv) inv.clear();
                world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS | Block.FORCE_STATE);
            } else {
                // Spawn đúng danh sách drop đã được kiểm tra ở trên.
                // Không gọi lại Block.dropStacks() để tránh việc kiểm tra drop lần hai
                // cho kết quả khác với kết quả dùng để tính độ bền.
                for (ItemStack drop : drops) {
                    if (!drop.isEmpty()) {
                        Block.dropStack(world, pos, drop);
                    }
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

    private static boolean isPickaxeOrShovel(ItemStack stack) {
        if (stack.isEmpty()) return false;
        String name = stack.getItem().getTranslationKey();
        return name.contains("pickaxe") || name.contains("shovel");
    }

    private MiningJobManager() {}
}
