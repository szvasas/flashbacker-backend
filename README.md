# Welcome to Flashbacker

Flashbacker is a minimalist app which helps you to save the precious memories of your life in a form
of tiny written stories. It's not a tool for writing a journal, it's more like a space for the "tweets"
you send to your future self to be able to relive the moments which were special for you.

## Introduction
This repository contains the code of the backend only. Please refer to
[this link](https://gitlab.com/flashbacker/frontend) to access the code of the frontend.

The QA environment of the application is accessible on
[https://flashbacker-qa.vasas.dev](https://flashbacker-qa.vasas.dev).

## Building
This project is a pretty standard Gradle project so `./gradlew assemble` does the trick.

## Testing
The project uses [Testcontainers](https://www.testcontainers.org/) to start up a local DynamoDB instance
for the integration tests so you will need a configured Docker environment on your development machine
to be able to run all of the tests.

Once you have that just run `./gradlew test`.

## Packaging
The built project is distributed in a Docker image which is created by the GitLab pipeline using
[Jib](https://github.com/GoogleContainerTools/jib) and pushed to the GitLab registry of this repository.
Please refer to [.gitlab-ci.yml](.gitlab-ci.yml) for further details.

## Deploying
The project is meant to be deployed on a Kubernetes cluster. Please refer to
[this repository](https://gitlab.com/flashbacker/deployment) for the manifest files.
