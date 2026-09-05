package dev.flatmine.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public final class FlatMinePayloads {
    public record Select(BlockPos pos) implements CustomPayload { 
        public static final Id<Select> ID=new Id<>(Identifier.of("flatmine","select")); 
        public static final PacketCodec<RegistryByteBuf,Select> CODEC=PacketCodec.tuple(BlockPos.PACKET_CODEC,Select::pos,Select::new); 
        public Id<? extends CustomPayload> getId(){return ID;} 
    }
    
    // Thêm boolean destroyDrops vào payload
    public record Action(int action, long maxBlocks, boolean destroyDrops) implements CustomPayload { 
        public static final Id<Action> ID=new Id<>(Identifier.of("flatmine","action")); 
        public static final PacketCodec<RegistryByteBuf,Action> CODEC=PacketCodec.tuple(
            PacketCodecs.VAR_INT, Action::action,
            PacketCodecs.VAR_LONG, Action::maxBlocks,
            PacketCodecs.BOOL, Action::destroyDrops, 
            Action::new
        ); 
        public Id<? extends CustomPayload> getId(){return ID;} 
    }
    
    public record Status(int kind, BlockPos a, BlockPos b, long blocks, int durability) implements CustomPayload { 
        public static final Id<Status> ID=new Id<>(Identifier.of("flatmine","status")); 
        public static final PacketCodec<RegistryByteBuf,Status> CODEC=PacketCodec.tuple(PacketCodecs.VAR_INT,Status::kind,BlockPos.PACKET_CODEC,Status::a,BlockPos.PACKET_CODEC,Status::b,PacketCodecs.VAR_LONG,Status::blocks,PacketCodecs.VAR_INT,Status::durability,Status::new); 
        public Id<? extends CustomPayload> getId(){return ID;} 
    }
    
    private FlatMinePayloads(){}
}