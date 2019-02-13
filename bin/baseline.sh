#!/bin/bash
set -eu

. .testprio

project=$1
slug=$2

function run_strategy() {
  java -jar ${PRIO_JAR} $1 --user ma --project ${project} --output results/${slug}-$1.csv
}

for strategy in untreated random lru recently-failed matrix matrix-similarity
do
  echo ${strategy}
  run_strategy ${strategy}
done
