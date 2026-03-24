package com.design.chat.config;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TomcatConfig implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {
    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        // 强制使用 NIO2 (基于 Windows IOCP)，从而绕过 Java 17 NIO Pipe 默认使用的 UnixDomainSocket 导致启动崩溃的问题
        factory.setProtocol("org.apache.coyote.http11.Http11Nio2Protocol");
    }
}
