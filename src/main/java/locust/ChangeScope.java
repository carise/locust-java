package locust;

/** Describes what has changed within the source tree. */
final class ChangeScope {
  final String idNodeName;
  final int startLine;
  final int endLine;

  ChangeScope(String idNodeName, int startLine, int endLine) {
    this.idNodeName = idNodeName;
    this.startLine = startLine;
    this.endLine = endLine;
  }
}
