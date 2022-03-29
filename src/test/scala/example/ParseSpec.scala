package example

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ParseSpec extends AnyFlatSpec with Matchers {

  val l1 = """199.72.81.55 - - [01/Jul/1995:00:00:01 -0400] "GET /history/apollo/ HTTP/1.0" 200 6245"""
  val l2 = """burger.letters.com - - [02/Jul/1995:00:00:12 -0400] "GET /images/NASA-logosmall.gif HTTP/1.0" 304 0"""
  val l3 = """dd15-062.compuserve.com - - [02/Jul/1995:00:01:12 -0400] "GET /news/sci.space.shuttle/archive/sci-space-shuttle-22-apr-1995-40.txt HTTP/1.0" 404 -"""
  val l4 = """pm4_23.digital.net - - [03/Jul/1995:00:05:58 -0400] "GET /images/shuttle-patch-small.gif HTTP/1.0" 200 4179"""
  val l5 = """pipe6.nyc.pipeline.com - - [03/Jul/1995:00:22:43 -0400] "GET /shuttle/missions/sts-71/movies/sts-71-mir-dock.mpg" 200 946425"""
  val l6 = """204.120.229.63 - - [01/Jul/1995:04:29:05 -0400] "GET /history/history.html                                                 hqpao/hqpao_home.html HTTP/1.0" 200 1502"""
  val l7 = """@.scimaging.com - - [02/Jul/1995:21:08:54 -0400] "GET / HTTP/1.0" 200 7074"""
  val l8 = """frank.mtsu.edu - - [03/Jul/1995:02:41:15 -0400] "GET /images/" HTTP/1.0" 404 -"""

  "Parse object" should "parse hosts from typical CLF log lines" in {
    Parse.parseHost(l1) shouldEqual "199.72.81.55"
    Parse.parseHost(l2) shouldEqual "burger.letters.com"
    Parse.parseHost(l3) shouldEqual "dd15-062.compuserve.com"
    //Parse.parseHost(l4) shouldEqual "pm4_23.digital.net"
    an [ParseException] should be thrownBy Parse.parseHost(l4)
    Parse.parseHost(l5) shouldEqual "pipe6.nyc.pipeline.com"
    Parse.parseHost(l6) shouldEqual "204.120.229.63"
    an [ParseException] should be thrownBy Parse.parseHost(l7)
    Parse.parseHost(l8) shouldEqual "frank.mtsu.edu"
  }

  "Parse object" should "parse dates from typical CLF log lines" in {
    Parse.parseDate(l1) shouldEqual "19950701"
    Parse.parseDate(l2) shouldEqual "19950702"
    Parse.parseDate(l3) shouldEqual "19950702"
    Parse.parseDate(l4) shouldEqual "19950703"
    Parse.parseDate(l5) shouldEqual "19950703"
    Parse.parseDate(l6) shouldEqual "19950701"
    // TODO l7, l8
  }

  "Parse object" should "parse url from typical CLF log lines" in {
    Parse.parseURL(l1) shouldEqual "/history/apollo/"
    Parse.parseURL(l2) shouldEqual "/images/NASA-logosmall.gif"
    Parse.parseURL(l3) shouldEqual "/news/sci.space.shuttle/archive/sci-space-shuttle-22-apr-1995-40.txt"
    Parse.parseURL(l4) shouldEqual "/images/shuttle-patch-small.gif"
    Parse.parseURL(l5) shouldEqual "/shuttle/missions/sts-71/movies/sts-71-mir-dock.mpg"
    Parse.parseURL(l6) shouldEqual "/history/history.html"
    // TODO l7, l8
    //an [ParseException] should be thrownBy Parse.parseURL(l8)
  }

  "Parse object" should "parse status from typical CLF log lines" in {
    Parse.parseStatus(l1) shouldEqual 200
    Parse.parseStatus(l2) shouldEqual 304
    Parse.parseStatus(l3) shouldEqual 404
    Parse.parseStatus(l4) shouldEqual 200
    Parse.parseStatus(l5) shouldEqual 200
    Parse.parseStatus(l6) shouldEqual 200
    Parse.parseStatus(l7) shouldEqual 200
    // TODO l8
    //an [ParseException] should be thrownBy Parse.parseStatus(l7)
  }


}

