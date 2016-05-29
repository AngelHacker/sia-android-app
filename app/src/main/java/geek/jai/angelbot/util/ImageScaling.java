package geek.jai.angelbot.util;

import android.graphics.Bitmap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Created by JAID on 28-05-2016.
 */
public class ImageScaling {
    private static int count = 0;

    public ImageScaling() {
    }

    public String decodeFile(String path, int DESIREDWIDTH, int DESIREDHEIGHT) {
        String strMyImagePath = null;
        Bitmap scaledBitmap = null;

        try {
            // Part 1: Decode image
            Bitmap unscaledBitmap = ScalingUtilities.decodeFile(path, DESIREDWIDTH, DESIREDHEIGHT, ScalingUtilities.ScalingLogic.FIT);

            if (!(unscaledBitmap.getWidth() <= DESIREDWIDTH && unscaledBitmap.getHeight() <= DESIREDHEIGHT)) {
                // Part 2: Scale image
                scaledBitmap = ScalingUtilities.createScaledBitmap(unscaledBitmap, DESIREDWIDTH, DESIREDHEIGHT, ScalingUtilities.ScalingLogic.FIT);
            } else {
                unscaledBitmap.recycle();
                return path;
            }

            // Store to tmp file
            FileUtil fileUtil = new FileUtil();
            fileUtil.checkAndCreateDirectories();
            String folder = fileUtil.getBckPicLocation();

            File mFolder = new File(folder);
            if (!mFolder.exists()) {
                mFolder.mkdir();
            }

            String tempImageName = FileUtil.SCALED_IMAGE_NAME + count++;

            File f = new File(mFolder.getAbsolutePath(), tempImageName);

            strMyImagePath = f.getAbsolutePath();
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(f);
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 75, fos);
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            scaledBitmap.recycle();
        } catch (Throwable e) {

        }

        if (strMyImagePath == null) {
            return path;
        }
        return strMyImagePath;

    }
}
