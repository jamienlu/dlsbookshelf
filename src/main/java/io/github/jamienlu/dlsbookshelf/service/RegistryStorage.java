package io.github.jamienlu.dlsbookshelf.service;

import io.github.jamienlu.dlsbookshelf.cluster.Snapshot;
import io.github.jamienlu.dlsbookshelf.model.InstanceMeta;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author jamieLu
 * @create 2024-04-27
 */
@Data
public class RegistryStorage {
    private Map<String, List<InstanceMeta>> serverInstances = new ConcurrentHashMap<>();
    private Map<String, Long> serverVersions = new ConcurrentHashMap<>();;
    private Map<String, Long> serverTimestamp = new ConcurrentHashMap<>();;
    private AtomicLong version = new AtomicLong(0);

    public long forkSnapshot(Snapshot snapshot) {
        serverInstances.clear();
        serverInstances.putAll(snapshot.getServerInstances());
        serverVersions.clear();
        serverVersions.putAll(snapshot.getServerVersions());
        serverTimestamp.clear();
        serverTimestamp.putAll(snapshot.getServerTimestamp());
        version.set(snapshot.getVersion());
        return version.get();
    }

    public void addInstance(String serverName, InstanceMeta instanceMeta) {
        serverInstances.putIfAbsent(serverName, new CopyOnWriteArrayList<>());
        serverInstances.get(serverName).add(instanceMeta);
    }

    public void updateVersion(String serverName) {
        serverVersions.put(serverName, version.incrementAndGet());
    }

    public void recordServerTimestamp(String key, long time) {
        serverTimestamp.put(key, time);
    }
    public List<InstanceMeta> pullServerInstances(String serverName) {
        return serverInstances.get(serverName);
    }

    public Long pullServerVersion(String serverName) {
        return serverVersions.get(serverName);
    }
}
