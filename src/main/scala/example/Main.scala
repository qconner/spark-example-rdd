package example

import scala.util.{Failure, Success, Try}

// Java
import java.nio.file.Paths

// Scala
import scala.util.matching.Regex

// Log4J
import org.apache.log4j.Logger
import org.apache.log4j.Level._

// Apache Spark (incl Hadoop fs)
import org.apache.hadoop.fs.Path
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD

import example.Parse._


object Main extends App {
  lazy val log = Logger.getLogger(getClass.getName)

  lazy val sparkConf = new SparkConf().setAppName("Apache log file URL and IP aggregation").setMaster("local[*]")

  lazy val sc: SparkContext = {
    val x = new SparkContext(sparkConf)
    x.setLogLevel("INFO")
    x
  }

  def run(args: List[String]): Int = {
    log.setLevel(DEBUG)

    val cwd = Paths.get(".").toAbsolutePath
    println(s"current directory is: ${cwd}")

    println(s"${args.size} arguments given:")
    println(args.mkString("\n"))
    assert(args.size == 1)
    val N: Int = args.head.toInt
    println(s"preparing to compute top ${N} URLs and client IPs for each day in NASA clf")

    log.info("creating spark context")
    log.debug(s"spark conf: ${sc.getConf.toDebugString}")

    // look for data file
    val hadoopConf = sc.hadoopConfiguration
    val fs = org.apache.hadoop.fs.FileSystem.get(hadoopConf)

    def checkURLexists(httpURL: String): Boolean = {
      /*
      val httpClient = Http().outgoingConnection(host = "jsonplaceholder.typicode.com")
      val request = HttpRequest(uri = Uri("/comments"), headers = List(cookie))
      val flow = Source.single(request)
         .via(httpClient)
         .mapAsync(1)(r => Unmarshal(r.entity).to[List[Post]])
         .runWith(Sink.head)

      flow.andThen {
         case Success(list) => println(s"request succeded ${list.size}")
         case Failure(_) => println("request failed")
      }.andThen {
         case _ => system.terminate()
      }
      */
      false
    }

    val fileNameLong = "NASA_access_log_Jul95.gz"
    val fullFileNameLong = cwd + "/data/" + fileNameLong
    val longExists: Boolean = fs.exists(new Path(fullFileNameLong))
    log.debug(s"local long file: ${fullFileNameLong}  exists: ${longExists}")

    val fileNameShort = "NASA_access_log_Jul95.head"
    val fullFileNameShort = cwd + "/data/" + fileNameShort
    val shortExists: Boolean = fs.exists(new Path(fullFileNameShort))
    log.debug(s"local short file: ${fullFileNameShort}  exists: ${shortExists}")

    val githubURI = s"https://github.com/qconner/spark-example-rdd/raw/${fileNameLong}"
    val githubExists = checkURLexists(githubURI)
    log.debug(s"github-hosted long file: ${githubURI}  exists: ${githubExists}")


    val uri = longExists match {
      case true =>
        // use long file for development
        "file://" + fullFileNameLong
      case false =>
        shortExists match {
          case true =>
            // use short file for development
            "file://" + fullFileNameShort
          case false =>
            // prefer github raw
            githubExists match {
              case true =>
                "https://githubraw.com/qconner/spark-example-rdd/develop/data/NASA_access_log_Jul95.gz"
              case false =>
                // fall back to LBL FTP site
                "ftp://ita.ee.lbl.gov/traces/NASA_access_log_Jul95.gz"
            }
        }
    }

    // read a Stream of Strings representing lines in the Apache Common Log Format
    val textRDD: RDD[String] = Try {
      log.info(s"opening ${uri}")
      sc.textFile(uri)
    } match {
      case Success(x) =>
        x
      case Failure(ex) =>
        log.error(s"failed to open text file RDD")
        log.error(ex.getMessage)
        log.error(ex)
        sc.stop
        sc.emptyRDD
    }

    log.info(s"total line count: ${textRDD.collect.size}")

    def goodStatus(x: Option[WebHit]): Boolean = x match {
      case None =>
        false
      case Some(x) if (x.status == 200) =>
        true
      case Some(x) if (x.status == 302) =>
        true
      case _ =>
        log.debug(s"bad status: ${x}")
        false
    }

    // parse
    val hits = textRDD map webhit
    log.info(s"parseable line count: ${hits.collect.size}")

    val goodhits = textRDD map webhit filter goodStatus
    log.info(s"good line count: ${goodhits.collect.size}")
    goodhits.foreach(x => log.info(s"goodhit: ${x}"))

    // graceful shutdown
    sc.stop
    0
  }


  final case class WebHit(yyyymmdd: String, host: String, url: String, status: Int)

  def webhit(clfLine: String): Option[WebHit] = Try {
    WebHit(parseDate(clfLine), parseHost(clfLine), parseURL(clfLine), parseStatus(clfLine))
  } match {
    case Success(x) =>
      Some(x)
    case Failure(ex) =>
      log.warn(ex.getMessage)
      log.warn(ex)
      None
  }



  // find the top ten (or user-specified top-N) IPs and URLs
  run(List(args.size match {
    case 0 => "10"
    case n: Int => args.head.toString
  }))
}

