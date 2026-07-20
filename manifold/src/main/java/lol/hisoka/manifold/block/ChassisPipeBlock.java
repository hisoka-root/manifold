package lol.hisoka.manifold.block;

import lol.hisoka.manifold.api.endpoint.EndpointType;

/**
 * v0.2+ — generic carrier for {@code PipeModule}s (Extraction, Filter, Void, etc.).
 * Not registered in {@link ManifoldRegistries} for v0.1.
 */
public final class ChassisPipeBlock extends AbstractPipeBlock {

    public ChassisPipeBlock(Properties properties) {
        super(properties, EndpointType.CHASSIS);
    }
}
