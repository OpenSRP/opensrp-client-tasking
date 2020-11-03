package org.smartregister.tasking.contract;

import android.location.Location;

import androidx.annotation.StringRes;

import com.mapbox.mapboxsdk.geometry.LatLng;

import org.smartregister.tasking.presenter.ValidateUserLocationPresenter;


/**
 * Created by samuelgithengi on 2/13/19.
 */
public interface UserLocationContract {


    interface UserLocationPresenter {

        void requestUserLocation();

        void onGetUserLocation(Location location);

        void onGetUserLocationFailed();

        void waitForUserLocation();
    }

    interface UserLocationView {

        Location getUserCurrentLocation();

        void showProgressDialog(@StringRes int title, @StringRes int message);

        void hideProgressDialog();

        void requestUserLocation();
    }

    interface UserLocationCallback {

        void onLocationValidated();

        LatLng getTargetCoordinates();

        void requestUserPassword();

        ValidateUserLocationPresenter getLocationPresenter();

    }
}
