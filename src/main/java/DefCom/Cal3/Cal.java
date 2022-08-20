package DefCom.Cal3;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Cal {
  abstract static class Expr {
    abstract int eval(Map<String, Integer> env);

    abstract int yolo(Map<String, Integer> loc, int[] env);

    abstract Function<int[], Integer> again(Map<String, Integer> loc);

    abstract void locate(Map<String, Integer> loc);
  }

  static class Lit extends Expr {
    int val;

    Lit(int val) {this.val = val;}

    public String toString() {return String.valueOf(val);}

    int eval(Map<String, Integer> env) {return val;}

    Function<int[], Integer> again(Map<String, Integer> loc) {return env -> val;}

    int yolo(Map<String, Integer> loc, int[] env) {return val;}

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

    int eval(Map<String, Integer> env) {return left.eval(env) + right.eval(env);}

    int yolo(Map<String, Integer> loc, int[] env) {return left.yolo(loc, env) + right.yolo(loc, env);}

    Function<int[], Integer> again(Map<String, Integer> loc) {
      Function<int[], Integer> left = this.left.again(loc);
      Function<int[], Integer> right = this.right.again(loc);
      return env -> left.apply(env) + right.apply(env);
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

    int eval(Map<String, Integer> env) {return left.eval(env) * right.eval(env);}

    int yolo(Map<String, Integer> loc, int[] env) {return left.yolo(loc, env) * right.yolo(loc, env);}

    Function<int[], Integer> again(Map<String, Integer> loc) {
      Function<int[], Integer> left = this.left.again(loc);
      Function<int[], Integer> right = this.right.again(loc);
      return env -> left.apply(env) * right.apply(env);
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

    int yolo(Map<String, Integer> loc, int[] env) {
      int idx = loc.get(name);
      return env[idx];
    }

    Function<int[], Integer> again(Map<String, Integer> loc) {
      int idx = loc.get(name);
      return env -> env[idx];
    }

    int eval(Map<String, Integer> env) {return env.get(name);}

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

  public static void profileEval(int n, int length) {
    Expr example = getExample(n);
    Map<String, Integer> env = getExampleEnv(n);
    for (int i = 0; i < length; ++i) {
      example.eval(env);
    }
  }

  public static void profileYolo(int n, int length) {
    Expr example = getExample(n);
    Map<String, Integer> env = getExampleEnv(n);
    Map<String, Integer> loc = new HashMap<>();
    example.locate(loc);
    int[] locEnv = envToLocEnv(env, loc);
    for (int i = 0; i < length; ++i) {
      example.yolo(loc, locEnv);
    }
  }

  public static void profileAgain(int n, int length) {
    Expr example = getExample(n);
    Map<String, Integer> env = getExampleEnv(n);
    Map<String, Integer> loc = new HashMap<>();
    example.locate(loc);
    Function<int[], Integer> located = example.again(loc);
    int[] locEnv = envToLocEnv(env, loc);
    for (int i = 0; i < length; ++i) {
      located.apply(locEnv);
    }
  }

  public static void main(String[] args) {
    try {
      int n = 4;
      int length = 1024 * 1024;
      long time0 = System.currentTimeMillis();
      profileEval(n, length);
      long time1 = System.currentTimeMillis();
      profileYolo(n, length);
      long time2 = System.currentTimeMillis();
      profileAgain(n, length);
      long time3 = System.currentTimeMillis();
      System.out.printf("Eval took %s%n", time1 - time0);
      System.out.printf("Yolo took %s%n", time2 - time1);
      System.out.printf("Again took %s%n", time3 - time2);
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }
}