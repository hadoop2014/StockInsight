package org.stockinsight.Hdfs

import java.io.File

import org.scalatest.{BeforeAndAfter, FlatSpec, ShouldMatchers}
import org.stockinsight.common._

/**
 * Created by asus on 2015/8/2.
 */
class HdfsManager$Test extends FlatSpec with ShouldMatchers with LogSupport with BeforeAndAfter{

  val target = s"${HdfsManager.getDefaultFS()}/hdfstest"
  val localPath = ConfigManager.configHome  // "./src/main/resources/"

  it  should "get default FS name" in {
    HdfsManager.getDefaultFS() should be ("hdfs://Master:9000")
  }

  it should "create Hdfs test dictionary " in {
    HdfsManager.mkDir(target)
    HdfsManager.isDirectory(target) should be (true)
  }

  it should "copy local file to hdfs" in{
    HdfsManager.putFilesToHdfs(localPath,target)
    HdfsManager.listFiles(target).foreach(log.info)
    HdfsManager.listFiles(target).size should be (6)
  }

  it should "copy hdfs file to local and delete local path" in {
    val targetPath = "./hdfstest"
    HdfsManager.getFilesFromHdfs(target,targetPath)
    new File(targetPath).listFiles().foreach(file => log.info(s"${file.getPath}"))
    new File(targetPath).listFiles().size should be (6)
    HdfsManager.rmLocalDir(targetPath)
  }

  it should "delete hdfs test dictionary" in {
    HdfsManager.delete(target)
    HdfsManager.isDirectory(target) should be (false)
  }
}
