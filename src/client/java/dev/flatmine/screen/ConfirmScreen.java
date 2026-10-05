package dev.flatmine.screen;

import dev.flatmine.client.ClientState;
import dev.flatmine.network.FlatMinePayloads;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.text.Text;

public final class ConfirmScreen extends Screen {
    private final long blocks;
    private final int durability;
    private CheckboxWidget destroyDropsCheckbox;

    public ConfirmScreen(long b, int d) {
        super(Text.literal("Xác nhận vùng"));
        this.blocks = b;
        this.durability = d;
    }

    @Override
    protected void init() {
        int y = height / 2;

        destroyDropsCheckbox = CheckboxWidget.builder(Text.literal("Tiêu hủy vật phẩm rơi ra (Chống lag)"), textRenderer)
                .pos(width / 2 - 100, y - 30)
                .checked(ClientState.destroyDrops)
                .build();
        addDrawableChild(destroyDropsCheckbox);

        if (blocks > ClientState.maxBlocks) {
            addDrawableChild(ButtonWidget.builder(Text.literal("Thu nhỏ vùng"), x -> {
                ClientState.destroyDrops = destroyDropsCheckbox.isChecked();
                ClientPlayNetworking.send(new FlatMinePayloads.Action(
                    2, ClientState.maxBlocks, ClientState.miningSpeedLevel(), ClientState.destroyDrops
                ));
                close();
            }).dimensions(width / 2 - 155, y, 100, 20).build());

            addDrawableChild(ButtonWidget.builder(Text.literal("Giữ nguyên"), x -> next())
                    .dimensions(width / 2 - 50, y, 100, 20).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("Hủy"), x -> cancel())
                    .dimensions(width / 2 + 55, y, 100, 20).build());
        } else {
            addDrawableChild(ButtonWidget.builder(Text.literal("Bắt đầu Đào"), x -> next())
                    .dimensions(width / 2 - 105, y, 100, 20).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("Hủy"), x -> cancel())
                    .dimensions(width / 2 + 5, y, 100, 20).build());
        }
    }

    private void next() {
        ClientState.destroyDrops = destroyDropsCheckbox != null && destroyDropsCheckbox.isChecked();

        if (blocks > durability / 2L && client != null) {
            client.setScreen(new DurabilityScreen(blocks, ClientState.destroyDrops));
        } else {
            ClientPlayNetworking.send(new FlatMinePayloads.Action(
                1, ClientState.maxBlocks, ClientState.miningSpeedLevel(), ClientState.destroyDrops
            ));
            close();
        }
    }

    private void cancel() {
        ClientPlayNetworking.send(new FlatMinePayloads.Action(0, ClientState.maxBlocks, 1, false));
        ClientState.clear();
        close();
    }

    @Override
    public void render(DrawContext c, int x, int y, float d) {
        super.render(c, x, y, d);
        c.drawCenteredTextWithShadow(textRenderer, "Đã chọn " + blocks + " block.", width / 2, 60, 0xFFFFFF);
        if (blocks > ClientState.maxBlocks) {
            c.drawCenteredTextWithShadow(textRenderer, "Vùng đã chọn quá lớn và có thể gây lag.", width / 2, 82, 0xFFCC55);
        }
    }
}
