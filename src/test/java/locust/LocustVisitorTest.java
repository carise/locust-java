package locust;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.hubspot.jackson.datatype.protobuf.ProtobufModule;
import locust.parse.Parse;
import locust.testing.RawDefinitions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

final class LocustVisitorTest {

  @ParameterizedTest
  @CsvFileSource(files = "src/test/resources/test_src_input_output.csv", numLinesToSkip = 1)
  public void testLocustVisitor_hasCorrectDefinitions(String inputSourcePath, String outputPath)
      throws IOException {
    RawDefinitions expected = parseExpectedOutput(outputPath);
    File inputSource = new File(inputSourcePath);
    CompilationUnit compilationUnit = StaticJavaParser.parse(inputSource.getAbsoluteFile());
    LocustVisitor locustVisitor = new LocustVisitor();

    locustVisitor.visit(compilationUnit, null);
    List<Parse.RawDefinition> definitions = locustVisitor.getDefinitions();

    assertThat(definitions).isEqualTo(expected.getDefinitions());
  }

  private RawDefinitions parseExpectedOutput(String filePath) throws IOException {
    String input = Files.readString(Path.of(filePath));
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new ProtobufModule());
    return mapper.readValue(input, RawDefinitions.class);
  }
}
