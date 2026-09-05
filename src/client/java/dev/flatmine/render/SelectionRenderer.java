package dev.flatmine.render;
import dev.flatmine.client.ClientState; import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext; import net.minecraft.client.render.RenderLayer; import net.minecraft.client.render.WorldRenderer; import net.minecraft.util.math.Box;
public final class SelectionRenderer {
 public static void render(WorldRenderContext c){if(ClientState.a==null)return;var cam=c.camera().getPos();var b=ClientState.b==null?ClientState.a:ClientState.b;double minX=Math.min(ClientState.a.getX(),b.getX())-cam.x,minY=Math.min(ClientState.a.getY(),b.getY())-cam.y,minZ=Math.min(ClientState.a.getZ(),b.getZ())-cam.z,maxX=Math.max(ClientState.a.getX(),b.getX())+1-cam.x,maxY=Math.max(ClientState.a.getY(),b.getY())+1-cam.y,maxZ=Math.max(ClientState.a.getZ(),b.getZ())+1-cam.z;var vc=c.consumers().getBuffer(RenderLayer.getLines());WorldRenderer.drawBox(c.matrixStack(),vc,new Box(minX,minY,minZ,maxX,maxY,maxZ),1,1,1,1);}
 private SelectionRenderer(){}
}
