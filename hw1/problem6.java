package hw1;

import java.lang.Thread;
import java.util.Random;

class Store {
    static int[] sharedResource = {0, 0, 0, 0, 0};
    static int currProduceIdx = 0;
    static int currConsumeIdx = 0;

    public synchronized void produce (int value) {
        while (sharedResource[currProduceIdx] != 0) {
            try {
                wait(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("[Store] \t produced value: " + value + " at index " + currProduceIdx);
        sharedResource[currProduceIdx] = value;
        currProduceIdx = (currProduceIdx + 1) % 5; // next index to produce

        notifyAll();
    }

    public synchronized void consume () {
        while (sharedResource[currConsumeIdx] == 0) {
            try {
                wait(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("[Store] \t consumed value: " + sharedResource[currConsumeIdx] + " at index " + currConsumeIdx);
        sharedResource[currConsumeIdx] = 0;
        currConsumeIdx = (currConsumeIdx + 1) % 5; // next index to consume

        notifyAll();
    }
}

class Producer extends Thread {
    private Store store;

    public Producer(Store store) {
        this.store = store;
    }

    @Override
    public void run() {
        Random rand = new Random();
        for (int i = 0; i < 10; i++) {
            int value = rand.nextInt(100);
            store.produce(value);
            System.out.println("[Producer] \t produced value: " + value);
        }
    }
}

class Consumer extends Thread {
    private Store store;

    public Consumer(Store store) {
        this.store = store;
    }

    @Override
    public void run() {
        for (int i = 0; i < 10; i++) {
            System.out.println("[Consumer] \t wanted to consume value");
            store.consume();
        }
    }
}

public class Problem6 {
    // testing
    public static void main(String[] args) {
        System.out.println("[main] \t Starting producer-consumer problem...");

        Store store = new Store();
        Producer producer = new Producer(store);
        Consumer consumer = new Consumer(store);

        producer.start();
        consumer.start();

        try {
            System.out.println("[main] \t Waiting for threads to finish...");
            producer.join();
            consumer.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("[main] \t Producer-consumer problem finished.");
    }
}
