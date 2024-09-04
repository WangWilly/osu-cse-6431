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

TBD

## 7）the readers-writers problem (w/ reader’s priority) by using Java supports

TBD
