package com.poldroc.rpc.framework.core.serialize.fastjson;

import com.alibaba.fastjson.JSON;
import com.poldroc.rpc.framework.core.serialize.SerializeFactory;

/**
 * FastJson序列化工厂
 * @author Poldroc
 * @date 2023/10/11
 */

public class FastJsonSerializeFactory implements SerializeFactory {

    @Override
    public <T> byte[] serialize(T t) {
        String jsonStr = JSON.toJSONString(t);
        return jsonStr.getBytes();
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        return JSON.parseObject(new String(data),clazz);
    }

}
