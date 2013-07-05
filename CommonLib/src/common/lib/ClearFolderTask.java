package common.lib;

import java.io.File;

import android.os.AsyncTask;

public class ClearFolderTask extends AsyncTask<String, Integer, String> {//identical

	@Override
	protected String doInBackground(String... params) {// ugly to handle png file in this way
		if ("png".equals(params[1]))
			clearFolder(new File(params[0]), "png");
		else {
			clearFolder(new File(params[0]), "");
			if (!params[0].equals(params[1]))
				clearFolder(new File(params[1]), "");
		}
		
		return null;
	}

	int clearFolder(final File dir, String suffix) {
		int deletedFiles = 0;
		if (dir != null && dir.isDirectory()) {
			try {
				for (File child : dir.listFiles()) {
					// first delete subdirectories recursively
					if (child.isDirectory())
						deletedFiles += clearFolder(child, suffix);
					// then delete the files and subdirectories in this dir
					if (!"".equals(suffix)) {
						if (child.getName().endsWith(suffix))
							if (child.delete()) deletedFiles++;
					}
					else if (child.delete()) deletedFiles++;
				}
			} catch (Exception e) {
			}
		}
		return deletedFiles;
	}

}