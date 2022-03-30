/*
 * Copyright (C) 2022 Quentin Alan Conner - All Rights Reserved
 * You may not use, distribute or modify this code.  All rights
 * will remain with the author.  Contact the author with any
 * permission or licensing requests.
 *
 * Quentin Conner
 * 13100 Delphinus Walk
 * Austin, TX  78732
 *
 */

package example

// Java

// Scala

// Log4J
import org.apache.log4j.Logger
import org.apache.log4j.Level._


object Main extends App {
  lazy val log = Logger.getLogger(getClass.getName)

  def run(args: List[String]): Int = {
    log.setLevel(DEBUG)

    log.debug(s"${args.size} arguments received:")
    log.debug(args.mkString("\n"))
    assert(args.size == 1)
    val N: Int = args.head.toInt
    log.info(s"preparing to compute top ${N} URLs and client IPs for each day")

    Analytics.run(N)
  }

  //
  // main entry point for this (batch) application
  //
  // find the top ten (or user-specified top-N) IPs and URLs
  //
  run(List(args.size match {
    case 0 =>
      // default value if no arguments given
      "10"
    case n: Int =>
      args.head.toString
  }))
}

