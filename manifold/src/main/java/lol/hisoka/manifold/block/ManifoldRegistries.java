package lol.hisoka.manifold.block;

import lol.hisoka.manifold.Manifold;
import lol.hisoka.manifold.blockentity.PipeBlockEntity;
import lol.hisoka.manifold.blockentity.PipeBlockEntityData;
import lol.hisoka.manifold.item.WrenchItem;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public final class ManifoldRegistries {

    private static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(Manifold.MOD_ID);

    private static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(Manifold.MOD_ID);

    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Manifold.MOD_ID);

    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Manifold.MOD_ID);

    public static final Supplier<AttachmentType<PipeBlockEntityData>> PIPE_DATA =
            ATTACHMENT_TYPES.register("pipe_data", PipeBlockEntityData::createAttachment);

    public static final Supplier<Block> TRANSPORT_PIPE = BLOCKS.registerBlock(
            "transport_pipe",
            TransportPipeBlock::new,
            BlockBehaviour.Properties.of()
                    .strength(1.0f, 4.0f)
                    .sound(SoundType.GLASS)
                    .noOcclusion()
    );

    public static final Supplier<Block> PROVIDER_PIPE = BLOCKS.registerBlock(
            "provider_pipe",
            ProviderPipeBlock::new,
            BlockBehaviour.Properties.of()
                    .strength(1.0f, 4.0f)
                    .sound(SoundType.GLASS)
                    .noOcclusion()
    );

    public static final Supplier<Block> REQUESTER_PIPE = BLOCKS.registerBlock(
            "requester_pipe",
            RequesterPipeBlock::new,
            BlockBehaviour.Properties.of()
                    .strength(1.0f, 4.0f)
                    .sound(SoundType.GLASS)
                    .noOcclusion()
    );

    public static final Supplier<Block> SUPPLIER_PIPE = BLOCKS.registerBlock(
            "supplier_pipe",
            SupplierPipeBlock::new,
            BlockBehaviour.Properties.of()
                    .strength(1.0f, 4.0f)
                    .sound(SoundType.GLASS)
                    .noOcclusion()
    );

    public static final Supplier<Item> TRANSPORT_PIPE_ITEM =
            ITEMS.register("transport_pipe", () -> new BlockItem(TRANSPORT_PIPE.get(), new Item.Properties()));
    public static final Supplier<Item> PROVIDER_PIPE_ITEM =
            ITEMS.register("provider_pipe", () -> new BlockItem(PROVIDER_PIPE.get(), new Item.Properties()));
    public static final Supplier<Item> REQUESTER_PIPE_ITEM =
            ITEMS.register("requester_pipe", () -> new BlockItem(REQUESTER_PIPE.get(), new Item.Properties()));
    public static final Supplier<Item> SUPPLIER_PIPE_ITEM =
            ITEMS.register("supplier_pipe", () -> new BlockItem(SUPPLIER_PIPE.get(), new Item.Properties()));

    public static final Supplier<Item> WRENCH =
            ITEMS.register("wrench", () -> new WrenchItem(new Item.Properties()
                    .stacksTo(1)));

    public static final Supplier<BlockEntityType<PipeBlockEntity>> PIPE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("pipe", () -> {
                var type = BlockEntityType.Builder.of(
                        PipeBlockEntity::new,
                        TRANSPORT_PIPE.get(),
                        PROVIDER_PIPE.get(),
                        REQUESTER_PIPE.get(),
                        SUPPLIER_PIPE.get()
                ).build(null);
                PipeBlockEntity.setType(type);
                return type;
            });

    private ManifoldRegistries() {}

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        ATTACHMENT_TYPES.register(modBus);
    }
}
