package org.stockinsight

/**
 * Created by asus on 2015/6/27.
 */

import java.io._
import java.net.{HttpURLConnection, URL}

import org.stockinsight.common.LogSupport

import scala.io.Source


object UrlDownloader extends LogSupport {

  def downloadFile(url:String,filename:String):Unit = {
    val printWriter = new PrintWriter(filename)
    try {
      printWriter.write(Source.fromURL(url,"UTF-8").mkString)
    }
    catch {
      case e:IOException =>  {
        log.error(s"write $filename failure!")
        e.printStackTrace()
      }
    }
    finally {
      printWriter.close()
    }
  }


  def downloadFileJava(url:String,filename:String):Unit = {
    val destUrl = new URL(url)
    val connUrl = destUrl.openConnection().asInstanceOf[HttpURLConnection]
    connUrl.setConnectTimeout(30000)
    connUrl.connect()
    //connUrl.setDoInput(true)
    //val sourceStream = new BufferedInputStream(connUrl.getInputStream)
    val inputStream = connUrl.getInputStream
    //log.debug(inputStream.available().toString)
    //val inputStream = new ByteArrayInputStream(sourceStream.)
    //val printWriter = new PrintWriter(filename)
    val outputStream = new FileOutputStream(filename)
    //var buf = new Array[Byte](4096);
    try {
      //IOUtils.copyBytes(sourceStream, outputStream,sourceStream,4096 , true)
      var bytes = new Array[Byte](40960)
      var len = -1
      while ({ len = inputStream.read(bytes, 0, 40960); len != -1 }) {
        outputStream.write(bytes, 0, len)
      }
    }catch{
      case e:IOException => {
        log.error(s"read from $url,write to $filename failure!")
        e.printStackTrace()
      }
    }
    finally{
      connUrl.disconnect()
      inputStream.close()
      outputStream.close()
    }
  }
}

