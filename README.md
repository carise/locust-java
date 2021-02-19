# locust-java

## Undocumentation

Build and run with the sample code
```shell
mvn package
java -jar target/locust-java-1.0-SNAPSHOT.jar -i sample/git_java.json -o out.json 
```

Run with `locust`
```shell
locust -r ../locust-test-cases 407a161 7943ac3 --plugins "java -jar target/locust-java-1.0-SNAPSHOT.jar"
```