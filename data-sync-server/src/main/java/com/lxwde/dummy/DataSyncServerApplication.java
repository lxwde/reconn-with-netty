package com.lxwde.dummy;

import com.lxwde.dummy.datasync.DataSyncServer;
import com.lxwde.dummy.datasync.SimpleServerChannelHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;

@SpringBootApplication
@EnableScheduling
public class DataSyncServerApplication {

	@Autowired
	private SimpleServerChannelHandler simpleServerChannelHandler;

	@PostConstruct
	public void init() throws Exception {
		// TODO: read port from configuration file
		DataSyncServer dataSyncServer = new DataSyncServer(false, 9000, simpleServerChannelHandler);
		dataSyncServer.start();
	}

	public static void main(String[] args) {
		SpringApplication.run(DataSyncServerApplication.class, args);
	}
}
