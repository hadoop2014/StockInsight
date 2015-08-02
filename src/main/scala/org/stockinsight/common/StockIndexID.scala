package org.stockinsight.common

import java.util.Calendar

/**
 * Created by asus on 2015/6/27.
 */

object StockIndexID extends Enumeration{
  type StockIndexID = Value

  val idShanghaiIndex = Value(1,"China")                      //上证指数代号
  val idShenzhenIndex = Value(399001,"China")                 //深证成指代号
  val idHushen300Index = Value(399300,"China")                //沪深300指数代号
  //val idSMEComposite = Value(399101,"China")                  //中小板指数代号，英文SSE SME COMPOSITE 目前从yahoo获取不到
  //val idCHNComposite = Value(399006,"China")                  //创业板指数代号CHINEXT  目前从雅虎获取不到
  val idHangshengIndex = Value(90001,"Internation")                 //恒生指数代号
  val idDowJonesIndex = Value(80001,"Internation")                  //道琼斯工业平均指数代号，Dow Jones Industrial Average Index
  val idNikkeiIndex = Value(80002,"Internation")                    //日经指数代号Nikkei Average Index
  //val idUSDollarIndex = Value(10001,"Internation")                  //美元指数代号，US Dollar Index

  def getStockIndexFileName(stockIndexId:StockIndexID):String = {
    /*stockIndexId match {
      case idShanghaiIndex => "HangShengIndex.csv"                        //恒生指数文件名
      case idDowJonesIndex => "DowJonesIndustrialAverageIndex.csv"      //道琼斯工业平均指数文件名
      case idNikkeiIndex => "NikkeiAverageIndex.csv"                      //日经指数文件名
      case idUSDollarIndex => "USDollarIndex.csv"                         //美元指数文件名
      case _ => ""
    }*/
    if (stockIndexId == idNikkeiIndex )"NikkeiAverageIndex.csv"
    else if (stockIndexId == idHangshengIndex)"HangShengIndex.csv"
    else if (stockIndexId == idDowJonesIndex) "DowJonesIndustrialAverageIndex.csv"
    //else if(stockIndexId == idUSDollarIndex) "USDollarIndex.csv"
    else if(stockIndexId == idHushen300Index) "HuShen300Index.csv"
    else if(stockIndexId == idShanghaiIndex) "ShangHaiIndex.csv"
    else if(stockIndexId == idShenzhenIndex) "ShenZhenIndex.csv"
    //else if(stockIndexId == idSMEComposite) "SMEComposite.csv"
    //else if(stockIndexId == idCHNComposite) "CHINEXTComposite.csv"
    else ""
  }

  def getDomesticStockIndexUrl(idStockIndex:StockIndexID):String = {  //获取国际指数
    val codeStockIndex =
      if (idStockIndex == idShanghaiIndex ) "000001.ss"       //上证指数代号
      else if (idStockIndex == idShenzhenIndex) "399001.sz"   //深证成指代号
      else if (idStockIndex == idHushen300Index) "000300.ss"
      //else if (idStockIndex == idSMEComposite) "399101.sz"
      //else if (idStockIndex == idCHNComposite) "399006.sz"
      else ""
    //val currentTime = Calendar.getInstance().getTime
    s"http://table.finance.yahoo.com/table.csv?s=$codeStockIndex"
    //http://table.finance.yahoo.com/table.csv?s=600000.ss
  }

  def getInternationalStockIndexUrl(idStockIndex:StockIndexID):String = { //获取国内指数
    val codeStockIndex =
      if (idStockIndex == idNikkeiIndex ) "N225"
      else if (idStockIndex == idHangshengIndex) "HSI"
      else if (idStockIndex == idDowJonesIndex) "DJI"
      else ""
    val currentTime = Calendar.getInstance().getTime
    s"http://ichart.finance.yahoo.com/table.csv?s=%5E$codeStockIndex&amp;d=${currentTime.getMonth}&amp;e=${currentTime.getDate}&amp;f=${Calendar.getInstance().get(Calendar.YEAR)}&amp;g=d&amp;a=9&amp;b=1&amp;c=1928&amp;ignore=.csv"
    //http://real-chart.finance.yahoo.com/table.csv?s=000001.SS&a=00&b=29&c=1985&d=06&e=1&f=2015&g=d&ignore=.csv
  }

  def getStockIndexUrl(idStockIndex:StockIndexID):String = {
    if (idStockIndex.toString == "China")getDomesticStockIndexUrl(idStockIndex)
    else if(idStockIndex.toString == "Internation")getInternationalStockIndexUrl(idStockIndex)
    else ""
  }
 //implicit def string2StockIndexID( value:Int):StockIndexID =
 // 上市数据链接：http://table.finance.yahoo.com/table.csv?s=600000.ss
 //上证综指代码：000001.ss，深证成指代码：399001.SZ，沪深300代码：000300.ss
}
