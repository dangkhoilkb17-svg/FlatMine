package dev.flatmine.screen;

import dev.flatmine.client.ClientState;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public final class SettingsScreen extends Screen {
    private final Screen parent;
    private TextFieldWidget field;

    public SettingsScreen(Screen p) {
        super(Text.literal("FlatMine Settings"));
        this.parent = p;
    }

    @Override
    protected void init() {
        int centerX = width / 2;

        // Ô nhập số block tối đa
        field = new TextFieldWidget(textRenderer, centerX - 80, 45, 160, 20, Text.literal("Vùng xoá tối đa"));
        field.setText(Long.toString(ClientState.maxBlocks));
        addDrawableChild(field);

        // Giá trị ban đầu cho Slider
        double initialSliderValue = (ClientState.miningSpeed == 2.0f) ? 1.0 : (ClientState.miningSpeed == 1.5f ? 0.5 : 0.0);

        // Thanh trượt chọn tốc độ 3 nấc
        addDrawableChild(new SliderWidget(centerX - 80, 90, 160, 20, getSpeedText(ClientState.miningSpeed), initialSliderValue) {
            @Override
            protected void updateMessage() {
                setMessage(getSpeedText(getSnapSpeed(this.value)));
            }

            @Override
            protected void applyValue() {
                if (this.value < 0.25) this.value = 0.0;
                else if (this.value < 0.75) this.value = 0.5;
                else this.value = 1.0;

                ClientState.miningSpeed = getSnapSpeed(this.value);
            }
        });

        // Nút Lưu
        addDrawableChild(ButtonWidget.builder(Text.literal("Lưu"), b -> {
            try {
                ClientState.maxBlocks = Math.max(1, Long.parseLong(field.getText()));
            } catch (NumberFormatException ignored) {}
            close();
        }).dimensions(centerX - 50, 150, 100, 20).build());
    }

    private float getSnapSpeed(double val) {
        if (val >= 0.75) return 2.0f;
        if (val >= 0.25) return 1.5f;
        return 1.0f;
    }

    private Text getSpeedText(float speed) {
        if (speed == 2.0f) return Text.literal("Tốc độ: 2.0x (Cực nhanh)");
        if (speed == 1.5f) return Text.literal("Tốc độ: 1.5x (Nhanh)");
        return Text.literal("Tốc độ: 1.0x (Gốc x2)");
    }

    @Override
    public void close() {
        if (client != null) {
            client.setScreen(parent);
        }
    }

    @Override
    public void render(DrawContext c, int x, int y, float d) {
        super.render(c, x, y, d);
        int centerX = width / 2;

        c.drawCenteredTextWithShadow(textRenderer, "Vùng xoá tối đa (ngưỡng cảnh báo)", centerX, 30, 0xFFFFFF);
        c.drawCenteredTextWithShadow(textRenderer, "Tốc độ đào", centerX, 77, 0xFFFFFF);
        c.drawCenteredTextWithShadow(textRenderer, "(Cảnh báo: Tốc độ cao có thể gây giật lag server/máy)", centerX, 115, 0xFF5555);
    }
}