package com.rootbot

import java.io.DataOutputStream

object RootHelper {
    fun exec(cmd: String): String {
        try {
            val p = Runtime.getRuntime().exec("su")
            val os = DataOutputStream(p.outputStream)
            os.writeBytes("$cmd\n")
            os.writeBytes("exit\n")
            os.flush()
            val out = p.inputStream.bufferedReader().readText()
            p.waitFor()
            return out
        } catch (e: Exception) {
            return "ERROR: ${e.message}"
        }
    }

    fun isRooted(): Boolean {
        return exec("id").contains("uid=0")
    }

    fun captureScreen(path: String): Boolean {
        exec("screencap -p $path")
        return true
    }

    fun tap(x: Int, y: Int) {
        exec("input tap $x $y")
    }

    fun swipe(x1: Int, y1: Int, x2: Int, y2: Int, ms: Int = 300) {
        exec("input swipe $x1 $y1 $x2 $y2 $ms")
    }

    fun pressBack() {
        exec("input keyevent 4")
    }

    fun pressHome() {
        exec("input keyevent 3")
    }

    fun launchApp(pkg: String, act: String): Boolean {
        return exec("am start -n $pkg/$act").contains("Starting")
    }

    fun isAppRunning(pkg: String): Boolean {
        return exec("pidof $pkg").trim().isNotEmpty()
    }
}
