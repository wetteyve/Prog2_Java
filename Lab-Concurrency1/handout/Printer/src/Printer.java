public class Printer implements Runnable{
    private char ch;
    private int sleepTime;

    public Printer(char c, int t) {
        ch = c;
        sleepTime = t;
    }

    public void run() {
        System.out.println(Thread.currentThread().getName() + " run laueft an");
        for (int i = 1; i < 100; i++) {
            System.out.print(ch);
            Thread.yield();
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
            }
        }
        System.out.println('\n' + Thread.currentThread().getName() + " run  fertig");
    }

}
