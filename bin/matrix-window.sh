#!/bin/bash
set -eu

. .testprio

# okhttp has 598 observations
seq 0 20 600 | xargs -t -IwindowSize java -jar $PRIO_JAR \
  matrix-naive \
  --host localhost \
  --port 5432 \
  --user ma \
  --db github \
  --project "square/okhttp" \
  --window windowSize \
  --output square-okhttp-matrix-windowSize.csv
