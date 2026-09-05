# FlatMine
Fabric mod for Minecraft Java 1.21.1 / Java 21.

Build: `./gradlew clean build`

Selection is a free cuboid from two arbitrary block positions. Server mining visits every X/Z position of maxY before descending to the next Y. It calls `ServerPlayerInteractionManager.tryBreakBlock` after vanilla break progress reaches 1, preserving vanilla drops, enchantments, XP and durability.
