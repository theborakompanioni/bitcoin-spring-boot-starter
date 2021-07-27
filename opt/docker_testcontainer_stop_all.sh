#!/usr/bin/env bash

set -e

# on systems where ryuk can not be started (and is therefore disabled)
# sometimes testcontainers sshd containers must be stopped and cleaned up
# manually. read more about which images testcontainers make use of here
# https://www.testcontainers.org/supported_docker_environment/image_registry_rate_limiting/
#
# the following images of Testcontainers are used in this project:
# testcontainers/ryuk - performs fail-safe cleanup of containers, and always required (unless Ryuk is disabled)
# testcontainers/sshd - required if exposing host ports to containers

docker ps -a \
  | grep 'tbk-testcontainer-' \
  | awk '{ print $1 }' \
  | xargs --no-run-if-empty docker stop \
  | xargs --no-run-if-empty docker rm

# stop sshd of testcontainers
docker ps -a \
  | grep 'testcontainers/sshd' \
  | awk '{ print $1 }' \
  | xargs --no-run-if-empty docker stop \
  | xargs --no-run-if-empty docker rm