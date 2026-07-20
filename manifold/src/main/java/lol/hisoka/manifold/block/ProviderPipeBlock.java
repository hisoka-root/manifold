package lol.hisoka.manifold.block;

import lol.hisoka.manifold.api.endpoint.EndpointType;

public final class ProviderPipeBlock extends AbstractPipeBlock {

    public ProviderPipeBlock(Properties properties) {
        super(properties, EndpointType.PROVIDER);
    }
}
