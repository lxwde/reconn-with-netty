package com.lxwde.dummy.datasync;


import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.ClassResolvers;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;
import org.jboss.netty.handler.ssl.SslContext;
import org.jboss.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.Timer;
import org.jboss.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DataSyncClient {
	private final Logger logger = LoggerFactory.getLogger(DataSyncClient.class);

	private boolean ssl;
	private String serverAddress;
	private int port;
	private DataSyncClientChannelHandler channelHandler;
	private ClientBootstrap bootstrap;
	private Channel channel;
	private final EventBus eventBus = new EventBus();
	private int reconnectDelayInSeconds;

	private final Timer timer = new HashedWheelTimer();

	private int countOfErrors;

	private int sendFailedCount;

	private int reconnFailedCount;
	@Subscribe
	public void handleChannelClosedEvent(final ChannelClosedEvent event) {
		logger.warn("handleChannelClosedEvent(). Channel: {}. Reconnect after {} seconds.", event.getRemoteAddress(),
				reconnectDelayInSeconds);
		timer.newTimeout(new TimerTask() {
			public void run(Timeout timeout) {
				try {
					channel = bootstrap.connect(new InetSocketAddress(serverAddress, port)).sync().getChannel();
					logger.warn("handleChannelClosedEvent(). Reconnected to: {}", event.getRemoteAddress());
					
				} catch (Exception e) {
					logger.error("handleChannelClosedEvent(). Reconnecting to: {} failed. ", event.getRemoteAddress(), e);
					handleChannelClosedEvent(event);
					if (reconnFailedCount++ >= 10) {
						// send mail and sms
						reconnFailedCount = 0;
					}
					
				}
			}
		}, reconnectDelayInSeconds, TimeUnit.SECONDS);
	}

	public DataSyncClient(boolean ssl, String serverAddress, int port, DataSyncClientChannelHandler channelHandler,
			int reconnectDelayInSeconds) {
		this.ssl = ssl;
		this.port = port;
		this.serverAddress = serverAddress;
		this.channelHandler = channelHandler;
		this.reconnectDelayInSeconds = reconnectDelayInSeconds;
		this.channelHandler.setEventBus(eventBus);
		eventBus.register(this);
	}

	
	public void send(Object msg) {
		try {
			channel.write(msg);
			logger.debug("send() done. {}", msg); 
			countOfErrors = 0;
		} catch (Exception e) {
			countOfErrors++;
			if (countOfErrors <= 1000) {
				logger.error("send() failed. countOfErrors:{}, msg:{}. {}", countOfErrors, msg, e.getMessage());
			} else if (countOfErrors % 1000 == 0){
				logger.error("send() failed. continous errors more than 1000, will print failed logs every 1000 msg. countOfErrors:{}, mgs:{}. {}", 
						countOfErrors, msg, e.getMessage());				
			}
		}
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

	public void connect() {
		try {
			// Configure SSL.
			final SslContext sslCtx;
			if (ssl) {
				sslCtx = SslContext.newClientContext(InsecureTrustManagerFactory.INSTANCE);
			} else {
				sslCtx = null;
			}

			// Configure the client.
			bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(Executors.newCachedThreadPool(),
					Executors.newCachedThreadPool()));
			// Set up the pipeline factory.
			bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
				public ChannelPipeline getPipeline() {
					ChannelPipeline p = Channels.pipeline(new ObjectEncoder(),
							new ObjectDecoder(ClassResolvers.cacheDisabled(getClass().getClassLoader())),
							channelHandler);

					if (sslCtx != null) {
						p.addFirst("ssl", sslCtx.newHandler(serverAddress, port));
					}
					return p;
				}
			});

			// Start the connection attempt.
			// Wait until the connection attempt is finished and then the
			// connection is closed.
			// channel = bootstrap.connect(new InetSocketAddress(serverAddress, port)).sync().getChannel();
			connectInternal();			
			// channel.getCloseFuture().sync();
			// logger.info("Quit and close...");

		} catch (Exception e) {
			logger.error("connect() {}:{} failed. {}", serverAddress, port, e.getMessage());
			if (bootstrap != null) {
				bootstrap.releaseExternalResources();
			}
		}
	}

	private int connFailedCount;
	private void connectInternal() {
		try {
			channel = bootstrap.connect(new InetSocketAddress(serverAddress, port)).sync().getChannel();
			logger.info("connectInternal() done. RemoteAddress:{}, LocalAddress:{}", channel.getRemoteAddress(), channel.getLocalAddress());
		} catch (Exception e) {
			logger.error("connectInternal() {}:{} failed. Reconnect after {} seconds.", serverAddress, port, reconnectDelayInSeconds, e.getMessage());
			
			if (connFailedCount++ >= 10) {
				// send mail and sms
				connFailedCount = 0;
			}
			
			timer.newTimeout(new TimerTask() {
				public void run(Timeout timeout) {
					connectInternal();
				}
			}, reconnectDelayInSeconds, TimeUnit.SECONDS);
		}
	}
	
//	@Scheduled(fixedRate = 600 * 1000)
//	public void sendHeartBeat() {
//		if (bootstrap != null) {
//			this.send(new DataWrapper<String>("dummy", "heartbeat"));
//		}
//	}

}
