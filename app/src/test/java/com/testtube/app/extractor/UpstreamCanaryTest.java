package com.testtube.app.extractor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Request;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.exceptions.SignInConfirmNotBotException;
import org.schabi.newpipe.extractor.localization.Localization;
import org.schabi.newpipe.extractor.stream.StreamInfo;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

/**
 * Upstream breakage canary: extracts two stable public videos with the vendored NewPipe
 * extractor and asserts playable streams exist. If YouTube changes its player API or the
 * extractor rots, this goes red before users report black screens.
 *
 * <p>Runs only in the scheduled {@code upstream-canary} workflow (not in every PR build):
 * it needs real network access to YouTube. Bot checks and rate limits are treated as
 * inconclusive, not failures.
 */
public final class UpstreamCanaryTest {
	/** Blender Foundation's Big Buck Bunny: CC-BY, never restricted. */
	private static final String VIDEO_STABLE_1 = "https://www.youtube.com/watch?v=aqz-KE-bpKQ";
	/** Long-standing unrestricted music video. */
	private static final String VIDEO_STABLE_2 = "https://www.youtube.com/watch?v=dQw4w9WgXcQ";

	@BeforeClass
	public static void initNewPipe() {
		// Only runs in the scheduled upstream-canary workflow (TESTTUBE_CANARY=1).
		// Skipped in normal builds/PRs: it needs real network access to YouTube.
		Assume.assumeTrue("canary only", System.getenv("TESTTUBE_CANARY") != null);
		NewPipe.init(new Downloader() {
			private final OkHttpClient client = new OkHttpClient();

			@Override
			public Response execute(Request request) throws IOException, ReCaptchaException {
				String method = request.httpMethod() != null ? request.httpMethod() : "GET";
				okhttp3.Request.Builder builder = new okhttp3.Request.Builder().url(request.url());
				if (request.headers() != null) {
					for (Map.Entry<String, List<String>> entry : request.headers().entrySet()) {
						for (String value : entry.getValue()) {
							builder.addHeader(entry.getKey(), value);
						}
					}
				}
				byte[] body = request.dataToSend();
				if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) || body != null) {
					builder.method(method, RequestBody.create(body != null ? body : new byte[0],
									(MediaType) null));
				} else {
					builder.method(method, null);
				}
				try (okhttp3.Response response = client.newCall(builder.build()).execute()) {
					String responseBody = response.body() != null ? response.body().string() : "";
					return new Response(response.code(), response.message(),
									response.headers().toMultimap(), responseBody, request.url());
				}
			}
		}, new Localization("en", "US"));
	}

	@Test(timeout = 240_000L)
	public void stableVideosStillExtract() throws Exception {
		checkPlayable(VIDEO_STABLE_1);
		checkPlayable(VIDEO_STABLE_2);
	}

	private static void checkPlayable(String url) throws Exception {
		final StreamInfo info;
		try {
			info = StreamInfo.getInfo(url);
		} catch (ReCaptchaException | SignInConfirmNotBotException e) {
			// Bot check / rate limit from the CI runner: proves nothing about the extractor.
			Assume.assumeNoException("inconclusive, YouTube bot check", e);
			return;
		} catch (ExtractionException e) {
			if (causedByBotCheck(e)) {
				Assume.assumeNoException("inconclusive, YouTube bot check", e);
				return;
			}
			throw e;
		}
		assertTrue("no title for " + url,
						info.getName() != null && !info.getName().isBlank());
		assertFalse("no video streams for " + url, info.getVideoStreams().isEmpty());
	}

	private static boolean causedByBotCheck(Throwable error) {
		Throwable current = error;
		while (current != null) {
			if (current instanceof ReCaptchaException || current instanceof SignInConfirmNotBotException) {
				return true;
			}
			current = current.getCause();
		}
		return false;
	}
}
