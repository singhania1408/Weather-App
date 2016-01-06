package com.example.abhishek.mysunshine.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 * Created by abhishek on 28-12-2015.
 */
public class WeatherProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private DbHelper mOpenHelper;

    static final int WEATHER = 100;
    static final int WEATHER_WITH_LOCATION = 101;
    static final int WEATHER_WITH_LOCATION_AND_DATE = 102;
    static final int LOCATION = 300;

    private static final SQLiteQueryBuilder sWeatherByLocationSettingQueryBuilder;

    static{
        sWeatherByLocationSettingQueryBuilder = new SQLiteQueryBuilder();

        //This is an inner join which looks like
        //weather INNER JOIN location ON weather.location_id = location._id
        sWeatherByLocationSettingQueryBuilder.setTables(
                Weathercontract.WeatherEntry.TABLE_NAME + " INNER JOIN " +
                        Weathercontract.LocationEntry.TABLE_NAME +
                        " ON " + Weathercontract.WeatherEntry.TABLE_NAME +
                        "." + Weathercontract.WeatherEntry.COLUMN_LOC_KEY +
                        " = " + Weathercontract.LocationEntry.TABLE_NAME +
                        "." + Weathercontract.LocationEntry._ID);
    }

    //location.location_setting = ?
    private static final String sLocationSettingSelection =
            Weathercontract.LocationEntry.TABLE_NAME+
                    "." + Weathercontract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? ";

    //location.location_setting = ? AND date >= ?
    private static final String sLocationSettingWithStartDateSelection =
            Weathercontract.LocationEntry.TABLE_NAME+
                    "." + Weathercontract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? AND " +
                    Weathercontract.WeatherEntry.COLUMN_DATE + " >= ? ";

    //location.location_setting = ? AND date = ?
    private static final String sLocationSettingAndDaySelection =
            Weathercontract.LocationEntry.TABLE_NAME +
                    "." + Weathercontract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? AND " +
                    Weathercontract.WeatherEntry.COLUMN_DATE + " = ? ";

    private Cursor getWeatherByLocationSetting(Uri uri, String[] projection, String sortOrder) {
        String locationSetting = Weathercontract.WeatherEntry.getLocationSettingFromUri(uri);
        long startDate = Weathercontract.WeatherEntry.getStartDateFromUri(uri);

        String[] selectionArgs;
        String selection;

        if (startDate == 0) {
            selection = sLocationSettingSelection;
            selectionArgs = new String[]{locationSetting};
        } else {
            selectionArgs = new String[]{locationSetting, Long.toString(startDate)};
            selection = sLocationSettingWithStartDateSelection;
        }

        return sWeatherByLocationSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getWeatherByLocationSettingAndDate(
            Uri uri, String[] projection, String sortOrder) {
        String locationSetting = Weathercontract.WeatherEntry.getLocationSettingFromUri(uri);
        long date = Weathercontract.WeatherEntry.getDateFromUri(uri);

        return sWeatherByLocationSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sLocationSettingAndDaySelection,
                new String[]{locationSetting, Long.toString(date)},
                null,
                null,
                sortOrder
        );
    }

    /*
        Students: Here is where you need to create the UriMatcher. This UriMatcher will
        match each URI to the WEATHER, WEATHER_WITH_LOCATION, WEATHER_WITH_LOCATION_AND_DATE,
        and LOCATION integer constants defined above.  You can test this by uncommenting the
        testUriMatcher test within TestUriMatcher.
     */
    static UriMatcher buildUriMatcher() {
        // 1) The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case. Add the constructor below.


        // 2) Use the addURI function to match each of the types.  Use the constants from
        // WeatherContract to help define the types to the UriMatcher.


        // 3) Return the new matcher!
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
              final String authority = Weathercontract.CONTENT_AUTHORITY;
                // For each type of URI you want to add, create a corresponding code.
                matcher.addURI(authority, Weathercontract.PATH_WEATHER, WEATHER);
               matcher.addURI(authority, Weathercontract.PATH_WEATHER + "/*", WEATHER_WITH_LOCATION);
                matcher.addURI(authority, Weathercontract.PATH_WEATHER + "/*/#", WEATHER_WITH_LOCATION_AND_DATE);

               matcher.addURI(authority, Weathercontract.PATH_LOCATION, LOCATION);
                return matcher;
    }

    /*
        Students: We've coded this for you.  We just create a new WeatherDbHelper for later use
        here.
     */
    @Override
    public boolean onCreate() {
        mOpenHelper = new DbHelper(getContext());
        return true;
    }

    /*
        Students: Here's where you'll code the getType function that uses the UriMatcher.  You can
        test this by uncommenting testGetType in TestProvider.
     */
    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            // Student: Uncomment and fill out these two cases
