package mvc.model.datasource;

public abstract class NumberOperation<A, B> implements Runnable {

  private final A a;
  private final B b;
  public NumberOperation(A a, B b) {
    this.a = a;
    this.b = b;
  }

  @Override
  public abstract void run();
}