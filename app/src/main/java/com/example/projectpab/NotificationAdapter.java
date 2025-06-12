// NotificationAdapter.java
package com.example.projectpab;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends ArrayAdapter<NotificationItem> {
    public NotificationAdapter(Context context, List<NotificationItem> notifications) {
        super(context, 0, notifications);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        NotificationItem notification = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_notification, parent, false);
        }

        TextView tvTitle = convertView.findViewById(R.id.tvNotificationTitle);
        TextView tvMessage = convertView.findViewById(R.id.tvNotificationMessage);
        TextView tvTime = convertView.findViewById(R.id.tvNotificationTime);

        tvTitle.setText(notification.getTitle());
        tvMessage.setText(notification.getMessage());

        // Format timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
        String formattedTime = sdf.format(new Date(notification.getTimestamp()));
        tvTime.setText(formattedTime);

        // Change background if read/unread
        convertView.setBackgroundColor(notification.isRead() ?
                0xFFFFFFFF : // White for read
                0xFFE3F2FD); // Light blue for unread

        return convertView;
    }
}