package lol.hisoka.manifold.block;

import lol.hisoka.manifold.api.endpoint.EndpointType;

public final class RequesterPipeBlock extends AbstractPipeBlock {

    public RequesterPipeBlock(Properties properties) {
        super(properties, EndpointType.REQUESTER);
    }
}
