public class Account {

    private int id;
    private int saldo = 0;
    private volatile boolean locked = false;

    public Account(int id, int initialSaldo) {
        this.id = id;
        this.saldo = initialSaldo;
    }

    public int getId() {
        return id;
    }

    public int getSaldo() {
        return saldo;
    }

    public synchronized void changeSaldo(int delta) {
        this.saldo += delta;
    }
}
