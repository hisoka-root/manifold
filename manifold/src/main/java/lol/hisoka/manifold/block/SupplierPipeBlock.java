package lol.hisoka.manifold.block;

import lol.hisoka.manifold.api.endpoint.EndpointType;

public final class SupplierPipeBlock extends AbstractPipeBlock {

    public SupplierPipeBlock(Properties properties) {
        super(properties, EndpointType.SUPPLIER);
    }
}
