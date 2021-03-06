package org.smartregister.tasking.viewholder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.smartregister.tasking.R;

public class DownloadedOfflineMapViewHolder extends BaseOfflineMapViewHolder {

    private TextView tvDownloadingLabel;


    public DownloadedOfflineMapViewHolder(@NonNull View itemView) {
        super(itemView);
        tvDownloadingLabel = itemView.findViewById(R.id.downloading_label);
    }

    public void setDownloadedMapSize(String offlineMapSize) {
        this.tvDownloadingLabel.setText(offlineMapSize);
    }


    public void displayDownloadSizeLabel(boolean displayDownloadSizeLabel) {
        if (displayDownloadSizeLabel) {
            tvDownloadingLabel.setVisibility(View.VISIBLE);
        } else {
            tvDownloadingLabel.setVisibility(View.GONE);
        }
    }

}
