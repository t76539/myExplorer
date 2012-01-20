package com.myExplorer;


import java.util.List;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.File;

public class ItemsAdapter extends ArrayAdapter<Item> {
	private final Activity context;
	private final List<Item> itemList;

	public ItemsAdapter(Activity context, List<Item> itemList) {
		super(context, R.layout.item, itemList);
		this.context = context;
		this.itemList = itemList; 
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = context.getLayoutInflater();
		View rowView = inflater.inflate(R.layout.item, null, true);
		TextView title1 = (TextView) rowView.findViewById(R.id.rowtext);
		TextView title2 = (TextView) rowView.findViewById(R.id.rowtext2);
		ImageView imageView = (ImageView) rowView.findViewById(R.id.image);
		
		String name = itemList.get(position).getName();
		Book b = new Book();
		if (!itemList.get(position).isDir() && b.loadFile(itemList.get(position).getPath())) {
			title1.setText(b.book_title);
			title2.setText(b.getAuthor());
			if (b.cover != null) {
				File imgFile = new  File("/sdcard/books/unzip/" + b.cover);
				if(imgFile.exists()) {
				    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
				    imageView.setImageBitmap(myBitmap);
				}				
			}
			else
				imageView.setImageResource(R.drawable.fb2_32x32);
		}
		else {
			title1.setText(name);
			title2.setText(itemList.get(position).getPath());
			if (itemList.get(position).isDir())
				imageView.setImageResource(R.drawable.ic_launcher);
			else
				imageView.setImageResource(R.drawable.pulse);
		}

		return rowView;
	}
}
