package lol.hisoka.manifold.data;

import lol.hisoka.manifold.Manifold;
import lol.hisoka.manifold.block.AbstractPipeBlock;
import lol.hisoka.manifold.block.ManifoldRegistries;

import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.Map;
import java.util.function.Supplier;

public final class ManifoldBlockStateProvider extends BlockStateProvider {

    private static final Map<Supplier<? extends Block>, String> PIPES = Map.of(
            ManifoldRegistries.TRANSPORT_PIPE, "transport",
            ManifoldRegistries.PROVIDER_PIPE,  "provider",
            ManifoldRegistries.REQUESTER_PIPE, "requester",
            ManifoldRegistries.SUPPLIER_PIPE,  "supplier"
    );

    public ManifoldBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, Manifold.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        for (var entry : PIPES.entrySet()) {
            pipeBlock(entry.getKey(), entry.getValue());
        }
    }

    private void pipeBlock(Supplier<? extends Block> block, String type) {
        ModelFile core = models().getExistingFile(modLoc("block/pipe_core_" + type));
        ModelFile cap = models().getExistingFile(modLoc("block/pipe_cap_" + type));

        var builder = getMultipartBuilder(block.get());
        builder.part().modelFile(core).addModel().end();

        addCap(builder, cap, AbstractPipeBlock.NORTH, Direction.NORTH);
        addCap(builder, cap, AbstractPipeBlock.SOUTH, Direction.SOUTH);
        addCap(builder, cap, AbstractPipeBlock.EAST, Direction.EAST);
        addCap(builder, cap, AbstractPipeBlock.WEST, Direction.WEST);
        addCap(builder, cap, AbstractPipeBlock.UP, Direction.UP);
        addCap(builder, cap, AbstractPipeBlock.DOWN, Direction.DOWN);
    }

    private void addCap(net.neoforged.neoforge.client.model.generators.MultiPartBlockStateBuilder builder,
                         ModelFile cap, net.minecraft.world.level.block.state.properties.BooleanProperty prop,
                         Direction dir) {
        int xRot = 0, yRot = 0;
        switch (dir) {
            case DOWN -> xRot = 90;
            case UP -> xRot = 270;
            case SOUTH -> yRot = 180;
            case EAST -> yRot = 90;
            case WEST -> yRot = 270;
        }
        builder.part().modelFile(cap).rotationX(xRot).rotationY(yRot).addModel()
                .condition(prop, true).end();
    }
}
