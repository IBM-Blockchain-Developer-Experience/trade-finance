FROM gradle:5.5.1-jdk8 AS TEMP_BUILD_IMAGE
COPY ./sp-rest /home/sp-rest
COPY ./assets /home/assets
COPY ./ledger-api /home/ledger-api
COPY ./common /home/common
WORKDIR /home/sp-rest
RUN gradle build

FROM openjdk:8
ENV ARTIFACT_NAME=sp-rest.zip
WORKDIR /home/tmp
COPY --from=TEMP_BUILD_IMAGE /home/sp-rest/build/distributions .
RUN unzip $ARTIFACT_NAME -d /home
WORKDIR /home/sp-rest
COPY ./sp-rest/start.sh /home/sp-rest/start.sh
RUN rm -rf /home/tmp
CMD ["./start.sh"]
