/**
 * Created by asus on 2015/6/27.
 */
import scala.io.Source

class URLDownloader {

}

object URLDownloader extends App{
  val downloadFile = Source.fromURL("http://ichart.finance.yahoo.com/table.csv?s=%5EHSI&amp;d=5&amp;e=27&amp;f=2015&amp;g=d&amp;a=9&amp;b=1&amp;c=1928&amp;ignore=.csv")

  downloadFile.map(line => println(line))
  println(downloadFile)
}
