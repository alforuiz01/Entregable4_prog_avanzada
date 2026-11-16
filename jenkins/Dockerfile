# Jenkins con Maven, Docker CLI y curl (usa el Docker del host via socket)
FROM jenkins/jenkins:lts-jdk17
USER root
RUN apt-get update \
 && apt-get install -y --no-install-recommends maven docker.io curl \
 && rm -rf /var/lib/apt/lists/*
USER root
