package com.zhaoxinms.wxmp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 肇新智慧物业 · 住宅物业C端微信小程序服务启动类
 * <p>
 * 端口：8081（与B端 admin 8080 区分）
 * 接口前缀：/wxmp/**
 *
 * @author zhaoxinms
 */
@SpringBootApplication(scanBasePackages = {"com.zhaoxinms"},
    exclude = {DataSourceAutoConfiguration.class})
@EnableTransactionManagement
@MapperScan(basePackages = {"com.zhaoxinms.**.mapper"})
public class WxMpApplication {

    public static void main(String[] args) {
        SpringApplication.run(WxMpApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  肇新智慧物业C端服务启动成功   ლ(´ڡ`ლ)ﾞ  \n" +
            "  _____                                                         \n" +
            " |__  /___ _ __ ___   ___ _ __ __ _  __ _ _ __   ___  ___  ___ \n" +
            "   / // _ \\ '_ ` _ \\ / _ \\ '__/ _` |/ _` | '_ \\ / _ \\/ __|/ _ \\n" +
            "  / /|  __/ | | | | |  __/ | | (_| | (_| | | | |  __/\\__ \\  __/\n" +
            " /____\\___|_| |_| |_|\\___|_|  \\__, |\\__,_|_| |_|\\___||___/\\___|\n" +
            "                               |___/                            ");
    }
}
