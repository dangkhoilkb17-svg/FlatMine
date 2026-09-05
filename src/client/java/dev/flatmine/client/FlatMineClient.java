package dev.flatmine.client;

import dev.flatmine.network.FlatMinePayloads;
import dev.flatmine.render.SelectionRenderer;
import dev.flatmine.screen.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

public final class FlatMineClient implements ClientModInitializer {
    private boolean tutorialShown = false;

    @Override
    public void onInitializeClient() {
        KeyBinding cancel = KeyBindingHelper.registerKeyBinding(
            new KeyBinding("key.flatmine.cancel", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_X, "category.flatmine")
        );
        KeyBinding settings = KeyBindingHelper.registerKeyBinding(
            new KeyBinding("key.flatmine.settings", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, "category.flatmine")
        );

        UseBlockCallback.EVENT.register((p, w, h, hit) -> {
            if (!w.isClient || h != Hand.MAIN_HAND) return ActionResult.PASS;
            if (!isTool(p.getMainHandStack())) return ActionResult.PASS;
            
            BlockState s = w.getBlockState(hit.getBlockPos());
            if (s.createScreenHandlerFactory(w, hit.getBlockPos()) != null || 
                s.getBlock() instanceof DoorBlock || 
                s.getBlock() instanceof TrapdoorBlock || 
                s.getBlock() instanceof FenceGateBlock || 
                s.getBlock() instanceof ButtonBlock || 
                s.getBlock() instanceof LeverBlock || 
                s.getBlock() instanceof CraftingTableBlock) {
                return ActionResult.PASS;
            }

            ClientPlayNetworking.send(new FlatMinePayloads.Select(hit.getBlockPos()));
            return ActionResult.SUCCESS;
        });

        ClientPlayNetworking.registerGlobalReceiver(FlatMinePayloads.Status.ID, (payload, ctx) -> 
            ctx.client().execute(() -> handle(payload))
        );

        WorldRenderEvents.AFTER_TRANSLUCENT.register(SelectionRenderer::render);

        ClientTickEvents.END_CLIENT_TICK.register(c -> {
            while (cancel.wasPressed()) {
                ClientState.clear();
                // ĐÃ SỬA: THÊM , false
                ClientPlayNetworking.send(new FlatMinePayloads.Action(0, ClientState.maxBlocks, false));
                c.setScreen(null);
            }
            while (settings.wasPressed()) {
                c.setScreen(new SettingsScreen(c.currentScreen));
            }
            if (!tutorialShown && c.player != null) {
                tutorialShown = true;
                c.setScreen(new TutorialScreen());
            }
        });
    }

    static boolean isTool(net.minecraft.item.ItemStack s) {
        return s.isIn(net.minecraft.registry.tag.ItemTags.PICKAXES) || 
               s.isIn(net.minecraft.registry.tag.ItemTags.SHOVELS);
    }

    static void handle(FlatMinePayloads.Status p) {
        MinecraftClient c = MinecraftClient.getInstance();
        if (p.kind() == 0) {
            ClientState.clear();
            return;
        }
        ClientState.a = p.a();
        ClientState.b = p.b();
        
        if (p.kind() == 2) {
            c.setScreen(new ConfirmScreen(p.blocks(), p.durability()));
        } else if (p.kind() == 3) {
            c.setScreen(new FinalConfirmScreen(p.blocks()));
        }
    }
}