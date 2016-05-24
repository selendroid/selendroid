package io.selendroid.server.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * A helper class for working with intents
 */
public class Intents {
    /**
     * Create an intent to start an activity, for both ServerInstrumentation and LightweightInstrumentation
     */
    public static Intent createStartActivityIntent(Context context, String mainActivityName) {
        Intent intent = new Intent();
        intent.setClassName(context, mainActivityName);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        return intent;
    }

    /**
     * Create an implicit intent based on the given URI.
     */
    public static Intent createUriIntent(String intentUri) {
        return new Intent(Intent.ACTION_VIEW, Uri.parse(intentUri))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }
}
