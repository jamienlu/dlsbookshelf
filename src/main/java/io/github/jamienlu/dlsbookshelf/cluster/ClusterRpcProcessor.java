package io.github.jamienlu.dlsbookshelf.cluster;

import com.alibaba.fastjson2.JSON;
import com.alipay.remoting.exception.CodecException;
import com.alipay.remoting.serialization.SerializerManager;
import com.alipay.sofa.jraft.Status;
import com.alipay.sofa.jraft.entity.Task;
import com.alipay.sofa.jraft.rpc.RpcContext;
import com.alipay.sofa.jraft.rpc.RpcProcessor;
import io.github.jamienlu.dlsbookshelf.api.RegistryResult;
import io.github.jamienlu.dlsbookshelf.api.RegistryServiceOperate;
import io.github.jamienlu.dlsbookshelf.conf.ProtobufSerializer;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;

/**
 * @author jamieLu
 * @create 2024-04-28
 */
@Slf4j
public class ClusterRpcProcessor implements RpcProcessor<RegistryServiceOperate> {
    private final RegistryServer registryServer;

    public ClusterRpcProcessor(RegistryServer registryServer) {
        this.registryServer = registryServer;
    }

    @Override
    public void handleRequest(RpcContext rpcContext, RegistryServiceOperate o) {
        final RegistryClosure closure = new RegistryClosure() {
            @Override
            public void run(Status status) {
                rpcContext.sendResponse(getRegistryResult());
            }
        };
        closure.setRegistryServiceOperate(o);
        closure.setRegistryResult(RegistryResult.builder().build());
        try {
            Task task = new Task();
            task.setDone(closure);
            ByteBuffer data = ByteBuffer.wrap(SerializerManager.getSerializer(ProtobufSerializer.PROTOBUF).serialize(o));
            task.setData(data);
            registryServer.getNode().apply(task);
        } catch (CodecException e) {
            log.error("process serialization fail!", e);
        }
        log.info("process prepare send node task:" + JSON.toJSONString(o));

    }

    @Override
    public String interest() {
        return RegistryServiceOperate.class.getName();
    }
}
