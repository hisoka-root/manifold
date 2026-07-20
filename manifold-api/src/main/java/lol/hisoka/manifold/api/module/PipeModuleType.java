package lol.hisoka.manifold.api.module;

import lol.hisoka.manifold.api.endpoint.Endpoint;

import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public record PipeModuleType<T extends PipeModule>(
        ResourceLocation id,
        Function<Endpoint, T> factory
) {

    public T create(Endpoint endpoint) {
        return factory.apply(endpoint);
    }

    @Override
    public String toString() {
        return "PipeModuleType[%s]".formatted(id);
    }
}
