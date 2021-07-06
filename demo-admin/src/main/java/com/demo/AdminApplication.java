package com.demo;

import com.demo.utils.FileUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;

import java.io.File;


@SpringBootApplication
public class AdminApplication {

	public static void main(String[] args) {
		SpringApplication.run(AdminApplication.class, args);
	}

	@GetMapping(value = "/", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Mono<Void> IndexAction(ServerHttpRequest request, ServerHttpResponse response) {
		File file = new File();
		FileUtil.downloadFile(request, response, file);
	}
}
