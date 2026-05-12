package com.avox.launcher

import android.content.Context
import android.content.pm.PackageManager
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class IconPackResolverTest {

    @Test
    fun resolve_returnsNullWhenNoPackLoaded() {
        val context = mock<Context>()
        val resolver = IconPackResolver(context)

        assertNull(resolver.resolve("com.example.app"))
    }

    @Test
    fun load_returnsFalseOnException() {
        val context = mock<Context>()
        val pm = mock<PackageManager>()
        whenever(context.packageManager).thenReturn(pm)
        whenever(pm.getResourcesForApplication(any<String>())).thenThrow(PackageManager.NameNotFoundException())

        val resolver = IconPackResolver(context)
        val result = resolver.load("com.icon.pack")

        assertFalse(result)
    }
}
