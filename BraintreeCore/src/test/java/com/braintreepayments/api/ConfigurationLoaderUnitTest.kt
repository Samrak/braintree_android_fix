package com.braintreepayments.api

import android.util.Base64
import io.mockk.*
import org.robolectric.RobolectricTestRunner
import org.json.JSONException
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import org.skyscreamer.jsonassert.JSONAssert
import java.lang.Exception

@RunWith(RobolectricTestRunner::class)
class ConfigurationLoaderUnitTest {
    private var configurationCache: ConfigurationCache = mockk(relaxed = true)
    private var braintreeHttpClient: BraintreeHttpClient = mockk(relaxed = true)
    private var callback: ConfigurationLoaderCallback = mockk(relaxed = true)
    private var authorization: Authorization = mockk(relaxed = true)

    @Test
    fun loadConfiguration_loadsConfigurationForTheCurrentEnvironment() {

        every{ authorization.configUrl } returns "https://example.com/config"

        val sut = ConfigurationLoader(braintreeHttpClient, configurationCache)
        sut.loadConfiguration(authorization, callback)

        val expectedConfigUrl = "https://example.com/config?configVersion=3"
        val callbackSlot = slot<HttpResponseCallback>()
        verify {
            braintreeHttpClient[
                    expectedConfigUrl,
                    null,
                    authorization,
                    HttpClient.RETRY_MAX_3_TIMES,
                    capture(callbackSlot)
            ]
        }

        val httpResponseCallback = callbackSlot.captured
        httpResponseCallback.onResult(Fixtures.CONFIGURATION_WITH_ACCESS_TOKEN, null)

        verify { callback.onResult(ofType(ConfigurationLoaderResult::class), null) }
    }

    @Test
    fun loadConfiguration_savesFetchedConfigurationToCache() {
        every { authorization.configUrl } returns "https://example.com/config"
        every { authorization.bearer } returns "bearer"

        val sut = ConfigurationLoader(braintreeHttpClient, configurationCache)
        sut.loadConfiguration(authorization, callback)

        val expectedConfigUrl = "https://example.com/config?configVersion=3"
        val callbackSlot = slot<HttpResponseCallback>()
        verify {
            braintreeHttpClient[
                    expectedConfigUrl,
                    null,
                    authorization,
                    HttpClient.RETRY_MAX_3_TIMES,
                    capture(callbackSlot)
            ]
        }

        val httpResponseCallback = callbackSlot.captured
        httpResponseCallback.onResult(Fixtures.CONFIGURATION_WITH_ACCESS_TOKEN, null)
        val cacheKey = Base64.encodeToString(
            "https://example.com/config?configVersion=3bearer".toByteArray(),
            0
        )

        verify {
            configurationCache.saveConfiguration(ofType(Configuration::class), cacheKey)
        }
    }

    @Test
    fun loadConfiguration_onJSONParsingError_forwardsExceptionToErrorResponseListener() {
        every { authorization.configUrl } returns "https://example.com/config"
        val sut = ConfigurationLoader(braintreeHttpClient, configurationCache)
        sut.loadConfiguration(authorization, callback)

        val callbackSlot = slot<HttpResponseCallback>()
        verify {
            braintreeHttpClient[
                    ofType(String::class),
                    null,
                    authorization,
                    HttpClient.RETRY_MAX_3_TIMES,
                    capture(callbackSlot)
            ]
        }
        val httpResponseCallback = callbackSlot.captured
        httpResponseCallback.onResult("not json", null)
        verify {
            callback.onResult(null, ofType(JSONException::class))
        }
    }

