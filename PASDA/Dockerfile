# Build image with: 
#   `docker build -t pasda .`
# Run container with:
#   `docker run -it pasda /bin/sh`
# Run benchmarks with:
#   `./RunningBenchmarks.sh`
FROM --platform=amd64 ubuntu:23.04

RUN apt-get update -y && apt-get install -y \
    openjdk-8-jdk \
    git

COPY docker/sqlean/sqlean-ubuntu /usr/bin/sqlite3
RUN chmod +x /usr/bin/sqlite3

RUN mkdir -p /home/pasda

COPY benchmarks /home/pasda/benchmarks
COPY Implementation /home/pasda/implementation

RUN mkdir -p /home/pasda/implementation/analysis/results
RUN chmod +x /home/pasda/implementation/RunningBenchmarks.sh

WORKDIR /home/pasda/implementation
