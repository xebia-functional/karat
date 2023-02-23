package karat.scalacheck

import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test
import org.scalacheck.util.ConsoleReporter
import org.scalacheck.{Test => SchkTest}

class ScalacheckTest {

  @Test def testProperty = {
    assertTrue(SchkTest.check(TestCounter.validProperty)(_.withTestCallback(ConsoleReporter())).passed)
  }

}
