package io.github.jamienlu.dlsbookshelf.conf;

import com.alipay.remoting.serialization.Serializer;
import com.alipay.remoting.serialization.SerializerManager;
import io.github.jamienlu.dlsbookshelf.cluster.RegistryServer;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.alipay.remoting.config.Configs.SERIALIZER;

/**
 * @author jamieLu
 * @create 2024-04-27
 */
@Configuration
public class RegistryConf {
    @Autowired
    private Serializer serializer;

    @Bean(initMethod = "initServer")
    public RegistryServer registryServer(@Autowired RegistryConfigProperties properties) {
        return new RegistryServer(properties.getGroupId(), properties.getNodePath(), properties.getPort(), properties.getServerList());
    }
    @PostConstruct
    public void init() {
        SerializerManager.addSerializer(ProtobufSerializer.PROTOBUF, serializer);
        System.setProperty(SERIALIZER, String.valueOf(ProtobufSerializer.PROTOBUF));
    }
}
