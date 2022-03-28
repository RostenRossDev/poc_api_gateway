package com.message.sender.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cassandra.CassandraProperties.Request;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.message.sender.model.ChanelAndMessage;
import com.message.sender.model.CommitMessage;

@RestController
public class SenderMessageController {
	private Logger log = LoggerFactory.getLogger(SenderMessageController.class);

	@Autowired
	private Environment env;

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

	private void sendEvent(ChanelAndMessage chanelAndMsg) throws Exception {
		// String msg = "Hello World " + System.currentTimeMillis();
		log.info("==========SENDING MESSAGE==========>>>> Chanel: " + chanelAndMsg.getMessage());
		jmsTemplate.convertAndSend(chanelAndMsg.getChanel(), chanelAndMsg.getMessage());
	}

	@PostMapping("/send-message")
	public ResponseEntity<?> sendMessage(RequestEntity<Map<String, Object>> req) {
		// public ResponseEntity<?> sendMessage() {
		// log.info("msj: "+chanelAndMsg.toString());
		// log.info("msj: "+chanelAndMsg.getMessage());
		// log.info("msj: "+chanelAndMsg.getChanel());
		/// log.info("body: " + req);
		log.info("body: " + req.getBody());
		log.info("body: " + req.getBody().get("push"));
		log.info("body: " + req.getBody().get("push").getClass().getSimpleName());
		HashMap<String, Object> body = (HashMap<String, Object>) req.getBody().get("push");
		List<?> changes = (List<?>) body.get("changes");
		log.info("changes: " + changes);
		Map<String, Object> commitMessage = null;

		Map<String, Object> newCommit = (Map<String, Object>) changes.get(0);
		log.info("object: " + newCommit);
		String msg = null;
		commitMessage = (Map<String, Object>) newCommit.get("new");
		log.info("Commit message in for new : " + commitMessage);

		commitMessage = (Map<String, Object>) commitMessage.get("target");
		log.info("message: " + commitMessage);

		// commitMessage = (Map<String, Object>) commitMessage.get("message");

		msg = (String) commitMessage.get("message");
		log.info("msg: " + msg.toString());

		if (commitMessage != null) {
			log.info("Commit message: " + commitMessage.get("message"));
		}
		String queueToNotify = msg.substring(0, msg.indexOf("."));
		log.info("Cola a notificar: " + queueToNotify);
		// ([a-zA-Z])+(-)([a-zA-Z]+)+(-)([a-zA-Z]+)
		String cadena = "servicio-items-dev.properties fue modificado la propertie xxxx";
		// patron para el nombre del archivo de configuracion modificado
		Pattern NameAppConfigPattern = Pattern.compile("([a-zA-Z])+(-)([a-zA-Z]+)+(-)([a-zA-Z]+)",
				Pattern.CASE_INSENSITIVE);
		// patron para el nombre de de la app a actualizar
		Pattern NameAppPattern = Pattern.compile("([a-zA-Z])+(-)([a-zA-Z]+)", Pattern.CASE_INSENSITIVE);

		// extraemos el patron del nombre del archivo
		Matcher matcherNameAppConfig = NameAppConfigPattern.matcher(cadena);
		matcherNameAppConfig.find();

		// del nombre del archivo extraemos el nombre de la app a actualizar
		Matcher matcherNameApp = NameAppPattern.matcher(matcherNameAppConfig.group());
		matcherNameApp.find();

		Map<String, String> response = new HashMap<>();
		response.put("Nombre del archivo de configuracion: ", matcherNameAppConfig.group());
		response.put("Nombre del archivo del microservicio: ", matcherNameApp.group());
		HttpStatus status;

		try { // enviamos el mensaje al canal indicado
			ChanelAndMessage chanelAndMsg = new ChanelAndMessage("ConfigServer",matcherNameApp.group());
			sendEvent(chanelAndMsg);
			response.put("msg", "Mensaje enviado con exito.");
			status = HttpStatus.OK;
		} catch (Exception e) {
			// fallo en envio de mensaje
			response.put("msg", "Fallo al enviar mensaje.");
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			e.printStackTrace();
		}

		return new ResponseEntity<Map<String, String>>(response, /* status */HttpStatus.OK);
	}

	@PostMapping("/send-message-manual")
	private ResponseEntity<?> manualSendMessage(@RequestBody ChanelAndMessage chanelAndMessage) throws Exception {
		Map<String, String> response = new HashMap<>();
		response.put("Canal a notificar : ", chanelAndMessage.getChanel());
		response.put("Cola  a notificar : ", chanelAndMessage.getMessage());
		HttpStatus status;
		try { // enviamos el mensaje al canal indicado
			sendEvent(chanelAndMessage);
			response.put("msg", "Mensaje enviado con exito.");
			status = HttpStatus.OK;
		} catch (Exception e) {
			// fallo en envio de mensaje
			response.put("msg", "Fallo al enviar mensaje.");
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			e.printStackTrace();
		}
		return new ResponseEntity<Map<String, String>>(response, /* status */HttpStatus.OK);

	}

}
