package com.periculum.internal

import android.telephony.PhoneNumberUtils
import androidx.core.text.isDigitsOnly
import com.periculum.internal.models.AffordabilityModel
import com.periculum.internal.repository.AffordabilityRepository
import com.periculum.internal.repository.AnalyticsRepository
import com.periculum.internal.utils.Utils
import com.periculum.models.Response
import com.periculum.models.ErrorType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


internal class PericulumManager {

    suspend fun startAnalytics(phoneNumber: String, bvn: String, token: String): Response =
        withContext(Dispatchers.IO) {
            try {
                if (bvn.length != 10 || !bvn.isDigitsOnly()) {
                    Response(
                        message = "Invalid BVN Number",
                        responseBody = null,
                        isError = true,
                        errorType = ErrorType.InvalidData
                    )
                } else if (token.isEmpty()) {
                    Response(
                        message = "Invalid token",
                        responseBody = null,
                        isError = true,
                        errorType = ErrorType.InvalidToken
                    )
                } else if (phoneNumber.isEmpty() || !PhoneNumberUtils.isGlobalPhoneNumber(
                        phoneNumber
                    )
                ) {
                    Response(
                        message = "Invalid Phone Number",
                        responseBody = null,
                        isError = true,
                        errorType = ErrorType.InvalidData
                    )
                } else if (!Utils().isInternetConnected()) {
                    Response(
                        message = "Internet Connection Required",
                        responseBody = null,
                        isError = true,
                        errorType = ErrorType.InternetConnectionError
                    )
                } else if (!Utils().isSmsPermissionGranted()) {
                    Response(
                        message = "Sms Permission Required",
                        responseBody = null,
                        isError = true,
                        errorType = ErrorType.SmsPermissionError
                    )
                } else if (!Utils().isLocationPermissionGranted()) {
                    Response(
                        message = "Access Location Permission Required",
                        responseBody = null,
                        isError = true,
                        errorType = ErrorType.LocationPermissionError
                    )
                } else if (!Utils().isLocationEnabled()) {
                    Response(
                        message = "Location not enabled.",
                        responseBody = null,
                        isError = true,
                        errorType = ErrorType.LocationNotEnabledError
                    )
                } else {
                    val analyticsResponse = AnalyticsRepository()
                        .postAnalyticsDataToServer(phoneNumber, bvn, token = token)
                    if (analyticsResponse.isError) {
                        Response(
                            message = analyticsResponse.message,
                            isError = true,
                            errorType = analyticsResponse.errorType,
                            responseBody = null
                        )
                    } else {
                        Response(
                            message = "Success",
                            isError = false,
                            responseBody = analyticsResponse.response
                        )
                    }
                }
            } catch (e: Exception) {
                if (!Utils().isInternetConnected()) {
                    Response(
                        message = "Internet Connection Required",
                        isError = true,
                        errorType = ErrorType.InternetConnectionError,
                        responseBody = null
                    )
                } else if (!Utils().isLocationEnabled()) {
                    Response(
                        message = "Location not enabled.",
                        isError = true,
                        errorType = ErrorType.LocationNotEnabledError,
                        responseBody = null
                    )
                } else {
                    Response(
                        message = "Error Occurred",
                        isError = true,
                        errorType = ErrorType.UnknownError,
                        responseBody = null
                    )
                }
            }
        }

    suspend fun startAffordability(dti: Double, loanTenure: Int, statementKey: Int, token: String): Response =
        withContext(Dispatchers.IO) {
            try {
                if (token.isEmpty()) {
                    Response(
                        message = "Invalid token",
                        responseBody = null,
                        isError = true,
                        errorType = ErrorType.InvalidToken
                    )
                } else if (!Utils().isInternetConnected()) {
                    Response(
                        message = "Internet Connection Required",
                        responseBody = null,
                        isError = true,
                        errorType = ErrorType.InternetConnectionError
                    )
                } else {
                    val affordabilityResponse =
                        AffordabilityRepository().postAffordabilityDataToServer(
                            AffordabilityModel(
                                dti = dti,
                                loanTenure = loanTenure,
                                statementKey = statementKey
                            ),
                            token = token
                        )
                    if (!affordabilityResponse.isError) {
                        Response(
                            message = "Success",
                            isError = false,
                            responseBody = affordabilityResponse.responseBody
                        )
                    } else {
                        Response(
                            message = affordabilityResponse.responseBody,
                            isError = true,
                            errorType = affordabilityResponse.errorType,
                            responseBody = null
                        )
                    }
                }
            } catch (e: Exception) {
                if (!Utils().isInternetConnected()) {
                    Response(
                        message = "Internet Connection Required",
                        isError = true,
                        errorType = ErrorType.InternetConnectionError,
                        responseBody = null
                    )
                } else if (!Utils().isLocationEnabled()) {
                    Response(
                        message = "Location not enabled.",
                        isError = true,
                        errorType = ErrorType.LocationNotEnabledError,
                        responseBody = null
                    )
                } else {
                    Response(
                        message = "Error Occurred",
                        isError = true,
                        errorType = ErrorType.UnknownError,
                        responseBody = null
                    )
                }
            }
        }
}