package com.izontechnology.dcapp.base.common

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.izontechnology.dcapp.data.WrappedResponse
import com.izontechnology.dcapp.utils.DeviceLogs
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

suspend fun <T> makeApiCall(apiCall: suspend () -> Response<WrappedResponse<T>>): Resource<T> {
    return try {
        val response = apiCall.invoke()
        if (response.isSuccessful) {
            val body = response.body() ?: throw IllegalStateException("Response body is null")
            val responseWrapper = body as WrappedResponse<*>
            if (responseWrapper.status == "success") {
                if (responseWrapper.data != null) {
                    val data = responseWrapper.data as? T
                        ?: throw IllegalStateException("Data is null or not of expected type")
                    Resource.Success(responseWrapper.message, data)
                } else {
                    Resource.Success(responseWrapper.message, null)
                }
            } else {
                Resource.Error(responseWrapper.status.toString(), responseWrapper.message)
            }
        } else {
            val errorBody = response.errorBody()?.charStream()
                ?: throw IllegalStateException("Error body is null")
            val type = object : TypeToken<WrappedResponse<T>>() {}.type
            val err: WrappedResponse<T> = Gson().fromJson(errorBody, type)
            Resource.Error(response.code().toString(), err.message)
        }
    } catch (exception: Exception) {
        Log.d("TAG", "makeApiCall: ${exception.message}")
        exception.printStackTrace()
        val errorResponse = getErrorMessage(exception)
        Resource.APIException(errorResponse.statusCode, errorResponse.message)
    }
}

data class ErrorResponse(val statusCode: Int, val message: String)

fun getErrorMessage(exception: Exception): ErrorResponse {
    val statusCode = when (exception) {
        is HttpException -> exception.code()
        is SocketTimeoutException -> 408 // Request Timeout
        else -> 500 // Internal Server Error
    }
    val message = when (exception) {
        is HttpException -> {
            when (exception.code()) {
                400 -> "Sorry, we can't process your request at this time."
                401 -> "Unauthorized"
                403 -> "Sorry, you're not allowed to access this resource."
                404 -> "Sorry, we couldn't find the resource you requested."
                500 -> "Sorry, there's an internal server error. Please try again later."
                else -> "Sorry, something went wrong. Please try again later."
            }
        }
        is SocketTimeoutException -> "Sorry, we couldn't establish a connection with the server. Please try again later."
        is IOException -> "Sorry, there was an issue with the network. Please check your internet connection and try again."
        else -> "Sorry, something went wrong. Please try again later."
    }
    return ErrorResponse(statusCode, message)
}


/** Api base response not same in all apis as up now so temporary created this function.
 * After required response change in backend side then remove it*/
suspend fun <T> makeApiCallNormal(apiCall: suspend () -> Response<T>): Resource<T> {
    return try {
        val response = apiCall.invoke()
        if (response.isSuccessful) {
            val body = response.body() ?: throw IllegalStateException("Response body is null")
            Resource.Success(response.message(), body)
        } else {
            val errorBody = response.errorBody()?.string()
                ?: throw IllegalStateException("Error body is null")
//            val type = object : TypeToken<Response<T>>() {}.type
//            val err: Response<T> = Gson().fromJson(errorBody, type)
            DeviceLogs.e("API call",errorBody)
            Resource.Error(response.code().toString(), errorBody)
        }
    } catch (exception: Exception) {
        val errorResponse = getErrorMessage(exception)
        DeviceLogs.e("API call",errorResponse.message)
        Resource.APIException(errorResponse.statusCode, errorResponse.message)
    }
}
