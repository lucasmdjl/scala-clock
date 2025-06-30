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

import java.util.concurrent.atomic.AtomicLong

/** Abstraction for a time source to enable testable code.
  *
  * Provides the current time in milliseconds since Unix epoch. The default
  * implementation uses System.currentTimeMillis.
  *
  * Implementors of this class must ensure now never returns a negative value.
  */
trait Clock {

  /** Returns the current time value.
    *
    * @return
    *   the current time in milliseconds since Unix epoch
    */
  def now(): Long
}

object Clock {

  /** Default clock implementation using system time */
  given system: Clock = () => System.currentTimeMillis

  /** Creates a clock that always returns the same time value.
    *
    * Useful for tests where you need predictable, unchanging time.
    *
    * @param time
    *   the fixed time in milliseconds since Unix epoch (default: 0)
    * @return
    *   a FixedClock instance
    */
  def fixed(time: Long = 0): FixedClock = FixedClock(time)

  /** Creates a clock that advances by a fixed delta each time tick() is called.
    *
    * Useful for tests where you need controlled, incremental time progression.
    *
    * @param initialTime
    *   starting time in milliseconds since Unix epoch (default: 0)
    * @param step
    *   amount to advance on each tick in milliseconds (default: 1)
    * @return
    *   a TickingClock instance
    */
  def incremental(initialTime: Long = 0, step: Long = 1): IncrementalClock =
    IncrementalClock(initialTime, step)

  /** Creates a clock with fully manual time control.
    *
    * Useful for tests where you need to set arbitrary times or advance by
    * variable amounts.
    *
    * @param initialTime
    *   starting time in milliseconds since Unix epoch (default: 0)
    * @return
    *   a ManualClock instance
    */
  def manual(initialTime: Long = 0): ManualClock = ManualClock(initialTime)
}

/** A clock that always returns the same fixed time value.
  *
  * Thread-safe and immutable after construction.
  *
  * @param time
  *   the fixed time in milliseconds since Unix epoch
  */
class FixedClock(time: Long = 0) extends Clock {
  require(time >= 0, "time must not be negative")

  /** Returns the current time value.
    *
    * @return
    *   the current time in milliseconds since Unix epoch
    */
  override def now(): Long = time
}

/** A clock that advances by a fixed step each time tick() is called.
  *
  * Thread-safe. The time starts at initialTime and advances by step
  * milliseconds each time tick() is invoked.
  *
  * @param initialTime
  *   starting time in milliseconds since Unix epoch
  * @param step
  *   amount to advance on each tick in milliseconds
  */
class IncrementalClock(initialTime: Long = 0, step: Long = 1) extends Clock {
  require(initialTime >= 0, "initial time must not be negative")
  require(step > 0, "step must be positive")

  private val currentTime = AtomicLong(initialTime)

  /** Returns the current time value.
    *
    * @return
    *   the current time in milliseconds since Unix epoch
    */
  override def now(): Long = currentTime.get()

  /** Advances the clock by the configured delta amount.
    *
    * Thread-safe operation that atomically increments the current time.
    */
  def tick(): Unit = currentTime.addAndGet(step)
}

/** A clock with full manual control over time progression.
  *
  * Thread-safe. Allows setting arbitrary time values and advancing by arbitrary
  * amounts.
  *
  * @param initialTime
  *   starting time in milliseconds since Unix epoch
  */
class ManualClock(initialTime: Long = 0) extends Clock {
  require(initialTime >= 0, "initial time must not be negative")

  private val currentTime = AtomicLong(initialTime)

  /** Returns the current time value.
    *
    * @return
    *   the current time in milliseconds since Unix epoch
    */
  override def now(): Long = currentTime.get()

  /** Sets the clock to a specific time value.
    *
    * @param newTime
    *   the new time in milliseconds since Unix epoch (must not be before
    *   current time)
    */
  def setTime(newTime: Long): Unit = {
    require(newTime >= now(), "new time must not be before current time")
    currentTime.set(newTime)
  }

  /** Advances the clock by the specified amount.
    *
    * @param step
    *   amount to advance in milliseconds (must not be negative)
    */
  def advance(step: Long): Unit = {
    require(step >= 0, "step must not be negative")
    currentTime.addAndGet(step)
  }
}
