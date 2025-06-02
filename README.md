# Distributed Key-Value Store with Chord and Suzuki-Kasami Mutex

This project is a distributed key-value store based on the Chord distributed hash table (DHT) algorithm, enhanced with Suzuki-Kasami's distributed mutual exclusion (mutex) protocol for safe concurrent operations. The system features node failure detection and buddy-based data backup to guarantee fault tolerance.

---

## Features

- **Chord DHT**: Each node stores part of the key space and can join or leave the system dynamically. Keys are distributed using consistent hashing.
- **Suzuki-Kasami Mutex**: Guarantees mutual exclusion for critical operations (joins, leaves, key transfers, etc.) using a circulating token and request queue.
- **Failure Detection**: Each node periodically pings its predecessor and successor. Failure is detected in two stages (suspect/confirmed) based on response time.
- **Buddy Backup**: Every node keeps a backup of its predecessor's and successor's data. On node failure, buddies restore data and maintain system integrity.
- **Concurrent Join/Leave**: Nodes can enter or leave the ring concurrently; mutual exclusion prevents race conditions and state corruption.
- **Recovery**: If the circulating token is lost due to node failure, the system automatically detects the loss and generates a new token.

---

## Technology Stack

- **Java** (main language)
- **Socket-based messaging**
- Custom message protocol for Chord and mutex control

---
```
## Directory Structure
chord/                     # error, input and output files for servent processes
src/
├── app/                   # Main Chord and App Configuration
├── cli/                   # Commands in input files
├── message/               # Custom message types and handlers
├── mutex                  # Distributed mutex - here Suzuki-Kasami mutex is used

```

## Running the System

### Configure Nodes

Each node has its own input file (e.g., `servent0_in.txt`, `servent1_in.txt`, ...), specifying commands and simulation actions (like DHT put/get, pauses, stops).

### Start Nodes

Clicking run, starts each servent for input file as a new process

The first node started becomes the bootstrap node and initiates the Chord ring.

---

## How It Works

- Nodes join and leave the ring dynamically.
- Each critical section (membership change, key movement, backup restore) is protected by the Suzuki-Kasami mutex.
- On suspected node failure (no PONG within 4s), a second opinion is requested from another node; after 10s of no response, the node is confirmed as failed and removed.
- Buddy nodes creates backup data for their predecessor and successor preventing loss of data, and the token is recovered if lost.

---

## File Descriptions

- `ChordState.java` – Handles Chord ring state, membership, and backup logic.
- `SuzukiKasamiMutex.java` – Implements the distributed mutex.
- `HealthCheckThread.java` – Monitors neighbors and triggers failure detection.
- `PutHandler.java`, `AskGetHandler.java`, etc. – Handle network messages for data storage and retrieval.
- `TokenMessage.java`, `TokenRequestMessage.java`, etc. – Used for Suzuki-Kasami mutex protocol.

---

## Notes

- The system tolerates up to 2 simultaneous node failures if they are not direct neighbors (thanks to buddy backup).
- Only critical modifying operations are mutex-protected; read-only operations can proceed concurrently.
- Custom messages are easily extensible for further features (e.g., snapshot, audit, etc.).
