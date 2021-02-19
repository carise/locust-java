# locust-java

## Undocumentation

Build and run with the sample code
```shell
mvn package
java -jar target/locust-java-1.0-SNAPSHOT.jar -i sample/git_java.json -o out.json 
```

Run with `locust`
```shell
locust -r . 407a161 7943ac3 --plugins "java -jar ../locust-java/target/locust-java-1.0-SNAPSHOT.jar" | jq     
{
  "locust": [
    {
      "file": "Hello.java",
      "changes": [
        {
          "name": "main",
          "type": "method",
          "line": 2,
          "changed_lines": 1,
          "total_lines": 3,
          "children": []
        },
        {
          "name": "Hello",
          "type": "class",
          "line": 1,
          "changed_lines": 1,
          "total_lines": 5,
          "children": []
        }
      ]
    }
  ],
  "refs": {
    "initial": "407a161",
    "terminal": "7943ac3"
  }
}
```