package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ProjectJsonConverter
import com.example.model.GenrePresets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Live Studio", appName)
    }

    @Test
    fun `default techno project serializes and deserializes properly`() {
        val project = GenrePresets.createTechnoProject()
        val json = ProjectJsonConverter.toJson(project)
        val loaded = ProjectJsonConverter.fromJson(json)

        assertNotNull(loaded)
        assertEquals(project.name, loaded?.name)
        assertEquals(project.bpm, loaded?.bpm)
        assertEquals(project.tracks.size, loaded?.tracks?.size)
    }
}
