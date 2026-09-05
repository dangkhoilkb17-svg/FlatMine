package dev.flatmine.screen;

import dev.flatmine.client.ClientState;
import dev.flatmine.network.FlatMinePayloads;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class DurabilityScreen extends Screen {
    private final long blocks;

    public DurabilityScreen(long b) {
        super(Text.literal("Cảnh báo độ bền"));
        this.blocks = b;
    }

    @Override
    protected void init() {
        // ĐÃ SỬA: THÊM , false
        addDrawableChild(ButtonWidget.builder(Text.literal("Tiếp tục"), b -> ClientPlayNetworking.send(new FlatMinePayloads.Action(1, ClientState.maxBlocks, false))).dimensions(width / 2 - 105, height / 2, 100, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Hủy"), b -> {
            // ĐÃ SỬA: THÊM , false
            ClientPlayNetworking.send(new FlatMinePayloads.Action(0, ClientState.maxBlocks, false));
            close();
        }).dimensions(width / 2 + 5, height / 2, 100, 20).build());
    }

    @Override
    public void render(DrawContext c, int x, int y, float d) {
        super.render(c, x, y, d);
        c.drawCenteredTextWithShadow(textRenderer, "Tool có thể hỏng trong quá trình đào.", width / 2, 60, 0xFFAA55);
        c.drawCenteredTextWithShadow(textRenderer, "Gợi ý: chọn vùng nhỏ hơn hoặc đổi công cụ.", width / 2, 80, 0xFFFFFF);
    }
}