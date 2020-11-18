package locust;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import locust.git.Git;

import java.io.IOException;
import java.nio.file.Files;

import java.nio.file.Paths;

public final class LocustMain {

  public static void main(String[] args) throws IOException {
    // Arg parsing for -i and -o
    String input = args[0];
    String inputFilename = args[1];
    String output = args[2];
    String outputFilename = args[3];

    String inputJson = Files.readString(Paths.get(inputFilename));
    JsonFormat.Parser parser = JsonFormat.parser();
    Git.GitResult.Builder gitResult = Git.GitResult.newBuilder();
    parser.merge(inputJson, gitResult);

    System.out.println(gitResult.build());
  }
}
