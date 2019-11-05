FROM gradle:5.5.1-jdk8 AS TEMP_BUILD_IMAGE
COPY ./po-rest /home/po-rest
COPY ./assets /home/assets
COPY ./ledger-api /home/ledger-api
COPY ./common /home/common
WORKDIR /home/po-rest
RUN gradle build

FROM openjdk:8
ENV ARTIFACT_NAME=po-rest.zip
WORKDIR /home/tmp
COPY --from=TEMP_BUILD_IMAGE /home/po-rest/build/distributions .
RUN unzip $ARTIFACT_NAME -d /home
WORKDIR /home/po-rest
COPY ./po-rest/start.sh /home/po-rest/start.sh
RUN rm -rf /home/tmp
CMD ["./start.sh"]
