package lol.hisoka.manifold.api.endpoint;

public enum EndpointType {
    TRANSPORT(false, false, false),
    PROVIDER(true, false, false),
    REQUESTER(false, true, false),
    SUPPLIER(false, false, true),
    CRAFTING(false, false, false),
    CHASSIS(false, false, false);

    private final boolean provider;
    private final boolean requester;
    private final boolean supplier;

    EndpointType(boolean provider, boolean requester, boolean supplier) {
        this.provider = provider;
        this.requester = requester;
        this.supplier = supplier;
    }

    public boolean isProvider() {
        return provider;
    }

    public boolean isRequester() {
        return requester;
    }

    public boolean isSupplier() {
        return supplier;
    }
}
