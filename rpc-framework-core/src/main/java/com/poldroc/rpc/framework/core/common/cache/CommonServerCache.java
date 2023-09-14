package com.poldroc.rpc.framework.core.common.cache;

import java.util.HashMap;
import java.util.Map;


/**
 * 服务缓存 存储服务端信息
 * @author Poldroc
 * @date 2023/9/15
 */

public class CommonServerCache {

    /**
     * 服务端缓存，保存服务实现的接口名和服务实现类的映射关系
     */
    public static final Map<String,Object> PROVIDER_CLASS_MAP = new HashMap<>();
}
