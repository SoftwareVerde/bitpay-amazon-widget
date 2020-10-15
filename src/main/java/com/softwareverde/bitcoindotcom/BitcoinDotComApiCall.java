package com.softwareverde.bitcoindotcom;

import com.softwareverde.api.ApiCall;
import com.softwareverde.api.ApiConfiguration;
import com.softwareverde.api.ApiRequest;
import com.softwareverde.api.ApiResponse;
import com.softwareverde.http.HttpMethod;
import com.softwareverde.http.HttpResponse;
import com.softwareverde.logging.Logger;
import com.softwareverde.util.Util;

import java.util.List;
import java.util.Map;

public abstract class BitcoinDotComApiCall<REQUEST extends ApiRequest, RESPONSE extends ApiResponse> extends ApiCall<REQUEST, RESPONSE> {
    private static Integer _rateLimitTotal = null;
    private static Integer _rateLimitRemaining = null;
    private static Long _rateLimitResetTimestamp = null;
    private static int _callsInProgress = 0;

    public BitcoinDotComApiCall(final ApiConfiguration configuration) {
        super(configuration);
    }

    @Override
    protected HttpResponse _call(final String requestPath, final HttpMethod requestMethod, final REQUEST request) throws Exception {
        _checkAndWaitForRateLimit();
        final HttpResponse httpResponse = super._call(requestPath, requestMethod, request);
        _updateRateLimitInformation(httpResponse);
        return httpResponse;
    }

    protected static synchronized void _checkAndWaitForRateLimit() {
        final long targetMillisPerRequest = _getTargetMillisecondsPerRequest();

        if (targetMillisPerRequest > 1000) {
            try { Thread.sleep(targetMillisPerRequest); } catch (InterruptedException ignored) {}
        }

        _callsInProgress++;
    }

    protected static synchronized long _getTargetMillisecondsPerRequest() {
        if (_rateLimitTotal != null && _rateLimitRemaining != null && _rateLimitResetTimestamp != null) {
            final long millisUntilRateLimitReset = (_rateLimitResetTimestamp * 1000) - System.currentTimeMillis();
            final long trueRateLimitRemaining = Math.max(1L, _rateLimitRemaining - _callsInProgress);
            final long target = Math.max(0L, (long) Math.ceil(((double) millisUntilRateLimitReset) / trueRateLimitRemaining));
            return target;
        }
        return 0;
    }

    protected static synchronized void _updateRateLimitInformation(final HttpResponse httpResponse) {
        _callsInProgress--;

        final Integer lastKnownRateLimitRemaining = _rateLimitRemaining;

        final Map<String, List<String>> headers = httpResponse.getHeaders();
        final String rateLimitTotalHeader = _getHeader(headers, "x-ratelimit-limit");
        final String rateLimitRemainingHeader = _getHeader(headers, "x-ratelimit-remaining");
        final String rateLimitResetTimestampHeader = _getHeader(headers, "x-ratelimit-reset");

        _rateLimitTotal = Util.parseInt(rateLimitTotalHeader, _rateLimitTotal);
        _rateLimitRemaining = Util.parseInt(rateLimitRemainingHeader, _rateLimitRemaining);
        _rateLimitResetTimestamp = Util.parseLong(rateLimitResetTimestampHeader, _rateLimitResetTimestamp);


        if (_rateLimitRemaining != null && _rateLimitRemaining == 0 && (lastKnownRateLimitRemaining == null || lastKnownRateLimitRemaining != 0)) {
            Logger.warn("bitcoin.com rate limit reached, will reset at " + _rateLimitResetTimestamp);
        }
    }

    protected static String _getHeader(final Map<String, List<String>> headers, final String headerName) {
        final List<String> headerValues = headers.get(headerName);
        if (headerValues == null || headerValues.size() == 0) {
            return null;
        }
        return headerValues.get(0);
    }
}
