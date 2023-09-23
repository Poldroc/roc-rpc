package com.poldroc.rpc.framework.core.serialize;

import java.io.Serializable;

/**
 *
 * @author Poldroc
 * @date 2023/9/23
 */

public class User implements Serializable {

    private static final long serialVersionUID = -1728196331321496561L;
    private Integer id;

    private Long tel;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getTel() {
        return tel;
    }

    public void setTel(Long tel) {
        this.tel = tel;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", tel=" + tel +
                '}';
    }

}
