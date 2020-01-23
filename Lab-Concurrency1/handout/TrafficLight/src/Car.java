class Car extends Thread {
    private TrafficLight[] trafficLights;
    private int pos;

    public Car(String name, TrafficLight[] trafficLights) {
        super(name);
        this.trafficLights = trafficLights;
        pos = 0;                // start at first light
        start();
    }

    public int position() {
        return pos;
    }

    private void gotoNextLight() {
        try {
            sleep((int)(Math.random() * 500));
        } catch (InterruptedException e) {
            System.err.println("Exception " + e);
        }
        pos = (pos + 1) % trafficLights.length;
    }

    public void run() {
        while (true) {
            trafficLights[pos].passby();
            gotoNextLight();
        }
    }
}
