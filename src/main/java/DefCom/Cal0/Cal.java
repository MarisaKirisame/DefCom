// A Pretty Printer and Interpreter for our Language, defined in JSON.
package DefCom.Cal0;

import org.json.JSONObject;

public class Cal {
  static JSONObject getExample() {
    return new JSONObject(
      "{type: 'Multiply', " +
        "left: {type: 'Plus', " +
        "left: {type: 'Literal', value: 1}, " +
        "right: {type: 'Literal', value: 2}}, " +
        "right: {type: 'Plus', " +
        "left: {type: 'Literal', value: 3}, " +
        "right: {type: 'Literal', value: 4}}}");
  }

  static String prettyPrint(JSONObject j) {
    String type = j.getString("type");
    return switch (type) {
      case "Literal" -> String.valueOf(j.getInt("value"));
      case "Plus" -> "(" + prettyPrint(j.getJSONObject("left")) +
        "+" + prettyPrint(j.getJSONObject("right")) + ")";
      case "Multiply" -> "(" + prettyPrint(j.getJSONObject("left")) +
        "*" + prettyPrint(j.getJSONObject("right")) + ")";
      default -> throw new RuntimeException("Unexpected value: " + type);
    };
  }

  static int evaluate(JSONObject j) {
    String type = j.getString("type");
    return switch (type) {
      case "Literal" -> j.getInt("value");
      case "Plus" -> evaluate(j.getJSONObject("left")) + evaluate(j.getJSONObject("right"));
      case "Multiply" -> evaluate(j.getJSONObject("left")) * evaluate(j.getJSONObject("right"));
      default -> throw new RuntimeException("Unexpected value: " + type);
    };
  }

  public static void main(String[] args) {
    try {
      JSONObject example = getExample();
      System.out.println(example);
      System.out.println(prettyPrint(example));
      System.out.println(evaluate(example));
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }
}