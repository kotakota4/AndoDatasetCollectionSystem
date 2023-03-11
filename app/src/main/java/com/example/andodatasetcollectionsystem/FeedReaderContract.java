package com.example.andodatasetcollectionsystem;

import android.provider.BaseColumns;

public final class FeedReaderContract {

    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private FeedReaderContract() {}

    /* Inner class that defines the table contents */
    public static class FeedEntry implements BaseColumns {
        public static final String TABLE_NAME = "dataset";
        public static final String COLUMN_NAME_TIME = "time";
        public static final String COLUMN_NAME_RPM = "rpm";
        public static final String COLUMN_NAME_THROTTLE = "throttle";
        public static final String COLUMN_NAME_GPS_1 = "latitude";
        public static final String COLUMN_NAME_GPS_2 = "longitude";
    }
}
