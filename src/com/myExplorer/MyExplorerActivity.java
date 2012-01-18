package com.myExplorer;

//import android.app.Activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.net.Uri;
//import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.util.Enumeration;
import java.io.InputStream;
import java.io.FileOutputStream;

public class MyExplorerActivity extends ListActivity {
	
	private List<String> item = null;
	private List<String> path = null;
	private String root="/";
	private TextView myPath;

	@Override
  public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.main);
      myPath = (TextView)findViewById(R.id.path);
//      myPath.setTextSize(28);
//    getDir("/sdcard/My Files/Books");
    getDir("/sdcard/books");
//      getDir(root);
  }

  private void getDir(String dirPath) {

      myPath.setText("Положение: " + dirPath);
      item = new ArrayList<String>();
      path = new ArrayList<String>();

      File f = new File(dirPath);
      File[] files = f.listFiles();

      if(!dirPath.equals(root)) {
//          item.add(root);
//          path.add(root);
          item.add("../");
          path.add(f.getParent());
      }

      for (int i=0; i < files.length; i++) {
          File file = files[i];
          path.add(file.getPath());
          if(file.isDirectory())
          	item.add(file.getName() + "/");
          else
          	item.add(file.getName());
      }

      //ArrayAdapter<String> fileList = new ArrayAdapter<String>(this, R.layout.item, R.id.rowtext, item);
      //setListAdapter(fileList);
    
      SimpleAdapter adapter = new SimpleAdapter(this, createSensorsList(), R.layout.item, 
              new String[] {"title1", "title2"}, 
              new int[] {R.id.rowtext, R.id.rowtext2});
      setListAdapter(adapter);
  }

  private List<Map<String, ?>> createSensorsList() {
	  List<Map<String, ?>> items = new ArrayList<Map<String, ?>>();
	  
	  for (int i = 0; i < item.size(); i++) {
		  Map<String, Object> map = new HashMap<String, Object>();
		  
		  String filename = path.get(i);
		  String ext = getFileExt( item.get(i).toLowerCase() );
		  if (ext.contains(".zip")) {
			  String fname = getZIP(filename);
			  if (0 < fname.length()) {
				  ext = ".fb2.zip";
				  filename = fname;
			  }
		  }

		  if (ext.contains(".fb2") || ext.contains(".fb2.zip")) {
			  try {
				  Book b = getFB2(filename);
				  map.put("title1", b.book_title + " " + b.first_name + " " + b.middle_name + " " + b.last_name 
						  + " " + b.coverpage + " " + b.cover + " " + b.jpg);
			  } catch (Exception e) {
				  map.put("title1", e.getMessage());
			  }
		  } else {
			  map.put("title1", filename);
		  }
		  map.put("title2", path.get(i));
		  items.add(map);
	  }
	  
	  return items;
  }
  
  private String getZIP(String filename) {
	  File outputFile = null;
	  try {
		  ZipFile zip = new ZipFile(filename);
	      Enumeration<? extends ZipEntry> zippedFiles = zip.entries();
	      while (zippedFiles.hasMoreElements()) {
	    	  ZipEntry entry = zippedFiles.nextElement();
	          InputStream is = zip.getInputStream(entry);
	          String name = entry.getName();	    	  
	          outputFile = new File("/sdcard/books/unzip/" + name);
	          String outputPath = outputFile.getCanonicalPath();
	          name = outputPath.substring(outputPath.lastIndexOf("/") + 1);
	          outputPath = outputPath.substring(0, outputPath.lastIndexOf("/"));
	          File outputDir = new File(outputPath);
	          outputDir.mkdirs();
	          outputFile = new File(outputPath, name);
	          outputFile.createNewFile();
	          FileOutputStream out = new FileOutputStream(outputFile);

	          byte buf[] = new byte[16384];
	          do {
		            int numread = is.read(buf);
		            if (numread <= 0) {
		            	break;
		            } else {
		            	out.write(buf, 0, numread);
		            }
		            break;
	          } while (true);

	          is.close();
	          out.close();
	          break;
	        }
	  } catch (Exception e) {
//		  return e.getMessage();
		  return "";
	  }
	  if (outputFile == null)
		  return "";
	  else
		  return outputFile.getPath();
  }
  
  private Book getFB2(String filename) 
		  throws XmlPullParserException, IOException 
  {
      XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
      factory.setNamespaceAware(true);
      XmlPullParser xpp = factory.newPullParser();
      
      String enc = getEncode(filename);
      xpp.setInput( new FileInputStream(filename), enc);
      int eventType = xpp.getEventType();
      
      boolean in_book_title = false;
      String tag_book_title = "book-title";
      
      boolean in_first_name = false;
      String tag_first_name = "first-name";
      
      boolean in_middle_name = false;
      String tag_middle_name = "middle-name";
      
      boolean in_last_name = false;
      String tag_last_name = "last-name";
      
      boolean in_coverpage = false;
      String tag_coverpage = "coverpage";
      
      boolean in_image = false;
      String tag_image = "image";
      
      boolean in_binary = false;
      String tag_binary = "binary";
      
      Book book = new Book();
      
      while (eventType != XmlPullParser.END_DOCUMENT) {
      	if(eventType == XmlPullParser.START_DOCUMENT)	;//Log.e("2","Start document");
      	else if(eventType == XmlPullParser.START_TAG){
      		String tagName = xpp.getName();
      		if (tagName.equals(tag_book_title))			in_book_title = true;
      		else if (tagName.equals(tag_first_name))	in_first_name = true;
      		else if (tagName.equals(tag_middle_name))	in_middle_name = true;
      		else if (tagName.equals(tag_last_name))		in_last_name = true;
      		else if (tagName.equals(tag_coverpage))		in_coverpage = true;
      		else if (tagName.equals(tag_image))			{
      			in_image = true;
      			book.coverpage = xpp.getAttributeValue(0);
      			if (book.coverpage.charAt(0) == '#')
      				book.coverpage = book.coverpage.substring(1);
      		}
      		else if (tagName.equals(tag_binary)) {
      			book.cover = xpp.getAttributeValue("", "id");
      			if (book.coverpage.equals(book.cover))
      				in_binary = true;
      		}
      		else if (tagName.equals("body")) break;
      	}
      	else if(eventType == XmlPullParser.END_TAG)	{
      		String tagName = xpp.getName();
      		if (tagName.equals(tag_book_title)) 		in_book_title = false;
      		else if (tagName.equals(tag_first_name))	in_first_name = false;
      		else if (tagName.equals(tag_middle_name))	in_middle_name = false;
      		else if (tagName.equals(tag_last_name))		in_last_name = false;
      		else if (tagName.equals(tag_coverpage))		in_coverpage = false;
      		else if (tagName.equals(tag_image))			in_image = false;
      	}
      	else if(eventType == XmlPullParser.TEXT) {
      		if (in_book_title) 			book.book_title = xpp.getText();
      		else if (in_first_name)		book.first_name = xpp.getText();
      		else if (in_middle_name)	book.middle_name = xpp.getText();
      		else if (in_last_name)		book.last_name = xpp.getText();
      		else if (in_binary) {
      			book.jpg = ":" + xpp.getText();
      		}
//      		else if (in_coverpage && in_image)book.coverpage = "cp:" + xpp.getAttributeValue(0);
      	}
      	eventType = xpp.next();
      }

//      return book.book_title + " " + book.first_name + " " + book.middle_name + " " + book.last_name;
      return book;
  }
  
  private String getEncode(String filename) {
      String header = "";
  	try {
  		InputStreamReader r = new InputStreamReader( new FileInputStream(filename) );
          int data = r.read();
          while (data != -1) {
          	header = header + (char)data;
          	if (header.indexOf("?>") != -1)
          		break;
          	data = r.read();
          }
  	} catch (Exception e) { return ""; }
      
      String enc = "empty";
      int pEnc = header.indexOf("encoding=\"");
      if (pEnc != -1) {
      	enc = header.substring(pEnc + 10);
          pEnc = enc.indexOf("\"");
          if (pEnc != -1) 
          	enc = enc.substring(0, pEnc);
          else
          	return "";
      }
      else
      	return "";
  	return enc;
  }
  
  @Override
  protected void onListItemClick(ListView l, View v, int position, long id) {
      final File file = new File(path.get(position));

      if (file.isDirectory()) {
          if(file.canRead())
              getDir(path.get(position));
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
  
  private String getFileExt(String fname) {
	  for (int i = fname.length()-1; 0 < i; i--)
		  if (fname.charAt(i) == '.')
			  return fname.substring(i);
	  return "";
  }
}
