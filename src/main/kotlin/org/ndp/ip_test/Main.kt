package org.ndp.ip_test

import org.w3c.dom.NodeList
import java.io.File
import java.lang.Exception
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory
import kotlin.system.exitProcess

object Main {

    private val appStatusDir = File("/tmp/appstatus/")
    private val resultDir = File("/tmp/result/")
    private val resultFile = File("/tmp/result/result")

    init {
        appStatusDir.mkdirs()
        resultDir.mkdirs()
    }

    private fun parseParam(): String {
        val param = File("/tmp/conf/busi.conf")
        return param.readText()
    }

    private fun execute(ips: String) {
        val command = "nmap -sn -n -oX result.xml $ips"
        Runtime.getRuntime().exec(command)
    }

    private fun parseMidResult(): Array<String> {
        val xml = File("./result.xml").readText()
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xml)
        val xPath = XPathFactory.newInstance().newXPath()
        val qNodes = xPath.evaluate("//address/@addr", doc, XPathConstants.NODE) as NodeList
        return Array(qNodes.length) { qNodes.item(it).textContent }
    }

    private fun writeResult(result: Array<String>) {
        resultFile.writeText(result.joinToString(","))
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
        val ips = parseParam()
        // 执行
        try {
            execute(ips)
        } catch (e: Exception) {
            Log.error(e.toString())
            println(e.stackTrace)
            errorEnd(e.toString(), 11)
        }
        // 解析中间文件
        val result: Array<String>
        try {
            result = parseMidResult()
            // 写结果
            writeResult(result)
        } catch (e: Exception) {
            Log.error(e.toString())
            println(e.stackTrace)
            errorEnd(e.toString(), 11)
        }
        // 结束
        successEnd()
    }
}
