#!/bin/bash
set -eu

. .testprio

project=facebook/buck
slug=buck

function run_strategy() {
  java -jar ${PRIO_JAR} $1 --user ma --project ${project} --output ${slug}-$1.csv
}

for strategy in untreated random lru recently-failed matrix
do
  echo ${strategy}
  run_strategy ${strategy}
done
