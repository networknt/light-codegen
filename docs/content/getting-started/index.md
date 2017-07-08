---
date: 2017-07-08T10:23:14-04:00
title: Getting Started
---

# Usage

Light-codegen supports all the frameworks we build and it can be easily extended to other
frameworks. For out-of-box generators, please refer to [generators](https://networknt.github.io/light-codegen/generator/)

# Deploy

Most users will use the generator in Java command line, docker command line or script
which can be easily integrated with any DevOp flows.

For users who don't want to clone and build the generator locally, we provide a web
service with UI so that you can generate project remotely and then download/build
locally. 

codegen-web is the web service built to handle request from the Internet. The UI is 
built in React to help user to choose the right parameters and model to control how
generator works. This web service is built on top of light-hybrid-4j framework and
it cannot be started on its own. In order to start the web service, we must first
generate a hybrid server and then put the codegen-web jar file into the classpath
of the server.

Here are the steps to start the codegen-web.



# Customize


