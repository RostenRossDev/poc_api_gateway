package com.formacionbdi.springboot.app.productos;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

@RefreshScope
@EnableEurekaClient
@SpringBootApplication
@EnableScheduling
public class SpringbootServicioProductosApplication {

	private Logger log = LoggerFactory.getLogger(SpringbootServicioProductosApplication.class);
	
	@Autowired
	Environment env;
	
	public static void main(String[] args) {
		SpringApplication.run(SpringbootServicioProductosApplication.class, args);
	}
	
	@Autowired
	private JmsTemplate jmsTemplate;

	@PostConstruct
	private void customizeJmsTemplate() {
		// Update the jmsTemplate's connection factory to cache the connection
		CachingConnectionFactory ccf = new CachingConnectionFactory();
		ccf.setTargetConnectionFactory(jmsTemplate.getConnectionFactory());
		jmsTemplate.setConnectionFactory(ccf);

		// By default Spring Integration uses Queues, but if you set this to true you
		// will send to a PubSub+ topic destination
		jmsTemplate.setPubSubDomain(false);
	}

	/*
	@Value("canal")
	private String queueName;

	//@Scheduled(fixedRate = 30000)
	public void sendEvent() throws Exception {
		String msg = "Hello World " + System.currentTimeMillis();
		log.info("==========SENDING MESSAGE========== " + msg);
		jmsTemplate.convertAndSend(queueName, msg);
	}
*/
	
	/////////////// revice el mensaje
	
	@JmsListener(destination = "servicio-productos")
	public void handle(Message message) {
		Date receiveTime = new Date();
		log.info("mensaje recivido: "+message.toString());
		if (message instanceof TextMessage) {
			TextMessage tm = (TextMessage) message;
			try {
				log.info("Message Received at " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(receiveTime)
								+ " with message content of: " + tm.getText());
				if(tm.getText().equals("refresh")) {
					HttpHeaders headers = new HttpHeaders();
					headers.setContentType(MediaType.APPLICATION_JSON);
					RestTemplate rest = new RestTemplate();
					HttpEntity<String> request = new HttpEntity<String>(headers);					
					
					String response = rest.postForObject("http://127.0.0.1:"+env.getProperty("server.port")+"/actuator/refresh", request, String.class);
					
					log.info("respuesta: "+response);
					log.info("respuesta: "+ response.charAt(0));
					log.info("############ actualizando ");
					log.info("autor: "+env.getProperty("configuracion.autor.nombre"));
					log.info("texto: "+env.getProperty("configuracion.texto"));
				} 
			} catch (JMSException e) {
				e.printStackTrace();
			}
		} else {
			log.info(message.toString());
		}
		
	}
}
