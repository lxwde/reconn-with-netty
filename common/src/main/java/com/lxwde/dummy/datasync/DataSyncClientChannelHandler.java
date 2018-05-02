package com.lxwde.dummy.datasync;

import com.google.common.eventbus.EventBus;
import org.jboss.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataSyncClientChannelHandler extends SimpleChannelUpstreamHandler {
	private final Logger logger = LoggerFactory.getLogger(DataSyncClientChannelHandler.class);
	
	private EventBus eventBus;
	
	void setEventBus(EventBus eventBus) {
		this.eventBus = eventBus;
	}
	
	@Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
        logger.warn("channelDisconnected(): {}", e.getChannel().getRemoteAddress());
    }

    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) {
    	ChannelClosedEvent event = new ChannelClosedEvent(e.getChannel().getRemoteAddress().toString(), 
				  e.getChannel().getLocalAddress().toString());
        if (eventBus != null) {        	
        	eventBus.post(event);
        	logger.warn("channelClosed(): {} posted to event bus.", event);
        } else {
        	logger.error("channelClosed(): {} cannot be posted to event bus.", event);
        }
    }
    
	@Override
    public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
        if (e instanceof ChannelStateEvent &&
            ((ChannelStateEvent) e).getState() != ChannelState.INTEREST_OPS) {
            System.err.println(e);
        }
        super.handleUpstream(ctx, e);
    }
	
	@Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		logger.error("exceptionCaught: channel:{}, exception:{}", e.getChannel(), e.getCause().getMessage());
    //    e.getChannel().close();
    }
}
