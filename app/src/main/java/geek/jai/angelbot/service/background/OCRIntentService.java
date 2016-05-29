package geek.jai.angelbot.service.background;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import geek.jai.angelbot.service.heavenondemand.OCRDocument;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class OCRIntentService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_OCR = "geek.jai.angelbot.service.background.action.OCR";

    // TODO: Rename parameters
    private static final String EXTRA_FILE_LOC = "geek.jai.angelbot.service.background.extra.EXTRA_FILE_LOC";

    public OCRIntentService() {
        super("OCRIntentService");
    }

    /**
     * Starts this service to perform action OCR with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionOCR(Context context, String filePath) {
        Intent intent = new Intent(context, OCRIntentService.class);
        intent.setAction(ACTION_OCR);
        intent.putExtra(EXTRA_FILE_LOC, filePath);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_OCR.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_FILE_LOC);
                handleActionFoo(param1);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String filePath) {
        // TODO: Handle action Foo
        OCRDocument ocrDocument = new OCRDocument(this);
        ocrDocument.makeRequest(filePath);
    }
}
