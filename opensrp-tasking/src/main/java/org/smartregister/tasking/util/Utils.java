package org.smartregister.tasking.util;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.util.Pair;

import com.google.gson.JsonElement;
import com.mapbox.geojson.Feature;
import com.mapbox.mapboxsdk.geometry.LatLng;

import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.CoreLibrary;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.domain.Location;
import org.smartregister.domain.tag.FormTag;
import org.smartregister.job.DocumentConfigurationServiceJob;
import org.smartregister.job.PullUniqueIdsServiceJob;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.tasking.BuildConfig;
import org.smartregister.tasking.R;
import org.smartregister.tasking.TaskingLibrary;
import org.smartregister.tasking.util.Constants.CONFIGURATION;
import org.smartregister.tasking.util.Constants.Properties;
import org.smartregister.tasking.util.Constants.Tags;
import org.smartregister.util.Cache;
import org.smartregister.util.CacheableData;
import org.smartregister.util.DatabaseMigrationUtils;
import org.smartregister.util.RecreateECUtil;
import org.smartregister.view.activity.DrishtiApplication;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;

import static org.smartregister.tasking.util.Constants.CONFIGURATION.KILOMETERS_PER_DEGREE_OF_LATITUDE_AT_EQUITOR;
import static org.smartregister.tasking.util.Constants.CONFIGURATION.KILOMETERS_PER_DEGREE_OF_LONGITUDE_AT_EQUITOR;
import static org.smartregister.tasking.util.Constants.CONFIGURATION.METERS_PER_KILOMETER;
import static org.smartregister.tasking.util.Constants.DateFormat.CARD_VIEW_DATE_FORMAT;
import static org.smartregister.tasking.util.Constants.Intervention.FI;
import static org.smartregister.tasking.util.Constants.Intervention.IRS;
import static org.smartregister.tasking.util.Constants.Intervention.LARVAL_DIPPING;
import static org.smartregister.tasking.util.Constants.Intervention.MDA;
import static org.smartregister.tasking.util.Constants.Intervention.MOSQUITO_COLLECTION;
import static org.smartregister.tasking.util.Constants.Intervention.PAOT;

public class Utils {

    public static final ArrayList<String> ALLOWED_LEVELS;
    public static final String DEFAULT_LOCATION_LEVEL = Tags.HEALTH_CENTER;
    public static final String REVEAL_PROJECT = "reveal";

    private static Cache<Location> cache = new Cache<>();

    static {
        ALLOWED_LEVELS = new ArrayList<>();
        ALLOWED_LEVELS.add(DEFAULT_LOCATION_LEVEL);
        ALLOWED_LEVELS.add(Tags.OPERATIONAL_AREA);
        ALLOWED_LEVELS.add(Tags.CANTON);
        ALLOWED_LEVELS.add(Tags.VILLAGE);
        ALLOWED_LEVELS.add(REVEAL_PROJECT);
    }

    public static void saveLanguage(String language) {
        AllSharedPreferences allSharedPreferences = new AllSharedPreferences(PreferenceManager.getDefaultSharedPreferences(DrishtiApplication.getInstance().getApplicationContext()));
        allSharedPreferences.saveLanguagePreference(language);
        setLocale(new Locale(language));
    }

