APP_ROOT=~/Apps/Public
declare -A configNameMap
configNameMap["mt0-oauth2"]="AuthService"
configNameMap["mt1-proxy"]="EdgeProxyService"
configNameMap["mt2-user-profile"]="UserProfile"
configNameMap["mt3-product"]="Product"
configNameMap["mt4-messenger"]="Messenger"
configNameMap["mt5-file-upload"]="FileUpload"
configNameMap["mt6-payment"]="Payment"

declare -A configPortMap
configPortMap["mt0-oauth2"]="8080"
configPortMap["mt1-proxy"]="8111"
configPortMap["mt2-user-profile"]="8082"
configPortMap["mt3-product"]="8083"
configPortMap["mt4-messenger"]="8085"
configPortMap["mt5-file-upload"]="8086"
configPortMap["mt6-payment"]="8087"

for i in "${!configNameMap[@]}"; do
  cp ./config/.gitignore $APP_ROOT/$i/.gitignore
  cp ./config/LICENSE $APP_ROOT/$i/LICENSE
  cp ./config/lombok.config $APP_ROOT/$i/lombok.config
  cp ./config/pom.xml $APP_ROOT/$i/shared/parent-pom.xml
  cp ./config/application-shared.properties $APP_ROOT/$i/src/main/resources/application-shared.properties
  cp -r ./src/main/java/com/hw/shared $APP_ROOT/$i/src/main/java/com/hw
  cp ./config/Dockerfile $APP_ROOT/$i/Dockerfile
  sed -i "s/{jar_name}/${configNameMap[$i]}.jar/g" $APP_ROOT/$i/Dockerfile
  sed -i "s/{port_num}/${configPortMap[$i]}/g" $APP_ROOT/$i/Dockerfile
  cp ./config/logback-spring.xml $APP_ROOT/$i/src/main/resources/logback-spring.xml
  sed -i "s/{log_file_name}/${configNameMap[$i]}/g" $APP_ROOT/$i/src/main/resources/logback-spring.xml
done
