package io.github.jamienlu.dlsbookshelf.service;

import io.github.jamienlu.dlsbookshelf.cluster.Snapshot;
import io.github.jamienlu.dlsbookshelf.model.InstanceMeta;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author jamieLu
 * @create 2024-04-27
 */
@Slf4j
public class JMRegistryService implements RegistryService {
    private static final RegistryStorage registryStorage = new RegistryStorage();
    @Override
    public InstanceMeta register(String service, InstanceMeta instance) {
        List<InstanceMeta> metas = registryStorage.pullServerInstances(service);
        if(metas != null && !metas.isEmpty()) {
            if(metas.contains(instance)) {
                log.info(" ====> instance {} already registered", instance.toPath());
                instance.setStatus(true);
                return instance;
            }
        }
        log.info(" ====> register instance {}", instance.toPath());
        registryStorage.addInstance(service, instance);
        instance.setStatus(true);
        renew(instance, service);
        registryStorage.updateVersion(service);
        return instance;
    }

    @Override
    public InstanceMeta unregister(String service, InstanceMeta instance) {
        List<InstanceMeta> metas = registryStorage.pullServerInstances(service);
        if(metas == null || metas.isEmpty()) {
            return null;
        }
        log.info(" ====> unregister instance {}", instance.toPath());
        metas.removeIf( m -> m.equals(instance));
        instance.setStatus(false);
        renew(instance, service);
        registryStorage.updateVersion(service);
        return instance;
    }

    @Override
    public List<InstanceMeta> getAllInstances(String service) {
        return registryStorage.pullServerInstances(service);
    }
    @Override
    public long renew(InstanceMeta instance, String... services) {
        long time = System.currentTimeMillis();
        for (String service : services) {
            registryStorage.recordServerTimestamp(service+"@"+instance.toPath(), time);
        }
        return time;
    }
    @Override
    public Long version(String service) {
        return registryStorage.pullServerVersion(service);
    }
    @Override
    public Map<String, Long> versions(String... services) {
        return Arrays.stream(services)
                .collect(Collectors.toMap(x->x, registryStorage::pullServerVersion, (a, b)->b));
    };

    public static Snapshot snapshot() {
        synchronized (registryStorage) {
            return Snapshot.builder().serverInstances(registryStorage.getServerInstances()).serverVersions(registryStorage.getServerVersions()).serverTimestamp(registryStorage.getServerTimestamp()).version(registryStorage.getVersion().get())
                    .build();
        }
    }

    public static synchronized long restore(Snapshot snapshot) {
        synchronized (registryStorage) {
            return registryStorage.forkSnapshot(snapshot);
        }
    }
}
