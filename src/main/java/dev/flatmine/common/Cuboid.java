package dev.flatmine.common;

import net.minecraft.util.math.BlockPos;

public record Cuboid(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
    public static Cuboid of(BlockPos a, BlockPos b) {
        return new Cuboid(Math.min(a.getX(), b.getX()), Math.min(a.getY(), b.getY()), Math.min(a.getZ(), b.getZ()),
                Math.max(a.getX(), b.getX()), Math.max(a.getY(), b.getY()), Math.max(a.getZ(), b.getZ()));
    }
    public long sizeX() { return (long) maxX - minX + 1; }
    public long sizeY() { return (long) maxY - minY + 1; }
    public long sizeZ() { return (long) maxZ - minZ + 1; }
    public long volume() { return Math.multiplyExact(Math.multiplyExact(sizeX(), sizeY()), sizeZ()); }
    public boolean contains(BlockPos p) { return p.getX() >= minX && p.getX() <= maxX && p.getY() >= minY && p.getY() <= maxY && p.getZ() >= minZ && p.getZ() <= maxZ; }
    public Cuboid shrinkTo(long maxBlocks) {
        if (maxBlocks < 1 || volume() <= maxBlocks) return this;
        long footprint = sizeX() * sizeZ();
        if (footprint <= maxBlocks) {
            int keptY = (int)Math.max(1, maxBlocks / footprint);
            return new Cuboid(minX, maxY - keptY + 1, minZ, maxX, maxY, maxZ);
        }
        long keptX = Math.max(1, Math.min(sizeX(), maxBlocks / Math.max(1, sizeZ())));
        long keptZ = Math.max(1, Math.min(sizeZ(), maxBlocks / keptX));
        return new Cuboid(minX, maxY, minZ, minX + (int)keptX - 1, maxY, minZ + (int)keptZ - 1);
    }
}
