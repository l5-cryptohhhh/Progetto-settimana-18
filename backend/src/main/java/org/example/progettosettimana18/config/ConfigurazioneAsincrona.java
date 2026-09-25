package org.example.progettosettimana18.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Serve al listener degli avvisi: senza @EnableAsync il suo @Async
 * verrebbe ignorato e l'amministratore resterebbe ad aspettare Gmail.
 */
@Configuration
@EnableAsync
public class ConfigurazioneAsincrona {

    @Bean(name = "taskExecutor")
    public TaskExecutor esecutoreMail() {
        ThreadPoolTaskExecutor esecutore = new ThreadPoolTaskExecutor();
        esecutore.setCorePoolSize(2);
        esecutore.setMaxPoolSize(4);
        esecutore.setQueueCapacity(100);
        esecutore.setThreadNamePrefix("avvisi-");
        esecutore.initialize();
        return esecutore;
    }
}
