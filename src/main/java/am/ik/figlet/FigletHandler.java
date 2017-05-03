package am.ik.figlet;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.*;

import com.github.lalyos.jfiglet.FigletFont;

import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

public class FigletHandler {
	private static final Logger log = LoggerFactory.getLogger(FigletHandler.class);
	private static final String FORWARDED_URL = "X-CF-Forwarded-Url";
	private static final String PROXY_METADATA = "X-CF-Proxy-Metadata";
	private static final String PROXY_SIGNATURE = "X-CF-Proxy-Signature";
	private static final String FIGLET_FONT = "X-Figlet-Font";
	private final WebClient webClient = WebClient.create();

	public RouterFunction<ServerResponse> route() {
		return RouterFunctions.route(incoming(), this::jack);
	}

	private RequestPredicate incoming() {
		return req -> {
			final HttpHeaders h = req.headers().asHttpHeaders();
			return h.containsKey(FORWARDED_URL) && h.containsKey(PROXY_METADATA)
					&& h.containsKey(PROXY_SIGNATURE);
		};
	}

	private Mono<ServerResponse> jack(ServerRequest req) {
		final HttpHeaders headers = headers(req.headers().asHttpHeaders());
		final URI uri = headers.remove(FORWARDED_URL).stream().findFirst()
				.map(URI::create).orElseThrow(() -> new IllegalStateException(
						String.format("No %s header present", FORWARDED_URL)));

		final WebClient.RequestHeadersSpec<?> spec = webClient.method(req.method())
				.uri(uri).headers(headers);
		final List<String> figletFont = req.headers().header(FIGLET_FONT);

		return req
				.bodyToMono(String.class).<WebClient.RequestHeadersSpec<?>>map(
						((WebClient.RequestBodySpec) spec)::syncBody)
				.switchIfEmpty(Mono.just(spec)).flatMap(s -> s.exchange()
						.flatMap(res -> convertResponse(res, figletFont)));
	}

	private Mono<ServerResponse> convertResponse(ClientResponse res,
			List<String> figletFont) {
		final Mono<String> body = res.bodyToMono(String.class);
		final ServerResponse.BodyBuilder builder = ServerResponse.status(res.statusCode())
				.headers(res.headers().asHttpHeaders());
		if (isTextPlain(res)) {
			return body.map(b -> figlet(b, figletFont))
					.flatMap(t -> builder.contentLength(t.getT2()).syncBody(t.getT1()));
		}
		return builder.body(body, String.class);
	}

	private boolean isTextPlain(ClientResponse res) {
		return res.headers().contentType()
				.filter(mediaType -> mediaType.toString().startsWith("text/plain"))
				.isPresent();
	}

	private Tuple2<String, Long> figlet(String text, List<String> figletFont) {
		try {
			final String s = figletFont.isEmpty() ? FigletFont.convertOneLine(text)
					: FigletFont.convertOneLine(
							"classpath:/fonts/" + figletFont.get(0).trim() + ".flf",
							text);
			return Tuples.of(s, (long) s.getBytes(StandardCharsets.UTF_8).length);
		}
		catch (Exception e) {
			return Tuples.of(text, (long) text.getBytes(StandardCharsets.UTF_8).length);
		}
	}

	private HttpHeaders headers(HttpHeaders incomingHeaders) {
		final HttpHeaders headers = new HttpHeaders();
		headers.putAll(incomingHeaders);
		final String host = URI.create(incomingHeaders.getFirst(FORWARDED_URL)).getHost();
		headers.put(HttpHeaders.HOST, Collections.singletonList(host));
		return headers;
	}
}
