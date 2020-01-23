import java.util.Locale;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/*
 * Controls the traffic passing the bridge
 */
public class TrafficController {
    private boolean bridgeOccupied = false;
    private ReentrantLock mutex = new ReentrantLock();
    private Condition leftWaiting = mutex.newCondition();
    private Condition rightWaiting = mutex.newCondition();

    /* Called when a car wants to enter the bridge form the left side */
    public void enterLeft() {
        mutex.lock();
        try {
            while (bridgeOccupied) {
                leftWaiting.await();
            }
            bridgeOccupied = true;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mutex.unlock();
    }

    /* Called when a wants to enter the bridge form the right side */
    public void enterRight() {
        mutex.lock();
        try {
            while (bridgeOccupied) {
                rightWaiting.await();
            }
            bridgeOccupied = true;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mutex.unlock();
    }

    ;

    /* Called when the car leaves the bridge on the left side */
    public void leaveLeft() {
        mutex.lock();
        if(!mutex.hasWaiters(rightWaiting)){
            bridgeOccupied = false;
            leftWaiting.signalAll();
        } else {
            bridgeOccupied = false;
            rightWaiting.signalAll();
        }
        mutex.unlock();
    }

    ;

    /* Called when the car leaves the bridge on the right side */
    public void leaveRight() {
        mutex.lock();
        if(!mutex.hasWaiters(leftWaiting)){
            bridgeOccupied = false;
            rightWaiting.signalAll();
        } else {
            bridgeOccupied = false;
            leftWaiting.signalAll();
        }
        mutex.unlock();
    }

    ;
}
