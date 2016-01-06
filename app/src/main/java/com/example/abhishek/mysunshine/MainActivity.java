package com.example.abhishek.mysunshine;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements weatherlist_fragment.Callback {

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String DETAILFRAGMENT_TAG = "DFTAG";

    private String mLocation;
    private boolean mTwoPane;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocation = Utility.getPreferredLocation(this);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.weather_detail_container) != null) {
            mTwoPane = true;
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.weather_detail_container, new DetailFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);
        }
         weatherlist_fragment forecastFragment =  ((weatherlist_fragment)getSupportFragmentManager()
                                .findFragmentById(R.id.fragment_forecast));
               forecastFragment.setUseTodayLayout(!mTwoPane);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        if (id == R.id.action_map) {
            openpreferredlocation();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void openpreferredlocation()
    {
        String location = Utility.getPreferredLocation(this);

        Uri geoLocation = Uri.parse("geo:0,0?").buildUpon().
                appendQueryParameter("q", location).build();


        Intent intent = new Intent(Intent.ACTION_VIEW);
             intent.setData(geoLocation);

                       if (intent.resolveActivity(getPackageManager()) != null) {
                  startActivity(intent);
               } else {
                   Log.d(LOG_TAG, "Couldn't call " + location + ", no receiving apps installed!");
               }
    }
    @Override
        protected void onResume() {
                super.onResume();
                String location = Utility.getPreferredLocation( this );
                // update the location in our second pane using the fragment manager
        if (location != null && !location.equals(mLocation)) {
                       weatherlist_fragment ff = (weatherlist_fragment)getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
                       if ( null != ff) {
                           ff.onLocationChanged();
                            }

                       DetailFragment df = (DetailFragment)getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
                       if ( null != df ) {
                                df.onLocationChanged(location);
                       }
                     mLocation = location;
                  }
           }

    @Override
        public void onItemSelected(Uri contentUri) {
                if (mTwoPane) {
                        // In two-pane mode, show the detail view in this activity by
                                // adding or replacing the detail fragment using a
                                        // fragment transaction.
                    Bundle args = new Bundle();
                    args.putParcelable(DetailFragment.DETAIL_URI, contentUri);

                                DetailFragment fragment = new DetailFragment();
                        fragment.setArguments(args);

                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.weather_detail_container, fragment, DETAILFRAGMENT_TAG)
                                        .commit();
                    } else {
                        Intent intent = new Intent(this, detail.class)
                                        .setData(contentUri);
                        startActivity(intent);
                    }
            }
    }

