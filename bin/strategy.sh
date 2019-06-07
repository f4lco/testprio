#!/bin/bash
set -eu

. .testprio

# Run a single strategy on different GitHub projects.
#
# Example invocation:
# ./strategy.sh lru facebook buck
#
# Example with xargs:
# xargs -t -n2 ./strategy.sh lru <data/projects

strategy=$1
owner=$2
repo=$3

java -jar $PRIO_JAR ${strategy} \
  --project ${owner}/${repo} \
  --user ma \
  --output results/${owner}@${repo}/baseline/${repo}@${strategy}.csv
