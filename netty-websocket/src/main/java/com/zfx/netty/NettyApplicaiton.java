package com.zfx.netty;

import ch.qos.logback.classic.net.SimpleSocketServer;
import com.zfx.netty.simple.client.SimpleClient;
import com.zfx.netty.simple.server.SimpleServer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @description:
 * @author: zheng-fx
 * @time: 2020/10/23 0023 23:55
 */
@SpringBootApplication
public class NettyApplicaiton implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(NettyApplicaiton.class, args);    
    }
    
    @Override
    public void run(String... args) throws Exception {
//       new SimpleServer().start();
        new SimpleClient().start();
    }
}
