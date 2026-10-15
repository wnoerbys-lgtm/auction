package dev.codex.paytracker;

import net.fabricmc.loader.api.FabricLoader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PayTrackerStore {
    private static final Pattern PAYMENT = Pattern.compile("^(.+?) paid you \\$\\s*([\\d,]+(?:\\.\\d+)?)$", Pattern.CASE_INSENSITIVE);
    private static final int MAX_ENTRIES = 100;
    private static final Deque<Payment> PAYMENTS = new ArrayDeque<>();
    private static final Path CONFIG = FabricLoader.getInstance().getConfigDir().resolve("paytracker.properties");
    private static BigDecimal minimum = BigDecimal.ZERO;

    private PayTrackerStore() {}

    public static synchronized void load() {
        if (!Files.exists(CONFIG)) return;
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(CONFIG)) {
            properties.load(reader);
            minimum = new BigDecimal(properties.getProperty("minimum", "0"));
        } catch (IOException | NumberFormatException ignored) {
            minimum = BigDecimal.ZERO;
        }
    }

    public static synchronized void setMinimum(BigDecimal amount) {
        minimum = amount.max(BigDecimal.ZERO);
        Properties properties = new Properties();
        properties.setProperty("minimum", minimum.stripTrailingZeros().toPlainString());
        try {
            Files.createDirectories(CONFIG.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG)) {
                properties.store(writer, "Donut Pay Tracker settings");
            }
        } catch (IOException ignored) {
            // The setting remains active for this session even if saving fails.
        }
    }

    public static synchronized BigDecimal getMinimum() { return minimum; }

    public static synchronized void recordPayment(String message) {
        Matcher matcher = PAYMENT.matcher(message);
        if (!matcher.matches()) return;
        try {
            BigDecimal amount = new BigDecimal(matcher.group(2).replace(",", ""));
            if (amount.compareTo(minimum) < 0) return;
            PAYMENTS.addFirst(new Payment(matcher.group(1).trim(), amount));
            while (PAYMENTS.size() > MAX_ENTRIES) PAYMENTS.removeLast();
        } catch (NumberFormatException ignored) {
            // Ignore malformed messages that resemble payment receipts.
        }
    }

    public static synchronized Payment[] getPayments() { return PAYMENTS.toArray(Payment[]::new); }

    public static synchronized BigDecimal getTotal() {
        return PAYMENTS.stream().map(Payment::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public record Payment(String player, BigDecimal amount) {}
}
