package org.stockinsight.Http

import java.io.{FileOutputStream, IOException}

import org.apache.http.client.ClientProtocolException
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.{CloseableHttpClient, HttpClients}
import org.stockinsight.common.{Constant, LogSupport, Using}

/**
 * Created by asus on 2015/8/2.
 */
trait HttpFunction extends LogSupport with Using{

  //实现HttpClient的租借模式
  def usingHttpClient(errMsg: String)(f: CloseableHttpClient => Unit) = {
    val httpClient = HttpClients.createDefault()

    try {
      f(httpClient)
    }
    catch {
      case e: ClientProtocolException  =>
        log.error(errMsg + s"ClientProtocolException", e)
        throw e
      case e:IOException =>
        log.error(errMsg ,e)
        throw e
      case ex: Throwable =>
        log.error(errMsg , ex)
        throw ex
    }
    finally{
      try{
        if(httpClient != null) httpClient.close()
      }catch {
        case e:IOException =>
          log.error(errMsg + "close httpclient failed.",e)
          e.printStackTrace()
      }
    }
  }

  //通过URL下载文件
  def downloadFromUrl(url: String,fileName: String) = {
    usingHttpClient("download from url failed."){
      httpClient =>
        val httpGet = new HttpGet(url)
        log.debug("http get:" + url)
        using(httpClient.execute(httpGet)){
          httpResponse =>
            if (httpResponse.getStatusLine().getStatusCode == 200){
              val entity = httpResponse.getEntity
              using(entity.getContent()){
                inputStream =>
                  using(new FileOutputStream(fileName)){
                    outputStream =>
                      var bytes = new Array[Byte](Constant.constBufferSize.id)
                      var len = -1
                      while ({ len = inputStream.read(bytes, 0, Constant.constBufferSize.id); len != -1 }) {
                        outputStream.write(bytes, 0, len)
                      }
                  }

              }

            }else{
              log.info(s"download stockindex failed from $url!")
            }
        }
    }
  }


}
