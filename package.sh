cd back-end
mvn package -Dmaven.test.skip=true 
cp target/back-end-0.0.1-SNAPSHOT-jar-with-dependencies.jar ../ips-backend.jar
cp -R lib/ ../lib
cp -R example-generation/ ../input-generator
cp -R setcover/ ../setcover
cd ..
zip -r ips.zip lib/ ips-backend.jar front-end/ input-generator/ setcover/
rm -r lib/
rm -r input-generator/
rm -r setcover/
rm ips-backend.jar
