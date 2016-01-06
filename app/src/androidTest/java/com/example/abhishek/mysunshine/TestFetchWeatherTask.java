package com.example.abhishek.mysunshine;

/**
 * Created by abhishek on 28-12-2015.
 */
import android.annotation.TargetApi;
import android.database.Cursor;
import android.test.AndroidTestCase;

import com.example.abhishek.mysunshine.data.Weathercontract;


public class TestFetchWeatherTask extends AndroidTestCase{
    static final String ADD_LOCATION_SETTING = "Sunnydale, CA";
    static final String ADD_LOCATION_CITY = "Sunnydale";
    static final double ADD_LOCATION_LAT = 34.425833;
    static final double ADD_LOCATION_LON = -119.714167;

    /*
        Students: uncomment testAddLocation after you have written the AddLocation function.
        This test will only run on API level 11 and higher because of a requirement in the
        content provider.
     */
    @TargetApi(22)
    public void testAddLocation() {
        // start from a clean state
        getContext().getContentResolver().delete(Weathercontract.LocationEntry.CONTENT_URI,
                Weathercontract.LocationEntry.COLUMN_LOCATION_SETTING + " = ?",
                new String[]{ADD_LOCATION_SETTING});

        FetchWeatherTask fwt = new FetchWeatherTask(getContext());
        long locationId = fwt.addLocation(ADD_LOCATION_SETTING, ADD_LOCATION_CITY,
                ADD_LOCATION_LAT, ADD_LOCATION_LON);

        // does addLocation return a valid record ID?
        assertFalse("Error: addLocation returned an invalid ID on insert",
                locationId == -1);

        // test all this twice
        for ( int i = 0; i < 2; i++ ) {

            // does the ID point to our location?
            Cursor locationCursor = getContext().getContentResolver().query(
                    Weathercontract.LocationEntry.CONTENT_URI,
                    new String[]{
                            Weathercontract.LocationEntry._ID,
                            Weathercontract.LocationEntry.COLUMN_LOCATION_SETTING,
                            Weathercontract.LocationEntry.COLUMN_CITY_NAME,
                            Weathercontract.LocationEntry.COLUMN_COORD_LAT,
                            Weathercontract.LocationEntry.COLUMN_COORD_LONG
                    },
                    Weathercontract.LocationEntry.COLUMN_LOCATION_SETTING + " = ?",
                    new String[]{ADD_LOCATION_SETTING},
                    null);

            // these match the indices of the projection
            if (locationCursor.moveToFirst()) {
                assertEquals("Error: the queried value of locationId does not match the returned value" +
                        "from addLocation", locationCursor.getLong(0), locationId);
                assertEquals("Error: the queried value of location setting is incorrect",
                        locationCursor.getString(1), ADD_LOCATION_SETTING);
                assertEquals("Error: the queried value of location city is incorrect",
                        locationCursor.getString(2), ADD_LOCATION_CITY);
                assertEquals("Error: the queried value of latitude is incorrect",
                        locationCursor.getDouble(3), ADD_LOCATION_LAT);
                assertEquals("Error: the queried value of longitude is incorrect",
                        locationCursor.getDouble(4), ADD_LOCATION_LON);
            } else {
                fail("Error: the id you used to query returned an empty cursor");
            }

            // there should be no more records
            assertFalse("Error: there should be only one record returned from a location query",
                    locationCursor.moveToNext());

            // add the location again
            long newLocationId = fwt.addLocation(ADD_LOCATION_SETTING, ADD_LOCATION_CITY,
                    ADD_LOCATION_LAT, ADD_LOCATION_LON);

            assertEquals("Error: inserting a location again should return the same ID",
                    locationId, newLocationId);
        }
        // reset our state back to normal
        getContext().getContentResolver().delete(Weathercontract.LocationEntry.CONTENT_URI,
                Weathercontract.LocationEntry.COLUMN_LOCATION_SETTING + " = ?",
                new String[]{ADD_LOCATION_SETTING});

        // clean up the test so that other tests can use the content provider
        getContext().getContentResolver().
                acquireContentProviderClient(Weathercontract.LocationEntry.CONTENT_URI).
                getLocalContentProvider().shutdown();
    }
}