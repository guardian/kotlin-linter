package com.guardian.ktlinter.usecases

import com.guardian.ktlinter.git.GetPatchMetaData
import com.guardian.ktlinter.git.ParseGitPatchIntoLines
import com.guardian.ktlinter.git.PatchLine
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import org.junit.Test

class ParseGitPatchIntoLinesTest {

    @Test
    fun `Test that a new file patch returns the correct values`() {

        val patch = "@@ -0,0 +1,4 @@\n" +
                "            +package com.guardian.feature.stream\n" +
                "            +\n" +
                "            +data class TestData(val string: String) {\n" +
                "            +}"
        val parseGitPatchIntoLines =
            ParseGitPatchIntoLines(GetPatchMetaData())
        val output = parseGitPatchIntoLines.invoke("filename", "sha", patch)
        assertEquals(4, output.lines.size)
        assertEquals(4, output.lines.filterIsInstance(PatchLine.Addition::class.java).size)
        assertNotNull(output)
    }

    @Test
    fun `Test that a modification patch returns the correct values`() {

        val patch = "@@ -172,22 +172,23 @@ class HomeActivity : BaseActivity(), ContentScreenLauncher,\n" +
                "     private var currentSectionItem: SectionItem? = null\n" +
                "     private var homePageChangeDetected: Boolean = false\n" +
                " \n" +
                "-    private var statusBarAnimator: ValueAnimator? = null\n" +
                "+    private var statusBarAnimator: ValueAnimator? =\n" +
                "+            null\n" +
                " \n" +
                "     /**\n" +
                "      * Drawer listener responsible for sending analytics events when the navigation drawer has been\n" +
                "      * opened via a swipe gesture.\n" +
                "      */\n" +
                "     private val drawerListener: DrawerLayout.DrawerListener =\n" +
                "-            object : DrawerLayout.SimpleDrawerListener() {\n" +
                "+            object :DrawerLayout.SimpleDrawerListener() {\n" +
                " \n" +
                "                 /**\n" +
                "                  * True if the [DrawerLayout.STATE_DRAGGING] state has happened to this drawer.\n" +
                "                  */\n" +
                "                 private var hasBeenDragged: Boolean = false\n" +
                " \n" +
                "                 override fun onDrawerClosed(drawerView: View) {\n" +
                "-                    if (hasBeenDragged) {\n" +
                "+                    if(hasBeenDragged){\n" +
                "                         homeScreenNavigationContext.menuClosed(HomeScreenStrategy.Interaction.GESTURE)\n" +
                "                         hasBeenDragged = false\n" +
                "                     }"

        val parseGitPatchIntoLines =
            ParseGitPatchIntoLines(GetPatchMetaData())
        val output = parseGitPatchIntoLines.invoke("filename", "sha", patch)
        assertEquals(26, output.lines.size)
        assertEquals(4, output.lines.filterIsInstance(PatchLine.Addition::class.java).size)
    }
}