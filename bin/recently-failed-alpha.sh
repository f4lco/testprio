#!/bin/bash
set -eu

. .testprio

seq 0 0.1 1 | xargs -t -Ivalue java -jar $PRIO_JAR \
  recently-failed \
  --host localhost \
  --port 5432 \
  --user ma \
  --db github \
  --project "square/okhttp" \
  --alpha value \
  --output square-okhttp-recently-failed-value.csv

