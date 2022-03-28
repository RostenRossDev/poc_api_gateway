package com.formacionbdi.springboot.app.item;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Consumer;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.web.client.RestTemplate;

import com.formacionbdi.springboot.app.item.models.SensorReading;
import com.netflix.ribbon.proxy.annotation.Http;

@RefreshScope
@EnableCircuitBreaker
@EnableEurekaClient
@EnableFeignClients
@SpringBootApplication
public class SpringbootServicioItemApplication {
	
	private Logger log = LoggerFactory.getLogger(SpringbootServicioItemApplication.class);
	
	@Autowired
	Environment env;
	
	public static void main(String[] args) {
		SpringApplication.run(SpringbootServicioItemApplication.class, args);
	}

	
	
	@JmsListener(destination = "servicio-items")
	public void handle(Message message) {
		log.info(env.getProperty("configuracion.autor.nombre"));
		log.info(env.getProperty("configuracion.texto"));
		Date receiveTime = new Date();

		if (message instanceof TextMessage) {
			TextMessage tm = (TextMessage) message;
			try {
				log.info(
						"Message Received at " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(receiveTime)
								+ " with message content of: " + tm.getText());
				if(tm.getText().equals("refresh")) {
					HttpHeaders headers = new HttpHeaders();
					headers.setContentType(MediaType.APPLICATION_JSON);
					RestTemplate rest = new RestTemplate();
					HttpEntity<String> request = new HttpEntity<String>(headers);
					
					
					String response = rest.postForObject("http://127.0.0.1:"+env.getProperty("server.port")+"/actuator/refresh", request, String.class);
					
					log.info("respuesta: "+response);
					log.info("respuesta: "+ response.charAt(0));
					log.info("datos ahora: ");
					log.info(env.getProperty("configuracion.autor.nombre"));
					log.info(env.getProperty("configuracion.texto"));
				}
			} catch (JMSException e) {
				e.printStackTrace();
			}
		} else {
			log.info(message.toString());
		}
				
	}
}
