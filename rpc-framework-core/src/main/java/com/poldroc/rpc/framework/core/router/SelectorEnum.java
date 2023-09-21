package com.poldroc.rpc.framework.core.router;
/**
 * 轮询路由实现类
 * @author Poldroc
 * @date 2023/9/21
 */

public enum SelectorEnum {

    RANDOM_SELECTOR(0,"random");

    int code;
    String desc;

    SelectorEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }


}