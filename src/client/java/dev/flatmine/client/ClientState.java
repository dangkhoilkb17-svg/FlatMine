package dev.flatmine.client;

import net.minecraft.util.math.BlockPos;

public final class ClientState {
    public static BlockPos a, b;
    public static long maxBlocks = 100000;
    public static float miningSpeed = 1.0f;
    public static boolean enabled = false;
    public static boolean destroyDrops = false;

    public static int miningSpeedLevel() {
        if (miningSpeed >= 1.75f) return 3;
        if (miningSpeed >= 1.25f) return 2;
        return 1;
    }

    public static void clear() {
        a = null;
        b = null;
        destroyDrops = false;
    }

    private ClientState() {}
}
