## 1）fix multi-threading banking system bugs

My explaination of the bugs:
```
time    <thread1>               <thread2>
|       lock(A)
|                               lock(B)
|       lock(B) // blocked
|                               lock(A) // blocked
↓                 <deadlock>

```
The issue in the original code is the potential deadlock. For example, given 2 threads, thread1 and thread2. Thread1 wants to transfer money from account A to account B, and thread2 wants to transfer money from account B to account A. If thread1 locks account A and thread2 locks account B at the same time, then both threads will be blocked forever.

```
int balance[NO_USERS];
pthread_mutex_t locks[NO_USERS] = {PTHREAD_MUTEX_INITIALIZER};

void transfer(int source, int target, int amount){
    pthread_mutex_lock(&locks[source]);
    if(balance[source] > amount) {
        balance[source] -= amount;
    }
    pthread_mutex_unlock(&locks[source]);

    pthread_mutex_lock(&locks[target]);
    balance[target] += amount;
    pthread_mutex_unlock(&locks[target]);
}
```

## 2）fix multi-threading bugs
### a）
```
condition cond = PTHREAD_COND_INITIALIZER;
mutex lock = PTHREAD_MUTEX_INITIALIZER;
bool done = false;

void *thread1(void *arg) {
    pthread_mutex_lock(&lock);
    while(!done) pthread_cond_wait(&cond, &lock);
    pthread_mutex_unlock(&lock); //perform thread1’s task
}

void *thread2(void *arg) {
    //perform thread2’s task
    pthread_mutex_lock(&lock);
    done = true;
    pthread_cond_broadcast(&cond);
    pthread_mutex_unlock(&lock);
}
```

### b）
```
condition cond = PTHREAD_COND_INITIALIZER;
mutex lock = PTHREAD_MUTEX_INITIALIZER;

void *thread1(void *arg) {
    pthread_mutex_lock(&lock);
    ticket += 3;
    pthread_cond_broadcast(&cond);
    pthread_mutex_unlock(&lock);
}

void *thread2(void *arg) {
    pthread_mutex_lock(&lock);
    while(!(ticket>=2)) pthread_cond_wait(&cond, &lock);
    ticket-=2;
    pthread_mutex_unlock(&lock);
}

void *thread3(void *arg) {
    pthread_mutex_lock(&lock);
    while(!(ticket>=5)) pthread_cond_wait(&cond, &lock);
    ticket-=5;
    pthread_mutex_unlock(&lock);
}
```

## 3）the readers/writers problem (w/ writers' priority) by using semaphores
```
mutex writer_mutex = 1;
mutex reader_mutex = 1
int reader_count = 0;
int writer_count = 0;
mutex writer_count_mutex = 1;
mutex single_writer_mutex = 1;

void *reader(void *arg) {
    P(reader_mutex);
    reader_count++;
    if(reader_count == 1) P(writer_mutex);
    V(reader_mutex);

    //perform reading

    P(reader_mutex);
    reader_count--;
    if(reader_count == 0) V(writer_mutex);
    V(reader_mutex);
}

void *writer(void *arg) {
    P(writer_count_mutex);
    writer_count++;
    if(writer_count == 1) P(writer_mutex);
    V(writer_count_mutex);

    P(single_writer_mutex);

    //perform writing

    V(single_writer_mutex);

    P(writer_count_mutex);
    writer_count--;
    if(writer_count == 0) V(writer_mutex);
    V(writer_count_mutex);
}
```

## 4）alternates between readers and writers by using monitors

```
monitor read_write {
    int fifo_uniq_id = 0;
    int reader_count = 0;
    bool is_writing = false;
    condition read_write_cond;

    void start_read() {
        // there is reader using the resource. feel free to read
        if (reader_count > 0) {
            reader_count++;
            return;
            // perform reading
        }

        // give an ordered fifo id to the current reader
        int current_fifo = ++fifo_uniq_id;
        while (writing) read_write_cond.wait(current_fifo);

        reader_count++;
        // perform reading
    }

    void end_read() {
        // end reading
        reader_count--;
        if (reader_count == 0) read_write_cond.signal();
    }

    void start_write() {
        // there is no reader or writer using the resource. feel free to write
        if (reader_count == 0 && !is_writing) {
            is_writing = true;
            return;
            // perform writing
        }

        // give an ordered fifo id to the current reader
        int current_fifo = ++fifo_uniq_id;
        while (reader_count > 0 || is_writing) read_write_cond.wait(current_fifo);

        is_writing = true;
        // perform writing
    }

    void end_write() {
        // end writing
        is_writing = false;
        read_write_cond.signal();
    }
}
```

## 5）file sharing by using monitors

```
monitor file_sharing {
    int n = FIXED_LIMIT;
    int current = 0;
    condition access_cond;

    void limited_access(int uid) {
        while( current + uid >= n ) access_cond.wait();
        current += uid;
    }

    void release(int uid) {
        current -= uid;
        access_cond.signal();
    }
}
```

## 6）the producer-consumer problem (w/ N buffers) by using Java supports

Java codes:
```java
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
```

