package com.myExplorer;


import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ItemsAdapter extends ArrayAdapter<String> {
	private final Activity context;
	private final String[] names;

	public ItemsAdapter(Activity context, String[] names) {
		super(context, R.layout.item, names);
		this.context = context;
		this.names = names;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = context.getLayoutInflater();
		View rowView = inflater.inflate(R.layout.item, null, true);
		TextView textView = (TextView) rowView.findViewById(R.id.rowtext);
		ImageView imageView = (ImageView) rowView.findViewById(R.id.image);
		String s = names[position];
		textView.setText(s);
		if (position % 2 == 0)
			imageView.setImageResource(R.drawable.ic_launcher);
		else
			imageView.setImageResource(R.drawable.pulse);

		return rowView;
	}
}
