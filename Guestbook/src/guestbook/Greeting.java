package guestbook;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Greeting {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    public Key key;

    @Persistent
    public String processor;

    @Persistent
    public String bogomips;

    @Persistent
    public String hardware;

    @Persistent
    public String memtotal;

    @Persistent
    public String resolution;

    @Persistent
    public String camera;

    @Persistent
    public String vendor;

    @Persistent
    public String product;
    
    @Persistent
    public String sensors;

    @Persistent
    public String sdkversion;

    @Persistent
    public int count;

    public Greeting(String processor, String bogomips, String hardware, String memtotal, String resolution, String camera, String vendor, String product, String sensors, String sdkversion, int count, Key key) {
        this.processor = processor;
        this.bogomips = bogomips;
        this.hardware = hardware;
        this.memtotal = memtotal;
        this.resolution = resolution;
        this.camera = camera;
        this.vendor = vendor;
        this.product = product;
        this.sensors = sensors;
        this.sdkversion = sdkversion;
        this.count = count;
        this.key = key;

    }
}
