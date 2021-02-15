package com.theguardian.ktlinter.changerequests.github

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

internal class ParseGitPatchIntoLinesTest {

    @Test
    fun `Verify a simple patch has the correct additions and deletions`() {
        val patch = """
            |@@ -11,7 +11,8 @@ buildscript {
            |         rxandroid_version = '2.1.1'
            |         jackson_version = '2.11.1'
            |         retrofit_version = '2.6.1'
            |-        okhttp_version = '4.7.0'
            |+        okio_version = '2.10.0'
            |+        okhttp_version = '4.9.1'
            |         junit_version = '4.12'
            |         mockito_version = '2.24.0'
            |         mockito_kotlin_version = '2.2.0'
        """.trimMargin()

        val parseGitPatchIntoLines = ParseGitPatchIntoLines()
        val patches = parseGitPatchIntoLines.invoke("test", "88b38ed1e2", patch)
        assertNotNull(patches)
        assertEquals(2, patches.additions.size)
        assertEquals(1, patches.deletions.size)
        assertEquals(6, patches.noChanges.size)
    }

    @Test
    fun `Verify a simple patch has the correct addition lines`() {
        val patch = """
            |@@ -11,7 +11,8 @@ buildscript {
            |         rxandroid_version = '2.1.1'
            |         jackson_version = '2.11.1'
            |         retrofit_version = '2.6.1'
            |-        okhttp_version = '4.7.0'
            |+        okio_version = '2.10.0'
            |+        okhttp_version = '4.9.1'
            |         junit_version = '4.12'
            |         mockito_version = '2.24.0'
            |         mockito_kotlin_version = '2.2.0'
        """.trimMargin()

        val parseGitPatchIntoLines = ParseGitPatchIntoLines()
        val patches = parseGitPatchIntoLines.invoke("test", "88b38ed1e2", patch)
        assertEquals("+        okio_version = '2.10.0'", patches.additions[0].change)
        assertEquals("+        okhttp_version = '4.9.1'", patches.additions[1].change)
    }

    @Test
    fun `Verify a simple patch has the correct deletion lines`() {
        val patch = """
            |@@ -11,7 +11,8 @@ buildscript {
            |         rxandroid_version = '2.1.1'
            |         jackson_version = '2.11.1'
            |         retrofit_version = '2.6.1'
            |-        okhttp_version = '4.7.0'
            |+        okio_version = '2.10.0'
            |+        okhttp_version = '4.9.1'
            |         junit_version = '4.12'
            |         mockito_version = '2.24.0'
            |         mockito_kotlin_version = '2.2.0'
        """.trimMargin()

        val parseGitPatchIntoLines = ParseGitPatchIntoLines()
        val patches = parseGitPatchIntoLines.invoke("test", "88b38ed1e2", patch)
        assertEquals("-        okhttp_version = '4.7.0'", patches.deletions[0].change)
    }
}