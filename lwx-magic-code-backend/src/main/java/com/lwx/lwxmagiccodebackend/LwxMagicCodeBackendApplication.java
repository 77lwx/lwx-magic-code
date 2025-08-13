package com.lwx.lwxmagiccodebackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.lwx.lwxmagiccodebackend.mapper")
public class LwxMagicCodeBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(LwxMagicCodeBackendApplication.class, args);
	}

}
