package org.nem.core.connect;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.*;
import org.apache.http.*;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.hamcrest.core.*;
import org.junit.*;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.nem.core.serialization.Deserializer;
import org.nem.core.test.*;
import org.nem.core.utils.ExceptionUtils;

import java.net.URL;
import java.util.concurrent.CancellationException;
import java.util.function.*;

@RunWith(Enclosed.class)
public class HttpMethodClientTest {
	private static final HttpDeserializerResponseStrategy DEFAULT_STRATEGY = new HttpJsonResponseStrategy(null);
	private static final int GOOD_TIMEOUT = 15000;

	private static <T> HttpMethodClient.AsyncToken<T> sendPost(
			final HttpMethodClient<T> client,
			final URL url,
			final HttpResponseStrategy<T> responseStrategy) {
		return client.post(url, new HttpJsonPostRequest(new MockSerializableEntity()), responseStrategy);
	}

	public static class GetMethodTest extends TestRunner {
		public GetMethodTest() {
			super("GET", HttpMethodClient::get);
		}
	}

	public static class PostMethodTest extends TestRunner {
		public PostMethodTest() {
			super("POST", HttpMethodClientTest::sendPost);
		}
	}

	private static HttpMethodClient<Deserializer> createClient(final int timeout) {
		return new HttpMethodClient<>(timeout, timeout, 10000);
	}

	private static class TestRunner {
		private static final String GOOD_URL = "http://bigalice2.nem.ninja/test.json";
		private static final String MALFORMED_URI = "http://www.example.com/customers/[12345]";
		private static final String HOST_LESS_URI = "file:///~/calendar";

		public interface SendAsyncStrategy {
			<T> HttpMethodClient.AsyncToken<T> send(
					final HttpMethodClient<T> client,
					final URL url,
					final HttpResponseStrategy<T> responseStrategy);
		}

		private final String httpMethod;
		private final SendAsyncStrategy strategy;

		public TestRunner(final String httpMethod, final SendAsyncStrategy strategy) {
			this.httpMethod = httpMethod;
			this.strategy = strategy;
		}

		@Test
		public void cannotCallSendAfterClose() {
			// Arrange:
			final HttpMethodClient<Deserializer> client = createClient(GOOD_TIMEOUT);

			// Act:
			client.close();

			// Assert: this should fail because the underlying client is closed
			ExceptionAssert.assertThrows(
					v -> this.strategy.send(client, this.stringToUrl(GOOD_URL), DEFAULT_STRATEGY).get(),
					IllegalStateException.class);
		}

		@Test
		public void sendReturnsJsonDeserializerOnSuccess() {
			// Arrange:
			final HttpMethodClient<Deserializer> client = createClient(GOOD_TIMEOUT);

			// Act:
			final Deserializer deserializer = this.strategy.send(client, this.stringToUrl(GOOD_URL), DEFAULT_STRATEGY).get();

			// Assert:
			Assert.assertThat(deserializer, IsNull.notNullValue());
			Assert.assertThat(deserializer.readString("test"), IsEqual.equalTo("org.nem.core.connect.HttpMethodClientTest"));
			Assert.assertThat(deserializer.readString("one"), IsEqual.equalTo("two"));
		}

		@Test
		public void sendDelegatesToStrategyOnSuccess() {
			// Arrange:
			final HttpDeserializerResponseStrategy strategy = Mockito.mock(HttpDeserializerResponseStrategy.class);
			final HttpMethodClient<Deserializer> client = createClient(GOOD_TIMEOUT);

			// Act:
			this.strategy.send(client, this.stringToUrl(GOOD_URL), strategy).get();

			// Assert:
			Mockito.verify(strategy, Mockito.times(1)).coerce(Mockito.any(HttpRequestBase.class), Mockito.any(HttpResponse.class));
		}

		@Test
		public void sendSetsRequestHeadersCorrectly() {
			// Arrange:
			final MockHttpResponseStrategy<Object> strategy = new MockHttpResponseStrategy<>();
			final HttpMethodClient<Object> client = new HttpMethodClient<>(GOOD_TIMEOUT, GOOD_TIMEOUT, GOOD_TIMEOUT);

			// Act:
			this.strategy.send(client, this.stringToUrl(GOOD_URL), strategy).get();

			// Assert:
			Assert.assertThat(strategy.getRequestMethod(), IsEqual.equalTo(this.httpMethod));
			Assert.assertThat(strategy.getRequestContentType(), IsEqual.equalTo("application/json"));
			Assert.assertThat(strategy.getRequestAcceptHeader(), IsEqual.equalTo("content-type/supported"));
		}

