package simple.home.jtbuaa;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ShellInterface {

	public static String doExec(String[] commands, boolean suNeeded) {
		Process process = null;
		DataOutputStream os = null;
		DataInputStream osRes = null;
		String res = "";

		try {
			if (suNeeded) {// Getting Root
				process = Runtime.getRuntime().exec("su");
			} else {
				process = Runtime.getRuntime().exec("sh");
			}

			os = new DataOutputStream(process.getOutputStream());
			osRes = new DataInputStream(process.getInputStream());
			
			for (String single : commands) {
				os.writeBytes(single + "\n");
				os.flush();
			}
			
			os.writeBytes("exit\n");
			os.flush();
			
			String line = "";
			while((line = osRes.readLine()) != null) {
				res += line + "\n";
				Log.d("============", line);
			}

			process.waitFor();

        } catch (Exception e) {
        	res += e.getMessage();
			Log.d("===============", e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				if (os != null) {
					os.close();
				}
				if (osRes != null) {
					osRes.close();
				}
				process.destroy();
			} catch (Exception e) {
				// nothing
			}
		}
		return res;
	}
}
