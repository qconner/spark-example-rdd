package example


// Java
import java.nio.file.Paths

// Scala
import scala.util.{Failure, Success, Try}
import scala.util.matching.Regex

// Log4J
import org.apache.log4j.Logger
import org.apache.log4j.Level._

// Apache Spark (incl Hadoop fs)
import org.apache.hadoop.fs.Path
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD

import example.Parse._


object Analytics {
  lazy val log = Logger.getLogger(getClass.getName)

  //
  // application title set here for management UI:
  // http://localhost:4040
  // http://localhost:4041
  //
  lazy val sparkConf = new SparkConf().setAppName("Apache log file URL and IP aggregation").setMaster("local[*]")

  lazy val sc: SparkContext = {
    val x = new SparkContext(sparkConf)
    x.setLogLevel("WARN")
    //x.setLogLevel("INFO")
    //x.setLogLevel("DEBUG")
    x
  }

  def run(N: Int): Int = {
    log.setLevel(DEBUG)

    val cwd = Paths.get(".").toAbsolutePath
    log.info(s"current directory is: ${cwd}")
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
        log.error(s"failed to open text file")
        log.error(ex.getMessage)
        log.error(ex)
        sc.stop
        sc.emptyRDD
    }
    //log.info(s"total line count: ${textRDD.count}")


    //
    // parse text and toss out bad records
    //
    val hits = textRDD map webhit
    log.info(s"parsed line count: ${hits.count}")

    val goodHitOptions = textRDD map webhit filter goodStatus
    val goodHits = textRDD.map(webhit).filter(goodStatus).map(_.get)
    log.info(s"valid line count: ${goodHits.count}")


    // urlTuple: ((date, url), count)
    val urlTuples: RDD[((String, String), Int)] = goodHits.map(x => ((x.date, x.url), 1))
    val urlCounts: RDD[((String, String), Int)] = urlTuples.reduceByKey(_ + _)
    //.sortBy(_._1, ascending = false).take(N)
    println("\n\n")
    urlCounts.foreach(println(_))

    // hostTuple: ((date, host), count)
    val hostTuples: RDD[((String, String), Int)] = goodHits.map(x => ((x.date, x.host), 1))
    val hostCounts: RDD[((String, String), Int)] = hostTuples.reduceByKey(_ + _)
    //.sortBy(_._2, ascending = false).take(N)
    println("\n\n")
    hostCounts.foreach(println(_))

/*
    def urlSelector(x: WebHit) = x.url
    def dateSelector(x: WebHit) = x.date
    def hostSelector(x: WebHit) = x.host

    // group by day
    val hitsByDay = goodHits groupBy dateSelector

    // within each day count distinct URLs
    val hitsByURL = hitsByDay.map( t => {
      val (date: String, xs: Iterable[WebHit]) = t
      println(date)
      println(xs)
      val foo = xs.groupBy(urlSelector).map( (url: String, ys: Iterable[WebHit]) => {
        (date, url, ys.size)
      })
    })
 */
    //val urlTopTen = hitsByURL.sortBy(_._3, ascending = false).take(N)
    //urlTopTen.foreach(log.debug(_))


    // within each day count distinct URLs and keep top N
    //val hitsByHost = ???
    //counts foreach println



    // TODO: switch to non-hadoop spark jar
    //       or fix the Hadoop config file and missing jar errors on exit
    // graceful shutdown
    sc.stop
    0
  }


  //
  //  our RDD "record" will be this case class
  //  after parsing the text CLF log entry
  //
  final case class WebHit(date: String, host: String, url: String, status: Int)
  def webhit(clfLine: String): Option[WebHit] = Try {
    WebHit(parseDate(clfLine), parseHost(clfLine), parseURL(clfLine), parseStatus(clfLine))
  } match {
    case Success(x) =>
      Some(x)
    case Failure(ex: Throwable) =>
      log.debug(s"parse failed due to ${ex.getMessage}")
      None
  }

  // use this to filter log lines to those that are relevant
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

}

