package com.lxwde.dummy.datasync;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.ClassResolvers;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;
import org.jboss.netty.handler.ssl.SslContext;
import org.jboss.netty.handler.ssl.util.SelfSignedCertificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.net.InetSocketAddress;
import java.time.ZonedDateTime;
import java.util.concurrent.Executors;

public class DataSyncServer {
	private final Logger logger = LoggerFactory.getLogger(DataSyncServer.class);
	
    private boolean ssl = false;
    private int port = 9000;
    private ChannelHandler channelHandler;
    private ServerBootstrap bootstrap;
    
    public DataSyncServer(boolean ssl, int port, ChannelHandler channelHandler) {
    	this.ssl = ssl;
    	this.port = port;
    	this.channelHandler = channelHandler;
    }
    
    @Override
    public void finalize() {
    	shutdown();
    }
    
    public void shutdown() {
    	try {
    		if (bootstrap != null) {
    			bootstrap.shutdown();
    		}
		} catch (Exception e) {
			logger.error("shutdown failed. {}", e.getMessage());     
        	if (bootstrap != null) {
        		bootstrap.releaseExternalResources();
        	}
		}
    }
    
    public void start() throws Exception {    	
    	try {
    		if (bootstrap != null) {
    			bootstrap.releaseExternalResources();
        	}
            // Configure SSL.
            final SslContext sslCtx;
            if (ssl) {
                SelfSignedCertificate ssc = new SelfSignedCertificate();
                sslCtx = SslContext.newServerContext(ssc.certificate(), ssc.privateKey());
            } else {
                sslCtx = null;
            }

            // Configure the server.
            bootstrap = new ServerBootstrap(
                    new NioServerSocketChannelFactory(
                            Executors.newCachedThreadPool(),
                            Executors.newCachedThreadPool()));

            // Set up the pipeline factory.
            bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
                public ChannelPipeline getPipeline() {
                    ChannelPipeline p = Channels.pipeline(
                            new ObjectEncoder(),
                            new ObjectDecoder(ClassResolvers.cacheDisabled(getClass().getClassLoader())),
                            channelHandler);
                    if (sslCtx != null) {
                        p.addFirst("ssl", sslCtx.newHandler());
                    }
                    return p;
                }
            });        
            
            // Bind and start to accept incoming connections.
            bootstrap.bind(new InetSocketAddress(port));
            logger.info("start() done. Bind on port {} and start to accept incoming connections...", port);
		} catch (Exception e) {
			logger.error("start() on port {} failed. {}", port, e.getMessage());
			if (bootstrap != null) {
        		bootstrap.releaseExternalResources();
        	}
			
			throw e;
		}  	
    }

}
