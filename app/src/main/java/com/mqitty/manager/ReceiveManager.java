package com.mqitty.manager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.mqitty.R;
import com.mqitty.model.ReceiverModel;

public class ReceiveManager {

    public static View addReceiveModelToLayout(ViewGroup container, ReceiverModel model) {
        if (container == null || model == null) return null;

        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        // Inflate the model layout
        View receiveModelView = inflater.inflate(R.layout.receive_model, container, false);

        // Bind data to views
        TextView nameTv = receiveModelView.findViewById(R.id.name_receive_model);
        TextView descriptionTv = receiveModelView.findViewById(R.id.description_receive_model);

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
            container.addView(receiveModelView, addContainerIndex);
        } else {
            container.addView(receiveModelView);
        }

        return receiveModelView;
    }
}
