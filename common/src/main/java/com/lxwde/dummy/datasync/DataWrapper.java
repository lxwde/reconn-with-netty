package com.lxwde.dummy.datasync;

import java.io.Serializable;

public class DataWrapper<T extends Serializable> implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public DataWrapper(String key, T data) {
        super();
        this.key = key;
        this.data = data;
    }

    private String key;
    private T data;

    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public T getData() {
        return data;
    }
    public void setData(T data) {
        this.data = data;
    }
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DataWrapper [key=").append(key).append(", data=").append(data).append("]");
        return builder.toString();
    }

}

