// Deriving a staged definitional interpreter
package DefCom.Cal4;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Cal {
  abstract static class Expr {
    abstract LExpr located(Map<String, Integer> loc);

    abstract void locate(Map<String, Integer> loc);
  }

  abstract static class LExpr {
    abstract int eval(int[] env);

    abstract String compile();
  }

  static class Lit extends Expr {
    int val;

    Lit(int val) {this.val = val;}

    public String toString() {return String.valueOf(val);}

    LExpr located(Map<String, Integer> loc) {
      return new LExpr() {
        int eval(int[] env) {
          return val;
        }

        String compile() {return String.valueOf(val);}
      };
    }

    void locate(Map<String, Integer> loc) {}
  }

  static Expr mkLit(int val) {return new Lit(val);}

  static class Plus extends Expr {
    Expr left, right;

    Plus(Expr left, Expr right) {
      this.left = left;
      this.right = right;
    }

    public String toString() {return "(" + left.toString() + "+" + right.toString() + ")";}

    LExpr located(Map<String, Integer> loc) {
      LExpr left = this.left.located(loc);
      LExpr right = this.right.located(loc);
      return new LExpr() {
        int eval(int[] env) {
          return left.eval(env) + right.eval(env);
        }

        String compile() {
          return "(" + left.compile() + "+" + right.compile() + ")";
        }
      };
    }

    void locate(Map<String, Integer> loc) {
      left.locate(loc);
      right.locate(loc);
    }
  }

  static Expr mkPlus(Expr left, Expr right) {return new Plus(left, right);}

  static class Mult extends Expr {
    Expr left, right;

    Mult(Expr left, Expr right) {
      this.left = left;
      this.right = right;
    }

    public String toString() {return "(" + left.toString() + "*" + right.toString() + ")";}

    LExpr located(Map<String, Integer> loc) {
      LExpr left = this.left.located(loc);
      LExpr right = this.right.located(loc);
      return new LExpr() {
        int eval(int[] env) {
          return left.eval(env) * right.eval(env);
        }

        String compile() {
          return "(" + left.compile() + "*" + right.compile() + ")";
        }
      };
    }

    void locate(Map<String, Integer> loc) {
      left.locate(loc);
      right.locate(loc);
    }
  }

  static Expr mkMult(Expr left, Expr right) {return new Mult(left, right);}

  static class Var extends Expr {
    String name;

    Var(String name) {this.name = name;}

    public String toString() {return name;}

    LExpr located(Map<String, Integer> loc) {
      int idx = loc.get(name);
      return new LExpr() {
        int eval(int[] env) {
          return env[idx];
        }

        String compile() {return "env[" + idx + "]";}
      };
    }

    void locate(Map<String, Integer> loc) {
      if (!loc.containsKey(name)) {
        loc.put(name, loc.size());
      }
    }
  }

  static Expr mkVar(String name) {return new Var(name);}

  static Expr getExample(int n) {
    Expr ret = mkLit(0);
    for (int i = 0; i < n; ++i) {
      for (int j = 0; j < n; ++j) {
        Expr dotProd = mkLit(0);
        for (int k = 0; k < n; ++k) {
          dotProd = mkPlus(
            dotProd,
            mkMult(mkVar("a_%s_%s".formatted(i, k)), mkVar("b_%s_%s".formatted(k, j))));
        }
        ret = mkPlus(ret, dotProd);
      }
    }
    return ret;
  }

  static Map<String, Integer> getExampleEnv(int n) {
    HashMap<String, Integer> ret = new HashMap<>();
    for (int i = 0; i < n; ++i) {
      for (int j = 0; j < n; ++j) {
        ret.put("a_%s_%s".formatted(i, j), 1);
        ret.put("b_%s_%s".formatted(i, j), 1);
      }
    }
    return ret;
  }

  static int[] envToLocEnv(Map<String, Integer> env, Map<String, Integer> loc) {
    int[] arr = new int[loc.size()];
    for (Map.Entry<String, Integer> x : loc.entrySet()) {
      arr[x.getValue()] = env.get(x.getKey());
    }
    return arr;
  }

  public static void profileLocatedEval(int n, int length) {
    Expr example = getExample(n);
    Map<String, Integer> env = getExampleEnv(n);
    Map<String, Integer> loc = new HashMap<>();
    example.locate(loc);
    LExpr located = example.located(loc);
    int[] locEnv = envToLocEnv(env, loc);
    for (int i = 0; i < length; ++i) {
      located.eval(locEnv);
    }
  }

  public static void profileLocatedCompile(int n, int length) throws Throwable {
    Expr example = getExample(n);
    Map<String, Integer> env = getExampleEnv(n);
    Map<String, Integer> loc = new HashMap<>();
    example.locate(loc);
    LExpr located = example.located(loc);
    Function<int[], Integer> compiled = javac(located.compile());
    int[] locEnv = envToLocEnv(env, loc);
    for (int i = 0; i < length; ++i) {
      compiled.apply(locEnv);
    }
  }

  static Function<int[], Integer> javac(String code) throws Throwable {
    String source =
      "package generated;\n" +
      "import java.util.function.Function;\n" +
      "public class Generated implements Function<int[], Integer> {\n" +
      "  public Integer apply(int[] env) {\n" +
      "    return " + code + ";\n" +
      "  }\n" +
      "}\n";

    File root = Files.createTempDirectory("compile").toFile();
    File sourceFile = new File(root, "generated/Generated.java");
    sourceFile.getParentFile().mkdirs();
    Files.writeString(sourceFile.toPath(), source);

    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    compiler.run(null, null, null, sourceFile.getPath());

    URLClassLoader classLoader = URLClassLoader.newInstance(new URL[] { root.toURI().toURL() });
    Class<?> cls = Class.forName("generated.Generated", true, classLoader);
    Object instance = cls.getDeclaredConstructor().newInstance();
    return (Function<int[], Integer>) instance;
  }

  public static void main(String[] args) {
    try {
      int n = 4;
      int length = 1024 * 1024 * 128;
      long time0 = System.currentTimeMillis();
      profileLocatedEval(n, length);
      long time1 = System.currentTimeMillis();
      profileLocatedCompile(n, length);
      long time2 = System.currentTimeMillis();
      System.out.printf("LocatedEval took %s%n", time1 - time0);
      System.out.printf("LocatedCompile took %s%n", time2 - time1);
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }
}