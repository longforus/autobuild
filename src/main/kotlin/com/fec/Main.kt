package com.fec

import java.io.*

private val APPID_PREFIX = "applicationId \""
private val JPUSH_APPKEY_PREFIX = "JPUSH_APPKEY : \""
private val BAIDU_KEY_PREFIX = "android:name=\"com.baidu.lbsapi.API_KEY\""
private var disposeAppGradle = false
private var disposeAppManifest = false
private val BAIDU_MAP_KEY = "baiduMapKey"
private val JPUSH_KEY = "jpushKey"

/**
 * Created by XQ Yang on 2017/8/15  19:34.
 * Description : 自动替换app 包名 应用名 icon
 */
fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("请传入自动配置文件路径")
        return
    }
    val config = File(args[0])
    if (!config.exists() || config.length() < 1) {
        println("自动配置文件不存在")
        return
    }
    println("定制开始 : ")
    val configReader = BufferedReader(FileReader(config))
    var line = configReader.readLine()
    val configMap  = HashMap<String,String>()
    while (line != null) {
        val split = line.split(":")
        configMap.put(split[0],split[1])
        line = configReader.readLine()
    }
    configReader.close()
    if (configMap.size > 0) {
        for ((key, value) in configMap) {
            when (key) {
                "icon"->disposeIcon(value)
                "appName"->disposeAppName(value)
                "appId", JPUSH_KEY -> disposeAppGradle(value,configMap)
                BAIDU_MAP_KEY -> disposeManifest(configMap)
            }
        }
    }
    println("定制完成,开始打包...")
}


private fun disposeIcon(iconPath: String) {
    val icon = File(iconPath)
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
    saveAndClose(reader, writer, oldFile, byteArrayOutputStream)
    println("应用名处理完成")
}

private fun disposeAppGradle(appId: String, configMap: HashMap<String, String>) {
    if (disposeAppGradle) {
        return
    }
    disposeAppGradle = true
    val oldFile = File("app/build.gradle")
    val reader = BufferedReader(InputStreamReader(FileInputStream(oldFile), "utf-8"))
    val byteArrayOutputStream = ByteArrayOutputStream()
    val writer = OutputStreamWriter(byteArrayOutputStream, "utf-8")
    var line = reader.readLine()
    var appIdisOk = false
    var jpushisOk = false
    while (line != null) {
        if (!appIdisOk && line.contains(APPID_PREFIX)) {
            line = APPID_PREFIX + appId + "\""
            println("替换包名为 : $appId")
            appIdisOk = true
        }
        if (!jpushisOk &&configMap.containsKey(JPUSH_KEY) && line.contains(JPUSH_APPKEY_PREFIX)) {
            val value = configMap.get(JPUSH_KEY)
            line = JPUSH_APPKEY_PREFIX + value + "\","
            println("替换极光key为 : $value")
            jpushisOk = true
        }
        writer.appendln(line)
        line = reader.readLine()
    }
    saveAndClose(reader, writer, oldFile, byteArrayOutputStream)
    println("app gradle处理完成")
}
private fun disposeManifest(configMap: HashMap<String, String>) {
    if (disposeAppManifest) {
        return
    }
    disposeAppManifest = true
    val oldFile = File("app/src/main/AndroidManifest.xml")
    val reader = BufferedReader(InputStreamReader(FileInputStream(oldFile), "utf-8"))
    val byteArrayOutputStream = ByteArrayOutputStream()
    val writer = OutputStreamWriter(byteArrayOutputStream, "utf-8")
    var line = reader.readLine()
    var baiduIsOk = false
    var replaceOK = false
    while (line != null) {
        if (!baiduIsOk &&configMap.containsKey(BAIDU_MAP_KEY)&& line.contains(BAIDU_KEY_PREFIX)) {
            baiduIsOk = true
        }else if (baiduIsOk&&!replaceOK) {
            val bdMapKey = configMap.get(BAIDU_MAP_KEY)
            line = "android:value=\"$bdMapKey\" />"
            println("百度地图key修改为:$bdMapKey")
            replaceOK = true
        }
        writer.appendln(line)
        line = reader.readLine()
    }
    saveAndClose(reader, writer, oldFile, byteArrayOutputStream)
    println("appManifest 处理完成")
}

private fun saveAndClose(reader: BufferedReader, writer: OutputStreamWriter, oldFile: File, byteArrayOutputStream: ByteArrayOutputStream) {
    reader.close()
    writer.flush()
    val outputStream = FileOutputStream(oldFile)
    outputStream.write(byteArrayOutputStream.toByteArray())
    outputStream.flush()
    outputStream.close()
    writer.close()
}