package locust;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.utils.Pair;
import com.google.protobuf.util.JsonFormat;
import locust.git.Git;
import locust.parse.Parse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class LocustMain {

  private static final class ChangeScope {
    private final String idNodeName;
    private final int startLine;
    private final int endLine;

    ChangeScope(String idNodeName, int startLine, int endLine) {
      this.idNodeName = idNodeName;
      this.startLine = startLine;
      this.endLine = endLine;
    }
  }

  public static void main(String[] args) throws IOException {
    // Arg parsing for -i and -o
    String input = args[0];
    String inputFilename = args[1];
    String output = args[2];
    String outputFilename = args[3];

    String inputJson = Files.readString(Paths.get(inputFilename));
    JsonFormat.Parser parser = JsonFormat.parser();
    Git.GitResult.Builder gitResultBuilder = Git.GitResult.newBuilder();
    parser.merge(inputJson, gitResultBuilder);
    Git.GitResult gitResult = gitResultBuilder.build();

    System.out.println(gitResult);

    calculateChanges(getDefinitionsByPatch(gitResult));
  }

  private static List getDefinitionsByPatch(
      Git.GitResult gitResult) {
    return gitResult.getPatchesList().stream()
        .filter(p -> p.getNewFile().toLowerCase().endsWith(".java"))
        .map(p -> new Pair(p, definitionsInPatch(p)))
        .collect(Collectors.toUnmodifiableList());
  }

  private static List<Parse.RawDefinition> definitionsInPatch(Git.PatchInfo patch) {
    String source = patch.getNewSource();

    if (source == null) {
      return Collections.unmodifiableList(List.of());
    }

    List<Parse.RawDefinition> definitions = new ArrayList<>();
    List<ChangeScope> scope = new ArrayList<>();

    try {
      CompilationUnit compilationUnit = StaticJavaParser.parse(new File(patch.getNewFile()));
      VoidVisitor<?> methodNameVisitor = new MethodNamePrinter();
      methodNameVisitor.visit(compilationUnit, null);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      throw new RuntimeException("failed to parse file due to " + e.getMessage());
    }

    return null;
  }

  private static class MethodNamePrinter extends VoidVisitorAdapter<Void> {
    @Override
    public void visit(MethodDeclaration methodDecl, Void arg) {
      super.visit(methodDecl, arg);
      System.out.println("Method name : " + methodDecl.getNameAsString());
    }
  }

  private static List<Parse.LocustChange> calculateChanges(
      List<Pair<Git.PatchInfo, List<Parse.RawDefinition>>> definitionsByPatch) {
    return Collections.unmodifiableList(List.of());
  }
}
