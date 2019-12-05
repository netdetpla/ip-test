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

    private fun parseParam() {
        val param = File("/tmp/conf/busi.conf").readText()
        val input = File("/input_file")
        input.writeText(param.replace(",", "\n"))
    }

    private fun execute() {
        val command = "nmap -sn -n -oX result.xml -iL /input_file"
        val nmap = Runtime.getRuntime().exec(command)
        nmap.waitFor()
    }

    private fun parseMidResult(): Array<String> {
        val xml = File("./result.xml")
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xml)
        val xPath = XPathFactory.newInstance().newXPath()
        val qNodes = xPath.evaluate("//@addr", doc, XPathConstants.NODESET) as NodeList
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
            execute()
            // 解析中间文件
            val result: Array<String> = parseMidResult()
            // 写结果
            writeResult(result)
        } catch (e: Exception) {
            Log.error(e.toString())
            e.printStackTrace()
            errorEnd(e.toString(), 11)
        }
        // 结束
        successEnd()
    }
}
