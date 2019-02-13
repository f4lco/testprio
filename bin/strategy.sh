#!/bin/bash
set -e

. .testprio

strategy=$1

function run() {
  java -jar $PRIO_JAR $strategy --project $1 --user ma --output results/$2-$strategy.csv
}

run apache/sling sling
run CloudifySource/cloudify cloudify
run eclipse/jetty.project jetty
run facebook/buck buck
run jOOQ/jOOQ jooq
run jsprit/jsprit jsprit
run SonarSource/sonarqube sonarqube
run square/okhttp okhttp
