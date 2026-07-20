package lol.hisoka.manifold.api.module;

public interface PipeModule {

    PipeModuleType<?> type();

    boolean isActive();

    void setActive(boolean active);

    default void onTick() {}
}
