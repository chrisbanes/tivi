// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.logging.LogLevel
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

@CacheableTask
abstract class AssetCopyTask : DefaultTask() {
  @get:OutputDirectory
  abstract val outputDirectory: DirectoryProperty

  @get:PathSensitive(PathSensitivity.RELATIVE)
  @get:InputFile
  abstract val inputFile: RegularFileProperty

  @get:Input
  abstract val outputFilename: Property<String>

  @TaskAction
  fun action() {
    val input = inputFile.get().asFile
    val output = outputDirectory.get()
      .file(outputFilename.get())
      .asFile

    logger.log(LogLevel.INFO, "Copying ${input.canonicalPath} to ${output.canonicalPath}")

    input.copyTo(target = output, overwrite = true)
  }
}
