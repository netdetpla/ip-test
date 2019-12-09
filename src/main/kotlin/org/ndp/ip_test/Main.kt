package org.ndp.ip_test

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.StandardCharsets
import kotlin.system.exitProcess


object Main {

    private val appStatusDir = File("/tmp/appstatus/")
    private val resultDir = File("/tmp/result/")
    private val resultFile = File("/tmp/result/result")
    private lateinit var ips: List<String>
    private val result = ArrayList<String>()

    init {
        appStatusDir.mkdirs()
        resultDir.mkdirs()
    }

    private fun parseParam() {
        val param = File("/tmp/conf/busi.conf").readText()
        Log.debug("params: ")
        Log.debug(param)
        ips = param.split(",")
    }

    private fun ping(target: String): Boolean {
        val pingBuilder = ProcessBuilder(
                ("ping -c 4 -W 2 $target").split(" ")
        )
        val ping = pingBuilder.start()
        ping.waitFor()
        val result = ByteArrayOutputStream()
        val buffer = ByteArray(ping.inputStream.available())
        var length: Int
        while (ping.inputStream.read(buffer).also { length = it } != -1) {
            result.write(buffer, 0, length)
        }
        val str: String = result.toString(StandardCharsets.UTF_8.name())
        return !str.contains("100% packet loss,")
    }

    private fun execute() = runBlocking {
        Log.info("ping start")
        val coroutineSet = HashMap<String,Deferred<Boolean>>()
        for (i in ips) {
            coroutineSet[i] = async { ping(i) }
        }
        for (i in ips) {
            if (coroutineSet[i]!!.await())
                result.add(i)
        }
        Log.info("ping end")
    }

    private fun writeResult() {
        val resultStr = result.joinToString(",")
        Log.debug("result: ")
        Log.debug(resultStr)
        Log.info("writing result file")
        resultFile.writeText(resultStr)
    }

    private fun successEnd() {
        val successFile = File("/tmp/appstatus/0")
        successFile.writeText("")
    }

    private fun errorEnd(message: String, code: Int) {
        val errorFile = File("/tmp/appstatus/1")
        errorFile.writeText(message)
        exitProcess(code)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        Log.info("ip-test start")
        // 获取配置
        parseParam()
        // 执行
        try {
            execute()
            // 写结果
            writeResult()
        } catch (e: Exception) {
            Log.error(e.toString())
            e.printStackTrace()
            errorEnd(e.toString(), 11)
        }
        // 结束
        successEnd()
        Log.info("ip-test end successfully")
    }
}
