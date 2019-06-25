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
    --cache cache-tr-git-commits-in-push \
    --patches "tr_commits_in_push" \
    --output results/${owner}@${repo}/commits-in-push/${repo}@${strategy}.csv
}

while IFS= read -r project; do
  while IFS= read -r strategy; do
    run ${strategy} ${project}
  done <data/strategies-matrix
done <data/projects


