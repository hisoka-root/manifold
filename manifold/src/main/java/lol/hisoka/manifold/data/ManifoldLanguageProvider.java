package lol.hisoka.manifold.data;

import lol.hisoka.manifold.Manifold;
import lol.hisoka.manifold.block.ManifoldRegistries;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public final class ManifoldLanguageProvider extends LanguageProvider {

    public ManifoldLanguageProvider(PackOutput output, String locale) {
        super(output, Manifold.MOD_ID, locale);
    }

    @Override
    protected void addTranslations() {
        add(ManifoldRegistries.TRANSPORT_PIPE.get(), "Transport Pipe");
        add(ManifoldRegistries.PROVIDER_PIPE.get(), "Provider Pipe");
        add(ManifoldRegistries.REQUESTER_PIPE.get(), "Requester Pipe");
        add(ManifoldRegistries.SUPPLIER_PIPE.get(), "Supplier Pipe");
        add(ManifoldRegistries.WRENCH.get(), "Manifold Wrench");
    }
}