//            case WEATHER_WITH_LOCATION_AND_DATE:
//            case WEATHER_WITH_LOCATION:
            case WEATHER_WITH_LOCATION_AND_DATE:
                               return Weathercontract.WeatherEntry.CONTENT_ITEM_TYPE;
                      case WEATHER_WITH_LOCATION:
                               return Weathercontract.WeatherEntry.CONTENT_TYPE;
            case WEATHER:
                return Weathercontract.WeatherEntry.CONTENT_TYPE;
            case LOCATION:
                return Weathercontract.LocationEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "weather/*/*"
            case WEATHER_WITH_LOCATION_AND_DATE:
            {
                retCursor = getWeatherByLocationSettingAndDate(uri, projection, sortOrder);
                break;
            }
            // "weather/*"
            case WEATHER_WITH_LOCATION: {
                retCursor = getWeatherByLocationSetting(uri, projection, sortOrder);
                break;
            }
            // "weather"
            case WEATHER: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        Weathercontract.WeatherEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "location"
            case LOCATION: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        Weathercontract.LocationEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    /*
        Student: Add the ability to insert Locations to the implementation of this function.
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case WEATHER: {
                normalizeDate(values);
                long _id = db.insert(Weathercontract.WeatherEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = Weathercontract.WeatherEntry.buildWeatherUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case LOCATION: {
                             long _id = db.insert(Weathercontract.LocationEntry.TABLE_NAME, null, values);
                             if ( _id > 0 )
                                        returnUri = Weathercontract.LocationEntry.buildLocationUri(_id);
                                else
                                    throw new android.database.SQLException("Failed to insert row into " + uri);
                                break;
                            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Student: Start by getting a writable database

        // Student: Use the uriMatcher to match the WEATHER and LOCATION URI's we are going to
        // handle.  If it doesn't match these, throw an UnsupportedOperationException.

        // Student: A null value deletes all rows.  In my implementation of this, I only notified
        // the uri listeners (using the content resolver) if the rowsDeleted != 0 or the selection
        // is null.
        // Oh, and you should notify the listeners here.

        // Student: return the actual rows deleted
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";
        switch (match) {
            case WEATHER:
                rowsDeleted = db.delete(
                        Weathercontract.WeatherEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case LOCATION:
                rowsDeleted = db.delete(
                        Weathercontract.LocationEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    private void normalizeDate(ContentValues values) {
        // normalize the date value
        if (values.containsKey(Weathercontract.WeatherEntry.COLUMN_DATE)) {
            long dateValue = values.getAsLong(Weathercontract.WeatherEntry.COLUMN_DATE);
            values.put(Weathercontract.WeatherEntry.COLUMN_DATE, Weathercontract.normalizeDate(dateValue));
        }
    }

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case WEATHER:
                normalizeDate(values);
                rowsUpdated = db.update(Weathercontract.WeatherEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case LOCATION:
                rowsUpdated = db.update(Weathercontract.LocationEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case WEATHER:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        normalizeDate(value);
                        long _id = db.insert(Weathercontract.WeatherEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(22)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}