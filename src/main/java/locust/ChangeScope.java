package locust;

final class ChangeScope {
  private final String idNodeName;
  private final int startLine;
  private final int endLine;
  // TODO: symbols? https://github.com/bugout-dev/locust/blob/main/locust/parse.py#L44

  ChangeScope(String idNodeName, int startLine, int endLine) {
    this.idNodeName = idNodeName;
    this.startLine = startLine;
    this.endLine = endLine;
  }
}
