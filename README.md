# OSU - CSE6431

Operating System

📌 Java environment:
```bash
java -version

# (on MacOS)
# java version "21.0.4" 2024-07-16 LTS
# Java(TM) SE Runtime Environment (build 21.0.4+8-LTS-274)
# Java HotSpot(TM) 64-Bit Server VM (build 21.0.4+8-LTS-274, mixed mode, sharing)

# (on OSU coelinux)
# openjdk version "20.0.2" 2023-07-18
# OpenJDK Runtime Environment (build 20.0.2+9-78)
# OpenJDK 64-Bit Server VM (build 20.0.2+9-78, mixed mode, sharing)
```


- [Homework 1](./hw1/hw1.pdf)
    - [myAnswer](./hw1/myAnswer.md)
    - Reference1: https://medium.com/@reetesh043/java-wait-notify-and-notifyall-methods-3d3b511bd3ae
    - Reference2: https://stackoverflow.com/questions/5887709/getting-random-numbers-in-java
    - Reference3: https://wangwilly.github.io/willywangkaa/2018/07/10/Operating-System-Process-Synchronization/
    - Reference4: https://wangwilly.github.io/willywangkaa/2018/08/04/Operating-System-Process-Synchronization-2/
    - Reference5: https://www.geeksforgeeks.org/static-variables-in-java-with-examples/

- Lab 1
    - Execute: `./scripts/runLab1.sh Database`
    - Debug: `DEBUG=true ./scripts/debugLab1.sh Database`


## Lab1

### Apply test cases

```bash
./scripts/runLab1.sh Database1 ./testcases/lab1/db1_test1.txt

./scripts/runLab1.sh Database2 ./testcases/lab1/db2_test1.txt
```

Test case format:

```text
Read
<TxId> R <RowId>

Write
<TxId> W <RowId> <Value>
```

### Deadlock Prevention

Prompt: Since all rows are known before executing a transaction, you should be able to avoid the deadlock problem.

- ❌ Deadlock Detection: Regularly check for cycles in the resource allocation graph and abort transactions to break deadlocks.
- 🚧 Deadlock Prevention: Ensure that the system will never enter a deadlock state.
    - **Hold and Wait**: Require processes to request all resources at once and prevent them from requesting additional resources while holding resources.
    - (For 2-phase locking) Lock all resources before starting the transaction by ordering the resources and requesting them in order.

### TODO

- [x] Implement the history of operations in the database.
- [x] Compute the serialization graph of the transactions from the history.
- [x] Enumerate all possible serial schedules.
- [x] Use flow algorithms to find a serial schedule from the serialization graph.
