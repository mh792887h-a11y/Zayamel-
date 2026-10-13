package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AdminUser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("زوامل اليمن", appName)
    }

    @Test
    fun `verify authorized admin email rule`() {
        assertTrue(AdminUser.isAuthorizedAdmin("m91857687@gmail.com"))
        assertTrue(AdminUser.isAuthorizedAdmin("M91857687@GMAIL.COM"))
        assertFalse(AdminUser.isAuthorizedAdmin("other.user@gmail.com"))
        assertFalse(AdminUser.isAuthorizedAdmin(null))
    }
}
