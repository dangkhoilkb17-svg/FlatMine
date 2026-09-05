package dev.flatmine;

import dev.flatmine.common.Cuboid;
import dev.flatmine.network.FlatMinePayloads;
import dev.flatmine.server.*;
import java.util.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public final class FlatMine implements ModInitializer {

    private static final Map<UUID, SelectionState> SEL = new HashMap<>();
    private static final Map<UUID, Cuboid> PENDING = new HashMap<>();

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playC2S().register(FlatMinePayloads.Select.ID, FlatMinePayloads.Select.CODEC);
        PayloadTypeRegistry.playC2S().register(FlatMinePayloads.Action.ID, FlatMinePayloads.Action.CODEC);
        PayloadTypeRegistry.playS2C().register(FlatMinePayloads.Status.ID, FlatMinePayloads.Status.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(FlatMinePayloads.Select.ID, (payload, context) ->
            context.server().execute(() -> select(context.player(), payload.pos()) )
        );

        ServerPlayNetworking.registerGlobalReceiver(FlatMinePayloads.Action.ID, (payload, context) ->
            context.server().execute(() -> action(context.player(), payload.action(), payload.maxBlocks(), payload.destroyDrops()))
        );

        MiningJobManager.init();
    }

    static boolean validTool(ItemStack stack) {
        return stack.isOf(Items.WOODEN_PICKAXE) || stack.isOf(Items.STONE_PICKAXE) ||
               stack.isOf(Items.IRON_PICKAXE) || stack.isOf(Items.GOLDEN_PICKAXE) ||
               stack.isOf(Items.DIAMOND_PICKAXE) || stack.isOf(Items.NETHERITE_PICKAXE) ||
               stack.isOf(Items.WOODEN_SHOVEL) || stack.isOf(Items.STONE_SHOVEL) ||
               stack.isOf(Items.IRON_SHOVEL) || stack.isOf(Items.GOLDEN_SHOVEL) ||
               stack.isOf(Items.DIAMOND_SHOVEL) || stack.isOf(Items.NETHERITE_SHOVEL);
    }

    static void select(ServerPlayerEntity p, BlockPos pos) {
        boolean creative = p.isCreative();
        if (!(p.getWorld() instanceof ServerWorld w) ||
            (!creative && !p.interactionManager.isSurvivalLike()) ||
            (!creative && !validTool(p.getMainHandStack())) ||
            !w.isInBuildLimit(pos) ||
            !w.isChunkLoaded(pos) ||
            InteractiveBlocks.isInteractive(w, pos)) {
            return;
        }

        SelectionState s = SEL.computeIfAbsent(p.getUuid(), u -> new SelectionState());

        if (s.first == null || s.dimension != w.getRegistryKey()) {
            s.clear();
            s.dimension = w.getRegistryKey();
            s.first = pos.toImmutable();
            send(p, 1, s.first, s.first, 1L, 0);
            return;
        }

        s.second = pos.toImmutable();
        Cuboid c = Cuboid.of(s.first, s.second);
        PENDING.put(p.getUuid(), c);
        int remaining = p.getMainHandStack().getMaxDamage() - p.getMainHandStack().getDamage();
        send(p, 2, s.first, s.second, c.volume(), remaining);
    }

    static void action(ServerPlayerEntity p, int a, long max, boolean destroyDrops) {
        if (a == 0) {
            cancel(p);
            return;
        }

        Cuboid c = PENDING.get(p.getUuid());
        SelectionState s = SEL.get(p.getUuid());
        boolean creative = p.isCreative();

        // Creative chỉ có một chế độ: đào và luôn tiêu hủy toàn bộ drop.
        boolean allowedMode = creative || p.interactionManager.isSurvivalLike();
        boolean allowedTool = creative || validTool(p.getMainHandStack());

        if (c == null || s == null || s.dimension != p.getWorld().getRegistryKey() ||
            !allowedMode || !allowedTool) {
            return;
        }

        if (a == 2) {
            c = c.shrinkTo(Math.max(1, max));
        }

        if (a == 1 || a == 2) {
            PENDING.put(p.getUuid(), c);
            send(p, 3, new BlockPos(c.minX(), c.minY(), c.minZ()), new BlockPos(c.maxX(), c.maxY(), c.maxZ()), c.volume(), 0);

            PENDING.remove(p.getUuid());
            // Creative luôn ép destroyDrops=true. Survival giữ đúng lựa chọn của người chơi.
            MiningJobManager.startJob(p, (ServerWorld) p.getWorld(), c, creative || destroyDrops);
            return;
        }

        if (a == 3) {
            MiningJobManager.startJob(p, (ServerWorld) p.getWorld(), c, creative || destroyDrops);
            PENDING.remove(p.getUuid());
            send(p, 4, new BlockPos(c.minX(), c.minY(), c.minZ()), new BlockPos(c.maxX(), c.maxY(), c.maxZ()), c.volume(), 0);
        }
    }

    static void cancel(ServerPlayerEntity p) {
        MiningJobManager.cancelJob(p);
        SEL.computeIfAbsent(p.getUuid(), u -> new SelectionState()).clear();
        PENDING.remove(p.getUuid());
        send(p, 0, BlockPos.ORIGIN, BlockPos.ORIGIN, 0L, 0);
    }

    static void send(ServerPlayerEntity p, int k, BlockPos a, BlockPos b, long n, int d) {
        ServerPlayNetworking.send(p, new FlatMinePayloads.Status(k, a, b, n, d));
    }
}