package geek.jai.angelbot.util;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Created by JAID on 28-05-2016.
 */
public class FileUtil {

    private static final String PACKAGE_NAME = "angle.bot";
    private static final String BCK_PIC_LOCATION = "Theme";

    public static final String SCALED_IMAGE_NAME = "temp_img";

    public FileUtil() {
    }

    public void checkAndCreateDirectories() {
        String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
        File folder = new File(extStorageDirectory, "/Android/data/" + PACKAGE_NAME);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        createBckPicDirectory(extStorageDirectory);
    }

    private void createBckPicDirectory(String extStorageDirectory) {
        File folder = new File(extStorageDirectory, "/Android/data/" + PACKAGE_NAME +
                File.separator + BCK_PIC_LOCATION);
        if (!folder.exists()) {
            folder.mkdirs();
        }

    }

    public static String getBckPicLocation() {
        String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
        return extStorageDirectory + "/Android/data/" + PACKAGE_NAME +
                File.separator + BCK_PIC_LOCATION;
    }
}
