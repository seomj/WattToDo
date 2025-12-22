package com.ssafy.wtd.backend.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

@Configuration
public class ChatClientConfig {

    /**
     * Cloudflare 호환을 위한 커스텀 RestClient.Builder
     * Spring AI가 이 빈을 자동으로 사용하여 OpenAI API 호출 시 적용됩니다
     */
    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder()
                .requestInterceptor(cloudflareHeaderInterceptor());
    }

    /**
     * Cloudflare가 요구하는 HTTP 헤더를 추가하는 인터셉터
     */
    @Bean
    public ClientHttpRequestInterceptor cloudflareHeaderInterceptor() {
        return (request, body, execution) -> {
            // Cloudflare 보안 검사를 통과하기 위한 헤더
            request.getHeaders().set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            request.getHeaders().set("Accept", "application/json");
            request.getHeaders().set("Connection", "keep-alive");
            return execution.execute(request, body);
        };
    }

    @Bean
    public ChatClient chatClient(OpenAiChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }
}
