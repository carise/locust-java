package locust;

/** The context type in the AST */
enum ContextType {
  UNKNOWN("unknown"),
  CLASS_DEF("class"),
  INTERFACE_DEF("interface"),
  CONSTRUCTOR_DEF("constructor"),
  METHOD_DEF("method"),
  ENUM_DEF("enum"),
  DEPENDENCY("dependency"),
  // Catch-all for attributes, values, etc.
  USAGE("usage");

  private final String type;

  ContextType(String type) {
    this.type = type;
  }

  String getType() {
    return type;
  }
}
