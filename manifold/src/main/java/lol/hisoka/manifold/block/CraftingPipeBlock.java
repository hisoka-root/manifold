package lol.hisoka.manifold.block;

import lol.hisoka.manifold.api.endpoint.EndpointType;

/**
 * v0.2+ — registers crafting recipes against the network's Crafting Table.
 * Not registered in {@link ManifoldRegistries} for v0.1.
 */
public final class CraftingPipeBlock extends AbstractPipeBlock {

    public CraftingPipeBlock(Properties properties) {
        super(properties, EndpointType.CRAFTING);
    }
}
