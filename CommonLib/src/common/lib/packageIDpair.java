package common.lib;

import java.io.File;

public class packageIDpair {
	public String packageName;
	File downloadedfile;
	public int notificationID;

	packageIDpair(String name, int id, File file) {
		packageName = name;
		notificationID = id;
		downloadedfile = file;
	}
}
