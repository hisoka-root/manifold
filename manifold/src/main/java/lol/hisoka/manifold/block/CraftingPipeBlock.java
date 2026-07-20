package lol.hisoka.manifold.block;

import lol.hisoka.manifold.api.endpoint.EndpointType;

public final class CraftingPipeBlock extends AbstractPipeBlock {

    public CraftingPipeBlock(Properties properties) {
        super(properties, EndpointType.CRAFTING);
    }
}
