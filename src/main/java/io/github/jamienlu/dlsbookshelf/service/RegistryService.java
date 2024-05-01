package io.github.jamienlu.dlsbookshelf.service;

import io.github.jamienlu.dlsbookshelf.model.InstanceMeta;

import java.util.List;
import java.util.Map;

/**
 * @author jamieLu
 * @create 2024-04-27
 */
public interface RegistryService {
    // 最基础的3个方法
    InstanceMeta register(String service, InstanceMeta instance);
    InstanceMeta unregister(String service, InstanceMeta instance);
    List<InstanceMeta> getAllInstances(String service);
    long renew(InstanceMeta instance,String... service);
    Long version(String service);
    Map<String, Long> versions(String... services);
}
