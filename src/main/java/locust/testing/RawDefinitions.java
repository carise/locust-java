package locust.testing;

import locust.parse.Parse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RawDefinitions {
  List<Parse.RawDefinition> definitions;
}
