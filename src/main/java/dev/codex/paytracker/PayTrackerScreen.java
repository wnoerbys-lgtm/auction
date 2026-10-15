package dev.codex.paytracker;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public final class PayTrackerScreen extends Screen {
    private EditBox minimumInput;
    private String status = "";

    public PayTrackerScreen() { super(Component.literal("Donut Pay Tracker")); }

    @Override
    protected void init() {
        int center = width / 2;
        minimumInput = new EditBox(font, center - 110, 54, 150, 20, Component.literal("Minimum payment"));
        minimumInput.setValue(PayTrackerStore.getMinimum().stripTrailingZeros().toPlainString());
        minimumInput.setHint(Component.literal("Minimum amount"));
        addRenderableWidget(minimumInput);
        addRenderableWidget(Button.builder(Component.literal("Save minimum"), button -> saveMinimum())
                .bounds(center + 48, 54, 92, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"), button -> onClose())
                .bounds(center - 40, height - 32, 80, 20).build());
    }

    private void saveMinimum() {
        try {
            BigDecimal value = new BigDecimal(minimumInput.getValue().replace(",", "").trim());
            if (value.signum() < 0) throw new NumberFormatException();
            PayTrackerStore.setMinimum(value);
            status = "Minimum saved";
        } catch (NumberFormatException exception) {
            status = "Enter a valid amount";
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(0, 0, width, height, 0xC0101010);
        int center = width / 2;
        graphics.drawCenteredString(font, title, center, 18, 0xFFFFFF);
        graphics.drawString(font, "Total tracked: $" + money(PayTrackerStore.getTotal()), center - 140, 88, 0x55FF55);
        graphics.drawString(font, "Minimum: $" + money(PayTrackerStore.getMinimum()), center - 140, 102, 0xAAAAAA);
        graphics.drawString(font, "Recent payments", center - 140, 124, 0xFFFFFF);

        PayTrackerStore.Payment[] payments = PayTrackerStore.getPayments();
        int visibleRows = Math.max(0, Math.min(10, (height - 180) / 14));
        for (int i = 0; i < Math.min(payments.length, visibleRows); i++) {
            PayTrackerStore.Payment payment = payments[i];
            graphics.drawString(font, payment.player(), center - 140, 142 + i * 14, 0xFFFFFF);
            graphics.drawString(font, "$" + money(payment.amount()), center + 80, 142 + i * 14, 0x55FF55);
        }
        if (payments.length == 0) graphics.drawString(font, "No matching payments yet.", center - 140, 144, 0x888888);
        if (!status.isEmpty()) graphics.drawCenteredString(font, status, center, height - 48, 0xFFFF55);
        super.render(graphics, mouseX, mouseY, delta);
    }

    private static String money(BigDecimal amount) {
        return NumberFormat.getNumberInstance(Locale.US).format(amount.stripTrailingZeros());
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
