package lol.hisoka.manifold;

import lol.hisoka.manifold.api.endpoint.Endpoint;
import lol.hisoka.manifold.api.endpoint.EndpointType;
import lol.hisoka.manifold.capability.CapabilityEndpoint;
import lol.hisoka.manifold.capability.CapabilityEndpointAdapter;
import lol.hisoka.manifold.capability.EndpointAdapterRegistry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.Optional;

@GameTestHolder(Manifold.MOD_ID)
public final class CapabilityDiscoveryTest {

    @GameTest(template = "test_room")
    public static void capabilityAdapterDiscoversChestInventory(GameTestHelper helper) {
        helper.startSequence()
                .thenExecute(() -> {
                    helper.setBlock(1, 1, 1, Blocks.CHEST);
                })
                .thenIdle(1)
                .thenExecute(() -> {
                    BlockPos chestPos = helper.absolutePos(new BlockPos(1, 1, 1));

                    Optional<Endpoint> endpoint = EndpointAdapterRegistry.get()
                            .tryCreate(helper.getLevel(), chestPos, Direction.NORTH);

                    if (endpoint.isEmpty()) {
                        helper.fail("CapabilityEndpointAdapter failed to discover chest inventory");
                        return;
                    }

                    if (!(endpoint.get() instanceof CapabilityEndpoint capEp)) {
                        helper.fail("Endpoint is not a CapabilityEndpoint");
                        return;
                    }

                    IItemHandler handler = capEp.getItemHandler();
                    if (handler == null) {
                        helper.fail("CapabilityEndpoint has no IItemHandler");
                        return;
                    }

                    if (handler.getSlots() < 27) {
                        helper.fail("Expected at least 27 slots, got " + handler.getSlots());
                        return;
                    }
                })
                .thenSucceed();
    }

    @GameTest(template = "test_room")
    public static void adapterRegistryHasCapabilityAdapter(GameTestHelper helper) {
        helper.startSequence()
                .thenExecute(() -> {
                    long capabilityCount = EndpointAdapterRegistry.get().all().stream()
                            .filter(a -> a instanceof CapabilityEndpointAdapter)
                            .count();

                    if (capabilityCount == 0) {
                        helper.fail("EndpointAdapterRegistry missing CapabilityEndpointAdapter");
                    }
                })
                .thenSucceed();
    }

    @GameTest(template = "test_room")
    public static void airBlockReturnsNoEndpoint(GameTestHelper helper) {
        helper.startSequence()
                .thenExecute(() -> {
                    BlockPos airPos = helper.absolutePos(new BlockPos(1, 2, 1));

                    Optional<Endpoint> endpoint = EndpointAdapterRegistry.get()
                            .tryCreate(helper.getLevel(), airPos, Direction.NORTH);

                    if (endpoint.isPresent()) {
                        helper.fail("Should not create endpoint for air block");
                    }
                })
                .thenSucceed();
    }
}
