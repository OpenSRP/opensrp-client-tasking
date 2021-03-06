package org.smartregister.tasking.adapter;

import android.content.Context;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mapbox.mapboxsdk.offline.OfflineRegion;
import com.mapbox.mapboxsdk.offline.OfflineRegionStatus;

import org.smartregister.tasking.R;
import org.smartregister.tasking.model.OfflineMapModel;
import org.smartregister.tasking.util.Utils;
import org.smartregister.tasking.viewholder.DownloadedOfflineMapViewHolder;

import java.util.ArrayList;
import java.util.List;

public class DownloadedOfflineMapAdapter extends RecyclerView.Adapter<DownloadedOfflineMapViewHolder> {

    private Context context;

    private View.OnClickListener offlineMapClickHandler;

    private List<OfflineMapModel> offlineMapModels = new ArrayList<>();

    public DownloadedOfflineMapAdapter(Context context, View.OnClickListener offlineMapClickHandler) {
        this.context = context;
        this.offlineMapClickHandler = offlineMapClickHandler;

    }

    @NonNull
    @Override
    public DownloadedOfflineMapViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.offline_map_row, parent, false);
        return new DownloadedOfflineMapViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DownloadedOfflineMapViewHolder viewHolder, int position) {
        OfflineMapModel offlineMapModel = offlineMapModels.get(position);
        viewHolder.setOfflineMapLabel(offlineMapModel.getDownloadAreaLabel());
        viewHolder.setItemViewListener(offlineMapModel, offlineMapClickHandler);

        switch (offlineMapModel.getOfflineMapStatus()) {
            case READY:
            case DOWNLOADED:
                viewHolder.checkCheckBox(false);
                break;
            default:
                break;

        }

        displayOfflineMapSize(offlineMapModel, viewHolder);

    }

    private void displayOfflineMapSize(OfflineMapModel offlineMapModel, DownloadedOfflineMapViewHolder viewHolder ) {
        if (offlineMapModel == null || offlineMapModel.getOfflineRegion() == null) {
            return;
        }

        offlineMapModel.getOfflineRegion().getStatus(new OfflineRegion.OfflineRegionStatusCallback() {
            @Override
            public void onStatus(OfflineRegionStatus status) {

                viewHolder.displayDownloadSizeLabel(true);

                String mapDownloadSize = Formatter.formatFileSize(context, status.getCompletedResourceSize());
                String downloadDate = Utils.formatDate(offlineMapModel.getDateCreated());

                viewHolder.setDownloadedMapSize(context.getString(R.string.offline_map_size, mapDownloadSize, downloadDate));

            }

            @Override
            public void onError(String error) {
                // Do nothing
            }
        });

    }

    @Override
    public int getItemCount() {
        return offlineMapModels.size();
    }

    public void setOfflineMapModels(List<OfflineMapModel> offlineMapModels) {
        this.offlineMapModels = offlineMapModels;
        notifyDataSetChanged();
    }
}
