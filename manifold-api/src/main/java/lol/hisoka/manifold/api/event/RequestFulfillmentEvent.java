package lol.hisoka.manifold.api.event;

import lol.hisoka.manifold.api.network.EndpointId;

import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class RequestFulfillmentEvent extends Event {

    private final EndpointId requesterId;
    private final ItemStack requested;

    protected RequestFulfillmentEvent(@NotNull EndpointId requesterId, @NotNull ItemStack requested) {
        this.requesterId = requesterId;
        this.requested = requested;
    }

    @NotNull
    public EndpointId getRequesterId() {
        return requesterId;
    }

    @NotNull
    public ItemStack getRequested() {
        return requested;
    }

    public static final class Pre extends RequestFulfillmentEvent implements ICancellableEvent {

        public Pre(@NotNull EndpointId requesterId, @NotNull ItemStack requested) {
            super(requesterId, requested);
        }
    }

    public static final class Post extends RequestFulfillmentEvent {

        private final boolean success;
        @Nullable
        private final EndpointId providerId;

        public Post(@NotNull EndpointId requesterId, @NotNull ItemStack requested,
                    boolean success, @Nullable EndpointId providerId) {
            super(requesterId, requested);
            this.success = success;
            this.providerId = providerId;
        }

        public boolean isSuccess() {
            return success;
        }

        @Nullable
        public EndpointId getProviderId() {
            return providerId;
        }
    }
}
