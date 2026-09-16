package com.example.maiplan.network

import com.example.maiplan.network.api.TideApi
import com.example.maiplan.network.sync.PreparedTideRequest
import com.example.maiplan.network.sync.TideRequestPreparer
import com.example.maiplan.network.sync.TideSyncRequest
import com.example.maiplan.network.sync.TideSyncResponse
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

data class SuccessfulTideExchange(
    val preparedRequest: PreparedTideRequest,
    val response: TideSyncResponse
)

open class TideExchangeException(message: String) : Exception(message)

class TideHttpException(val statusCode: Int) : TideExchangeException (
    "TIDE exchange failed with HTTP status $statusCode"
)

class TideProtocolException(message: String) : TideExchangeException(message)

class TideExchangeClient(
    private val tideApi: TideApi,
    private val requestPreparer: TideRequestPreparer
) {
    suspend fun exchange(preparedRequest: PreparedTideRequest): SuccessfulTideExchange {
        return try {
            val httpResponse = tideApi.exchange(preparedRequest.request)

            if (!httpResponse.isSuccessful) {
                val statusCode = httpResponse.code()
                httpResponse.errorBody()?.close()

                throw TideHttpException(statusCode)
            }

            val response = httpResponse.body() ?: throw TideProtocolException (
                "Successful TIDE response had no body"
            )

            validateEnvelope(request = preparedRequest.request, response = response)

            SuccessfulTideExchange(preparedRequest = preparedRequest, response = response)
        } catch (exception: CancellationException) {
            releaseAfterFailure(
                preparedRequest = preparedRequest,
                diagnostic = "CANCELLED",
                originalFailure = exception
            )

            throw exception
        } catch (exception: Exception) {
            releaseAfterFailure(
                preparedRequest = preparedRequest,
                diagnostic = exception.toDiagnosticCode(),
                originalFailure = exception
            )

            throw exception
        }
    }

    private fun validateEnvelope(request: TideSyncRequest, response: TideSyncResponse) {
        if (response.tideProtocolVersion != request.tideProtocolVersion) {
            throw TideProtocolException(
                "TIDE protocol version did not match"
            )
        }

        if (response.requestId != request.requestId) {
            throw TideProtocolException(
                "TIDE response request ID did not match"
            )
        }
    }

    private suspend fun releaseAfterFailure(
        preparedRequest: PreparedTideRequest,
        diagnostic: String,
        originalFailure: Throwable
    ) {
        try {
            withContext(NonCancellable) {
                requestPreparer.releaseClaim(preparedRequest = preparedRequest, diagnostic = diagnostic)
            }
        } catch (cleanupFailure: Exception) {
            originalFailure.addSuppressed(cleanupFailure)
        }
    }

    private fun Exception.toDiagnosticCode(): String {
        return when (this) {
            is TideHttpException -> "HTTP_$statusCode"

            is TideProtocolException -> "PROTOCOL_ERROR"

            else -> "TRANSPORT_ERROR"
        }
    }
}
