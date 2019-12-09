package org.ndp.ip_test

import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.StandardCharsets
import kotlin.system.exitProcess


object Main {

    private val appStatusDir = File("/tmp/appstatus/")
    private val resultDir = File("/tmp/result/")
    private val resultFile = File("/tmp/result/result")

    init {
        appStatusDir.mkdirs()
        resultDir.mkdirs()
    }

    private fun parseParam() {
        val param = File("/tmp/conf/busi.conf").readText()
        val input = File("/input_file")
        Log.debug("params: ")
        Log.debug(param)
        val ips = ArrayList<String>()
        for (i in param.split(",")) {
            when {
                i.contains('-') -> ips.addAll(Utils.splitINetSegment(i))
                i.contains('/') -> ips.addAll(Utils.splitMaskedINet(i))
                else -> ips.add(i)
            }
        }
        input.writeText(ips.joinToString("\n"))
    }

    private fun execute(): String {
        Log.info("ping start")
        val fpingBuilder = ProcessBuilder(
                ("fping -b 64 -f /input_file -a -q").split(" ")
        )
        val fping = fpingBuilder.start()
        fping.waitFor()
        val result = ByteArrayOutputStream()
        val buffer = ByteArray(fping.inputStream.available())
        var length: Int
        while (fping.inputStream.read(buffer).also { length = it } != -1) {
            result.write(buffer, 0, length)
        }
        Log.info("ping end")
        return result.toString(StandardCharsets.UTF_8.name())
    }

    private fun writeResult(result: String) {
        val resultStr = result.replace('\n', ',')
                .removeRange(result.length -1, result.length)
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
            val result = execute()
            // 写结果
            writeResult(result)
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
