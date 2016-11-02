package org.moallemi.gradle.internal

import groovy.text.SimpleTemplateEngine
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.moallemi.gradle.AdvancedBuildVersionPlugin

class FileOutputOptions {

    String nameFormat

    boolean renameOutput = false

    private def templateEngine = new SimpleTemplateEngine()

    void renameOutput(boolean b) {
        renameOutput = b
    }

    void nameFormat(String format) {
        nameFormat = format
    }

    def generateOutputName(Project project, variant) {
        def map = [
                'appName'    : project.name,
                'projectName': project.rootProject.name,
                'flavorName' : variant.flavorName,
                'buildType'  : variant.buildType.name,
                'versionName': variant.versionName,
                'versionCode': variant.versionCode
        ]

        def defaultTemplate = !variant.flavorName.equals("") && variant.flavorName != null ?
                '$appName-$flavorName-$buildType-$versionName' : '$appName-$buildType-$versionName'
        def template = nameFormat == null ? defaultTemplate : nameFormat
        def fileName = templateEngine.createTemplate(template).make(map).toString();

        variant.outputs.each { output ->
            def outputFile = output.outputFile
            if (outputFile != null && outputFile.name.endsWith('.apk')) {
                if (variant.buildType.zipAlignEnabled) {
                    output.outputFile = new File(outputFile.parent, fileName + ".apk")
                }
                def unaligned = output.packageApplication.outputFile;
                if (unaligned.getName().contains("unaligned")) {
                    output.packageApplication.outputFile = new File(outputFile.parent, fileName + "-unaligned.apk")
                }
            }
        }
    }
}
