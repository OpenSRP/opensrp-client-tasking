package org.smartregister.tasking.adapter;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.powermock.reflect.Whitebox;
import org.robolectric.RuntimeEnvironment;
import org.smartregister.tasking.BaseUnitTest;
import org.smartregister.tasking.R;
import org.smartregister.tasking.model.OfflineMapModel;
import org.smartregister.tasking.util.TestingUtils;
import org.smartregister.tasking.viewholder.AvailableOfflineMapViewHolder;

import java.util.ArrayList;
import java.util.List;

import edu.emory.mathcs.backport.java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.smartregister.tasking.model.OfflineMapModel.OfflineMapStatus.DOWNLOAD_STARTED;
import static org.smartregister.tasking.model.OfflineMapModel.OfflineMapStatus.READY;
import static org.smartregister.tasking.model.OfflineMapModel.OfflineMapStatus.SELECTED_FOR_DOWNLOAD;

/**
 * Created by Richard Kareko on 1/24/20.
 */

public class AvailableOfflineMapAdapterTest extends BaseUnitTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private View.OnClickListener onClickListener;

    private AvailableOfflineMapAdapter adapter;

    private Context context = RuntimeEnvironment.application;

    private List<OfflineMapModel> offlineMapModelList;

    @Before
    public void setUp() {
        adapter = new AvailableOfflineMapAdapter(context, onClickListener);
        offlineMapModelList = new ArrayList<>();
        offlineMapModelList.add(TestingUtils.getOfflineMapModel());
    }

    @Test
    public void testOnCreateViewHolder() {
        LinearLayout vg = new LinearLayout(context);
        AvailableOfflineMapViewHolder holder = adapter.onCreateViewHolder(vg,0);
        assertNotNull(holder);
        assertNotNull(Whitebox.getInternalState(holder, "tvOfflineMapNameLabel"));
        assertNotNull(Whitebox.getInternalState(holder, "tvDownloadingLabel"));
        assertNotNull(Whitebox.getInternalState(holder, "offlineMapCheckBox"));

    }

    @Test
    public void testOnbindViewHolderForReadyStatus() {
        OfflineMapModel model = TestingUtils.getOfflineMapModel();
        model.setOfflineMapStatus(READY);
        adapter.setOfflineMapModels(Collections.singletonList(model));
        LinearLayout vg = new LinearLayout(context);
        AvailableOfflineMapViewHolder holder = adapter.onCreateViewHolder(vg,0);
        adapter.onBindViewHolder(holder, 0);

        CheckBox offlineMapCheckbox = holder.itemView.findViewById(R.id.offline_map_checkbox);
        assertTrue(offlineMapCheckbox.isEnabled());
        assertFalse(offlineMapCheckbox.isSelected());
        assertEquals(View.GONE, holder.itemView.findViewById(R.id.downloading_label).getVisibility());

    }

    @Test
    public void testOnbindViewHolderForDownloadStartedStatus() {
        OfflineMapModel model = TestingUtils.getOfflineMapModel();
        model.setOfflineMapStatus(DOWNLOAD_STARTED);
        adapter.setOfflineMapModels(Collections.singletonList(model));
        LinearLayout vg = new LinearLayout(context);
        AvailableOfflineMapViewHolder holder = adapter.onCreateViewHolder(vg,0);
        adapter.onBindViewHolder(holder, 0);

        CheckBox offlineMapCheckbox = holder.itemView.findViewById(R.id.offline_map_checkbox);
        assertFalse(offlineMapCheckbox.isEnabled());
        assertEquals(View.VISIBLE, holder.itemView.findViewById(R.id.downloading_label).getVisibility());
        assertTrue(offlineMapCheckbox.isChecked());
        assertEquals(context.getString(R.string.downloading), ((TextView) holder.itemView.findViewById(R.id.downloading_label)).getText());

    }

    @Test
    public void testOnbindViewHolderForSelectedForDownloadStatus() {
        OfflineMapModel model = TestingUtils.getOfflineMapModel();
        model.setOfflineMapStatus(SELECTED_FOR_DOWNLOAD);
        adapter.setOfflineMapModels(Collections.singletonList(model));
        LinearLayout vg = new LinearLayout(context);
        AvailableOfflineMapViewHolder holder = adapter.onCreateViewHolder(vg,0);
        adapter.onBindViewHolder(holder, 0);

        CheckBox offlineMapCheckbox = holder.itemView.findViewById(R.id.offline_map_checkbox);
        assertTrue(offlineMapCheckbox.isEnabled());
        assertFalse(offlineMapCheckbox.isSelected());
        assertEquals(View.GONE, holder.itemView.findViewById(R.id.downloading_label).getVisibility());

    }

    @Test
    public void testGetItemCount() {
        assertEquals(0, adapter.getItemCount());
        adapter.setOfflineMapModels(offlineMapModelList);
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    public void testSetOfflineMapModels() {
        adapter.setOfflineMapModels(new ArrayList<>());
        adapter = spy(adapter);

        assertEquals(0, adapter.getItemCount());

        OfflineMapModel model = new OfflineMapModel();
        model.setOfflineMapStatus(DOWNLOAD_STARTED);
        model.setLocation(TestingUtils.gson.fromJson(TestingUtils.operationalAreaGeoJSON, org.smartregister.domain.Location.class));

        adapter.setOfflineMapModels(Collections.singletonList(model));
        assertEquals(1, adapter.getItemCount());
        verify(adapter).notifyDataSetChanged();

    }

}
