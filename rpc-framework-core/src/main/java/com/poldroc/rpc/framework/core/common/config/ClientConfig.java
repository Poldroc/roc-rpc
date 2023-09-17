package com.poldroc.rpc.framework.core.common.config;

import lombok.Data;

/**
 *
 * @author Poldroc
 * @date 2023/9/14
 */
@Data
public class ClientConfig {

    private String applicationName;

    private String registerAddr;

    private String proxyType;

}
