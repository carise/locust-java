package locust;

import com.github.javaparser.Range;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import locust.parse.Parse;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** A Java parser with visitation logic. */
final class LocustVisitor extends VoidVisitorAdapter<Void> {
  private final List<Parse.RawDefinition> definitions;
  private List<ChangeScope> scope;

  LocustVisitor() {
    super();
    definitions = new ArrayList<>();
    scope = new ArrayList<>();
  }

  List<Parse.RawDefinition> getDefinitions() {
    return definitions;
  }

  List<ChangeScope> getScope() {
    return scope;
  }

  @Override
  public void visit(ImportDeclaration importDecl, Void state) {
    super.visit(importDecl, state);
    processDeclaration(importDecl, importDecl.getNameAsString(), ContextType.DEPENDENCY);
  }

  @Override
  public void visit(ClassOrInterfaceDeclaration classInterfaceDecl, Void state) {
    super.visit(classInterfaceDecl, state);
    processDeclaration(
        classInterfaceDecl,
        classInterfaceDecl.getNameAsString(),
        classInterfaceDecl.isInterface() ? ContextType.INTERFACE_DEF : ContextType.CLASS_DEF);
  }

  @Override
  public void visit(ConstructorDeclaration constructorDecl, Void state) {
    super.visit(constructorDecl, state);
    processDeclaration(
        constructorDecl, constructorDecl.getNameAsString(), ContextType.CONSTRUCTOR_DEF);
  }

  @Override
  public void visit(MethodDeclaration methodDecl, Void state) {
    super.visit(methodDecl, state);
    processDeclaration(methodDecl, methodDecl.getNameAsString(), ContextType.METHOD_DEF);
  }

  @Override
  public void visit(EnumDeclaration enumDecl, Void state) {
    super.visit(enumDecl, state);
    processDeclaration(enumDecl, enumDecl.getNameAsString(), ContextType.ENUM_DEF);
  }

  void processDeclaration(Node node, String nodeName, ContextType changeType) {
    Range nodeRange = node.getRange().get();

    // deduce parent
    scope =
        scope.stream().filter(s -> s.endLine > nodeRange.begin.line).collect(Collectors.toList());
    Optional<Parse.DefinitionParent> defParent = Optional.empty();
    if (!scope.isEmpty()) {
      ChangeScope changeScopeParent = scope.get(scope.size() - 1);
      defParent =
          Optional.of(
              Parse.DefinitionParent.newBuilder()
                  .setName(changeScopeParent.idNodeName)
                  .setLine(changeScopeParent.startLine)
                  .build());
    }
    if (!defParent.isEmpty()) {
      nodeName = String.format("%s.%s", defParent.get().getName(), nodeName);
    }

    // Then build the definition
    Parse.RawDefinition.Builder def =
        Parse.RawDefinition.newBuilder()
            .setName(nodeName)
            .setChangeType(changeType.getType())
            .setLine(nodeRange.begin.line)
            .setOffset(nodeRange.begin.column)
            .setEndLine(nodeRange.end.line)
            .setEndOffset(nodeRange.end.column);
    defParent.ifPresent(p -> def.setParent(p));

    definitions.add(def.build());
  }
}
