package guestbook;

import java.io.IOException;

import javax.jdo.JDOCanRetryException;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;
import javax.servlet.http.*;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import guestbook.Greeting;
import guestbook.PMF;

public class SignGuestbookServlet extends HttpServlet {
    //private static final Logger log = Logger.getLogger(SignGuestbookServlet.class.getName());

    public void doPost(HttpServletRequest req, HttpServletResponse resp)
                throws IOException {

        String sensors = req.getParameter("sensors");
        String memtotal = req.getParameter("memtotal");
        
        if (memtotal != "") {
        String processor = req.getParameter("processor");
        String bogomips = req.getParameter("bogomips");
        String hardware = req.getParameter("hardware");
        String resolution = req.getParameter("resolution");
        String camera = req.getParameter("camera");
        String vendor = req.getParameter("vendor");
        String product = req.getParameter("product");
        String sdkversion = req.getParameter("sdkversion");
        String imei = req.getParameter("imei");
        
        String tmpkey = processor+hardware+memtotal+resolution+vendor;
        Key key = KeyFactory.createKey(Greeting.class.getSimpleName(), tmpkey);
        KeyFactory.Builder keyBuilder = new KeyFactory.Builder(Greeting.class.getSimpleName(), tmpkey);
        keyBuilder.addChild(Imei.class.getSimpleName(), imei);
        Key imeiKey = keyBuilder.getKey();

        boolean foundImei = false;

        PersistenceManager pm = PMF.get().getPersistenceManager();
        Transaction tx = pm.currentTransaction();

        int NUM_RETRIES = 3;
        for (int i = 0; i < NUM_RETRIES; i++) {
            try {
                tx.begin();

                Imei imeiInDb;
                try {
                    imeiInDb = pm.getObjectById(Imei.class, imeiKey);
                    foundImei = true;
                } catch (JDOObjectNotFoundException e) {
                	imeiInDb = new Imei(imeiKey);
                    pm.makePersistent(imeiInDb);
                }

                Greeting greeting = new Greeting(processor, bogomips, hardware, memtotal, resolution, camera, vendor, product, sensors, sdkversion, 1, key);
                try {
                	Greeting oldGreeting = (Greeting) pm.getObjectById(Greeting.class, key);
        			if (!foundImei) greeting.count = oldGreeting.count + 1;  
        			if (sensors.contains("trial")) greeting.sensors = oldGreeting.sensors;
        			if (camera == "") greeting.camera = oldGreeting.camera;
                } catch (JDOObjectNotFoundException e) {}
        		pm.makePersistent(greeting);
                
                try {
                    tx.commit();
                    break;

                } catch (JDOCanRetryException ex) {
                    if (i == (NUM_RETRIES - 1)) { 
                        throw ex;
                    }
                }
            } finally {
                if (tx.isActive()) {
                    tx.rollback();
                }
                pm.close();
            }
            }
        }
        
        if (sensors.contains("trial"))
        	resp.sendRedirect("/guestbook.jsp");
        else
        	resp.sendRedirect("/fullversion.jsp");
    }
}
