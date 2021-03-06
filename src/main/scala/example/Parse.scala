/* Copyright (C) 2022 Quentin Alan Conner - All Rights Reserved
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

// Scala
import scala.util.matching.Regex


object Parse {

  private def composeDate(day: String, monthName: String, year: String): String = {
    val month: String = monthName.map(_.toLower) match {
      case "jan" => "01"
      case "feb" => "02"
      case "mar" => "03"
      case "apr" => "04"
      case "may" => "05"
      case "jun" => "06"
      case "jul" => "07"
      case "aug" => "08"
      case "sep" => "09"
      case "oct" => "10"
      case "nov" => "11"
      case "dec" => "12"
    }
    year + month + day
  }


  //
  // TODO: consider benefit of one-pass parse versus maintainability
  //
  def parseAll(clfLine: String) = {
    val pattern = """^([A-Za-z0-9\.\-_]+)\s[^\[]+\[([0-9]+)/([A-Za-z]+)/([0-9]+):[^\]]+]\s\"\w+\s(\S+)\s[^\"]+\"\s\S+\s\S+$""".r
    clfLine match {
      case pattern(host, day, monthName, year, url) =>
        (host, composeDate(day, monthName, year), url)
      case _ =>
        throw new ParseException(s"parse failed for: ${clfLine}")
    }
  }

  
  def parseHost(clfLine: String): String = {
    //
    // TODO: disambiguate requirement for valid hostnames that can be used subsequently with DNS
    //       versus allowing any non-whitespace character we allow now
    // a-z, 0-9 and dash are valid characters for DNS hostnames
    //
    val strictPattern = """^([A-Za-z0-9\.\-]+)\s.+$""".r
    val pattern = """^(\S+)\s.+$""".r
    clfLine match {
      case pattern(host) =>
        host
      case _ =>
        throw new ParseException(s"host parse failed for: ${clfLine}")
    }
  }

  def parseDate(clfLine: String): String = {
    val pattern = """^.+\[([0-9]+)/([A-Za-z]+)/([0-9]+):[^\]]+\]\s.+$""".r
    clfLine match {
      case pattern(day, monthName, year) =>
        composeDate(day, monthName, year)
      case _ =>
        throw new ParseException(s"date parse failed for: ${clfLine}")
    }
  }
  
  def parseURL(clfLine: String): String = {
    val pattern = """[^\"]+\"([^\"]+)\"[^\"]+""".r
    clfLine match {
      case pattern(quotedString) =>
        val pattern2 = """^(GET|HEAD|OPTION|PUT|POST|DELETE)\s(\S+).*$""".r
        quotedString match {
          case pattern2(mode, url) =>
            url
          case _ =>
              throw new ParseException(s"url parse failed for: ${clfLine}")
        }
      case _ =>
        throw new ParseException(s"url parse failed for: ${clfLine}")
    }
  }

  def parseStatus(clfLine: String): Int = {
    val pattern1 = """[^\"]+\"[^\"]+\"([^\"]+)""".r
    clfLine match {
      case pattern1(afterQuoted) =>
        val pattern2 = """\s?(\-|[0-9]+)\s(\-|[0-9]+)$""".r
        afterQuoted match {
          case pattern2(status, bytesSent) =>
            status match {
              case "-" =>
                -1
              case x =>
                x.toInt
            }
          case _ =>
            throw new ParseException(s"status parse failed for: ${clfLine}")
        }
    }
  }

}
