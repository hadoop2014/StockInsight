/**
 * Created by asus on 2015/6/27.
 */

import java.io.{IOException, PrintWriter}
import java.net.URL
import org.stockinsight.common.LogSupport


import scala.io.Source

object URLDownloader extends LogSupport{
  def downloadFile(url:String,filename:String) = {
    try {
      val printWriter = new PrintWriter(filename)
      printWriter.write(Source.fromURL(url,"utf-8").mkString)
    }
    catch {
      case e:IOException =>  log.error(s"write file failure $filename!")
    }
  }
}
