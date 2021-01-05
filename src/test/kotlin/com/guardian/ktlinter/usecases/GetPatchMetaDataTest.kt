package com.guardian.ktlinter.usecases

import junit.framework.Assert.assertEquals
import org.junit.Test

class GetPatchMetaDataTest {

    @Test
    fun `Test that simple patch is correctly parsed`() {
        val examplePatchLine = "@@ -143,7 +143,7 @@ class HomeActivity : BaseActivity(), ContentScreenLauncher,"
        val pair = GetPatchMetaData().invoke(examplePatchLine)
        assertEquals(143, pair.patchStartLine)
        assertEquals(7, pair.patchLength)
    }
}