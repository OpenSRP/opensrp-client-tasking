package org.smartregister.tasking.view;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.util.Pair;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.mapbox.mapboxsdk.offline.OfflineManager;
import com.mapbox.mapboxsdk.offline.OfflineRegion;

import org.smartregister.tasking.R;
import org.smartregister.tasking.adapter.ViewPagerAdapter;
import org.smartregister.tasking.contract.OfflineMapDownloadCallback;
import org.smartregister.tasking.fragment.AvailableOfflineMapsFragment;
import org.smartregister.tasking.fragment.DownloadedOfflineMapsFragment;
import org.smartregister.tasking.model.OfflineMapModel;
import org.smartregister.tasking.util.OfflineMapHelper;

import java.util.List;
import java.util.Map;

import timber.log.Timber;

public abstract class OfflineMapsActivity extends AppCompatActivity implements OfflineMapDownloadCallback {

    private static final String TAG = OfflineMapsActivity.class.getName();

    private ViewPagerAdapter adapter;

    private ViewPager viewPager;

    private AvailableOfflineMapsFragment availableOfflineMapsFragment;

    private DownloadedOfflineMapsFragment downloadedOfflineMapsFragment;

    private OfflineManager offlineManager;

    public static  final int AVAILABLE_OFFLINE_MAPS_FRAGMENT_INDEX = 0;
    public static  final int DOWNLOADED_OFFLINE_MAPS_FRAGMENT_INDEX = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_offline_maps);

        setUpToolbar();

        setupViews();

        offlineManager = initOfflineManager();

        getOfflineDownloadedRegions(false);
    }

    protected void setUpToolbar() {
        Toolbar toolbar= this.findViewById(R.id.offline_maps_toolbar);
        toolbar.setTitle(R.string.offline_maps);
        this.setSupportActionBar(toolbar);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    protected void setupViews() {
        TabLayout tabLayout = findViewById(R.id.tabs);
        viewPager = findViewById(R.id.viewpager);
        tabLayout.setupWithViewPager(setupViewPager());
    }

    protected ViewPager setupViewPager() {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());

        availableOfflineMapsFragment = AvailableOfflineMapsFragment.newInstance(this.getIntent().getExtras(), getMapStyleAssetPath());
        availableOfflineMapsFragment.setOfflineMapDownloadCallback(this);
        adapter.addFragment(availableOfflineMapsFragment, this.getString(R.string.available).toUpperCase());

        downloadedOfflineMapsFragment = DownloadedOfflineMapsFragment.newInstance(this.getIntent().getExtras(), this );
        downloadedOfflineMapsFragment.setOfflineMapDownloadCallback(this);
        adapter.addFragment(downloadedOfflineMapsFragment, this.getString(R.string.downloaded).toUpperCase());

        viewPager.setAdapter(adapter);

        return viewPager;
    }

    public OfflineManager initOfflineManager() {
        return OfflineManager.getInstance(this);
    }

    @Override
    public void onMapDownloaded(OfflineMapModel offlineMapModel) {
        getOfflineDownloadedRegions(true);
    }

    @Override
    public void onOfflineMapDeleted(OfflineMapModel offlineMapModel) {
        availableOfflineMapsFragment = (AvailableOfflineMapsFragment)  adapter.getItem(AVAILABLE_OFFLINE_MAPS_FRAGMENT_INDEX);
        availableOfflineMapsFragment.updateOperationalAreasToDownload(offlineMapModel);
    }

    public void getOfflineDownloadedRegions(boolean refreshDownloadedListOnly) {

        offlineManager.listOfflineRegions(new OfflineManager.ListOfflineRegionsCallback() {
            @Override
            public void onList(final OfflineRegion[] offlineRegions) {
                Pair<List<String>, Map<String, OfflineRegion>> offlineRegionInfo = null;
                if (offlineRegions != null && offlineRegions.length > 0) {
                    offlineRegionInfo = OfflineMapHelper.getOfflineRegionInfo(offlineRegions);
                }
                setOfflineDownloadedMapNames(offlineRegionInfo, refreshDownloadedListOnly);
            }

            @Override
            public void onError(String error) {
                Timber.e(TAG, "ERROR :: "  + error);
            }
        });
    }

    public void setOfflineDownloadedMapNames(Pair<List<String>, Map<String, OfflineRegion>> offlineRegionInfo, boolean refreshDownloadedListOnly) {
        downloadedOfflineMapsFragment = (DownloadedOfflineMapsFragment)  adapter.getItem(DOWNLOADED_OFFLINE_MAPS_FRAGMENT_INDEX);
        downloadedOfflineMapsFragment.setOfflineDownloadedMapNames(offlineRegionInfo);

        if (refreshDownloadedListOnly){
            return;
        }

        availableOfflineMapsFragment = (AvailableOfflineMapsFragment)  adapter.getItem(AVAILABLE_OFFLINE_MAPS_FRAGMENT_INDEX);
        List<String> regionNames = offlineRegionInfo != null ? offlineRegionInfo.first : null;
        availableOfflineMapsFragment.setOfflineDownloadedMapNames(regionNames);
    }


    /**
     * Provide the asset path to the mapbox style eg asset://download_mapbox_style.json
     *
     * @return
     */
    @NonNull
    public abstract String getMapStyleAssetPath();
}
