// Removing JSON and using Java Object instead, resulting in cleaner code
package DefCom.Cal1;

public class Cal {

  abstract static class Expr {
    abstract int eval();
  }

  static class Lit extends Expr {
    int val;

    Lit(int val) {this.val = val;}

    public String toString() {return String.valueOf(val);}

    int eval() {return val;}
  }

  static Expr mkLit(int val) {return new Lit(val);}

  static class Plus extends Expr {
    Expr left, right;

    Plus(Expr left, Expr right) {
      this.left = left;
      this.right = right;
    }

    public String toString() {return "(" + left.toString() + "+" + right.toString() + ")";}

    int eval() {return left.eval() + right.eval();}
  }

  static Expr mkPlus(Expr left, Expr right) {return new Plus(left, right);}

  static class Mult extends Expr {
    Expr left, right;

    Mult(Expr left, Expr right) {
      this.left = left;
      this.right = right;
    }

    public String toString() {return "(" + left.toString() + "*" + right.toString() + ")";}

    int eval() {return left.eval() * right.eval();}
  }

  static Expr mkMult(Expr left, Expr right) {return new Mult(left, right);}

  static Expr getExample() {
    return mkMult(mkPlus(mkLit(1), mkLit(2)), mkPlus(mkLit(3), mkLit(4)));
  }

  static String pp(Expr expr) {
    if (expr instanceof Lit) {
      return String.valueOf(((Lit) expr).val);
    } else if (expr instanceof Plus) {
      return "(" + pp(((Plus) expr).left) + "+" + pp(((Plus) expr).right) + ")";
    } else if (expr instanceof Mult) {
      return "(" + pp(((Mult) expr).left) + "*" + pp(((Mult) expr).right) + ")";
    } else {
      throw new RuntimeException("Unexpected value: " + expr.getClass());
    }
  }

  static int eval(Expr expr) {
    if (expr instanceof Lit) {
      return ((Lit) expr).val;
    } else if (expr instanceof Plus) {
      return eval(((Plus) expr).left) + eval(((Plus) expr).right);
    } else if (expr instanceof Mult) {
      return eval(((Mult) expr).left) * eval(((Mult) expr).right);
    } else {
      throw new RuntimeException("Unexpected value: " + expr.getClass());
    }
  }

  public static void main(String[] args) {
    try {
      Expr example = getExample();
      System.out.println(pp(example));
      System.out.println(eval(example));
      System.out.println(example);
      System.out.println(example.eval());
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }
}