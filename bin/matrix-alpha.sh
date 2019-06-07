#!/bin/bash
set -eu

. .testprio

seq 0 0.1 1 | xargs -t -Ivalue java -jar $PRIO_JAR \
  matrix-naive \
  --host localhost \
  --port 5432 \
  --user ma \
  --db github \
  --project "square/okhttp" \
  --alpha value \
  --window -1 \
  --output square-okhttp-matrix-alpha-value.csv
