package guestbook;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Represents a counter in the datastore and stores the number of shards.
 *
 * @author j.s@google.com (Jeff Scudder)
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Counter {
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  public Long id;

  @Persistent
  public String counterName;

  @Persistent
  public Integer numShards;

  public Counter(String counterName) {
    this.counterName = counterName;
    this.numShards = new Integer(0);
  }

  public Counter(String counterName, Integer numShards) {
    this.counterName = counterName;
    this.numShards = numShards;
  }

  public void setShardCount(int count) {
    this.numShards = new Integer(count);
  }
}