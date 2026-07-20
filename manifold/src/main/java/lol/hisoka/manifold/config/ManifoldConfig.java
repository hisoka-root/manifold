package lol.hisoka.manifold.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ManifoldConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue FE_COST_PER_ITEM =
            BUILDER.comment("Forge Energy cost per item routed through a pipe segment")
                    .defineInRange("energyCostPerItem", 10, 0, 10_000);

    public static final ModConfigSpec.IntValue COALESCING_WINDOW_TICKS =
            BUILDER.comment("Ticks to coalesce inventory-change events before marking routing dirty")
                    .defineInRange("coalescingWindowTicks", 4, 1, 40);

    public static final ModConfigSpec.IntValue ROUTING_POOL_THREADS =
            BUILDER.comment("Thread count for off-thread routing computation (min 1, max 8)")
                    .defineInRange("routingPoolThreads",
                            Math.clamp(Runtime.getRuntime().availableProcessors() / 2, 1, 4),
                            1, 8);

    public static final ModConfigSpec.IntValue THROUGHPUT_CAP_PER_TICK =
            BUILDER.comment("Maximum items moved per pipe segment per tick")
                    .defineInRange("throughputCapPerTick", 4, 1, 64);

    public static final ModConfigSpec.BooleanValue GRACEFUL_DEGRADATION =
            BUILDER.comment("When true, zero-energy networks queue items instead of hard-stopping")
                    .define("gracefulDegradationOnZeroEnergy", true);

    public static final ModConfigSpec SPEC = BUILDER.build();
}
