mvn compile
mvn install -DskipTests clean package

./bin/flume-ng agent -n a1 -c conf/ -f multilpex.conf -Dflume.root.logger=DEBUG,console
./bin/flume-ng avro-client -H 0.0.0.0  -p 44444  -F example.conf  -c conf/ -Dflume.root.logger=DEBUG,console
