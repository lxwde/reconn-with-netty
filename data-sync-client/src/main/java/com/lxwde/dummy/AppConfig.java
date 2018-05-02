package com.lxwde.dummy;

import com.lxwde.dummy.datasync.DataSyncClient;
import com.lxwde.dummy.datasync.SimpleClientChannelHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Autowired
    private SimpleClientChannelHandler simpleClientChannelHandler;

    @Bean
    public DataSyncClient dataSyncClient() {
        // TODO: read settings from configuration file
        return new DataSyncClient(false, "127.0.0.1", 9000, simpleClientChannelHandler, 10);
    }
}
