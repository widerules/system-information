package guestbook;

import guestbook.PMF;

import java.util.List;
import java.util.Random;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

/**
 * A counter which can be incremented rapidly.
 *
 * Capable of incrementing the counter and increasing the number of shards.
 * When incrementing, a random shard is selected to prevent a single shard
 * from being written to too frequently. If increments are being made too
 * quickly, increase the number of shards to divide the load. Performs
 * datastore operations using JDO.
 *
 * @author j.s@google.com (Jeff Scudder)
 */
public class ShardedCounter {
  private String counterName;

  public ShardedCounter(String counterName) {
    this.counterName = counterName;
  }

  public String getCounterName() {
    return counterName;
  }

  private Counter getThisCounter(PersistenceManager pm) {
    Counter current = null;
    Query thisCounterQuery = pm.newQuery(Counter.class, 
        "counterName == nameParam");
    thisCounterQuery.declareParameters("String nameParam");
    List<Counter> counter = (List<Counter>) thisCounterQuery.execute(
        counterName);
    if (counter != null && !counter.isEmpty()) {
      current = counter.get(0);
    }
    return current;
  }

  public boolean isInDatastore() {
    boolean counterStored = false;
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      if (getThisCounter(pm) != null) {
        counterStored = true;
      }
    } finally {
      pm.close();
    }
    return counterStored;
  }

  public int getCount() {
    int sum = 0;
    PersistenceManager pm = PMF.get().getPersistenceManager();

    try {
      Query shardsQuery = pm.newQuery(CounterShard.class, 
                                      "counterName == nameParam");
      shardsQuery.declareParameters("String nameParam");
      List<CounterShard> shards = (List<CounterShard>) shardsQuery.execute(
          counterName);
      if (shards != null && !shards.isEmpty()) {
        for (CounterShard current : shards) {
          sum += current.count;
        }
      }
    } finally {
      pm.close();
    }
    return sum;
  }

  public int getNumShards() {
    int numShards = 0;
    // Find the current number of shards for this Counter.
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      Counter current = getThisCounter(pm);
      if (current != null) {
        numShards = current.numShards.intValue();
      }
    } finally {
      pm.close();
    }
    return numShards;
  }

  public int addShard() {
    return addShards(1);
  }

  public int addShards(int count) {
    int numShards = 0;
    // Find the initial shard count for this Counter.
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      Counter current = getThisCounter(pm);
      if (current != null) {
        numShards = current.numShards.intValue();
        current.setShardCount(numShards + count);
        // Save the increased shard count for this Counter.
        pm.makePersistent(current);
      }
    } finally {
      pm.close();
    }

    // Create new shard objects for this counter.
    pm = PMF.get().getPersistenceManager();
    try {
      for (int i = 0; i < count; i++) {
        CounterShard newShard = new CounterShard(getCounterName(), numShards);
        pm.makePersistent(newShard);
        numShards++;
      }
    } finally {
      pm.close();
    }
    return numShards;
  }

  public void increment() {
    increment(1);
  }

  public void increment(int count) {
    // Find how many shards are in this counter.
    int shardCount = 0;
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      Counter current = getThisCounter(pm);
      shardCount = current.numShards;
    } finally {
      pm.close();
    }

    // Choose the shard randomly from the available shards.
    Random generator = new Random();
    int shardNum = generator.nextInt(shardCount);
    
    pm = PMF.get().getPersistenceManager();
    try {
      Query randomShardQuery = pm.newQuery(CounterShard.class);
      randomShardQuery.setFilter(
          "counterName == nameParam && shardNumber == numParam");
      randomShardQuery.declareParameters("String nameParam, int numParam");
      List<CounterShard> shards = (List<CounterShard>) randomShardQuery
          .execute(counterName, shardNum);
      if (shards != null && !shards.isEmpty()) {
        CounterShard shard = shards.get(0);
        shard.increment(count);
        pm.makePersistent(shard);
      } 
    } finally {
      pm.close();
    }
  }
}