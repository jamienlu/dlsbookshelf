package io.github.jamienlu.dlsbookshelf.cluster;

import com.alipay.remoting.serialization.SerializerManager;
import com.alipay.sofa.jraft.JRaftUtils;
import com.alipay.sofa.jraft.RouteTable;
import com.alipay.sofa.jraft.conf.Configuration;
import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.jraft.error.RemotingException;
import com.alipay.sofa.jraft.option.CliOptions;
import com.alipay.sofa.jraft.rpc.InvokeCallback;
import com.alipay.sofa.jraft.rpc.impl.cli.CliClientServiceImpl;
import io.github.jamienlu.dlsbookshelf.api.RegistryServiceOperate;
import io.github.jamienlu.dlsbookshelf.conf.ProtobufSerializer;
import io.github.jamienlu.dlsbookshelf.model.InstanceMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeoutException;

import static com.alipay.remoting.config.Configs.SERIALIZER;

/**
 * @author jamieLu
 * @create 2024-04-29
 */
public class Client {
    public static void main(String[] args) throws InterruptedException, TimeoutException, RemotingException {
        Configuration conf = JRaftUtils.getConfiguration("192.168.0.100:8710,192.168.0.100:8711,192.168.0.100:8712");
        RouteTable.getInstance().updateConfiguration("default", conf);
        final CliClientServiceImpl cliClientService = new CliClientServiceImpl();
        cliClientService.init(new CliOptions());

        if (!RouteTable.getInstance().refreshLeader(cliClientService, "default", 1000).isOk()) {
            throw new IllegalStateException("Refresh leader failed");
        }
        final PeerId leader = RouteTable.getInstance().selectLeader("default");
        String serviceName = "test";
        List<Class<?>> clazzs = new ArrayList<>(){};
        clazzs.add(String.class);
        clazzs.add(InstanceMeta.class);
        RegistryServiceOperate request = RegistryServiceOperate.builder().methodName("register").paramsType(clazzs).args(new Object[]{serviceName,new InstanceMeta("http","192.168.0.100", 8888, "cd")}).build();
        cliClientService.getRpcClient().invokeAsync(leader.getEndpoint(), request, new InvokeCallback() {

            @Override
            public void complete(Object result, Throwable err) {
                if (err == null) {
                    System.out.println("register result:" + result);
                } else {
                    err.printStackTrace();
                }
            }

            @Override
            public Executor executor() {
                return null;
            }
        }, 5000);
        Thread.sleep(5000);
    }
}
