#!/bin/bash

seq 0 0.1 1 | xargs -t -Ialpha java -jar build/libs/testprio.jar \
  recently-failed \
  --host localhost \
  --port 4242 \
  --user ma \
  --db github \
  --project "square/okhttp" \
  --alpha alpha \
  --output square-okhttp-recently-failed-alpha.csv

