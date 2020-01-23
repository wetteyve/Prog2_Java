public class Printer2 {
    public static void main(String[] arg) {
        Printer a = new Printer('.', 0);
        Printer b = new Printer('*', 0);
        Thread t1 = new Thread(a, "PrinterA");
        Thread t2 = new Thread(b, "PrinterB");
        t1.start();
        t2.start();
    }
}
