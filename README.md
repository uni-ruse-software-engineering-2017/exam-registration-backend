# Exam Registration System for Students (back-end)

[![Build Status](https://travis-ci.org/uni-ruse-software-engineering-2017/exam-registration-backend.svg?branch=master)](https://travis-ci.org/uni-ruse-software-engineering-2017/exam-registration-backend)

This is a [Spring Boot](https://projects.spring.io/spring-boot/) project which exposes a RESTful API.

## Dependencies

You must install the following in order to run and contribute to the project:

1.  [Git](https://git-scm.com/downloads) (_You need to configure your GitHub account and SSH keys - [Instructions](https://help.github.com/articles/connecting-to-github-with-ssh/)_)
2.  [Java 1.8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
3.  [Maven](https://maven.apache.org/download.cgi)
4.  [PostgreSQL 10.0+](https://maven.apache.org/download.cgi)
5.  Eclipse or other Java IDE

You should also install a REST client for easier interacting with the server - we're using [Insomnia](https://insomnia.rest/).

## How to run

You should have JDK 1.8, Maven and Postgre configured properly.

PostgreSQL must be running as a service.

Maven must be added to your PATH, so that you can be able to run Maven commands from the terminal.

You must create a database named `exam-registration`, otherwise the application won't start.

You must set the following environmental variables (for a default `postgres` installation) in your shell
and in Eclipse, if you are going to run the application from there ([Eclipse instructions](https://stackoverflow.com/a/12810433/8597510)):

```
DB_PORT=5432
DB_HOST=localhost
DB_USER=postgres
DB_PASSWORD=postgres
```

You can run the server via the command line:

```bash
mvn spring-boot:run
```

Or you can start it from your IDE of choice.

## How to test

```bash
mvn spring-boot:run
```

or from Eclipse's UI.

## How to contribute

First ensure that you have the latest version of the master branch:

```bash
git checkout master
git pull origin master
```

Choose a task to work on. Create a new branch, for example:

```bash
git checkout -b feat-my-new-feature
```

This will create a new branch called `feat-my-new-feature` and switch the working directory to that new branch.

Implement the functionality and add tests for it.

When you are ready run the test suite. It's preferred to run it from the terminal, since some things
may work in Eclipse, but not on the CI server.

If your local tests pass, commit your changes and add the task ID in the commit message:

```
git add .
git commit -m "[#42] Adds super cool functionality"
```

Push your changes in your feature branch:

```
git push origin feat-my-new-feature
```

Wait for TravisCI to run the tests. If the build succeeds, go into the GitHub UI and create
a new Pull Request for that branch. Otherwise fix your issues until the CI server manages to build
the project successfully.
