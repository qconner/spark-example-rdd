/*
 * Copyright (C) 2022 Quentin Alan Conner - All Rights Reserved
 * You may not use, distribute or modify this code.  All rights
 * will remain with the author.  Contact the author with any permission
 * or licensing requests:
 *
 * Quentin Conner
 * 13100 Delphinus Walk
 * Austin, TX  78732
 *
 */

package example


// Java
import java.nio.file.Paths
import java.net.URI

// Scala
import scala.util.{Failure, Success, Try}
import scala.util.matching.Regex

// Log4J
import org.apache.log4j.Logger
import org.apache.log4j.Level._

// Apache Spark
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD

// Apache Hadoop
import org.apache.hadoop.fs.{FileSystem, Path}


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
    val fs = FileSystem.get(hadoopConf)


    // TODO: check if an HTTP URL exists by way of the HEAD request
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

      Try {
        fs.exists(new Path(httpURL))
      } match {
        case Success(x) =>
          x
        case Failure(ex) =>
          false
          //true // force github
      }
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
                "ftp://ftp:pass@ita.ee.lbl.gov/traces/NASA_access_log_Jul95.gz"
            }
        }
    }
/*
    val tempRDD: RDD[String] = Try {
      val githubResponse = scala.io.Source.fromURL("https://githubraw.com/qconner/spark-example-rdd/develop/data/NASA_access_log_Jul95.gz").mkString
      log.warn(githubResponse)
      val lines = githubResponse.split("\n").filter(_ != "")
      log.warn(lines.size)
      sc.parallelize(lines)
    } match {
      case Success(xs) =>
        xs
      case Failure(ex) =>
        println(ex.getMessage)
        sc.stop
        sc.emptyRDD
    }
    println(s"tempRDD count: ${tempRDD.count}")
 */
    //
    // read a Stream of Strings representing lines in the Apache Common Log Format,
    // from either HDFS, local filesystem or from FTP.
    //
    // TODO: verify this is single-threaded (unless reading from HDFS URI)
    //
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

    //
    // parse text and toss out bad records
    // which should happen in parallel
    //
    val hits = textRDD map webhit
    log.info(s"parsed line count: ${hits.count}")

    // filter based on webserver response status
    val goodHitOptions = hits filter goodStatus

    val goodHits = goodHitOptions.filter(_.nonEmpty).map(_.get)
    log.info(s"valid line count:  ${goodHits.count}")




    //
    // URL count analytics
    // using urlTuple: ((date, url), count)
    //
    // TODO: use a case class ILO tuple for clarity
    //        tuples are used for quick prototyping at the scala console
    //
    // First we create a tuple with count 1 for each valid URL from the log.
    // This will be used in a fold (Reduce of Map/Reduce fame) operation
    // to sum the counts for matching URLs.
    //
    val urlTuples: RDD[((String, String), Int)] = goodHits.map(x => ((x.date, x.url), 1))

    //
    // Now we run the reduce (fold) operation to count the distinct URL appearances
    //
    // IMPORTANT: we avoid groupBy here since it will perform (relatively) poorly on the full data set.
    // Instead, we keep the data in an easily-reduceable tuple with date and url for the key
    //
    val urlCounts: RDD[((String, String), Int)] = urlTuples.reduceByKey(_ + _)

    // map to 3-tuple of date, url, count to flatten a bit (helps with reasoning)
    val urlCountTuples: RDD[(String, String, Int)] = urlCounts.map(x => (x._1._1, x._1._2, x._2))

    //
    // now we can use groupBy without penalty, on the much-smaller aggregated Stream
    // to create a Map of dates containing a List/Stream of URLs and their counts
    //
    val urlCountsByDay: RDD[(String, Iterable[(String, String, Int)])] = urlCountTuples.groupBy(_._1)


    //
    // For each date, sort URLtuples by count in descending order.
    // Keep the top N tuples for each day.
    //
    val topNurlCountsByDaySorted: RDD[Seq[(String, String, Int)]] = urlCountsByDay.map((t: (String, Iterable[(String, String, Int)])) => {
      val date = t._1
      val topN: Seq[(String, String, Int)] = t._2.toSeq.sortWith( (x, y) => {
        // sort with date ascending, then count descending
        (x._1 == y._1) match {
          case false =>
            // date ascending
            (x._1 < y._1)
          case true =>
            // count descending
            (y._3 < x._3)
        }
      }).take(N).map(x => { (date, x._2, x._3) })

      topN
    })


    //
    // print out the top N URL results
    //
    // TODO: evaluate this topNurlCountsByDaySorted answer from
    // an automated test case using fixed data.
    //
    // TODO: Research whether a "mock Spark RDD" test harness exists
    //
    println(s"\n\nTop ${N} URLs by day:")
    topNurlCountsByDaySorted.collect.foreach( xs => {
      xs.foreach( t => {
        println(s"${t._1}  ${t._2}  ${t._3}")
      })
    })



    //
    // Host count analytics follow same pattern as URL count analytics
    //
    // using hostTuple:  ((date, host), count)
    //
    // TODO: case classes improve maintainability
    //
    // TODO: factor out host vs. URL (both are strings) and use a common function, called twice?
    //
    //
    val hostTuples: RDD[((String, String), Int)] = goodHits.map(x => ((x.date, x.host), 1))

    // the big fold (reduce)
    val hostCounts: RDD[((String, String), Int)] = hostTuples.reduceByKey(_ + _)
    val hostCountTuples: RDD[(String, String, Int)] = hostCounts.map(x => (x._1._1, x._1._2, x._2))

    // the smaller groupBy
    val hostCountsByDay: RDD[(String, Iterable[(String, String, Int)])] = hostCountTuples.groupBy(_._1)

    //
    // For each date, sort HostTuples by count in descending order.
    // Keep the top N tuples for each day.
    //
    val topNhostCountsByDaySorted: RDD[Seq[(String, String, Int)]] = hostCountsByDay.map((t: (String, Iterable[(String, String, Int)])) => {
      val date = t._1
      val topN: Seq[(String, String, Int)] = t._2.toSeq.sortWith( (x, y) => {
        // sort by date ascending, then count descending
        (x._1 == y._1) match {
          case false =>
            // date ascending
            (x._1 < y._1)
          case true =>
            // count descending
            (y._3 < x._3)
        }
      }).take(N).map(x => { (date, x._2, x._3) })

      topN
    })

    // print top N host by day
    println(s"\n\nTop ${N} Hosts by day:")
    topNhostCountsByDaySorted.collect.foreach( xs => {
      xs.foreach( t => {
        println(s"${t._1}  ${t._2}  ${t._3}")
      })
    })


    // graceful shutdown
    sc.stop
    0
  }


  //
  //  our RDD "record" will be this case class
  //  after parsing the text CLF log entry
  //
  //  TODO: more case classes for host and URL aggregations
  //        after prototyping with tuples is complete.  Why?
  //        To improve maintainability.
  //
  final case class WebHit(date: String, host: String, url: String, status: Int)
  def webhit(clfLine: String): Option[WebHit] = Try {
    WebHit(parseDate(clfLine), parseHost(clfLine), parseURL(clfLine), parseStatus(clfLine))
  } match {
    case Success(x) =>
      Some(x)
    case Failure(ex: Throwable) =>
      ex.getMessage match {
        case null =>
          log.warn(s"parse failed due to exception: ${ex}")
        case _ =>
          log.debug(s"parse failed due to: ${ex.getMessage}")
      }
      None
  }

  // use this to filter log lines to those that are relevant
  def goodStatus(x: Option[WebHit]): Boolean = true

  //
  //  TODO: disambiguate requirements for what constitutes a
  //        web site access.  Does content need to be successfully
  //        transmitted to the client host to be considered an
  //        visitor access?  For now we count all response codes
  //        and HTTP request types (HEAD, GET, et al)
  //
  def strictGoodStatus(x: Option[WebHit]): Boolean = x match {
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
