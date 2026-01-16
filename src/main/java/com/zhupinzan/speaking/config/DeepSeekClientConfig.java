package com.zhupinzan.speaking.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class DeepSeekClientConfig {

        @Bean
        public WebClient deepSeekWebClient(
                        @Value("${deepseek.base-url:https://api.deepseek.com}") String baseUrl,
                        @Value("${deepseek.api-key:}") String apiKey) {
                HttpClient httpClient = HttpClient.create()
                                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000)
                                .responseTimeout(Duration.ofSeconds(12))
                                .doOnConnected(conn -> conn
                                                .addHandlerLast(new ReadTimeoutHandler(12))
                                                .addHandlerLast(new WriteTimeoutHandler(12)));

                WebClient.Builder b = WebClient.builder()
                                .baseUrl(baseUrl)
                                .clientConnector(new ReactorClientHttpConnector(httpClient));

                if (apiKey != null && !apiKey.isEmpty()) {
                        b.defaultHeader("Authorization", "Bearer " + apiKey);
                }

                return b.build();
        }
}
