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
//import android.widget.LinearLayout;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ImageView;
import android.net.Uri;
import java.util.Arrays;
import android.view.Window;
import android.view.WindowManager;
import android.util.Log;
import android.app.ProgressDialog;


public class MyExplorerActivity extends ListActivity {
	private List<Item> itemList = null;
	private String root="/";
	private String dirParent = null;
	private TextView myPath;
	private ImageView myImage;
	private static String pathDir;
	private static String parentDir = "/";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);	// Убираем заголовок
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);	// Полный экран  		
	    setContentView(R.layout.main);
	    myPath = (TextView)findViewById(R.id.path);
	    myImage = (ImageView)findViewById(R.id.image);
	    
	    View.OnClickListener cl = new View.OnClickListener() {
	    	public void onClick(View v) {
	    		if (dirParent != null)
					runDir(dirParent);
			}
		}; 
	    		
	    myPath.setOnClickListener(cl);
	    myImage.setOnClickListener(cl);
	      
//	      runDir("/mnt/asec/books");
//		    getDir("/sdcard/My Files/Books");
	      runDir(root);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
	      final File file = new File(itemList.get(position).getPath());

	      if (file.isDirectory()) {
	          if(file.canRead())
	              runDir(file.getPath());
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

	private void runDir(String dirPath) {
        setTitle(dirPath);
		pathDir = dirPath;
		final ProgressDialog dialog = ProgressDialog.show(MyExplorerActivity.this, "", 
                "Loading. Please wait...", true);
		
        new Thread(new Runnable() {
            public void run() {
                // запустит фоновую процедуру
        		parentDir = getDir(pathDir);
                // по завершении которой в основном потоке
            	MyExplorerActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                    	myPath.setText(parentDir);
                    	ItemsAdapter adapter = new ItemsAdapter(MyExplorerActivity.this, itemList);
                    	setListAdapter(adapter);
                        dialog.dismiss();
                    }
                });
            }
        }).start(); 		
		
	}
	
	private String getDir(String dirPath) {
	    itemList = new ArrayList<Item>();

	    File f = new File(dirPath);
	    File[] files = f.listFiles();
	    Pair[] pairs = new Pair[files.length];
	    Book[] books = new Book[files.length];
		for (int i = 0; i < files.length; i++) {
	    	books[i] = new Book();
		    if (!files[i].isDirectory()) {
		    	books[i].is_book = books[i].loadFile(files[i].getPath());
		    	if (!books[i].is_book)
		    		books[i] = null;
		    }
		    pairs[i] = new Pair(files[i], books[i]);
		}
		Arrays.sort(pairs);

		for (int i=0; i < files.length; i++) {
			File file = pairs[i].f;
	        itemList.add(new Item(file.getName(), file.getPath(), file.isDirectory(), pairs[i].b));
		}
	      
	    if(!dirPath.equals(root)) {
	    	dirParent = f.getParent();
			return "../ (" + dirPath + ")";
	    }
	    else {
	    	dirParent = null;
	    	return "../";
	    }

	}

	private class Pair implements Comparable {
	    private File f;
	    private Book b;

	    public Pair(File file, Book book) {
	        f = file;
	        b = book; 
	    }

	    public int compareTo(Object o) {
	        Pair c = (Pair)o;
	        
	        String f1_name = f.getName();
	        String f2_name = c.f.getName();
	        
	        if (f.isDirectory()) {
	        	if (!c.f.isDirectory()) 
	        		return -1;
	        	else {
	    	        f1_name = f.getAbsolutePath();
	    	        f2_name = c.f.getAbsolutePath();
	        	}
	        } 
	        else if (c.f.isDirectory()) { 
	        	return 1;
	        }
	        
	        
	        if (!f.isDirectory() && (b != null ) && b.is_book)
	        	f1_name = b.book_title;
	        if (!c.f.isDirectory() && (c.b != null) && c.b.is_book)
	        	f2_name = c.b.book_title;
	        
	        int res = f1_name.compareTo(f2_name);
	        Log.e("F1", ":" + f1_name);
	        Log.e("F2", ":" + f2_name);
	        Log.e("CMP", "=" + res);
	        
	        return res;
	    }
	};

}
