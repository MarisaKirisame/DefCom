// Adding Input to our language, and adding a simplification pass
package DefCom.Cal2;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Cal {
  abstract static class Expr {
    abstract int eval(Map<String, Integer> env);

    Expr simp() {return this;}
  }

  static class Lit extends Expr {
    int val;

    Lit(int val) {this.val = val;}

    public String toString() {return String.valueOf(val);}

    int eval(Map<String, Integer> env) {return val;}

    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Lit lit = (Lit) o;
      return val == lit.val;
    }
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

    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Plus plus = (Plus) o;
      return Objects.equals(left, plus.left) && Objects.equals(right, plus.right);
    }

    Expr simp() {
      Expr left = this.left.simp();
      Expr right = this.right.simp();
      if (left instanceof Lit && right instanceof Lit) {
        return mkLit(((Lit) left).val + ((Lit) right).val);
      } else if (left.equals(mkLit(0))) {
        return right;
      } else if (right.equals(mkLit(0))) {
        return left;
      } else {
        return mkPlus(left, right);
      }
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

    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Mult mult = (Mult) o;
      return Objects.equals(left, mult.left) && Objects.equals(right, mult.right);
    }

    Expr simp() {
      Expr left = this.left.simp();
      Expr right = this.right.simp();
      if (left instanceof Lit && right instanceof Lit) {
        return mkLit(((Lit) left).val * ((Lit) right).val);
      } else if (left.equals(mkLit(0)) || right.equals(mkLit(0))) {
        return mkLit(0);
      } else if (left.equals(mkLit(1))) {
        return right;
      } else if (right.equals(mkLit(1))) {
        return left;
      } else {
        return mkMult(left, right);
      }
    }
  }

  static Expr mkMult(Expr left, Expr right) {return new Mult(left, right);}

  static class Var extends Expr {
    String name;

    Var(String name) {this.name = name;}

    public String toString() {return name;}

    int eval(Map<String, Integer> env) {return env.get(name);}

    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Var var = (Var) o;
      return Objects.equals(name, var.name);
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

  public static void main(String[] args) {
    try {
      int n = 2;
      Expr example = getExample(n);
      System.out.println(example);
      Expr simpExample = example.simp();
      System.out.println(simpExample);
      Map<String, Integer> env = getExampleEnv(n);
      System.out.println(example.eval(env));
      System.out.println(simpExample.eval(env));
      Expr oldExample = mkMult(mkPlus(mkLit(1), mkLit(2)), mkPlus(mkLit(3), mkLit(4)));
      System.out.println(oldExample);
      System.out.println(oldExample.simp());
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }
}