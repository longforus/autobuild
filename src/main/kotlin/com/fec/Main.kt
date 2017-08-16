package com.fec

import java.io.*

private val appidPrefix = "applicationId \""

/**
 * Created by XQ Yang on 2017/8/15  19:34.
 * Description : 自动替换app 包名 应用名 icon
 */
fun main(args: Array<String>) {
    val config = File("buildConfig.txt")
    if (!config.exists() || config.length() < 1) {
        println("配置文件不存在")
        return
    }
    val configReader = BufferedReader(FileReader(config))

    val icon = File(configReader.readLine())
    if (icon.exists() && icon.length() > 0) {
        try {
            icon.copyTo(File("app/src/main/res/mipmap-xxhdpi/ic_launcher.png"), true)
            println("icon替换完成")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    } else {
        println("icon 不存在")
    }

    disposeAppId(configReader.readLine())

    disposeAppName(configReader.readLine())

}

private fun disposeAppName(appName: String) {
    val oldFile = File("app/src/main/res/values/strings.xml")
    val reader = BufferedReader(InputStreamReader(FileInputStream(oldFile), "utf-8"))
    val byteArrayOutputStream = ByteArrayOutputStream()
    val writer = OutputStreamWriter(byteArrayOutputStream, "utf-8")
    var line = reader.readLine()
    var isOk = false
    while (line != null) {
        if (!isOk && line.contains("<string name=\"app_name\">")) {
            line = "<string name=\"app_name\">$appName</string>"
            println("替换应用名为 : $appName")
            isOk = true
        }
        writer.appendln(line)
        line = reader.readLine()
    }
    reader.close()
    writer.flush()
    val outputStream = FileOutputStream(oldFile)
    outputStream.write(byteArrayOutputStream.toByteArray())
    outputStream.flush()
    outputStream.close()
    writer.close()
    println("应用名处理完成")
}

private fun disposeAppId(appId: String) {
    val oldFile = File("app/build.gradle")
    val reader = BufferedReader(InputStreamReader(FileInputStream(oldFile), "utf-8"))
    val byteArrayOutputStream = ByteArrayOutputStream()
    val writer = OutputStreamWriter(byteArrayOutputStream, "utf-8")
    var line = reader.readLine()
    var isOk = false
    while (line != null) {
        if (!isOk && line.contains(appidPrefix)) {
            line = appidPrefix + appId + "\""
            println("替换包名为 : $line")
            isOk = true
        }
        writer.appendln(line)
        line = reader.readLine()
    }
    reader.close()
    writer.flush()
    val outputStream = FileOutputStream(oldFile)
    outputStream.write(byteArrayOutputStream.toByteArray())
    outputStream.flush()
    outputStream.close()
    writer.close()
    println("包名处理完成")
}