FROM node:9-alpine

MAINTAINER "Nicholas Azar <nicholas@nicholasazar.com>"

ADD yarn.lock /yarn.lock
ADD package.json /package.json

ENV NODE_PATH=/node_modules
ENV PATH=$PATH:/node_modules/.bin

RUN yarn install

WORKDIR /app
ADD . /app

EXPOSE 3000
EXPOSE 35729

RUN npm run setDockerProxy

ENTRYPOINT ["/bin/sh", "/app/run.sh"]
CMD ["start"]

# Considering using a build like this
# https://medium.com/@detrohutt/thanks-for-the-article-it-was-a-big-help-8d517cf69982