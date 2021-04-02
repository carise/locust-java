package locust;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.google.common.base.Strings;
import com.google.protobuf.util.JsonFormat;
import com.hubspot.jackson.datatype.protobuf.ProtobufModule;
import locust.git.Git;
import locust.parse.Parse;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Correlate the changes within a git initial/ending ref with the changes' source tree.
 *
 * <p>Locust will report changes correlated to the values of {@link ContextType}.
 */
public final class LocustMain {

  public static void main(String[] args) throws IOException {
    ParsedArguments parsedArgs = parseArgs(args);
    String inputJson = Files.readString(Paths.get(parsedArgs.inputFilename));

    JsonFormat.Parser parser = JsonFormat.parser();
    Git.GitResult.Builder gitResultBuilder = Git.GitResult.newBuilder();
    parser.merge(inputJson, gitResultBuilder);
    Git.GitResult gitResult = gitResultBuilder.build();

    List<List<Object>> defs = definitionsByPatch(gitResult);
    writeOutput(defs, parsedArgs.outputFilename);
  }

  private static ParsedArguments parseArgs(String[] args) {
    Options options = new Options();

    Option input = new Option("i", "input", true, "input file path");
    input.setRequired(true);
    options.addOption(input);

    Option output = new Option("o", "output", true, "output file");
    output.setRequired(false);
    options.addOption(output);

    CommandLineParser parser = new DefaultParser();
    HelpFormatter formatter = new HelpFormatter();
    CommandLine cmd = null;
    try {
      cmd = parser.parse(options, args);
      if (cmd == null) {
        throw new ParseException("Parse failed, unknown reason");
      }
    } catch (ParseException e) {
      System.out.println(e.getMessage());
      formatter.printHelp("java LocustMain", options);

      System.exit(1);
    }

    String inputFilePath = cmd.getOptionValue("input");
    String outputFilePath = cmd.getOptionValue("output");
    return new ParsedArguments(
        inputFilePath,
        Strings.isNullOrEmpty(outputFilePath) ? Optional.empty() : Optional.of(outputFilePath));
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

    if (Strings.isNullOrEmpty(source)) {
      return List.of();
    }

    CompilationUnit compilationUnit = StaticJavaParser.parse(source);
    LocustVisitor locustVisitor = new LocustVisitor();
    locustVisitor.visit(compilationUnit, null);

    return locustVisitor.getDefinitions();
  }

  private static void writeOutput(List<List<Object>> definitions, Optional<String> outFilename)
      throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new ProtobufModule());
    String json = mapper.writeValueAsString(definitions);
    Writer writer;
    if (outFilename.isEmpty()) {
      writer = new OutputStreamWriter(System.out);
    } else {
      writer = new FileWriter(outFilename.get());
    }
    try (BufferedWriter bufferedWriter = new BufferedWriter(writer)) {
      bufferedWriter.write(json);
    }
  }

  private final record ParsedArguments(String inputFilename, Optional<String> outputFilename) {}
}
