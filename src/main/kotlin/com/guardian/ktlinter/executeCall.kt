package com.guardian.ktlinter

import com.guardian.ktlinter.models.Value
import retrofit2.Call

fun <T> executeCall(call: Call<T>): Value {
    val response = call.execute()
    return if (response.isSuccessful) {
        val data = response.body()
        if (data != null) {
            Value.Data(data)
        } else {
            Value.Error("Request was successful, however, we have no response body.")
        }
    } else {
        Value.Error(
            "There has been an error:\n" +
                "HTTP Code: ${response.code()}\n" +
                "HTTP Body: ${response.errorBody()}"
        )
    }
}