package example

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MainSpec extends AnyFlatSpec with Matchers {

  "Main object" should "have a static invocation that runs, exits and returns zero" in {
    Main.run(List("10")) shouldEqual 0
  }

}

