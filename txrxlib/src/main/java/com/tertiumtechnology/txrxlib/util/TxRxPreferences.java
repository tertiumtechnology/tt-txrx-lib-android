package com.tertiumtechnology.txrxlib.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.tertiumtechnology.txrxlib.R;
import com.tertiumtechnology.txrxlib.rw.TxRxTimeouts;

/**
 * This class provides utility methods to manage TxRx preferences
 */

public class TxRxPreferences {
    static final String PREF_CONNECTION_TIMEOUT = "com.tertiumtechnology.txrxlib.PREF_CONNECTION_TIMEOUT";
    static final String PREF_FIRST_READ_TIMEOUT = "com.tertiumtechnology.txrxlib.PREF_FIRST_READ_TIMEOUT";
    static final String PREF_LATER_READ_TIMEOUT = "com.tertiumtechnology.txrxlib.PREF_LATER_READ_TIMEOUT";
    static final String PREF_WRITE_TIMEOUT = "com.tertiumtechnology.txrxlib.PREF_WRITE_TIMEOUT";

    /**
     * Retrieve current {@link TxRxTimeouts} preferences
     *
     * @param context The {@link Context} needed to retrieve {@link TxRxTimeouts}
     * @return current {@link TxRxTimeouts} preferences
     */
    public static TxRxTimeouts getTimeouts(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string
                .txrx_prefs_name), Context.MODE_PRIVATE);
        long connectionTimeout = sharedPreferences.getLong(PREF_CONNECTION_TIMEOUT, TxRxTimeouts
                .CONNECTION_TIMEOUT_DEFAULT_VALUE);
        long writeTimeout = sharedPreferences.getLong(PREF_WRITE_TIMEOUT, TxRxTimeouts.WRITE_TIMEOUT_DEFAULT_VALUE);
        long firstReadTimeout = sharedPreferences.getLong(PREF_FIRST_READ_TIMEOUT, TxRxTimeouts
                .FIRST_READ_TIMEOUT_DEFAULT_VALUE);
        long laterReadTimeout = sharedPreferences.getLong(PREF_LATER_READ_TIMEOUT, TxRxTimeouts
                .LATER_READ_TIMEOUT_DEFAULT_VALUE);

        return new TxRxTimeouts(connectionTimeout, writeTimeout, firstReadTimeout, laterReadTimeout);
    }

    /**
     * Save {@link TxRxTimeouts} preferences
     *
     * @param context The {@link Context} needed to save {@link TxRxTimeouts}
     */
    public static void saveTimeouts(Context context, TxRxTimeouts txRxTimeouts) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string
                .txrx_prefs_name), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(PREF_CONNECTION_TIMEOUT, txRxTimeouts.getConnectTimeout());
        editor.putLong(PREF_WRITE_TIMEOUT, txRxTimeouts.getWriteTimeout());
        editor.putLong(PREF_FIRST_READ_TIMEOUT, txRxTimeouts.getFirstReadTimeout());
        editor.putLong(PREF_LATER_READ_TIMEOUT, txRxTimeouts.getLaterReadTimeout());
        editor.apply();
    }
}
