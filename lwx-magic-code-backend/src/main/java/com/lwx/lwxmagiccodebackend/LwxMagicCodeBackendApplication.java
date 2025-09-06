package com.lwx.lwxmagiccodebackend;

import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@MapperScan("com.lwx.lwxmagiccodebackend.mapper")
@SpringBootApplication(exclude = {RedisEmbeddingStoreAutoConfiguration.class})
public class LwxMagicCodeBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(LwxMagicCodeBackendApplication.class, args);
	}

}
