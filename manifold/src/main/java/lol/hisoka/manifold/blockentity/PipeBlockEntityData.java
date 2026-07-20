package lol.hisoka.manifold.blockentity;

import lol.hisoka.manifold.api.endpoint.EndpointType;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.*;
import java.util.function.Supplier;

public final class PipeBlockEntityData implements INBTSerializable<CompoundTag> {

    public static AttachmentType<PipeBlockEntityData> createAttachment() {
        Supplier<PipeBlockEntityData> factory = PipeBlockEntityData::new;
        return AttachmentType.serializable(factory).build();
    }

    private EndpointType pipeType;
    private List<ItemStack> filter;
    private Map<String, Integer> bufferTargets;
    private List<PipeModuleInstance> modules;

    public PipeBlockEntityData() {
        this(EndpointType.TRANSPORT);
    }

    public PipeBlockEntityData(EndpointType pipeType) {
        this.pipeType = pipeType;
        this.filter = new ArrayList<>();
        this.bufferTargets = new LinkedHashMap<>();
        this.modules = new ArrayList<>();
    }

    public EndpointType pipeType() {
        return pipeType;
    }

    public void setPipeType(EndpointType pipeType) {
        this.pipeType = pipeType;
    }

    public List<ItemStack> filter() {
        return Collections.unmodifiableList(filter);
    }

    public void setFilter(List<ItemStack> filter) {
        this.filter = new ArrayList<>(filter);
    }

    public Map<String, Integer> bufferTargets() {
        return Collections.unmodifiableMap(bufferTargets);
    }

    public void setBufferTarget(Map<String, Integer> targets) {
        this.bufferTargets = new LinkedHashMap<>(targets);
    }

    public List<PipeModuleInstance> modules() {
        return Collections.unmodifiableList(modules);
    }

    public void addModule(PipeModuleInstance module) {
        modules.add(module);
    }

    public boolean removeModule(PipeModuleInstance module) {
        return modules.remove(module);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putString("pipeType", pipeType.name());

        ListTag filterTag = new ListTag();
        for (ItemStack stack : filter) {
            filterTag.add(stack.save(provider));
        }
        tag.put("filter", filterTag);

        CompoundTag bufferTag = new CompoundTag();
        for (var entry : bufferTargets.entrySet()) {
            bufferTag.putInt(entry.getKey(), entry.getValue());
        }
        tag.put("bufferTargets", bufferTag);

        ListTag modulesTag = new ListTag();
        for (PipeModuleInstance module : modules) {
            CompoundTag moduleTag = new CompoundTag();
            moduleTag.putString("typeId", module.typeId());
            modulesTag.add(moduleTag);
        }
        tag.put("modules", modulesTag);

        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        if (tag.contains("pipeType")) {
            pipeType = EndpointType.valueOf(tag.getString("pipeType"));
        }

        filter.clear();
        if (tag.contains("filter")) {
            ListTag filterTag = tag.getList("filter", Tag.TAG_COMPOUND);
            for (int i = 0; i < filterTag.size(); i++) {
                filter.add(ItemStack.parseOptional(provider, filterTag.getCompound(i)));
            }
        }

        bufferTargets.clear();
        if (tag.contains("bufferTargets")) {
            CompoundTag bufferTag = tag.getCompound("bufferTargets");
            for (String key : bufferTag.getAllKeys()) {
                bufferTargets.put(key, bufferTag.getInt(key));
            }
        }

        modules.clear();
        if (tag.contains("modules")) {
            ListTag modulesTag = tag.getList("modules", Tag.TAG_COMPOUND);
            for (int i = 0; i < modulesTag.size(); i++) {
                CompoundTag moduleTag = modulesTag.getCompound(i);
                modules.add(new PipeModuleInstance(
                        moduleTag.getString("typeId"),
                        Map.of()
                ));
            }
        }
    }

    public record PipeModuleInstance(String typeId, Map<String, Object> config) {}
}
