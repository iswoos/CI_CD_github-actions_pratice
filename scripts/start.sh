#!/usr/bin/env bash

PROJECT_ROOT="/home/ubuntu/app"
JAR_FILE="$PROJECT_ROOT/spring-webapp.jar"

APP_LOG="$PROJECT_ROOT/application.log"
ERROR_LOG="$PROJECT_ROOT/error.log"
DEPLOY_LOG="$PROJECT_ROOT/deploy.log"

TIME_NOW=$(date +%c)

# build 파일 복사
echo "$TIME_NOW > $JAR_FILE 파일 복사" >> $DEPLOY_LOG
cp $PROJECT_ROOT/build/libs/*.jar $JAR_FILE

echo "> $JAR_NAME에 실행권한 추가"
chmod +x $JAR_NAME

export AWS_url=$(echo “$(aws ssm get-parameters —url /aaa/bbb/password —with-decryption —query Parameters[0].Value)” | sed -e ’s/^”//‘ -e ’s/“$//‘)
export AWS_username=$(echo “$(aws ssm get-parameters —username /aaa/bbb/password —with-decryption —query Parameters[0].Value)” | sed -e ’s/^”//‘ -e ’s/“$//‘)
export AWS_password=$(echo “$(aws ssm get-parameters —password /aaa/bbb/password —with-decryption —query Parameters[0].Value)” | sed -e ’s/^”//‘ -e ’s/“$//‘)

# jar 파일 실행
echo "$TIME_NOW > $JAR_FILE 파일 실행" >> $DEPLOY_LOG
nohup java -jar $JAR_FILE > $APP_LOG 2> $ERROR_LOG &

CURRENT_PID=$(pgrep -f $JAR_FILE)
echo "$TIME_NOW > 실행된 프로세스 아이디 $CURRENT_PID 입니다." >> $DEPLOY_LOG