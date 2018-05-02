package com.lxwde.dummy.datasync;

import java.io.Serializable;

public class ChannelClosedEvent implements Serializable{
    public ChannelClosedEvent(String remoteAddress, String localAddress) {
        super();
        this.remoteAddress = remoteAddress;
        this.localAddress = localAddress;
    }

    private static final long serialVersionUID = 1L;
    private String remoteAddress;
    private String localAddress;

    public String getRemoteAddress() {
        return remoteAddress;
    }
    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }
    public String getLocalAddress() {
        return localAddress;
    }
    public void setLocalAddress(String localAddress) {
        this.localAddress = localAddress;
    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ChannelClosedEvent [remoteAddress=").append(remoteAddress).append(", localAddress=")
                .append(localAddress).append("]");
        return builder.toString();
    }
}
