package example

// Java

// Scala


class ParseException(message: String = null, cause: Throwable = null) extends
  RuntimeException(ParseException.defaultMessage(message, cause), cause)

object ParseException {
  def defaultMessage(message: String, cause: Throwable) =
    if (message != null) message
    else if (cause != null) cause.toString()
    else null
}
