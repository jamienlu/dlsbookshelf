package io.github.jamienlu.dlsbookshelf.cluster;

import com.alipay.sofa.jraft.JRaftUtils;
import com.alipay.sofa.jraft.Node;
import com.alipay.sofa.jraft.RaftGroupService;
import com.alipay.sofa.jraft.RaftServiceFactory;
import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.jraft.option.NodeOptions;
import com.alipay.sofa.jraft.option.RaftOptions;
import com.alipay.sofa.jraft.rpc.RaftRpcServerFactory;
import com.alipay.sofa.jraft.rpc.RpcContext;
import com.alipay.sofa.jraft.rpc.RpcProcessor;
import com.alipay.sofa.jraft.rpc.RpcServer;
import io.github.jamienlu.dlsbookshelf.service.JMRegistryService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author jamieLu
 * @create 2024-04-27
 */
@Slf4j
public class RegistryServer {
    private final String groupId;
    private final String nodePath;
    private final List<String> serverList;
    private final Integer port;
    private final RegistryStateMachine fsm;
    @Getter
    private Node node;
    public RegistryServer(String groupId, String nodePath, Integer port, List<String> serverList) {
        this.groupId = groupId;
        this.nodePath = nodePath;
        this.port = port;
        this.serverList = serverList;
        // 初始化状态机
        this.fsm = new RegistryStateMachine(new JMRegistryService());
    }

    public void initServer() throws IOException {
        FileUtils.forceMkdir(new File(nodePath));
        String host;
        try {
            host = new InetUtils(new InetUtilsProperties())
                    .findFirstNonLoopbackHostInfo().getIpAddress();
            log.info(" ===> findFirstNonLoopbackHostInfo = " + host);
        } catch (Exception e) {
            host = "127.0.0.1";
        }
        String address = host + ":" + port;
        RpcServer rpcServer = RaftRpcServerFactory.createRaftRpcServer(JRaftUtils.getEndPoint(address));
        // 注册业务处理器
        rpcServer.registerProcessor(new ClusterRpcProcessor(this));
        NodeOptions nodeOptions = new NodeOptions();
        // 设置集群节点
        nodeOptions.setInitialConf(JRaftUtils.getConfiguration(serverList.stream().reduce("", (v1,v2) -> v1+","+v2)));
        nodeOptions.setRaftOptions(new RaftOptions());
        nodeOptions.setSnapshotIntervalSecs(360);
        // 设置状态机到启动参数
        nodeOptions.setFsm(fsm);
        // 设置存储路径
        nodeOptions.setLogUri(nodePath + "/log");
        nodeOptions.setRaftMetaUri(nodePath + "/meta");
        nodeOptions.setSnapshotUri(nodePath + "/snapshot");
        // 初始化raft服务框架
        RaftGroupService raftGroupService = new RaftGroupService(groupId,  JRaftUtils.getPeerId(address), nodeOptions, rpcServer);
        // 启动服务节点
        this.node = raftGroupService.start();
    }
}
