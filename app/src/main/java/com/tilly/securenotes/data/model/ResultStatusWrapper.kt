package com.tilly.securenotes.data.model

// Wrapper class to handle successful and unsuccessful responses through live data
sealed class ResultStatusWrapper<out T>() {
    class Success<out R>(val data: R) : ResultStatusWrapper<R>()
    class Error(val message: String?, val exception: Exception) : ResultStatusWrapper<Nothing>()

}

