package lol.hisoka.manifold.block;

import lol.hisoka.manifold.api.endpoint.EndpointType;

public final class ChassisPipeBlock extends AbstractPipeBlock {

    public ChassisPipeBlock(Properties properties) {
        super(properties, EndpointType.CHASSIS);
    }
}
