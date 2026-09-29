package dev.flatmine.client;

import net.minecraft.util.math.BlockPos;

public final class ClientState { 
    public static BlockPos a, b; 
    public static long maxBlocks = 100000; 
    // Hệ số tốc độ đào: 1.0f (Gốc x2), 1.5f (Nhanh), 2.0f (Cực nhanh)
    public static float miningSpeed = 1.0f;
    // Công tắc an toàn: mặc định tắt để tránh bấm nhầm FlatMine.
    public static boolean enabled = false;

    public static void clear() {
        a = null;
        b = null;
    } 
    
    private ClientState() {} 
}