package com.myExplorer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.net.Uri;
import java.util.Arrays;

public class MyExplorerActivity extends ListActivity {
	private List<Item> itemList = null;
	private String root="/";
	private TextView myPath;

	@Override
	public void onCreate(Bundle savedInstanceState) {
	      super.onCreate(savedInstanceState);
	      setContentView(R.layout.main);
	      myPath = (TextView)findViewById(R.id.path);
//	      myPath.setTextSize(28);
//	    getDir("/sdcard/My Files/Books");
	      getDir("/sdcard/books");
//	      getDir(root);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
	      final File file = new File(itemList.get(position).getPath());

	      if (file.isDirectory()) {
	          if(file.canRead())
	              getDir(file.getPath());
	          else {
	              new AlertDialog.Builder(this)
	              .setIcon(R.drawable.ic_launcher)
	              .setTitle("[" + file.getName() + "] folder can't be read!")
	              .setPositiveButton("OK", 
	                new DialogInterface.OnClickListener() {
	                 public void onClick(DialogInterface dialog, int which) {
	                  // TODO Auto-generated method stub
	                 }
	                }).show();
	          }
	      }
	      else {
		   	   try {
		   		   	Intent i = new Intent(android.content.Intent.ACTION_VIEW, 
		   		   			Uri.parse("file://" + file.getAbsolutePath()));             		   
		    	    	startActivity(i);            	
		   	   }
		   	   catch (Exception e) {
		   	   }
	      }
	}

	private void getDir(String dirPath) {
		myPath.setText("Положение: " + dirPath);
	    itemList = new ArrayList<Item>();

	    File f = new File(dirPath);
	    File[] files = f.listFiles();
	    Pair[] pairs = new Pair[files.length];
		for (int i = 0; i < files.length; i++)
		    pairs[i] = new Pair(files[i]);
		Arrays.sort(pairs);
		for (int i = 0; i < files.length; i++)
		    files[i] = pairs[i].f;

	    if(!dirPath.equals(root)) {
	        itemList.add(new Item("..", f.getParent(), true));
	    }

	      for (int i=0; i < files.length; i++) {
	          File file = files[i];
	          itemList.add(new Item(file.getName(), file.getPath(), file.isDirectory()));
	      }

	      ItemsAdapter adapter = new ItemsAdapter(this, itemList);
	      setListAdapter(adapter);
	}

	private class Pair implements Comparable {
	    public File f;

	    public Pair(File file) {
	        f = file;
	    }

	    public int compareTo(Object o) {
	        Pair c = (Pair)o;
	        if (f.isDirectory()) {
	        	if (!c.f.isDirectory()) return -1;
	        } else if (c.f.isDirectory()) return 1;
    		return f.getName().compareTo(c.f.getName());
	    }
	};

}
