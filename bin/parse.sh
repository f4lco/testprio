#!/bin/bash
set -eu

. .testprio

function parse() {
  java -jar $PRIO_JAR parse --logs logfiles/$1 --type ${2:-"maven"} --output parsed/$1.csv
}

parse "adamfisk@LittleProxy"
parse "apache@sling"
parse "brettwooldridge@HikariCP"
parse "CloudifySource@cloudify"
parse "deeplearning4j@deeplearning4j"
parse "DSpace@DSpace"
parse "eclipse@jetty.project"
parse "facebook@buck" "buck"
parse "google@guava"
parse "jcabi@jcabi-github"
parse "jOOQ@jOOQ"
parse "jsprit@jsprit"
parse "l0rdn1kk0n@wicket-bootstrap"
parse "mockito@mockito"
parse "neuland@jade4j"
parse "SonarSource@sonarqube"
parse "square@okhttp"
parse "square@retrofit"
