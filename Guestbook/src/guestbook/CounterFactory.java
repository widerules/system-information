package guestbook;

import guestbook.PMF;

import javax.jdo.PersistenceManager;

/**
 * Finds or creates a sharded counter with the desired name.
 *
 * @author j.s@google.com (Jeff Scudder)
 */
public class CounterFactory {

  public ShardedCounter getCounter(String name) {
    ShardedCounter counter = new ShardedCounter(name);
    if (counter.isInDatastore()) {
      return counter;
    } else {
      return null;
    }
  }
    
  public ShardedCounter createCounter(String name) {
    ShardedCounter counter = new ShardedCounter(name);
    
    Counter counterEntity = new Counter(name, 0);
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      pm.makePersistent(counterEntity);
    } finally {
      pm.close();
    }

    return counter;
  }
}
