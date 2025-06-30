# Scala Clock

A minimal, test-friendly time abstraction micro-library for Scala 3.

## Why?

Time-dependent code is notoriously hard to test. This library provides a simple `Clock` abstraction that makes your code testable while keeping production code unchanged.

```scala
// Production: uses real system time
given Clock = Clock.system

// Testing: full control over time
given Clock = Clock.manual(1000)
```

## Installation

Add to your `build.sbt`:

```scala
libraryDependencies += "dev.lucasmdjl" %% "scala-clock" % "x.x.x"
```

## Usage

### Basic Pattern

Use `Clock` as a contextual parameter (Scala 3's `using`):

```scala
import dev.lucasmdjl.scala.clock.*

class Cache[T](ttl: Long)(using clock: Clock) {
  private var lastRefresh = clock.now()
  
  def isExpired: Boolean = 
    clock.now() - lastRefresh > ttl
}
```

### Production

```scala
given Clock = Clock.system  // Uses System.currentTimeMillis

val cache = Cache(ttl = 5000)
```

### Testing

#### Fixed Time
Perfect for tests that need predictable time:

```scala
given Clock = Clock.fixed(1000)

val cache = Cache(ttl = 5000)
cache.isExpired // Always false, time never changes
```

#### Manual Control
For tests that need to simulate time passage:

```scala
given testClock: ManualClock = Clock.manual(0)

val cache = Cache(ttl = 5000)
assert(!cache.isExpired)

testClock.advance(6000)
assert(cache.isExpired)
```

#### Incremental Steps
For tests that need regular time progression:

```scala
given testClock: IncrementalClock = Clock.incremental(0, step = 1000)

val timestamps = List.fill(3) {
  testClock.tick()
  testClock.now()
}
// timestamps = List(1000, 2000, 3000)
```

## API Reference

### Clock Implementations

| Type | Use Case | Factory Method |
|------|----------|----------------|
| `SystemClock` | Production code | `Clock.system` |
| `FixedClock` | Tests with unchanging time | `Clock.fixed(time)` |
| `ManualClock` | Tests with full time control | `Clock.manual(initialTime)` |
| `IncrementalClock` | Tests with regular steps | `Clock.incremental(initialTime, step)` |

### ManualClock Methods

```scala
val clock = Clock.manual(1000)

clock.now()           // Current time
clock.setTime(5000)   // Jump to specific time  
clock.advance(2000)   // Move forward by amount
```

### IncrementalClock Methods

```scala
val clock = Clock.incremental(initialTime = 0, step = 100)

clock.now()   // Current time
clock.tick()  // Advance by step amount
```

## Design Philosophy

- **Minimal** - Just what you need, nothing more
- **Thread-safe** - All implementations work safely in concurrent code
- **Zero overhead** - Abstracts time access, not time itself

## Requirements

- Scala 3.0+
- No additional dependencies

## License

AGPL-3.0
