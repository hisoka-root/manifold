package lol.hisoka.manifold.data;

import lol.hisoka.manifold.Manifold;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.List;

public final class ManifoldBlockModelProvider extends BlockModelProvider {

    private static final List<String> TYPES = List.of("transport", "provider", "requester", "supplier");

    public ManifoldBlockModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Manifold.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        for (String type : TYPES) {
            pipeCore(type);
            pipeCap(type);
        }
    }

    private void pipeCore(String type) {
        String tex = "block/pipe_" + type;
        getBuilder("block/pipe_core_" + type)
                .parent(new ModelFile.UncheckedModelFile(mcLoc("block/block")))
                .texture("pipe", modLoc(tex))
                .texture("particle", modLoc(tex))
                .element()
                    .from(4, 4, 4).to(12, 12, 12)
                    .face(net.minecraft.core.Direction.NORTH).uvs(4, 4, 12, 12).texture("#pipe").end()
                    .face(net.minecraft.core.Direction.SOUTH).uvs(4, 4, 12, 12).texture("#pipe").end()
                    .face(net.minecraft.core.Direction.EAST).uvs(4, 4, 12, 12).texture("#pipe").end()
                    .face(net.minecraft.core.Direction.WEST).uvs(4, 4, 12, 12).texture("#pipe").end()
                    .face(net.minecraft.core.Direction.UP).uvs(4, 4, 12, 12).texture("#pipe").end()
                    .face(net.minecraft.core.Direction.DOWN).uvs(4, 4, 12, 12).texture("#pipe").end()
                    .end();
    }

    private void pipeCap(String type) {
        String tex = "block/pipe_" + type;
        getBuilder("block/pipe_cap_" + type)
                .parent(new ModelFile.UncheckedModelFile(mcLoc("block/block")))
                .texture("pipe", modLoc(tex))
                .texture("particle", modLoc(tex))
                .element()
                    .from(4, 4, 0).to(12, 12, 4)
                    .face(net.minecraft.core.Direction.NORTH).uvs(4, 4, 12, 12).texture("#pipe").end()
                    .face(net.minecraft.core.Direction.SOUTH).uvs(4, 4, 12, 12).texture("#pipe").end()
                    .face(net.minecraft.core.Direction.EAST).uvs(0, 4, 4, 12).texture("#pipe").end()
                    .face(net.minecraft.core.Direction.WEST).uvs(12, 4, 16, 12).texture("#pipe").end()
                    .face(net.minecraft.core.Direction.UP).uvs(4, 0, 12, 4).texture("#pipe").end()
                    .face(net.minecraft.core.Direction.DOWN).uvs(4, 12, 12, 16).texture("#pipe").end()
                    .end();
    }
}
