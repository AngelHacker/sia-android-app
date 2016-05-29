package geek.jai.angelbot.service.camera;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by JAID on 28-05-2016.
 */
public class Camera {

    private Uri fileUri;
    private boolean logEnable = true;
    private static final String LOG_TAG = Camera.class.toString();

    public static final int CAPTURE_IMAGE_ACTIVITY_REQ = 200;
    private Activity activity;

    public Camera(Activity activity) {
        this.activity = activity;

    }

    public File getOutputPhotoFile() {
        File directory = new File(Environment.
                getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath());
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                if (logEnable) {
                    Log.e(LOG_TAG, "Failed to create storage directory.");
                }
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyMMdd_HHmmss", Locale.US).format(new Date());
        return new File(directory.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
    }

    public void startCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileUri = Uri.fromFile(getOutputPhotoFile());
        if (fileUri == null) {
            if (logEnable) {
                Log.e(LOG_TAG, "fileUri is null");
                return;
            }
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        activity.startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQ);

    }

    public void showPhoto(ImageView imageView, String photoUri) {
        File imageFile = new File(photoUri);
        if (imageFile.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            BitmapDrawable drawable = new BitmapDrawable(activity.getResources(), bitmap);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setImageDrawable(drawable);
        }
    }

    public Uri getFileUri() {
        return this.fileUri;
    }

    public Uri handleAndGetImageUri(int requestCode, int resultCode, Intent data, ImageView profilePhoto) {
        if (requestCode == Camera.CAPTURE_IMAGE_ACTIVITY_REQ) {
            if (resultCode == Activity.RESULT_OK) {
                Uri photoUri = null;
                if (data == null) {
                    photoUri = getFileUri();
                } else {
                    photoUri = getFileUri();
                }
                return photoUri;
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // User cancelled the image capture
                return null;
            } else {
                // Image capture failed, advise user
                return null;
            }
        }
        return null;
    }

    /*private void initiateCrop(Uri uri) {
        HawdiFileUtil fileUtil = new HawdiFileUtil();
        fileUtil.checkAndCreateHawdiDirectories();
        fileUtil.getProfilePicLocation();

        //Uri destination = Uri.fromFile(new File(activity.getCacheDir(), "cropped"));
        Uri destination = Uri.fromFile(new File(fileUtil.getProfilePicLocation(), "profile"));
        Crop.of(uri, destination).asSquare().start(activity);
    }*/
}
