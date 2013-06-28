package common.lib;

import java.io.File;

public class packageIDpair {
	String packageName;
	File downloadedfile;
	int notificationID;

	packageIDpair(String name, int id, File file) {
		packageName = name;
		notificationID = id;
		downloadedfile = file;
	}
}
