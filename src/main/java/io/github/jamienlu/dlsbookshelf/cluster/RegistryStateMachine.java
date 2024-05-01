package io.github.jamienlu.dlsbookshelf.cluster;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alipay.remoting.exception.CodecException;
import com.alipay.remoting.serialization.SerializerManager;
import com.alipay.sofa.jraft.Closure;
import com.alipay.sofa.jraft.Iterator;
import com.alipay.sofa.jraft.Status;
import com.alipay.sofa.jraft.core.StateMachineAdapter;
import com.alipay.sofa.jraft.error.RaftError;
import com.alipay.sofa.jraft.error.RaftException;
import com.alipay.sofa.jraft.storage.snapshot.SnapshotReader;
import com.alipay.sofa.jraft.storage.snapshot.SnapshotWriter;
import com.alipay.sofa.jraft.util.ThreadPoolUtil;
import io.github.jamienlu.dlsbookshelf.api.RegistryServiceOperate;
import io.github.jamienlu.dlsbookshelf.conf.ProtobufSerializer;
import io.github.jamienlu.dlsbookshelf.service.JMRegistryService;
import io.github.jamienlu.dlsbookshelf.service.RegistryService;
import io.github.jamienlu.dlsbookshelf.util.ThreadUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author jamieLu
 * @create 2024-04-27
 */
@Slf4j
public class RegistryStateMachine extends StateMachineAdapter {
    private final AtomicLong leaderTerm = new AtomicLong(-1);
    private final RegistryService registryService;

    public RegistryStateMachine(RegistryService registryService) {
        this.registryService = registryService;
    }

    @Override
    public void onApply(Iterator iterator) {
        RegistryServiceOperate registryServiceOperate = null;
        RegistryClosure closure = null;
        while (iterator.hasNext()) {
            // leader节点获取
            if (iterator.done() != null) {
                closure = (RegistryClosure) iterator.done();
                registryServiceOperate = closure.getRegistryServiceOperate();
            } else {
                ByteBuffer buffer = iterator.getData();
                try {
                    registryServiceOperate = SerializerManager.getSerializer(ProtobufSerializer.PROTOBUF).deserialize(buffer.array(), RegistryServiceOperate.class.getCanonicalName());
                } catch (CodecException e) {
                    log.error("state mache deserialize registryServiceOperate error", e);
                }
                // 读请求 follow节点不需要操作
                if (registryServiceOperate != null && !registryServiceOperate.getMethodName().equals("register") && !registryServiceOperate.getMethodName().equals("unregister")) {
                    iterator.next();
                    continue;
                }
            }
            log.info("state mache prepare invoke registryService method");
            // 完成业务操作
            if (registryServiceOperate != null) {
                log.info("state mache receive registryServiceOperate:" + JSON.toJSONString(registryServiceOperate));
                Method method = null;
                try {
                    method = registryService.getClass().getMethod(registryServiceOperate.getMethodName(), registryServiceOperate.getParamsType().toArray(Class<?>[]::new));
                    Object[] args = new Object[method.getParameterTypes().length];
                    if (registryServiceOperate.getArgs() != null) {
                        for (int i = 0; i < registryServiceOperate.getArgs().length; i++) {
                            args[i] = JSON.to(method.getParameterTypes()[i], registryServiceOperate.getArgs()[i]);
                        }
                    }
                    Object result = method.invoke(registryService, args);
                    log.info("state mache invoke result:" + JSON.toJSONString(result));
                    if (closure != null) {
                        closure.success(result);
                        closure.run(Status.OK());
                        log.info("success closure:"+ JSONObject.toJSONString(closure));
                    }
                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                    log.error("registryService invoke error method:{} params:{}",registryServiceOperate.getMethodName(), JSON.toJSONString(registryServiceOperate.getArgs()));
                }

            }
            iterator.next();
        }
    }
    @Override
    public void onLeaderStart(long term) {
        leaderTerm.set(term);
        super.onLeaderStart(term);
    }

    @Override
    public void onLeaderStop(Status status) {
        leaderTerm.set(-1);
        super.onLeaderStop(status);
    }

    @Override
    public void onError(RaftException e) {
        log.error("raft state error: {}",e, e);
    }

    @Override
    public void onSnapshotSave(SnapshotWriter writer, Closure done) {
        Snapshot snapshot = JMRegistryService.snapshot();
        log.info("prepare snapshot data:" + JSON.toJSONString(snapshot));
        ThreadUtil.submit(() -> {
            FileOutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(writer.getPath() + "/data");
                ObjectOutputStream objInput = new ObjectOutputStream(outputStream);
                objInput.writeObject(snapshot);
                if (writer.addFile("data")) {
                    done.run(Status.OK());
                } else {
                    done.run(new Status(RaftError.EIO, "Fail to add file to writer"));
                }
            } catch (IOException e) {
                done.run(new Status(RaftError.EIO, "Fail to save snapshot %s", writer.getPath()));
            }
        });



    }

    @Override
    public boolean onSnapshotLoad(SnapshotReader reader) {
        if (isLeader()) {
            log.info("Leader is not supposed to load snapshot");
            return false;
        }
        if (reader.getFileMeta("data") == null) {
            log.error("Fail to find data file in {}", reader.getPath());
            return false;
        }
        ThreadUtil.submit(() -> {
            FileInputStream inputStream = null;
            try {
                inputStream = new FileInputStream(reader.getPath() + "/data");
                ObjectInputStream objInput = new ObjectInputStream(inputStream);
                Snapshot snapshot = (Snapshot) objInput.readObject();
                JMRegistryService.restore(snapshot);
            } catch (IOException | ClassNotFoundException e) {
                log.error("Fail to load snapshot from {}", reader.getPath());
            }
        });
        return true;
    }

    public boolean isLeader() {
        return this.leaderTerm.get() > 0;
    }

}
