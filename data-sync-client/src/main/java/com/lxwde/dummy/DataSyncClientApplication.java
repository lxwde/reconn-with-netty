package com.lxwde.dummy;

import com.lxwde.dummy.datasync.DataSyncClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;

@SpringBootApplication
@EnableScheduling
public class DataSyncClientApplication {

	private static final Logger logger = LoggerFactory.getLogger(DataSyncClientApplication.class);

	@Autowired
	private DataSyncClient dataSyncClient;

	@PostConstruct
	public void init() {
		dataSyncClient.connect();
		logger.debug("connected to server successfully.");
	}

	@Scheduled(fixedRate = 20 * 1000)
	public void sendHearBeat() {
		dataSyncClient.send("heartbeat");
		logger.info("heartbeat sent.");
	}

	public static void main(String[] args) {
		SpringApplication.run(DataSyncClientApplication.class, args);
	}
}
