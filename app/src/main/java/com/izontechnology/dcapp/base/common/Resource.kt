package com.izontechnology.dcapp.base.common

sealed class Resource<out T> {
    data class Success<out T>(val message: String?, val data: T?) : Resource<T>()
    data class Error(val status: String?, val message: String?) : Resource<Nothing>()
    data class APIException(val status: Int?, val message: String?) : Resource<Nothing>()
    object Loading : Resource<Nothing>()
    object Idle : Resource<Nothing>()
}