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
import java.util.List;
import java.util.stream.Collectors;

/**
 * Correlate the changes within a git initial/ending ref with the changes' source tree.
 *
 * <p>Locust will report changes correlated to the values of {@link ContextType}.
 */
public final class LocustMain {

  private static final class ChangeScope {
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

  enum ContextType {
    UNKNOWN("unknown"),
    CLASS_DEF("class"),
    METHOD_DEF("method"),
    ENUM_DEF("enum"),
    DEPENDENCY("dependency"),
    // Catch-all for attributes, values, etc.
    USAGE("usage");

    private final String name;

    ContextType(String name) {
      this.name = name;
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

    List<Parse.LocustChange> changes =
        calculateChanges(gitResult, getDefinitionsByPatch(gitResult));
  }

  /** Build a list of all the changes in a GitResult, paired by patch and its changes. */
  private static List getDefinitionsByPatch(Git.GitResult gitResult) {
    return gitResult.getPatchesList().stream()
        .filter(p -> p.getNewFile().toLowerCase().endsWith(".java"))
        .map(p -> new Pair(p, definitionsInPatch(p)))
        .collect(Collectors.toUnmodifiableList());
  }

  /**
   * Build a list of the changes in a git patch.
   *
   * <p>The patch directly correlates to a single Java file.
   */
  private static List<Parse.RawDefinition> definitionsInPatch(Git.PatchInfo patch) {
    String source = patch.getNewSource();

    if (source == null || source.isEmpty()) {
      return List.of();
    }

    List<Parse.RawDefinition> definitions = new ArrayList<>();
    List<ChangeScope> scope = new ArrayList<>();

    try {
      CompilationUnit compilationUnit = StaticJavaParser.parse(new File(patch.getNewFile()));
      VoidVisitor<?> methodNameVisitor = new MethodNamePrinter();
      methodNameVisitor.visit(compilationUnit, null);
      // TODO
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      throw new RuntimeException("failed to parse file due to " + e.getMessage());
    }

    return null;
  }

  /** Temporary: visitor that prints the method names when visited. */
  private static final class MethodNamePrinter extends VoidVisitorAdapter<Void> {
    @Override
    public void visit(MethodDeclaration methodDecl, Void arg) {
      super.visit(methodDecl, arg);
      System.out.println("Method name : " + methodDecl.getNameAsString());
    }
  }

  /** Build the locust metadata for all of the git changes. */
  private static List<Parse.LocustChange> calculateChanges(
      Git.GitResult gitResult,
      List<Pair<Git.PatchInfo, List<Parse.RawDefinition>>> definitionsByPatch) {
    List<Parse.LocustChange> changes = new ArrayList<>();
    for (Pair<Git.PatchInfo, List<Parse.RawDefinition>> patchAndDef : definitionsByPatch) {
      Git.PatchInfo patch = patchAndDef.a;
      List<Parse.RawDefinition> definitions = patchAndDef.b;
      Pair<Git.PatchInfo, Parse.LocustChange> change =
          locustChangesInPatch(patch, definitions, gitResult);
      changes.add(change.b);
    }
    return changes;
  }

  /** Builds the locust metadata for a single git patch. */
  private static Pair<Git.PatchInfo, Parse.LocustChange> locustChangesInPatch(
      Git.PatchInfo patch, List<Parse.RawDefinition> definitions, Git.GitResult gitResult) {
    // TODO
    return new Pair(patch, Parse.LocustChange.getDefaultInstance());
  }
}
