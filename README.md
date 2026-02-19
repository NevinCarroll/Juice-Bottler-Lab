# Juice Bottler Lab

This repository contains a **multithreaded simulation of a juice bottling factory** written in Java.  
The goal of this lab is to demonstrate **concurrent programming**, **producer/consumer synchronization**, and **thread lifecycle management** using multiple worker threads that process oranges through several production stages.

## Project Overview

In this simulation:

- A **Company** launches several **Plants**.
- Each *Plant* uses multiple worker threads to process oranges through a pipeline:
    1. **Fetchers** - pick new oranges
    2. **Peelers** - peel oranges
    3. **Squeezers** - squeeze juice
    4. **Bottlers** - bottle the juice
- Each worker requests oranges from the plant, performs simulated work, and returns them for the next stage.
- After a fixed amount of time, the plant shuts down and reports production statistics.

## How It Works

### Production Pipeline

Each *Worker* represents one stage in the pipeline. Workers:

- Continuously request an orange to process.
- Simulate stage work with `Thread.sleep(...)`.
- Return oranges to the next stage stage via synchronized buffers.

The stages in order are:

1. `Fetched` → 2. `Peeled` → 3. `Squeezed` → 4. `Bottled` → 5. `Processed`

Production statistics include:

- Total oranges provided
- Total oranges processed
- Bottles created
- Leftover waste

## How to Build & Run

This project uses Apache Ant for building and running the simulation.

### Prerequisites

Java 21 - https://adoptium.net/temurin/releases?version=21&os=any&arch=any

Apache Ant - https://ant.apache.org/manual/install.html

### Compile and Run

Pull the repo into a local git repo

```
git pull https://github.com/NevinCarroll/Juice-Bottler-Lab
```

Then enter:

```aiignore
ant run
```

into the terminal. This will compile, create, and run the jar file.

## Expected Output

After running for a fixed simulation time (configured in Company.PROCESSING_TIME), the program prints:

Total provided/processed = <X>/<Y>

Created <Bottles>, wasted <Waste> oranges


This shows how many oranges were processed and how many bottles were produced.