Results:
```
[main]           Starting producer-consumer problem...
[main]           Waiting for threads to finish...
[Consumer]       wanted to consume value
[Store]          produced value: 39 at index 0
[Store]          consumed value: 39 at index 0
[Consumer]       wanted to consume value
[Producer]       produced value: 39
[Store]          produced value: 77 at index 1
[Producer]       produced value: 77
[Store]          consumed value: 77 at index 1
[Consumer]       wanted to consume value
[Store]          produced value: 90 at index 2
[Producer]       produced value: 90
[Store]          consumed value: 90 at index 2
[Consumer]       wanted to consume value
[Store]          produced value: 42 at index 3
[Producer]       produced value: 42
[Store]          consumed value: 42 at index 3
[Consumer]       wanted to consume value
[Store]          produced value: 89 at index 4
[Producer]       produced value: 89
[Store]          consumed value: 89 at index 4
[Consumer]       wanted to consume value
[Store]          produced value: 1 at index 0
[Producer]       produced value: 1
[Store]          consumed value: 1 at index 0
[Consumer]       wanted to consume value
[Store]          produced value: 21 at index 1
[Producer]       produced value: 21
[Store]          consumed value: 21 at index 1
[Consumer]       wanted to consume value
[Store]          produced value: 71 at index 2
[Producer]       produced value: 71
[Store]          consumed value: 71 at index 2
[Consumer]       wanted to consume value
[Store]          produced value: 32 at index 3
[Producer]       produced value: 32
[Store]          consumed value: 32 at index 3
[Consumer]       wanted to consume value
[Store]          produced value: 43 at index 4
[Producer]       produced value: 43
[Store]          consumed value: 43 at index 4
[main]           Producer-consumer problem finished.
```

## 7）the readers-writers problem (w/ reader’s priority) by using Java supports

Java codes:
```java
public class CondObj {
    public CondObj() {}
}
```

```java
package hw1;

import java.lang.Thread;

class FakeFileIO {
    private String fileContent;
    private CondObj condition;
    private boolean isUsing;
    private boolean writeBarrier;
    private int readCount;

    public FakeFileIO(String fileContent) {
        System.out.println("[FakeFileIO] \t initialized with content: " + fileContent);
        this.fileContent = fileContent;
        condition = new CondObj();
        isUsing = false;
        writeBarrier = true;
        readCount = 0;
    }

    public void write(String content) {
        synchronized (condition) {
            while (isUsing) {
                try {
                    condition.wait(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // force writer to wait, then reader can read the content.
            while (writeBarrier) {
                try {
                    condition.notifyAll();
                    condition.wait(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                writeBarrier = false;
            }
            
            isUsing = true;
            System.out.println("[FakeFileIO] \t writing content: " + content);
            fileContent = content;
    
            isUsing = false;
            writeBarrier = true;
            condition.notifyAll();
        }
    }

    public String read() {
        synchronized (condition) {
            if (readCount == 0) {
                while (isUsing) {
                    try {
                        condition.wait(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            readCount++;
            if (readCount == 1) {
                isUsing = true;
            }
            
            System.out.println("[FakeFileIO] \t reading content: " + fileContent);
            String content = fileContent;

            readCount--;
            if (readCount == 0) {
                isUsing = false;
                condition.notifyAll();
            }

            return content;
        }
    }
}

class Writer extends Thread {
    private FakeFileIO fileIO;
    private static String[] contents = {
        "content000",
        "content001",
        "content002",
        "content003",
        "content004",
        "content005",
        "content006",
        "content007",
        "content008",
        "content009"
    };

    public Writer(FakeFileIO fileIO) {
        this.fileIO = fileIO;
    }

    @Override
    public void run() {
        for (int i = 0; i < 10; i++) {
            fileIO.write(contents[i]);
            System.out.println("[Writer] \t wrote content: " + contents[i]);
        }
    }
}

class Reader extends Thread {
    private FakeFileIO fileIO;

    public Reader(FakeFileIO fileIO) {
        this.fileIO = fileIO;
    }

    @Override
    public void run() {
        for (int i = 0; i < 10; i++) {
            fileIO.read();
            System.out.println("[Reader] \t read content");
        }
    }
}


public class Problem7 {
    // testing
    public static void main(String[] args) {
        System.out.println("[main] \t Starting reader-writer problem...");

        FakeFileIO fileIO = new FakeFileIO("initial content");
        Writer writer = new Writer(fileIO);
        Reader reader = new Reader(fileIO);

        writer.start();
        reader.start();

        try {
            System.out.println("[main] \t Waiting for threads to finish...");
            writer.join();
            reader.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("[main] \t Reader-writer problem finished.");
    }
}
```


Results:
```
[main]           Starting reader-writer problem...
[FakeFileIO]     initialized with content: initial content
[main]           Waiting for threads to finish...
[FakeFileIO]     reading content: initial content
[Reader]         read content
[FakeFileIO]     writing content: content000
[FakeFileIO]     reading content: content000
[Reader]         read content
[Writer]         wrote content: content000
[FakeFileIO]     reading content: content000
[Reader]         read content
[FakeFileIO]     reading content: content000
[Reader]         read content
[FakeFileIO]     writing content: content001
[Writer]         wrote content: content001
[FakeFileIO]     reading content: content001
[Reader]         read content
[FakeFileIO]     reading content: content001
[Reader]         read content
[FakeFileIO]     writing content: content002
[Writer]         wrote content: content002
[FakeFileIO]     reading content: content002
[Reader]         read content
[FakeFileIO]     reading content: content002
[Reader]         read content
[FakeFileIO]     writing content: content003
[Writer]         wrote content: content003
[FakeFileIO]     reading content: content003
[Reader]         read content
[FakeFileIO]     reading content: content003
[Reader]         read content
[FakeFileIO]     writing content: content004
[Writer]         wrote content: content004
[FakeFileIO]     writing content: content005
[Writer]         wrote content: content005
[FakeFileIO]     writing content: content006
[Writer]         wrote content: content006
[FakeFileIO]     writing content: content007
[Writer]         wrote content: content007
[FakeFileIO]     writing content: content008
[Writer]         wrote content: content008
[FakeFileIO]     writing content: content009
[Writer]         wrote content: content009
[main]           Reader-writer problem finished.
```
