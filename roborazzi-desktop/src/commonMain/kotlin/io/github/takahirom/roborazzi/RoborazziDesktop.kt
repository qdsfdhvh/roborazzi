package io.github.takahirom.roborazzi

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.test.DesktopComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runDesktopComposeUiTest
import com.github.takahirom.roborazzi.*
import java.awt.image.BufferedImage
import java.io.File

@OptIn(ExperimentalTestApi::class)
fun captureRoboImage(
  filePath: String = DefaultFileNameGenerator.generateFilePath("png"),
  roborazziOptions: RoborazziOptions = provideRoborazziContext().options,
  test: DesktopComposeUiTest.() -> Unit = {},
  content: @Composable () -> Unit,
) {
  captureRoboImage(
    file = File(filePath),
    test = test,
    content = content
  )
}

fun ImageBitmap.captureRoboImage(
  filePath: String = DefaultFileNameGenerator.generateFilePath("png"),
  roborazziOptions: RoborazziOptions = provideRoborazziContext().options,
) {
  if (!roborazziEnabled()) {
    return
  }
  captureRoboImage(
    file = File(filePath),
    roborazziOptions = roborazziOptions
  )
}

fun ImageBitmap.captureRoboImage(
  file: File,
  roborazziOptions: RoborazziOptions
) {
  if (!roborazziEnabled()) {
    return
  }
  val awtImage = this.toAwtImage()
  val canvas = AwtRoboCanvas(
    width = awtImage.width,
    height = awtImage.height,
    filled = true,
    bufferedImageType = BufferedImage.TYPE_INT_ARGB
  )
  canvas.apply {
    drawImage(awtImage)
  }
  processOutputImageAndReportWithDefaults(
    canvas = canvas,
    goldenFile = file,
    roborazziOptions = roborazziOptions
  )
  canvas.release()
}

fun processOutputImageAndReportWithDefaults(
  canvas: RoboCanvas,
  goldenFile: File,
  roborazziOptions: RoborazziOptions,
) {
  processOutputImageAndReport(
    canvas = canvas,
    goldenFile = goldenFile,
    roborazziOptions = roborazziOptions,
    canvasFactory = { width, height, filled, bufferedImageType ->
      AwtRoboCanvas(
        width = width,
        height = height,
        filled = filled,
        bufferedImageType = bufferedImageType
      )
    },
    canvasFromFile = { file, bufferedImageType ->
      AwtRoboCanvas.load(file, bufferedImageType)
    },
    generateCompareCanvas = { actualCanvas, resizeScale, bufferedImageType ->
      AwtRoboCanvas.generateCompareCanvas(
        goldenCanvas = this as AwtRoboCanvas,
        newCanvas = actualCanvas as AwtRoboCanvas,
        newCanvasResize = resizeScale,
        bufferedImageType = bufferedImageType
      )
    }
  )
}