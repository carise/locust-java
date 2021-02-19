package locust;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.google.protobuf.util.JsonFormat;
import com.hubspot.jackson.datatype.protobuf.ProtobufModule;
import locust.git.Git;
import locust.parse.Parse;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Correlate the changes within a git initial/ending ref with the changes' source tree.
 *
 * <p>Locust will report changes correlated to the values of {@link ContextType}.
 */
public final class LocustMain {

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

    List<List<Object>> defs = definitionsByPatch(gitResult);

    writeOutput(defs, outputFilename);
  }

  /** Build a list of all the changes in a GitResult, paired by patch and its changes. */
  private static List definitionsByPatch(Git.GitResult gitResult) {
    return gitResult.getPatchesList().stream()
        .filter(p -> p.getNewFile().toLowerCase().endsWith(".java"))
        .map(p -> List.of(p, definitionsInPatch(p)))
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

    CompilationUnit compilationUnit = StaticJavaParser.parse(source);

    LocustVisitor locustVisitor = new LocustVisitor();
    locustVisitor.visit(compilationUnit, null);

    return locustVisitor.getDefinitions();
  }

  private static void writeOutput(List<List<Object>> definitions, String outFilename)
      throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new ProtobufModule());
    String json = mapper.writeValueAsString(definitions);
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(outFilename))) {
      writer.write(json);
    }
  }
}
