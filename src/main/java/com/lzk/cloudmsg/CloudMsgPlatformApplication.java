package com.lzk.cloudmsg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CloudMsgPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudMsgPlatformApplication.class, args);
    }

}