    public static void setLocale(Locale locale) {
        Resources resources = DrishtiApplication.getInstance().getApplicationContext().getResources();
        Configuration configuration = resources.getConfiguration();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(locale);
            DrishtiApplication.getInstance().getApplicationContext().createConfigurationContext(configuration);
        } else {
            configuration.locale = locale;
            resources.updateConfiguration(configuration, displayMetrics);
        }
    }


    public static void setTextViewText(@NonNull TextView textView, @NonNull @StringRes Integer labelResource, String value) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        ForegroundColorSpan blackSpan = new ForegroundColorSpan(textView.getResources().getColor(R.color.text_black));
        builder.append(textView.getResources().getString(labelResource)).append(" ");
        int start = builder.length();
        builder.append(value).setSpan(blackSpan, start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(builder);
    }

    public static String getPropertyValue(Feature feature, String propertyKey) {
        JsonElement featureProperty = feature.getProperty(propertyKey);
        return featureProperty == null ? null : featureProperty.getAsString();
    }

    public static void startImmediateSync() {
        PullUniqueIdsServiceJob.scheduleJobImmediately(PullUniqueIdsServiceJob.TAG);
        DocumentConfigurationServiceJob.scheduleJobImmediately(DocumentConfigurationServiceJob.TAG);

        TaskingLibrary.getInstance().getTaskingLibraryConfiguration().startImmediateSync();
    }


    public static Location getOperationalAreaLocation(String operationalArea) {
        return cache.get(operationalArea, new CacheableData<Location>() {
            @Override
            public Location fetch() {
                return CoreLibrary.getInstance().context()
                        .getLocationRepository().getLocationByName(operationalArea);
            }
        });
    }

    public static String formatDate(String date, String dateFormat) throws Exception {
        DateFormat sdf = new SimpleDateFormat(dateFormat, Locale.getDefault());
        Date originalDate = sdf.parse(date);

        sdf = new SimpleDateFormat(CARD_VIEW_DATE_FORMAT, Locale.getDefault());

        return sdf.format(originalDate);

    }

    public static String formatDate(Date originalDate) {
        if (originalDate == null) {
            return null;
        }
        DateFormat sdf = new SimpleDateFormat(CARD_VIEW_DATE_FORMAT, Locale.getDefault());
        return sdf.format(originalDate);

    }

    public static String getGlobalConfig(String key, String defaultValue) {
        Map<String, Object> globalConfigs = TaskingLibrary.getInstance().getTaskingLibraryConfiguration().getServerConfigs();//DrishtiApplication.getInstance().getServerConfigs();
        Object val = globalConfigs != null ? globalConfigs.get(key) : null;
        return val == null ? defaultValue : val.toString();
    }

    public static Float getLocationBuffer() {
        return TaskingLibrary.getInstance().getTaskingLibraryConfiguration().getLocationBuffer();
    }


    public static Float getPixelsPerDPI(Resources resources) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                1,
                resources.getDisplayMetrics()
        );
    }

    public static int getInterventionLabel() {
        /*String plan = PreferencesUtil.getInstance().getCurrentPlan();
        String interventionType = PreferencesUtil.getInstance().getInterventionTypeForPlan(plan);
        if (interventionType.equals(FI))
            return R.string.focus_investigation;
        else if (interventionType.equals(IRS))
            return R.string.irs;
        else if (interventionType.equals(MDA))
            return R.string.mda;
        else
            return R.string.irs;*/
        return TaskingLibrary.getInstance().getTaskingLibraryConfiguration().getInterventionLabel();
    }
