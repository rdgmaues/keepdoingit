
package com.rm.keepdoingit;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class OptionsListAdapter extends ArrayAdapter<Bundle> {
	private Context context;
	private ArrayList<Bundle> options = new ArrayList<Bundle>();
	private int role;

	private static class ViewHolder {
		public ImageView icon;
		public TextView text;
		public TextView title;
	}

	public OptionsListAdapter(Context context, int layout, ArrayList<Bundle> options, int role) {
		super(context, layout, options);
		this.context = context;
		this.options = options;
		this.role = role;
	}


	public int getType(int position) {
		return options.get(position).getInt("type");
	}

	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.optionslist_row, null);

			holder = new ViewHolder();
			holder.title = (TextView) convertView.findViewById(R.id.title);
			holder.text = (TextView) convertView.findViewById(R.id.text);
			holder.icon = (ImageView) convertView.findViewById(R.id.image);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		Bundle option = options.get(position);
			
		Drawable drawable = context.getResources().getDrawable(((KeepDoingItApplication) context.getApplicationContext()).getDrawableID(option.getInt("type")));
		holder.icon.setImageDrawable(drawable);
		holder.icon.setImageLevel(role);

		String description = "";
		//String suffix = " event";
		int color = context.getResources().getColor(R.color.red);;
		
		switch (role)
		{
		case 0:
			description = ((KeepDoingItApplication) context.getApplicationContext()).generateEventDescription(option);
			break;
		case 1:
			//suffix = " condition";
			color = context.getResources().getColor(R.color.yellow);
			description = "if "+((KeepDoingItApplication) context.getApplicationContext()).generateConditionDescription(option);
			break;
		case 2:
			//suffix = " action";
			color = context.getResources().getColor(R.color.green);
			description = ((KeepDoingItApplication) context.getApplicationContext()).generateActionDescription(option);
			break;
		}
		
		holder.title.setTextColor(color);
		holder.title.setText(option.getString("title"));//+suffix);
		
		holder.text.setText(Html.fromHtml(description));
		
		return convertView;
	}

}
