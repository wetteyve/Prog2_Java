import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class GuardedCircularBuffer<T> extends CircularBuffer<T> implements Buffer<T> {

    private Lock mutex;
    private Condition notEmptyAnymore;
    private Condition notFullAnymore;

    public GuardedCircularBuffer(Class<T> clazz, int bufferSize) {
        super(clazz, bufferSize);
        mutex = new ReentrantLock();
        notEmptyAnymore = mutex.newCondition();
        notFullAnymore = mutex.newCondition();
    }

    @Override
    public boolean put(T item) {
        mutex.lock();
        try {
            while (this.full()) {
                notFullAnymore.await();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        boolean success = super.put(item);
        notEmptyAnymore.signal();
        mutex.unlock();
        return success;
    }

    @Override
    public T get() {
        mutex.lock();
        try {
            while (this.empty()) {
                notEmptyAnymore.await();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        T t = super.get();
        notFullAnymore.signal();
        mutex.unlock();
        return t;
    }

    @Override
    public boolean empty() {
        return super.empty();
    }

    @Override
    public boolean full() {
        return super.full();
    }

    @Override
    public int count() {
        return super.count();
    }

    @Override
    public void print() {
        super.print();
    }
}
