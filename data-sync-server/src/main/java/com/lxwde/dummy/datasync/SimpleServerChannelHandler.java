package com.lxwde.dummy.datasync;

import org.jboss.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SimpleServerChannelHandler extends SimpleChannelUpstreamHandler {
	private final Logger logger = LoggerFactory.getLogger(SimpleServerChannelHandler.class);
	private Set<Channel> clientChannels = new HashSet<>();

	private Map<Channel, ZonedDateTime> lastReceiveTimes = new ConcurrentHashMap<>();

	// Autowire your biz logic beans here
//	@Autowired
//	private TrackingDataSnapshotUpdator snapshotUpdator;
//
//	@Autowired
//	private GpsPointStatustifier gpsPointStatustifier;
//
//	@Autowired
//	private MailService mailService;


	@Override
	public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
		if (e instanceof ChannelStateEvent && ((ChannelStateEvent) e).getState() != ChannelState.INTEREST_OPS) {
			System.err.println(e);
		}
		super.handleUpstream(ctx, e);
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		clientChannels.add(e.getChannel());
		logger.info("channelConnected(). RemoteAddress:{}. LocalAddress:{}",
				e.getChannel().getRemoteAddress(), e.getChannel().getLocalAddress());
	}

	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) {
		if (clientChannels.contains(e)) {
			clientChannels.remove(e.getChannel());
			logger.warn("channelClosed(). removed from forwarding targets. RemoteAddress:{}. LocalAddress:{}",
					e.getChannel().getRemoteAddress(), e.getChannel().getLocalAddress());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
		try {
			logger.info("messageReceived() {}. channel:{}", e.getMessage(), e.getChannel().getRemoteAddress());
			lastReceiveTimes.put(e.getChannel(), ZonedDateTime.now());

			if (e.getMessage() instanceof DataWrapper<?>) {
				logger.info("messageReceived(). DataWrapper:{}", e.getMessage());

				DataWrapper<?> wrapper = (DataWrapper<?>) e.getMessage();
//				if (wrapper.getData() instanceof BatteryData) {
//					batterVoltageNotifier.notifiy((DataWrapper<BatteryData>) e.getMessage());
//					logger.debug("messageReceived(). DataWrapper:{}", e.getMessage());
//
//				}
			} else {
				// TODO
			}
			if (!e.getMessage().equals("heartbeat")) {
				forwardingToClients(e);
			}

		} catch (Exception ex) {
			logger.error("messageReceived() error occured during handling.", ex);
		}
	}

	public void broadcastMsg(Object obj) {
		Iterator<Channel> it = clientChannels.iterator();
		while (it.hasNext()) {
			Channel channel = it.next();
			if (channel.isConnected()) {
				channel.write(obj);
				logger.info("broadcastMsg() to {}. {}", channel.getRemoteAddress(), obj);
			}
		}
	}

	private void forwardingToClients(MessageEvent e) {
		Iterator<Channel> it = clientChannels.iterator();
		while (it.hasNext()) {
			Channel channel = it.next();
			if (e.getChannel().equals(channel)) {
				continue;
			}

			if (channel.isConnected()) {
				channel.write(e.getMessage());
			} else {
				logger.warn("forwardingToClients() failed. msg: {}. channel not connected. {}", e.getMessage(),
						channel.getRemoteAddress());
				it.remove();
			}
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		logger.error("exceptionCaught: channel:{}, exception:{}", e.getChannel(), e.getCause().getMessage());
		// e.getChannel().close();
	}

	@Scheduled(fixedRate = 600 * 1000)
	public void checkHearBeat() {
		// get last receive time and compare to now
		logger.info("checkHearBeat . lastReceiveTimes:{}", lastReceiveTimes);

		lastReceiveTimes.forEach((k, v) -> {
			if (Math.abs(v.toEpochSecond() - ZonedDateTime.now().toEpochSecond()) > 3600) {
				// TODO: send your warning email here

				logger.warn("notification emails sent. last receive time:{}", v.toEpochSecond());
			}
		});
	}
}