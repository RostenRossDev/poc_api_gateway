package com.formacionbdi.springboot.app.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@EnableConfigServer //Para indicar que el micro servicio es un config server.
@SpringBootApplication
@RefreshScope
public class SpringbootServicioConfigServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringbootServicioConfigServerApplication.class, args);
	}

}
