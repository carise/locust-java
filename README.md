# locust-java

Java plugin for [locust](https://github.com/bugout-dev/locust)

## Build and run with the sample code

```shell
mvn package
java --enable-preview -jar target/locust-java-1.0-SNAPSHOT.jar -i sample/hello_java.json | jq

# or

java --enable-preview -jar target/locust-java-1.0-SNAPSHOT.jar -i sample/hello_java2.json | jq
```

## Run with `locust`

Using the [locust test cases](https://github.com/bugout-dev/locust-test-cases):

```shell
cd locust-test-cases
# the revisions are test_java_initial and test_java_terminal
locust -r . 407a161 6722c19 --plugins "java --enable-preview -jar ../locust-java/target/locust-java-1.0-SNAPSHOT.jar" | jq
{
  "locust": [
    {
      "file": "Hello.java",
      "changes": [
        {
          "name": "Hello",
          "type": "class",
          "line": 1,
          "changed_lines": 5,
          "total_lines": 9,
          "children": [
            {
              "name": "Hello.main",
              "type": "method",
              "line": 2,
              "changed_lines": 2,
              "total_lines": 3,
              "children": []
            },
            {
              "name": "Hello.hello",
              "type": "method",
              "line": 6,
              "changed_lines": 2,
              "total_lines": 3,
              "children": []
            }
          ]
        }
      ]
    }
  ],
  "refs": {
    "initial": "407a161",
    "terminal": "6722c19"
  }
}
```
