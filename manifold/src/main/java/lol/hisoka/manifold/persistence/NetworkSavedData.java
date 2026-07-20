package lol.hisoka.manifold.persistence;

import lol.hisoka.manifold.api.network.NetworkEdge;
import lol.hisoka.manifold.api.network.NetworkId;
import lol.hisoka.manifold.api.network.NetworkNode;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

public final class NetworkSavedData extends SavedData {

    private static final String DATA_NAME = "manifold_networks";

    private final Map<NetworkId, NetworkData> networks = new HashMap<>();

    public static NetworkSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        NetworkSavedData data = new NetworkSavedData();
        ListTag networksTag = tag.getList("networks", Tag.TAG_COMPOUND);
        for (int i = 0; i < networksTag.size(); i++) {
            CompoundTag netTag = networksTag.getCompound(i);
            UUID uuid = netTag.getUUID("id");
            NetworkId networkId = new NetworkId(uuid);

            Set<String> nodeIds = new HashSet<>();
            ListTag nodesTag = netTag.getList("nodes", Tag.TAG_STRING);
            for (int j = 0; j < nodesTag.size(); j++) {
                nodeIds.add(nodesTag.getString(j));
            }

            List<NetworkEdgeData> edgeDataList = new ArrayList<>();
            ListTag edgesTag = netTag.getList("edges", Tag.TAG_COMPOUND);
            for (int j = 0; j < edgesTag.size(); j++) {
                CompoundTag edgeTag = edgesTag.getCompound(j);
                edgeDataList.add(new NetworkEdgeData(
                        edgeTag.getString("source"),
                        edgeTag.getString("target"),
                        edgeTag.getInt("weight")
                ));
            }

            data.setNetworkData(networkId, new NetworkData(nodeIds, edgeDataList));
        }
        return data;
    }

    public static NetworkSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new Factory<>(NetworkSavedData::new, NetworkSavedData::load),
                DATA_NAME
        );
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag networksTag = new ListTag();
        for (var entry : networks.entrySet()) {
            CompoundTag netTag = new CompoundTag();
            netTag.putUUID("id", entry.getKey().id());

            ListTag nodesTag = new ListTag();
            for (String nodeId : entry.getValue().nodeIds) {
                nodesTag.add(StringTag.valueOf(nodeId));
            }
            netTag.put("nodes", nodesTag);

            ListTag edgesTag = new ListTag();
            for (NetworkEdgeData edgeData : entry.getValue().edges) {
                CompoundTag edgeTag = new CompoundTag();
                edgeTag.putString("source", edgeData.source);
                edgeTag.putString("target", edgeData.target);
                edgeTag.putInt("weight", edgeData.weight);
                edgesTag.add(edgeTag);
            }
            netTag.put("edges", edgesTag);

            networksTag.add(netTag);
        }
        tag.put("networks", networksTag);
        return tag;
    }

    public void setNetworkData(NetworkId id, NetworkData data) {
        networks.put(id, data);
        setDirty();
    }

    public Optional<NetworkData> getNetworkData(NetworkId id) {
        return Optional.ofNullable(networks.get(id));
    }

    public void removeNetwork(NetworkId id) {
        networks.remove(id);
        setDirty();
    }

    public Set<NetworkId> allNetworkIds() {
        return new HashSet<>(networks.keySet());
    }

    public record NetworkData(Set<String> nodeIds, List<NetworkEdgeData> edges) {}

    public record NetworkEdgeData(String source, String target, int weight) {}
}
