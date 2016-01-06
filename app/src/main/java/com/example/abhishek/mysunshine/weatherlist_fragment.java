package com.example.abhishek.mysunshine;


import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.example.abhishek.mysunshine.data.Weathercontract;

/**
 * Created by abhishek on 13-12-2015.
 */
public class weatherlist_fragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>
{

    private static final int FORECAST_LOADER = 0;
    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            Weathercontract.WeatherEntry.TABLE_NAME + "." + Weathercontract.WeatherEntry._ID,
            Weathercontract.WeatherEntry.COLUMN_DATE,
            Weathercontract.WeatherEntry.COLUMN_SHORT_DESC,
            Weathercontract.WeatherEntry.COLUMN_MAX_TEMP,
            Weathercontract.WeatherEntry.COLUMN_MIN_TEMP,
            Weathercontract.LocationEntry.COLUMN_LOCATION_SETTING,
            Weathercontract.WeatherEntry.COLUMN_WEATHER_ID,
            Weathercontract.LocationEntry.COLUMN_COORD_LAT,
            Weathercontract.LocationEntry.COLUMN_COORD_LONG
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;

    ForecastAdapter weatheradapter;
    private ListView listview;
    private int mPosition = ListView.INVALID_POSITION;
    private boolean mUseTodayLayout;

    private static final String SELECTED_KEY = "selected_position";
    public interface Callback {
               /**
                  * DetailFragmentCallback for when an item has been selected.
                  */
               public void onItemSelected(Uri dateUri);
            }
    public weatherlist_fragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateWeather();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        weatheradapter = new ForecastAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

         listview = (ListView) rootView.findViewById(R.id.listview_weather);
        listview.setAdapter(weatheradapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    String locationSetting = Utility.getPreferredLocation(getActivity());
                    ((Callback) getActivity())
                            .onItemSelected(Weathercontract.WeatherEntry.buildWeatherLocationWithDate(
                                    locationSetting, cursor.getLong(COL_WEATHER_DATE)
                            ));
                }
                mPosition = position;
            }

        });
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        weatheradapter.setUseTodayLayout(mUseTodayLayout);
        return (rootView);
    }
    @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            getLoaderManager().initLoader(FORECAST_LOADER, null, this);
            super.onActivityCreated(savedInstanceState);
        }
    void onLocationChanged( ) {
        updateWeather();
              getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
          }

    private void updateWeather() {
        FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity());
        String location = Utility.getPreferredLocation(getActivity());
        weatherTask.execute(location);
    }

       public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

    // Sort order:  Ascending, by date.
    String sortOrder = Weathercontract.WeatherEntry.COLUMN_DATE + " ASC";
           String locationSetting = Utility.getPreferredLocation(getActivity());
    Uri weatherForLocationUri = Weathercontract.WeatherEntry.buildWeatherLocationWithStartDate(
            locationSetting, System.currentTimeMillis());

    return new CursorLoader(getActivity(),
            weatherForLocationUri,
            FORECAST_COLUMNS,
            null,
            null,
            sortOrder);
}
    @Override
       public void onSaveInstanceState(Bundle outState) {
        if (mPosition != ListView.INVALID_POSITION) {
                        outState.putInt(SELECTED_KEY, mPosition);
                    }
                super.onSaveInstanceState(outState);
            }


    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        weatheradapter.swapCursor(cursor);
        if (mPosition != ListView.INVALID_POSITION) {
                        // If we don't need to restart the loader, and there's a desired position to restore
                                // to, do so now.
                                       listview.smoothScrollToPosition(mPosition);
                    }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        weatheradapter.swapCursor(null);
    }
    public void setUseTodayLayout(boolean useTodayLayout) {
                mUseTodayLayout = useTodayLayout;
                if (weatheradapter != null) {
                    weatheradapter.setUseTodayLayout(mUseTodayLayout);
                    }
            }


}