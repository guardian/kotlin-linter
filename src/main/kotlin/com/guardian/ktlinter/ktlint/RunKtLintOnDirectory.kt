package com.guardian.ktlinter.ktlint

import com.guardian.ktlinter.github.PullRequest
import java.io.File
import java.util.concurrent.TimeUnit

class RunKtLintOnDirectory {
    /**
     * @return the name of the report once created
     */
    operator fun invoke(pullRequest: PullRequest, directory: String, reportDirectory: String): String {
        val reportName = "$reportDirectory/${pullRequest.number}/ktlint-${pullRequest.number}.json"
        // Thank you stackoverflow 🙏 https://stackoverflow.com/questions/35421699/how-to-invoke-external-command-from-within-kotlin-code
        // Have disabled indent rules for the time being. Our continuation indents aren't correct
        //--reporter=plain?group_by_file
        "./ktlint $directory${pullRequest.number} --verbose --reporter=json,output=$reportName --disabled_rules=indent".runCommand(
            File("./")
        )
        return reportName
    }


    private fun String.runCommand(workingDir: File) {
        ProcessBuilder(*split(" ").toTypedArray())
            .directory(workingDir)
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start()
            .waitFor(60, TimeUnit.MINUTES)
    }

}