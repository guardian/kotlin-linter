package com.guardian.ktlinter.ktlint

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import java.io.FileReader

class ParseKtLintReport(
    private val gson: Gson
) {
    operator fun invoke(reportLocation: String): KtLintReport {
        val reader = JsonReader(FileReader(reportLocation))
        val reports: List<KtLintFileReport> =
            gson.fromJson(reader, object : TypeToken<List<KtLintFileReport?>?>() {}.type)
        return KtLintReport(reports)
    }
}

