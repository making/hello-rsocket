package com.example;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import io.rsocket.AbstractRSocket;
import io.rsocket.Payload;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.ServerTransport;
import io.rsocket.transport.netty.server.NettyContextCloseable;
import io.rsocket.transport.netty.server.WebsocketServerTransport;
import io.rsocket.util.DefaultPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class HelloRSocketServer {
	private static final Logger log = LoggerFactory.getLogger(HelloRSocketServer.class);

	public static void main(String[] args) throws Exception {
		int port = Optional.ofNullable(System.getenv("PORT")).map(Integer::parseInt)
				.orElse(7000);
		CountDownLatch latch = new CountDownLatch(1);
		ServerTransport<NettyContextCloseable> transport = WebsocketServerTransport
				.create("0.0.0.0", port);
		RSocketFactory.receive().acceptor(
				(setupPayload, reactiveSocket) -> Mono.just(new AbstractRSocket() {
					@Override
					public Mono<Payload> requestResponse(Payload payload) {
						log.info("[requestResponse] Received {}", payload.getDataUtf8());
						return Mono.just(DefaultPayload.create("Hello World!"))
								.log("requestResponse");
					}

					@Override
					public Flux<Payload> requestStream(Payload payload) {
						log.info("[requestStream] Received {}", payload.getDataUtf8());
						return Mono.fromCallable(OffsetDateTime::now) //
								.delayElement(Duration.ofMillis(100)) //
								.repeat(100) //
								.map(OffsetDateTime::toString) //
								.log("requestStream") //
								.map(DefaultPayload::create);
					}

					@Override
					public Mono<Void> fireAndForget(Payload payload) {
						log.info("[fireAndForget] Received {}", payload.getDataUtf8());
						return Mono.empty();
					}
				})) //
				.transport(transport) //
				.start() //
				.subscribe();
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			latch.countDown();
			log.info("Shutdown");
		}));
		latch.await();
		log.info("Bye");
	}
}
