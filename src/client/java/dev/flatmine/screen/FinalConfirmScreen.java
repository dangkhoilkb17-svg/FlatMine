package dev.flatmine.screen;

import dev.flatmine.client.ClientState;
import dev.flatmine.network.FlatMinePayloads;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class FinalConfirmScreen extends Screen {
    private final long blocks;

    public FinalConfirmScreen(long b) {
        super(Text.literal("Xác nhận cuối"));
        this.blocks = b;
    }

    @Override
    protected void init() {
        addDrawableChild(ButtonWidget.builder(Text.literal("Bắt đầu đào"), b -> {
            // ĐÃ SỬA: THÊM , false
            ClientPlayNetworking.send(new FlatMinePayloads.Action(3, ClientState.maxBlocks, false));
            close();
        }).dimensions(width / 2 - 105, height / 2, 100, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Hủy"), b -> {
            // ĐÃ SỬA: THÊM , false
            ClientPlayNetworking.send(new FlatMinePayloads.Action(0, ClientState.maxBlocks, false));
            close();
        }).dimensions(width / 2 + 5, height / 2, 100, 20).build());
    }

    @Override
    public void render(DrawContext c, int x, int y, float d) {
        super.render(c, x, y, d);
        c.drawCenteredTextWithShadow(textRenderer, "Đào " + blocks + " block từ Y cao xuống Y thấp?", width / 2, 70, 0xFFFFFF);
    }
}