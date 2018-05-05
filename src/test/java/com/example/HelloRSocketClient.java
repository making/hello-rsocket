package com.example;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.ClientTransport;
import io.rsocket.transport.netty.client.WebsocketClientTransport;
import io.rsocket.util.DefaultPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloRSocketClient {
	private static final Logger log = LoggerFactory.getLogger(HelloRSocketClient.class);

	public static void main(String[] args) throws Exception {
		int port = Optional.ofNullable(System.getenv("PORT")).map(Integer::parseInt)
				.orElse(7000);
		CountDownLatch latch = new CountDownLatch(1);
		ClientTransport transport = WebsocketClientTransport.create("localhost", port);
		RSocket rsocket = RSocketFactory.connect().transport(transport).start().block();
		rsocket.requestStream(DefaultPayload.create("hello")) //
				.map(Payload::getDataUtf8) //
				.log("requestStream") //
				.subscribe();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			latch.countDown();
			log.info("Shutdown");
		}));
		latch.await();
		log.info("Bye");
	}

}
