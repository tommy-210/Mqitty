package com.mqitty.manager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.mqitty.R;
import com.mqitty.model.SendModel;

public class SendManager {

    public static void addSendModelToLayout(ViewGroup container, SendModel model) {
        if (container == null || model == null) return;

        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        // Inflate the model layout
        View sendModelView = inflater.inflate(R.layout.send_model, container, false);

        // Bind data to views
        TextView nameTv = sendModelView.findViewById(R.id.name_send_model);
        TextView descriptionTv = sendModelView.findViewById(R.id.description_send_model);

        nameTv.setText(model.getName());
        descriptionTv.setText(model.getDescription());

        // Add the new view to the container. 
        // We add it at index 0 or before the 'add_container' if we want it above the button.
        int addContainerIndex = -1;
        for (int i = 0; i < container.getChildCount(); i++) {
            if (container.getChildAt(i).getId() == R.id.add_container) {
                addContainerIndex = i;
                break;
            }
        }

        if (addContainerIndex != -1) {
            container.addView(sendModelView, addContainerIndex);
        } else {
            container.addView(sendModelView);
        }
    }
}
