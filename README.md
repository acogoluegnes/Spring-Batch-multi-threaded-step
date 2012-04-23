Spring-Batch-multi-threaded-step
================================

This project features:
  - a simple test case to see how a multi-threaded step executes in Spring Batch (MultiThreadedStepTest)
  - a typical real-world sample that leverages Spring Batch's multi-thread step execution (BusinessJobMultiThreadedStepTest)

The approach in the real-world example would be suitable for jobs where reading is cheaper and faster 
than processing and/or writing. Jobs with CPU-intensive tasks in processing and/or writing on multi-core systems would
also benefit from this approach. Same thing for I/O-intensive or high-latency jobs (web service call, etc).
The processing/writing source would typically be the bottleneck, so contention should as small as possible.
Restartability is enabled thanks to dedicated flag (reader selects unprocessed items, writer marks the item
as processed just after the business processing).