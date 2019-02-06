#!/bin/bash

# okhttp has 598 observations

seq 0 20 300 | xargs -t -IwindowSize java -jar build/libs/testprio.jar \
  matrix \
  --host localhost \
  --port 4242 \
  --user ma \
  --db github \
  --project "square/okhttp" \
  --window windowSize \
  --output square-okhttp-matrix-windowSize.csv

