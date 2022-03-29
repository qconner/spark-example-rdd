package example

// Java

// Scala


class ParseException(x: String, ex: Throwable = null) extends RuntimeException

object ParseException {
  def apply(x: String) = new ParseException(x)
  def apply(ex: Throwable) = new ParseException(ex.getMessage, ex)
  def apply(x: String, ex: Throwable) = new ParseException(x, ex)
}


