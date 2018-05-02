package com.lxwde.dummy.datasync;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;

@Component
public class SimpleClientChannelHandler extends DataSyncClientChannelHandler {
	private final Logger logger = LoggerFactory.getLogger(SimpleClientChannelHandler.class);

	// Inject your working component here

//	@Autowired
//	private TrackingDataSnapshotUpdator updator;
//
//	@Autowired
//	private GpsPointProcessor gpsPointProcessor;

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
    	logger.info("channelConnected(). {}", e.getChannel().getRemoteAddress());
    	
        e.getChannel().write("Hi server, I'm connected.");
    }   
    
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
    	try{    		
            if (e.getMessage() instanceof DataWrapper<?>) {
            	logger.info("messageReceived(): {}", e.getMessage());
            	
            	DataWrapper<?> wrapper = (DataWrapper<?>)e.getMessage();
            	if (wrapper.getData() instanceof String || wrapper.getData() instanceof Integer) {

            	} else {
            		
            	}
            	
            } else {
            	logger.info("messageReceived(): {}", e.getMessage());
            }
            
    	} catch (Exception ex) {
    		logger.error("messageReceived() error occured during handling.", ex);
    	}
    }
}
