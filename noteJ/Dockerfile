FROM amazoncorretto:17-alpine
ARG JAR_FILE=build/libs/*.jar
ARG PROFILES
ARG ENV
ARG AWS_ACCESS_KEY_ID
ARG AWS_SECRET_ACCESS_KEY
ARG AWS_REGION
ARG S3_BUCKET_NAME
ARG RDS_ENDPOINT
ARG RDS_NAME
ARG RDS_PASSWORD
ARG RDS_USERNAME
ARG LIVE_SERVER_IP
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["sh", "-c", "java \
  -Dspring.profiles.active=${PROFILES} \
  -Dserver.env=${ENV} \
  -Dcloud.aws.credentials.access-key=${AWS_ACCESS_KEY_ID} \
  -Dcloud.aws.credentials.secret-key=${AWS_SECRET_ACCESS_KEY} \
  -Dcloud.aws.region.static=${AWS_REGION} \
  -Ds3.bucket=${S3_BUCKET_NAME} \
  -Dspring.datasource.url=jdbc:mysql://${RDS_ENDPOINT}:3306/${RDS_NAME} \
  -Dspring.datasource.username=${RDS_USERNAME} \
  -Dspring.datasource.password=${RDS_PASSWORD} \
  -jar app.jar"]

