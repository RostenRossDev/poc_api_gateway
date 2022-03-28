package com.formacionbdi.springboot.app.config;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;


@EnableConfigServer // Para indicar que el micro servicio es un config server.
@SpringBootApplication
@RefreshScope
public class SpringbootServicioConfigServerApplication {
	private Logger log = LoggerFactory.getLogger(SpringbootServicioConfigServerApplication.class);
	
	
	@Autowired
	Environment env;

	public static void main(String[] args) {
		SpringApplication.run(SpringbootServicioConfigServerApplication.class, args);
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

	private void sendEvent(String chanel, String msg) throws Exception {
		//String msg = "Hello World " + System.currentTimeMillis();
		log.info("==========SENDING MESSAGE========== " + msg);
		jmsTemplate.convertAndSend(chanel, msg);
	}

	// escuchamos el canal "ConfigServer"
	@JmsListener(destination = "ConfigServer")
	public void handle(Message message) throws Exception {

		Date receiveTime = new Date();

		if (message instanceof TextMessage) {
			TextMessage tm = (TextMessage) message;
			log.info("Mensaje 1: " + tm.getText());

			// intentamos actualizar las properties del config server
			try {
				String nameAppUpdated = tm.getText();
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);
				RestTemplate rest = new RestTemplate();
				HttpEntity<String> request = new HttpEntity<String>(headers);
				log.info(env.getProperty("configuracion.autor.nombre"));
				log.info(env.getProperty("configuracion.texto"));


				ResponseEntity<String> response = rest.postForEntity("http://127.0.0.1:"+env.getProperty("server.port")+"/actuator/refresh", request, String.class);
				//ResponseEntity<String> responseEntity = rest.postForEntity("http://127.0.0.1:" + env.getProperty("server.port") + "/actuator/refresh", request, String.class);

				
				
				log.info("Response body: " + response.toString());

				log.info("############################## Propiedades actualizadas ##############################");

				log.info(env.getProperty("configuracion.autor.nombre"));

				log.info(env.getProperty("configuracion.texto"));

				// enviamos aviso al canal espesifico del micro que se debe actualizar
				log.info(response.getBody()+ ", status: "+response.getStatusCode());
				if (response.getStatusCode().equals(HttpStatus.OK) && response.getBody().equals("[]")) {
					String msg = "refresh";
					sendEvent(nameAppUpdated, msg);
					log.info("queue : "+nameAppUpdated);
					log.info("msg : "+msg);
				}

			} catch (JMSException e) {
				e.printStackTrace();
			}

		} else {
			log.info("Mensaje: " + message.toString());
		}
	}

}
