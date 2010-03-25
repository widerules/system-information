package guestbook;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * One shard belonging to the named counter.
 *
 * An individual shard is written to infrequently to allow the counter in
 * aggregate to be incremented rapidly.
 *
 * @author j.s@google.com (Jeff Scudder)
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class CounterShard {
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  public Long id;

  @Persistent
  public Integer shardNumber;

  @Persistent
  public String counterName;

  @Persistent
  public Integer count;  

  public CounterShard(String counterName, int shardNumber) {
    this(counterName, shardNumber, 0);
  }

  public CounterShard(String counterName, int shardNumber, int count) {
    this.counterName = counterName;
    this.shardNumber = new Integer(shardNumber);
    this.count = new Integer(count);
  }

  public void increment(int amount) {
    count = new Integer(count.intValue() + amount);
  }
}