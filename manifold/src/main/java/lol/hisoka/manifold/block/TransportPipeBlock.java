package lol.hisoka.manifold.block;

import lol.hisoka.manifold.api.endpoint.EndpointType;

public final class TransportPipeBlock extends AbstractPipeBlock {

    public TransportPipeBlock(Properties properties) {
        super(properties, EndpointType.TRANSPORT);
    }
}
