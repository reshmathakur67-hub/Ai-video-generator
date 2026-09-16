package com.example

import com.example.model.AspectRatioOption
import com.example.model.CameraMotionOption
import com.example.model.DurationOption
import com.example.model.GenerationMode
import com.example.model.JobStatus
import com.example.model.VideoStyleOption
import com.example.service.PromptEnhancerService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testVideoOptions() {
    assertEquals(3, GenerationMode.values().size)
    assertEquals("9:16", AspectRatioOption.PORTRAIT_9_16.label)
    assertEquals(30, DurationOption.THIRTY_SEC.seconds)
    assertEquals("Cinematic", VideoStyleOption.CINEMATIC.title)
  }

  @Test
  fun testPromptEnhancerSynthesis() = runBlocking {
    val service = PromptEnhancerService()
    val enhanced = service.enhancePrompt(
      "A cute 3D cartoon elephant walking through village",
      VideoStyleOption.CINEMATIC,
      CameraMotionOption.DOLLY_IN
    )
    assertNotNull(enhanced)
    assertTrue(enhanced.contains("elephant"))
    assertTrue(enhanced.contains("lighting") || enhanced.contains("camera") || enhanced.contains("dolly"))
  }
}

