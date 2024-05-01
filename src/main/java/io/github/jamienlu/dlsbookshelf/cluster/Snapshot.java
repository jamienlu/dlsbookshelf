package io.github.jamienlu.dlsbookshelf.cluster;

import io.github.jamienlu.dlsbookshelf.model.InstanceMeta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Snapshot {
    private Map<String, List<InstanceMeta>> serverInstances;
    private Map<String, Long> serverVersions;
    private Map<String, Long> serverTimestamp;
    private long version;
}
