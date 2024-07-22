package io.github.jamienlu.dlsbookshelf.cluster;

import com.alibaba.fastjson2.JSON;
import com.alipay.sofa.jraft.JRaftUtils;
import com.alipay.sofa.jraft.RouteTable;
import com.alipay.sofa.jraft.conf.Configuration;
import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.jraft.error.RemotingException;
import com.alipay.sofa.jraft.option.CliOptions;
import com.alipay.sofa.jraft.rpc.InvokeCallback;
import com.alipay.sofa.jraft.rpc.RpcClient;
import com.alipay.sofa.jraft.rpc.impl.cli.CliClientServiceImpl;
import io.github.jamienlu.dlsbookshelf.api.RegistryResult;
import io.github.jamienlu.dlsbookshelf.api.RegistryServiceOperate;
import io.github.jamienlu.dlsbookshelf.model.InstanceMeta;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * @author jamieLu
 * @create 2024-04-29
 */
@Slf4j
public class RegistryRestClient {
    private String group;
    private RpcClient rpcClient;

    public RegistryRestClient(String urls, String group) {
        this.group = group;
        Configuration conf = JRaftUtils.getConfiguration(urls);
        RouteTable.getInstance().updateConfiguration(group, conf);
        CliClientServiceImpl cliClientService = new CliClientServiceImpl();
        cliClientService.init(new CliOptions());
        try {
            if (!RouteTable.getInstance().refreshLeader(cliClientService, group, 1000).isOk()) {
                throw new IllegalStateException("refresh leader failed");
            }
        } catch (InterruptedException | TimeoutException e) {
            throw new RuntimeException(e);
        }
        rpcClient = cliClientService.getRpcClient();

    }

    public void registry(String serviceName, InstanceMeta instanceMeta) throws RemotingException, InterruptedException {
        List<Class<?>> clazzs = new ArrayList<>(){};
        clazzs.add(String.class);
        clazzs.add(InstanceMeta.class);
        RegistryServiceOperate request = RegistryServiceOperate.builder().methodName("register").paramsType(clazzs).args(new Object[]{serviceName,instanceMeta}).build();
        final PeerId leader = RouteTable.getInstance().selectLeader(group);
        rpcClient.invokeSync(leader.getEndpoint(), request, 1000);
    }

    public void unregistry(String serviceName, InstanceMeta instanceMeta) throws RemotingException, InterruptedException {
        List<Class<?>> clazzs = new ArrayList<>(){};
        clazzs.add(String.class);
        clazzs.add(InstanceMeta.class);
        RegistryServiceOperate request = RegistryServiceOperate.builder().methodName("unregister").paramsType(clazzs).args(new Object[]{serviceName,instanceMeta}).build();
        final PeerId leader = RouteTable.getInstance().selectLeader(group);
        rpcClient.invokeSync(leader.getEndpoint(), request, 1000);
    }

    public List<InstanceMeta> fetchAll(String serviceName) throws RemotingException, InterruptedException {
        List<Class<?>> clazzs = new ArrayList<>(){};
        clazzs.add(String.class);
        RegistryServiceOperate request = RegistryServiceOperate.builder().methodName("getAllInstances").paramsType(clazzs).args(new Object[]{serviceName}).build();
        final PeerId leader = RouteTable.getInstance().selectLeader(group);
        RegistryResult registryResult = (RegistryResult) rpcClient.invokeSync(leader.getEndpoint(), request, 1000);
        if (registryResult.isStatus()) {
            log.info("fetch result:" + JSON.toJSONString(registryResult.getData()));
            return JSON.parseArray(JSON.toJSONString(registryResult.getData()),InstanceMeta.class);
        }
        return Collections.emptyList();
    }

    public static void main(String[] args) throws InterruptedException, TimeoutException, RemotingException {
        RegistryRestClient registryRestClient= new RegistryRestClient("192.168.0.101:8710,192.168.0.101:8711,192.168.0.101:8712","default");
        registryRestClient.registry("user", InstanceMeta.pathToInstance("92.168.0.101_8888"));
        registryRestClient.registry("user", InstanceMeta.pathToInstance("192.168.0.101_8881"));
        List<InstanceMeta> result1 = registryRestClient.fetchAll("test");
        registryRestClient.unregistry("user",InstanceMeta.pathToInstance("192.168.0.101_8888"));
        List<InstanceMeta> result2 = registryRestClient.fetchAll("test");
        log.info("fetch result1:" + JSON.toJSONString(result1));
        log.info("fetch result2:" + JSON.toJSONString(result2));

    }
}
