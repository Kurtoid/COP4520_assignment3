### problem 1
In the strategy listed in problem 1, a chain link could have been connected to a chain link that was being removed, causing that link to become innaccessible from the rest of the chain. Proper synchronization (like LockFreeList) would fix this.

### problem 2
each sensor writes to it's own atomic variable every 'minute' (subject to time scaling). Every minute, the main thread (or one of the 8 cpus) reads the value of the atomic variable and adds it to corresponding arrays/counters. By using separate counters, sensors can never interfere with each other. The processing task could interfere, but it should never take more than a minute.