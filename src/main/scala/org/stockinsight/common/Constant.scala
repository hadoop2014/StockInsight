package org.stockinsight.common

/**
 * Created by asus on 2015/6/27.
 */

object Constant {
  object StockIndexFileName extends Enumeration{
    type StockIndexFileName = Value

    val filenameHangSheng = Value("HangShengIndex.csv")                      //恒生指数文件名
    val filenameDowJones = Value("DowJonesIndustrialAverageIndex.csv")    //道琼斯工业平均指数文件名
    val filenameNikkei = Value("NikkeiAverageIndex.csv")                    //日经指数文件名
    val filenameUSDollar = Value("USDollarIndex.csv")                        //美元指数文件名
  }

  object StockIndexID extends Enumeration{
    type StockIndexID = Value

    val idShanghaiIndex = Value(1)                      //上证指数代号
    val idShenzhenIndex = Value(399001)                 //深证成指代
    val idHushen300Index = Value(399300)                //沪深300指数代号
    val idSMEComposite = Value(399101)                  //中小板指数代号，英文SSE SME COMPOSITE
    val idHangshengIndex = Value(90001)                 //恒生指数代号
    val idDowJonesIndex = Value(80001)                  //道琼斯工业平均指数代号，Dow Jones Industrial Average Index
    val idNikkeiIndex = Value(80002)                    //日经指数代号Nikkei Average Index
    val idUSDollarIndex = Value(10001)                  //美元指数代号，US Dollar Index
  }
}


