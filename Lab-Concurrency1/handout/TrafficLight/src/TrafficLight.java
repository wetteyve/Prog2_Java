// Template for Lab P02 Excercise 1
class TrafficLight {
    private boolean red;

    public TrafficLight() {
        red = true;
    }

    public synchronized void passby() {
        while(red){
            try {
                wait();
            } catch (InterruptedException e) {
                System.err.println("Exception " + e);
            }
        }
    }

    public synchronized void switchToRed() {
        red = true;
    }

    public synchronized void switchToGreen() {
        red = false;
        notifyAll();
        // waiting cars can now pass by
    }
}
