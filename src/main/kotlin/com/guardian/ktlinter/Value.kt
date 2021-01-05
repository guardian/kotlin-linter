package com.guardian.ktlinter

sealed class Value {
    data class Data<T>(val data: T) : Value()
    data class Error(val message: String) : Value()
}