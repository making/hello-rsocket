package com.example;

import java.util.Optional;

import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.ClientTransport;
import io.rsocket.transport.netty.client.WebsocketClientTransport;
import io.rsocket.util.DefaultPayload;

public class HelloRSocketClient {
	public static void main(String[] args) throws Exception {
		int port = Optional.ofNullable(System.getenv("PORT")).map(Integer::parseInt)
				.orElse(7000);
		ClientTransport transport = WebsocketClientTransport.create("localhost", port);
		RSocket rsocket = RSocketFactory.connect().transport(transport).start().block();
		rsocket.requestStream(DefaultPayload.create("hello")) //
				.map(Payload::getDataUtf8) //
				.log("requestStream") //
				.subscribe();
		System.in.read();
	}

}
