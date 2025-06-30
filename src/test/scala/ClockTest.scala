/*
 * Scala Clock - A minimal, test-friendly time abstraction micro-library for Scala 3.
 * Copyright (C) 2025 Lucas de Jong
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.lucasmdjl.scala.clock

import org.scalatest.flatspec.AnyFlatSpec

import java.util.concurrent.{CountDownLatch, Executors}
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}

class ClockTest extends AnyFlatSpec {

  "Clock.system" should "return a clock with system time" in {
    val clock = Clock.system
    var before = System.currentTimeMillis()
    var clockNow = clock.now()
    var after = System.currentTimeMillis()
    assert(
      clockNow >= before,
      "Expected clock.now to be at greater than or equal to System.currentTimeMillis before it"
    )
    assert(
      clockNow <= after,
      "Expected clock.now to be at less than or equal to System.currentTimeMillis after it"
    )
    Thread.sleep(5)
    before = System.currentTimeMillis()
    clockNow = clock.now()
    after = System.currentTimeMillis()
    assert(
      clockNow >= before,
      "Expected clock.now to be at greater than or equal to System.currentTimeMillis before it"
    )
    assert(
      clockNow <= after,
      "Expected clock.now to be at less than or equal to System.currentTimeMillis after it"
    )
  }

  "Clock.fixed" should "return a clock with fixed time" in {
    val clock = Clock.fixed(100)
    assertResult(100, "now")(clock.now())
    Thread.sleep(5)
    assertResult(100, "now after delay")(clock.now())
  }

  "Clock.fixed" should "throw when negative time" in {
    assertThrows[IllegalArgumentException](Clock.fixed(-100))
  }

  "Clock.fixed" should "not throw when zero time" in {
    Clock.fixed()
  }

  "Clock.incremental" should "return a clock with incremental time" in {
    val clock = Clock.incremental(1_000, 100)
    assertResult(1_000, "now")(clock.now())
    Thread.sleep(5)
    assertResult(1_000, "now after delay")(clock.now())
    clock.tick()
    assertResult(1_100, "now after tick")(clock.now())
    clock.tick()
    assertResult(1_200, "now after second tick")(clock.now())
  }

  "Clock.incremental" should "throw when negative initial time" in {
    assertThrows[IllegalArgumentException](Clock.incremental(-100, 100))
  }

  "Clock.incremental" should "not throw when zero initial time" in {
    Clock.incremental(0, 10)
  }

  "Clock.incremental" should "throw when zero step" in {
    assertThrows[IllegalArgumentException](Clock.incremental(100, 0))
  }

  "Clock.incremental" should "throw when negative step" in {
    assertThrows[IllegalArgumentException](Clock.incremental(100, -10))
  }

  "Clock.manual" should "return a clock with manual time" in {
    val clock = Clock.manual(1)
    assertResult(1, "now")(clock.now())
    Thread.sleep(5)
    assertResult(1, "now after delay")(clock.now())
    clock.advance(100)
    assertResult(101, "now after advance")(clock.now())
    clock.setTime(200)
    assertResult(200, "now after setting time")(clock.now())
  }

  "Clock.manual" should "throw when negative initial time" in {
    assertThrows[IllegalArgumentException](Clock.manual(-100))
  }

  "Clock.manual" should "not throw when zero initial time" in {
    Clock.manual()
  }

  "Clock.manual" should "throw when advancing negative time" in {
    val clock = Clock.manual(10)
    assertThrows[IllegalArgumentException](clock.advance(-100))
  }

  "Clock.manual" should "not throw when advancing zero time" in {
    val clock = Clock.manual(10)
    clock.advance(0)
  }

  "Clock.manual" should "throw when setting negative time" in {
    val clock = Clock.manual(1_000)
    assertThrows[IllegalArgumentException](clock.setTime(-1_000))
  }

  "Clock.manual" should "throw when setting time in past" in {
    val clock = Clock.manual(1_000)
    assertThrows[IllegalArgumentException](clock.setTime(100))
  }

  "Clock.manual" should "throw when setting time in past after advancing" in {
    val clock = Clock.manual(1_000)
    clock.advance(500)
    assertThrows[IllegalArgumentException](clock.setTime(1_100))
  }

  "Clock.manual" should "throw when setting time in past after setting time" in {
    val clock = Clock.manual(100)
    clock.advance(500)
    assertThrows[IllegalArgumentException](clock.setTime(300))
  }

  "Clock.incremental" should "be thread-safe" in {
    given ExecutionContext =
      ExecutionContext.fromExecutor(Executors.newFixedThreadPool(11))
    val startCdl = CountDownLatch(10)
    val endCdl = CountDownLatch(10)
    val clock = Clock.incremental()
    val futures = for (i <- 1 to 10) yield {
      Future {
        startCdl.countDown()
        startCdl.await()
        clock.tick()
        endCdl.countDown()
      }
    }
    val future = Future.sequence(futures)
    Await.result(future, 1.second)
    assertResult(10, "now")(clock.now())
  }

  "Clock.manual" should "be thread-safe" in {
    given ExecutionContext =
      ExecutionContext.fromExecutor(Executors.newFixedThreadPool(11))
    val startCdl = CountDownLatch(10)
    val clock = Clock.manual()
    val futures = for (i <- 1 to 10) yield {
      Future {
        startCdl.countDown()
        startCdl.await()
        clock.advance(i)
      }
    }
    val future = Future.sequence(futures)
    Await.result(future, 1.second)
    assertResult(55, "now")(clock.now())
  }

}
