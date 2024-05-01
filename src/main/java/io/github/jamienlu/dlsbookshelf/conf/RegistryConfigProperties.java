package io.github.jamienlu.dlsbookshelf.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
/**
 * @author jamieLu
 * @create 2024-04-27
 */
@Data
@ConfigurationProperties(prefix = "registry")
public class RegistryConfigProperties {
    private String groupId;
    private String nodePath;
    private Integer port;
    private List<String> serverList;

}