/*
    public static String getAge(String dob) {
        String dobString = org.smartregister.family.util.Utils.getDuration(dob);
        return dobString.contains("y") ? dobString.substring(0, dobString.indexOf("y")) : dobString;
    }*/

    /**
     * Uses the server setting "validate_far_structures" to determine whether to Validate Far Structures
     * If this variable is not available on the server the value is retrieved from BuildConfig.VALIDATE_FAR_STRUCTURES
     *
     * @return validateFarStructures
     */
    public static Boolean validateFarStructures() {
        //return Boolean.valueOf(getGlobalConfig(CONFIGURATION.VALIDATE_FAR_STRUCTURES, BuildConfig.VALIDATE_FAR_STRUCTURES + ""));
        return TaskingLibrary.getInstance().getTaskingLibraryConfiguration().validateFarStructures();
    }

    /**
     * Uses the server setting "resolve_location_timeout_in_seconds" to determine the Resolve Location Timeout In Seconds value
     * If this variable is not available on the server the value is retrieved from BuildConfig.RESOLVE_LOCATION_TIMEOUT_IN_SECONDS
     *
     * @return ResolveLocationTimeoutInSeconds
     */
    public static int getResolveLocationTimeoutInSeconds() {
        //return Integer.valueOf(getGlobalConfig(CONFIGURATION.RESOLVE_LOCATION_TIMEOUT_IN_SECONDS, BuildConfig.RESOLVE_LOCATION_TIMEOUT_IN_SECONDS + ""));
        return TaskingLibrary.getInstance().getTaskingLibraryConfiguration().getResolveLocationTimeoutInSeconds();
    }

    /**
     * Uses the server setting "admin_password_not_near_structures" to determine the Admin Password required to perform any edits when not near a structure
     * If this variable is not available on the server the value is retrieved from BuildConfig.ADMIN_PASSWORD_NOT_NEAR_STRUCTURES
     *
     * @return AdminPassword
     */
    public static String getAdminPasswordNotNearStructures() {
        //return getGlobalConfig(CONFIGURATION.ADMIN_PASSWORD_NOT_NEAR_STRUCTURES, BuildConfig.ADMIN_PASSWORD_NOT_NEAR_STRUCTURES);
        return TaskingLibrary.getInstance().getTaskingLibraryConfiguration().getAdminPasswordNotNearStructures();
    }

    /**
     * Creates a circle using a GeoJSON polygon.
     * It's not strictly a circle but by increasing the number of sides on the polygon you can get pretty close to one.
     * <p>
     * Adapted from https://stackoverflow.com/questions/37599561/drawing-a-circle-with-the-radius-in-miles-meters-with-mapbox-gl-js/39006388#39006388
     *
     * @param center - Coordinates for the center of the circle
     * @param radius - Radius of the circle in meters
     * @param points - Since this is a GeoJSON polygon, we need to have a large number of sides
     *               so that it gets as close as possible to being a circle
     * @return
     * @throws Exception
     */

    public static Feature createCircleFeature(LatLng center, Float radius, Float points) throws JSONException {
        Float radiusInKm = radius / METERS_PER_KILOMETER;

        JSONArray coordinates = new JSONArray();
        JSONArray coordinate = new JSONArray();
        JSONArray bufferArray = new JSONArray();
        double distanceX = radiusInKm / (KILOMETERS_PER_DEGREE_OF_LONGITUDE_AT_EQUITOR * Math.cos(center.getLatitude() * Math.PI / 180));
        double distanceY = radiusInKm / KILOMETERS_PER_DEGREE_OF_LATITUDE_AT_EQUITOR;

        double theta;
        double x;
        double y;
        for (int i = 0; i < points; i++) {
            theta = (i / points) * (2 * Math.PI);
            x = distanceX * Math.cos(theta);
            y = distanceY * Math.sin(theta);

            Double longitude = center.getLongitude() + x;
            Double latitude = center.getLatitude() + y;
            coordinate.put(longitude);
            coordinate.put(latitude);
            bufferArray.put(coordinate);
            coordinate = new JSONArray();
        }

        coordinates.put(bufferArray);

        JSONObject feature = new JSONObject();
        feature.put("type", "Feature");
        JSONObject geometry = new JSONObject();

        geometry.put("type", "Polygon");
        geometry.put("coordinates", coordinates);
        feature.put("geometry", geometry);

        return Feature.fromJson(feature.toString());
    }

    /**
     * Determines whether a structure is a residence based on the Task Code value
     *
     * @param taskCode
     * @return isResidentialStructure
     */
    public static boolean isResidentialStructure(String taskCode) {
        if (StringUtils.isEmpty(taskCode)) {
            return false;
        }
        return !(MOSQUITO_COLLECTION.equals(taskCode) || LARVAL_DIPPING.equals(taskCode) || PAOT.equals(taskCode));
    }


    public static boolean isFocusInvestigation() {
        //return getInterventionLabel() == R.string.focus_investigation;
        return TaskingLibrary.getInstance().getTaskingLibraryConfiguration().isFocusInvestigation();
    }

    public static boolean isMDA() {
        //return getInterventionLabel() == R.string.mda;
        return TaskingLibrary.getInstance().getTaskingLibraryConfiguration().isMDA();
    }

    public static boolean isFocusInvestigationOrMDA() {
        return isFocusInvestigation() || isMDA();
    }

    public static String getCurrentLocationId() {
        //Location currentOperationalArea = getOperationalAreaLocation(PreferencesUtil.getInstance().getCurrentOperationalArea());
        //return currentOperationalArea == null ? null : currentOperationalArea.getId();
        return TaskingLibrary.getInstance().getTaskingLibraryConfiguration().getCurrentLocationId();
    }

    public static FormTag getFormTag() {
        FormTag formTag = new FormTag();
        AllSharedPreferences sharedPreferences = DrishtiApplication.getInstance().getContext().allSharedPreferences();
        formTag.providerId = sharedPreferences.fetchRegisteredANM();
        formTag.locationId = TaskingLibrary.getInstance().getTaskingLibraryConfiguration().getCurrentOperationalAreaId();
        formTag.teamId = sharedPreferences.fetchDefaultTeamId(formTag.providerId);
        formTag.team = sharedPreferences.fetchDefaultTeam(formTag.providerId);
        formTag.databaseVersion = TaskingLibrary.getInstance().getTaskingLibraryConfiguration().getDatabaseVersion();
        formTag.appVersion = BuildConfig.VERSION_CODE;
        formTag.appVersionName = BuildConfig.VERSION_NAME;
        return formTag;
    }


    public static void tagEventMetadata(Event event, FormTag formTag) {
        event.setProviderId(formTag.providerId);
        event.setLocationId(formTag.locationId);
        event.setChildLocationId(formTag.childLocationId);
        event.setTeam(formTag.team);
        event.setTeamId(formTag.teamId);
        event.setClientDatabaseVersion(formTag.databaseVersion);
        event.setClientApplicationVersion(formTag.appVersion);
        event.addDetails(Properties.APP_VERSION_NAME, formTag.appVersionName);
    }

    public static void recreateEventAndClients(String query, String[] params, SQLiteDatabase db, FormTag formTag, String tableName, String eventType, String entityType, RecreateECUtil util) {
        try {
            if (!DatabaseMigrationUtils.isColumnExists(db, tableName, "id")) {
                return;
            }
            Pair<List<Event>, List<Client>> events = util.createEventAndClients(db, tableName, query, params, eventType, entityType, formTag);
            if (events.first != null) {
                TaskingLibrary.getInstance().getTaskingLibraryConfiguration().tagEventTaskDetails(events.first, db);
            }
            util.saveEventAndClients(events, db);
        } catch (Exception e) {
            Timber.e(e, "Error creating events and clients for %s", tableName);
        }
    }

    public static boolean matchesSearchPhrase(String toSearch, String searchPhrase) {
        if (StringUtils.isBlank(toSearch))
            return false;
        String wordsSpaceAndCommaRegex = "[\\w\\h,]*";
        return toSearch.toLowerCase().matches(wordsSpaceAndCommaRegex + searchPhrase.toLowerCase() + wordsSpaceAndCommaRegex);
    }


    public static Set<String> getInterventionUnitCodes(Set<String> filterList) {
        if (filterList == null) {
            return null;
        }
        Set<String> codes = new HashSet<>();
        if (filterList.contains(Constants.InterventionType.PERSON) || filterList.contains(Constants.InterventionType.FAMILY)) {
            codes.addAll(Constants.Intervention.PERSON_INTERVENTIONS);
        }
        if (filterList.contains(Constants.InterventionType.OPERATIONAL_AREA)) {
            codes.add(Constants.Intervention.BCC);
        }
        if (filterList.contains(Constants.InterventionType.STRUCTURE)) {
            List<String> interventions = new ArrayList<>(Constants.Intervention.FI_INTERVENTIONS);
            interventions.removeAll(Constants.Intervention.PERSON_INTERVENTIONS);
            interventions.addAll(Constants.Intervention.IRS_INTERVENTIONS);
            codes.addAll(interventions);
        }
        return codes;

    }

    /**
     * Uses the server setting "DISPLAY_DISTANCE_SCALE" to determine whether to display the distance scale
     * If this variable is not available on the server the value is retrieved from BuildConfig.DISPLAY_DISTANCE_SCALE
     *
     * @return displayDistanceScale
     */
    public static Boolean displayDistanceScale() {
        //return Boolean.valueOf(getGlobalConfig(CONFIGURATION.DISPLAY_DISTANCE_SCALE, BuildConfig.DISPLAY_DISTANCE_SCALE + ""));
        return TaskingLibrary.getInstance().getTaskingLibraryConfiguration().displayDistanceScale();
    }

}
