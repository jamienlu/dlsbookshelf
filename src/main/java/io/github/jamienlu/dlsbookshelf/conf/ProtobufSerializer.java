package io.github.jamienlu.dlsbookshelf.conf;

import com.alipay.remoting.exception.CodecException;
import com.alipay.remoting.serialization.Serializer;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author jamieLu
 * @create 2024-04-03
 */
@Service
public class ProtobufSerializer implements Serializer {
    public static final Integer PROTOBUF = 0;
    private Map<Class<?>, RuntimeSchema<?>> schemaCache = new ConcurrentHashMap<>();
    private static final Set<Class<?>> WRAPPER_SET = new HashSet<>();
    private static final Class<SerializationWrapper> WRAPPER_CLASS = SerializationWrapper.class;
    private static final Schema<SerializationWrapper> WRAPPER_SCHEMA = RuntimeSchema.createFrom(WRAPPER_CLASS);
    private static final LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
    static {
        WRAPPER_SET.add(List.class);
        WRAPPER_SET.add(ArrayList.class);
        WRAPPER_SET.add(CopyOnWriteArrayList.class);
        WRAPPER_SET.add(LinkedList.class);

        WRAPPER_SET.add(Map.class);
        WRAPPER_SET.add(HashMap.class);
        WRAPPER_SET.add(TreeMap.class);
        WRAPPER_SET.add(SortedMap.class);

        WRAPPER_SET.add(Set.class);
        WRAPPER_SET.add(HashSet.class);
        WRAPPER_SET.add(TreeSet.class);
        WRAPPER_SET.add(Object.class);
    }
    @Override
    public byte[] serialize(Object obj) throws CodecException {
        Object target = obj;
        Class clazz = obj.getClass();
        Schema schema = WRAPPER_SCHEMA;
        if (!WRAPPER_SET.contains(clazz)) {
            schema = getSchema(clazz);
        } else {
            target = SerializationWrapper.wrapper(obj);
        }
        byte[] data;
        try {
            data = ProtostuffIOUtil.toByteArray(target, schema, buffer);
        } finally {
            buffer.clear();
        }
        return data;
    }

    @Override
    public <T> T deserialize(byte[] data, String className) throws CodecException {
        try {
            Class clazz = Class.forName(className);
            if (!WRAPPER_SET.contains(clazz)) {
                T message = (T) clazz.newInstance();
                Schema<T> schema = getSchema(clazz);
                ProtostuffIOUtil.mergeFrom(data, message, schema);
                return message;
            } else {
                SerializationWrapper<T> wrapper = new SerializationWrapper();
                ProtostuffIOUtil.mergeFrom(data, wrapper, WRAPPER_SCHEMA);
                return wrapper.getData();
            }
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    private <T> Schema<T> getSchema(Class<T> clazz) {
        RuntimeSchema<T> schema = (RuntimeSchema<T>) schemaCache.get(clazz);
        if (schema == null) {
            schema = RuntimeSchema.createFrom(clazz);
            schemaCache.put(clazz, schema);
        }
        return schema;
    }
}
