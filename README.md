# OS Scheduling Algorithms Simulator (Java)

A comprehensive Java implementation of five CPU scheduling algorithms for Operating Systems coursework. This project simulates process scheduling with an event-driven simulation approach, supporting multiple scheduling policies with configurable parameters.

## 📋 Overview

This simulator implements **5 scheduling algorithms** using an event-driven simulation model with process state transitions (New → Ready → Running → Blocked/Exit). It reads process definitions from a program file and scheduler configuration from a schedule file, then outputs detailed performance metrics for each process.

## ✨ Features

| Algorithm | Description | Configuration |
|-----------|-------------|---------------|
| **FCFS** | First Come First Served | Non-preemptive, FIFO queue |
| **RR** | Round Robin | Preemptive with configurable time quantum |
| **SPN** | Shortest Process Next | Non-preemptive, selects shortest CPU burst (actual or estimated) |
| **HRRN** | Highest Response Ratio Next | Non-preemptive, selects highest (waiting + service)/service ratio |
| **Feedback** | Multi-level Feedback Queue | Multiple priority queues with aging/timeouts |

## 📊 Output Metrics

For each process, the simulator calculates:
- **Arrival Time** - When process enters system
- **Service Time** - Total CPU time consumed
- **Start Time** - First time process gets CPU
- **Finish Time** - When process terminates
- **Turnaround Time** - Finish - Arrival
- **Normalized Turnaround** - Turnaround / Service
- **Average Response Time** - Average wait between ready and running

Plus system-wide averages:
- Mean Turnaround Time
- Mean Normalized Turnaround
- Mean Average Response Time

## 🚀 Getting Started

### Prerequisites
- Java 8+ (JDK)
- Terminal/command line

### Building & Running

```bash
# Compile
javac Scheduling_Algorithms.java

# Run with schedule file and program file
java Scheduling_Algorithms <schedule_file> <program_file>
```

### Example Runs

```bash
# First Come First Served
java Scheduling_Algorithms fcfs.sf test.pf

# Round Robin (quantum=5)
java Scheduling_Algorithms rr.sf test.pf

# Shortest Process Next (with estimated service times)
java Scheduling_Algorithms spn.sf test.pf

# Highest Response Ratio Next
java Scheduling_Algorithms hrrn.sf test.pf

# Feedback Queue (quantum=50, 3 priority levels)
java Scheduling_Algorithms fb.sf test.pf
```

## 📁 File Formats

### Schedule File (`.sf`)
Configuration for the scheduler algorithm:

**FCFS** (`fcfs.sf`):
```
FCFS
```

**Round Robin** (`rr.sf`):
```
RR
quantum=5
```

**SPN** (`spn.sf`):
```
SPN
service_given=false
```

**HRRN** (`hrrn.sf`):
```
HRRN
service_given=false
```

**Feedback** (`fb.sf`):
```
FEEDBACK
quantum=50
num_priorities=3
```

### Program File (`.pf`)
Process definitions - one per line:
```
<arrival_time> <event1> <time1> <event2> <time2> ...
```

Events: `CPU` (compute), `IO` (blocking I/O)

**Example** (`test.pf`):
```
0 CPU 10 IO 3 CPU 50 IO 3 CPU 10
4 CPU 3 IO 3 CPU 2 IO 3 CPU 4 IO 7 CPU 2
5 CPU 100 IO 4 CPU 3
```

Process 0: Arrives at 0, CPU 10 → IO 3 → CPU 50 → IO 3 → CPU 10
Process 1: Arrives at 4, CPU 3 → IO 3 → CPU 2 → IO 3 → CPU 4 → IO 7 → CPU 2
Process 2: Arrives at 5, CPU 100 → IO 4 → CPU 3

## 🏗️ Architecture

### Core Classes

```
Scheduling_Algorithms.java (main)
├── Event class
│   - event type (Arrive, BLOCK, UNBLOCK, EXIT, TIMEOUT)
│   - timestamp
│   - process reference
│   - Comparable for priority queue ordering
├── process class (PCB)
│   - PID, arrival, service, start, finish times
│   - Response time tracking (ready→running transitions)
│   - Service Prediction (SP) with exponential averaging (α=0.8)
│   - Priority number for Feedback queues
│   - Event parsing from program file line
└── eventComparator / processComparator
    - Sorting utilities for event queue and exit queue
```

### Process States & Transitions

```
NEW → (Arrive) → READY → (Dispatch) → RUNNING
                              ↑              ↓
                              │         (Timeout) → AUXILIARY → READY
                              │              ↓
                              └──── (Block/IO) → BLOCKED → (Unblock) ──┘
                                                                    ↓
                                                              (Exit) → EXIT
```

### Scheduling Logic

| Algorithm | Ready Queue Selection | Preemptive? |
|-----------|----------------------|-------------|
| FCFS | FIFO (first arrived) | No |
| RR | FIFO with time quantum | Yes (timeout) |
| SPN | Shortest `eventTime` or `SP` | No |
| HRRN | Highest `(wait + service)/service` | No |
| Feedback | Highest priority non-empty queue | Yes (timeout + priority decay) |

### Exponential Averaging (SPN/HRRN)

When `service_given=false`, next CPU burst is estimated:
```
SP_new = α × actual_last_burst + (1-α) × SP_old
α = 0.8
```

### Response Time Tracking

Response times recorded as pairs: `[ready_time, running_time]`
Average = Σ(running - ready) / count

## 📈 Sample Output

```
Process 0 :
    Arrive = 0
    Service = 70
    Start = 0
    Finish = 78
    Turnaround = 78
    Normalized Turnaround = 1.1142857142857143
    Average Response Time = 3.0

Process 1 :
    Arrive = 4
    Service = 21
    Start = 10
    Finish = 38
    Turnaround = 34
    Normalized Turnaround = 1.619047619047619
    Average Response Time = 4.5

Process 2 :
    Arrive = 5
    Service = 103
    Start = 38
    Finish = 141
    Turnaround = 136
    Normalized Turnaround = 1.320388349514563
    Average Response Time = 33.0

Mean Turnaround: 82.66666666666667
Mean Normalized Turnaround: 1.351240560949312
Mean Average Response Time: 13.5
```

## 🎯 Learning Outcomes Demonstrated

- **Event-driven simulation** architecture
- **Process state management** (PCB design)
- **Scheduling algorithm implementation** (5 policies)
- **Priority queue** with custom comparators
- **Exponential averaging** for burst prediction
- **Multi-level feedback queues** with aging
- **Performance metrics** calculation (turnaround, response, normalized)
- **File I/O parsing** with error handling
- **Command-line argument** processing

## 📝 Coursework Context

- **Course**: Operating Systems (CS 4341/equivalent)
- **Project**: Project 3 - Scheduling Algorithms
- **Author**: Victor Vu (NetID: vhv180000)
- **Date**: December 2021

## 📄 License

Educational project - No license specified