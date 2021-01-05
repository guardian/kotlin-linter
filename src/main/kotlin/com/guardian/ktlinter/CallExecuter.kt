package com.guardian.ktlinter

import retrofit2.Call

fun <T> callExecutor(call: Call<T>): Value {
    val response = call.execute()
    if (response.isSuccessful) {
        val data = response.body()
        return if (data != null) {
            Value.Data(data)
        } else {
            Value.Error("Request was successful, however, we have no response body.")
        }
    } else {
        val errorBody = response.errorBody()
        return if (errorBody != null) {
            Value.Error(
                "There has been an error:\n" +
                    "HTTP Code: ${response.code()}\n" +
                    "HTTP Body: $errorBody"
            )
        } else {
            Value.Error(
                "There has been an error, but no body has been delivered:\n" +
                    "HTTP Code: ${response.code()}\n" +
                    "HTTP Body: Not returned"
            )
        }
    }
}