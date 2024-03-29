#!/bin/bash
set -eu

. .testprio

function run() {
  strategy=$1
  owner=$2
  repo=$3

  java -jar $PRIO_JAR ${strategy} \
    --project ${owner}/${repo} \
    --user ma \
    --output results/${owner}@${repo}/baseline/${repo}@offender-${strategy}.csv
}

while IFS= read -r project; do
  while IFS= read -r strategy; do
    run ${strategy} ${project}
  done <data/strategies-matrix
done <data/projects