		@Test(expected = InactivePeerException.class)
		public void sendThrowsInactivePeerExceptionOnConnectionTimeout() {
			// Arrange:
			this.runTestWithTimeoutService((mockService, timeoutUrl) -> {
				// - stop the service so that it's no longer running and will reject connections
				mockService.stop();
				final HttpMethodClient<Deserializer> client = new HttpMethodClient<>(500, GOOD_TIMEOUT, GOOD_TIMEOUT);

				// Act:
				this.strategy.send(client, this.stringToUrl(timeoutUrl), DEFAULT_STRATEGY).get();
			});
		}

		@Test(expected = BusyPeerException.class)
		public void sendThrowsBusyPeerExceptionOnSocketTimeout() {
			// Arrange:
			this.runTestWithTimeoutService((mockService, timeoutUrl) -> {
				// Arrange:
				// - set a delay in request processing to simulate a socket timeout
				mockService.addRequestProcessingDelay(1000);
				final HttpMethodClient<Deserializer> client = new HttpMethodClient<>(GOOD_TIMEOUT, 500, GOOD_TIMEOUT);

				// Act:
				this.strategy.send(client, this.stringToUrl(timeoutUrl), DEFAULT_STRATEGY).get();
			});
		}

		private void runTestWithTimeoutService(final BiConsumer<WireMockServer, String> action) {
			WireMockServer mockService = null;
			try {
				mockService = new WireMockServer(8890);
				mockService.start();
				mockService.stubFor(createTimeoutStub(WireMock::get));
				mockService.stubFor(createTimeoutStub(WireMock::post));
				action.accept(mockService, "http://localhost:" + mockService.port() + "/timeout");
			} finally {
				if (null != mockService) {
					mockService.stop();
				}
			}
		}

		private static MappingBuilder createTimeoutStub(final Function<UrlMatchingStrategy, MappingBuilder> createBuilder) {
			return createBuilder.apply(WireMock.urlEqualTo("/timeout")).willReturn(
					WireMock.aResponse().withStatus(200));
		}

		@Test(expected = CancellationException.class)
		public void sendThrowsCancellationExceptionOnCancel() {
			// Arrange:
			final HttpMethodClient<Deserializer> client = createClient(1);

			// Act:
			final HttpMethodClient.AsyncToken<Deserializer> token = this.strategy.send(
					client,
					this.stringToUrl(GOOD_URL),
					DEFAULT_STRATEGY);
			token.abort();
			token.get();
		}

		@Test(expected = FatalPeerException.class)
		public void sendThrowsFatalPeerExceptionOnUriError() {
			// Arrange:
			final HttpMethodClient<Deserializer> client = createClient(GOOD_TIMEOUT);

			// Act:
			this.strategy.send(client, this.stringToUrl(MALFORMED_URI), DEFAULT_STRATEGY).get();
		}

		@Test(expected = FatalPeerException.class)
		public void sendThrowsFatalPeerExceptionOnClientProtocolError() {
			// Arrange:
			final HttpMethodClient<Deserializer> client = createClient(GOOD_TIMEOUT);

			// Act:
			this.strategy.send(client, this.stringToUrl(HOST_LESS_URI), DEFAULT_STRATEGY).get();
		}

		@Test(expected = CancellationException.class)
		public void sendIsCancelledIfOperationTakesTooLong() {
			// Arrange:
			final HttpMethodClient<Deserializer> client = new HttpMethodClient<>(
					100 * GOOD_TIMEOUT,
					100 * GOOD_TIMEOUT,
					GOOD_TIMEOUT / 20);

			// Act:
			this.strategy.send(client, this.stringToUrl("http://10.255.255.1"), DEFAULT_STRATEGY).get();
		}

		private URL stringToUrl(final String s) {
			return ExceptionUtils.propagate(() -> new URL(s));
		}
	}

	private static class MockHttpResponseStrategy<T> implements HttpResponseStrategy<T> {
		private String requestMethod;
		private String requestContentType;
		private String requestAcceptHeader;

		@Override
		public T coerce(final HttpRequestBase request, final HttpResponse response) {
			final HttpEntity entity = response.getEntity();
			this.requestMethod = request.getMethod();
			this.requestContentType = null == entity ? null : ContentType.get(entity).getMimeType();
			this.requestAcceptHeader = request.getFirstHeader("Accept").getValue();
			return null;
		}

		@Override
		public String getSupportedContentType() {
			return "content-type/supported";
		}

		public String getRequestMethod() {
			return this.requestMethod;
		}

		public String getRequestContentType() {
			return this.requestContentType;
		}

		public String getRequestAcceptHeader() {
			return this.requestAcceptHeader;
		}
	}
}