    @Test
    fun loadConfiguration_onHttpError_forwardsExceptionToErrorResponseListener() {
        every { authorization.configUrl } returns "https://example.com/config"
        val sut = ConfigurationLoader(braintreeHttpClient, configurationCache)
        sut.loadConfiguration(authorization, callback)

        val callbackSlot = slot<HttpResponseCallback>()

        verify {
            braintreeHttpClient[
                    ofType(String::class),
                    null,
                    authorization,
                    HttpClient.RETRY_MAX_3_TIMES,
                    capture(callbackSlot)
            ]
        }

        val httpResponseCallback = callbackSlot.captured
        val httpError = Exception("http error")
        httpResponseCallback.onResult(null, httpError)
        val errorSlot = slot<Exception>()
        verify {
            callback.onResult(null, capture(errorSlot))
        }

        val error = errorSlot.captured as ConfigurationException
        assertEquals(
            "Request for configuration has failed: http error",
            error.message
        )
    }

    @Test
    fun loadConfiguration_whenInvalidToken_forwardsExceptionToCallback() {
        val authorization: Authorization = InvalidAuthorization("invalid", "token invalid")
        val sut = ConfigurationLoader(braintreeHttpClient, configurationCache)
        sut.loadConfiguration(authorization, callback)
        val errorSlot = slot<BraintreeException>()
        verify {
            callback.onResult(null, capture(errorSlot))
        }

        val exception = errorSlot.captured
        assertEquals("token invalid", exception.message)
    }

    @Test
    fun loadConfiguration_whenCachedConfigurationAvailable_loadsConfigurationFromCache() {
        val cacheKey = Base64.encodeToString(
            "https://example.com/config?configVersion=3bearer".toByteArray(),
            0
        )
        every { authorization.configUrl } returns "https://example.com/config"
        every { authorization.bearer } returns "bearer"
        every { configurationCache.getConfiguration(cacheKey) } returns Fixtures.CONFIGURATION_WITH_ACCESS_TOKEN

        val sut = ConfigurationLoader(braintreeHttpClient, configurationCache)
        sut.loadConfiguration(authorization, callback)

        verify(exactly = 0) {
            braintreeHttpClient[
                    ofType(String::class),
                    null,
                    authorization,
                    ofType(Int::class),
                    ofType(HttpResponseCallback::class)
            ]
        }
        verify { callback.onResult(ofType(ConfigurationLoaderResult::class), null) }
    }

    @Test
    fun loadConfiguration_forwardsConfigurationCacheErrors() {
        every { authorization.configUrl } returns "https://example.com/config"
        every { authorization.bearer } returns "bearer"

        val cacheLoadError = BraintreeSharedPreferencesException("cache load error")
        every { configurationCache.getConfiguration(ofType(String::class)) } throws cacheLoadError

        val cacheSaveError = BraintreeSharedPreferencesException("cache save error")
        every { configurationCache.saveConfiguration(ofType(Configuration::class), ofType(String::class)) } throws cacheSaveError

        val sut = ConfigurationLoader(braintreeHttpClient, configurationCache)
        sut.loadConfiguration(authorization, callback)

        val expectedConfigUrl = "https://example.com/config?configVersion=3"
        val callbackSlot = slot<HttpResponseCallback>()
        verify {
            braintreeHttpClient[
                    expectedConfigUrl,
                    null,
                    authorization,
                    HttpClient.RETRY_MAX_3_TIMES,
                    capture(callbackSlot)
            ]
        }

        val httpResponseCallback = callbackSlot.captured
        httpResponseCallback.onResult(Fixtures.CONFIGURATION_WITH_ACCESS_TOKEN, null)

        val resultSlot = slot<ConfigurationLoaderResult>()
        verify {
            callback.onResult(capture(resultSlot), null)
        }

        val result = resultSlot.captured
        val expectedConfig = JSONObject(Fixtures.CONFIGURATION_WITH_ACCESS_TOKEN)
        val actualConfig = JSONObject(result.configuration.toJson())
        JSONAssert.assertEquals(expectedConfig, actualConfig, true)
        assertSame(cacheLoadError, result.loadFromCacheError)
        assertSame(cacheSaveError, result.saveToCacheError)
    }
}