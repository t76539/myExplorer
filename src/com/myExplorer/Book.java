package com.myExplorer;

import java.io.InputStream;
import android.graphics.Bitmap;
import java.util.Enumeration;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.FileOutputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import java.io.StringReader;

public class Book {
    public String book_title = "";
    private String author = null;
    public String first_name = "";       
    public String middle_name = "";       
    public String last_name = "";
    public String coverpage = "";
    public String cover = null;
    public String jpg = "";
    public String errorMessage = "";
    public String descr = null;
    public Bitmap bmp = null;
    
    public String getAuthor() {
    	if (author == null)
    		return first_name + " " + middle_name + " " +last_name;
    	else
    		return author;
    }
    
    public boolean loadFile(String filename) {
    	String ext = getFileExt( filename.toLowerCase() );
    	
    	String zipname = null;
    	if (ext.equals(".zip")) {
    		zipname = unzip(filename);
    		if (zipname != null) {
    			filename = zipname;
    			ext = ".fb2";
    		}
      	}
    	
    	if (ext.equals(".fb2")) {
    		try {
    			boolean res = parseFB2(filename);
    			if (zipname != null) {
    				File dfile = new File(zipname);
    				if (dfile.exists())
    					dfile.delete();
    			}
    			return res;
    		} catch (Exception e) {
    			errorMessage = e.getMessage();
    			return false;
    		}
    	} else if (ext.equals(".epub")) {
    		try {
    			return parseEPUB(filename);
    		} catch (Exception e) {
    			errorMessage = e.getMessage();
    			return false;
    		}
    	}
    	return false;
    }
    
    private String getFileExt(String filename) {
  	  for (int i = filename.length()-1; 0 < i; i--)
  		  if (filename.charAt(i) == '.')
  			  return filename.substring(i);
  	  return "";
    }

    private String getFullPath(String container)
		  throws XmlPullParserException, IOException 
    {
		      XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		      factory.setNamespaceAware(true);
		      XmlPullParser xpp = factory.newPullParser();
		      
		      xpp.setInput( new StringReader ( container ) );
		      int eventType = xpp.getEventType();
		      
		      boolean in_rootfile = false;
		      String tag_rootfile = "rootfile";
		      
		      while (eventType != XmlPullParser.END_DOCUMENT) {
		      	if(eventType == XmlPullParser.START_DOCUMENT)	;//Log.e("2","Start document");
		      	else if(eventType == XmlPullParser.START_TAG){
		      		String tagName = xpp.getName();
		      		if (tagName.equals(tag_rootfile)) {
		      			in_rootfile = true;
		      			String full_path = xpp.getAttributeValue("", "full-path");
		      			return full_path;
		      		}
		      	}
		      	else if(eventType == XmlPullParser.END_TAG)	{
		      		String tagName = xpp.getName();
		      		if (tagName.equals(tag_rootfile)) in_rootfile = false;
		      	}
		      	else if(eventType == XmlPullParser.TEXT) {
		      		if (in_rootfile) book_title = xpp.getText();
		      	}
		      	eventType = xpp.next();
		      }

		      return "";
    }
    
    private boolean parseOpf(String opf)
		  throws XmlPullParserException, IOException 
    {
		      XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		      factory.setNamespaceAware(true);
		      XmlPullParser xpp = factory.newPullParser();
		      
		      xpp.setInput( new StringReader ( opf ) );
		      int eventType = xpp.getEventType();
		      
		      boolean in_title = false;
		      String tag_title = "title";
		      
		      boolean in_creator = false;
		      String tag_creator = "creator";
		      
		      boolean in_descr = false;
		      String tag_descr = "description";
		      
		      boolean in_embcover = false;
		      String tag_embcover = "embeddedcover";
		      
		      while (eventType != XmlPullParser.END_DOCUMENT) {
		      	if(eventType == XmlPullParser.START_DOCUMENT);
		      	else if(eventType == XmlPullParser.START_TAG){
		      		String tagName = xpp.getName();
		      		if (tagName.equals(tag_title)) 			in_title = true;
		      		else if (tagName.equals(tag_creator))	in_creator = true;
		      		else if (tagName.equals(tag_descr))		
		      			in_descr = true;
		      		else if (tagName.equals(tag_embcover))	
		      			in_embcover = true;
		      	}
		      	else if(eventType == XmlPullParser.END_TAG)	{
		      		String tagName = xpp.getName();
		      		if (tagName.equals(tag_title)) 			in_title = false;
		      		else if (tagName.equals(tag_creator)) 	in_creator = false;
		      		else if (tagName.equals(tag_descr))		in_descr = false;
		      		else if (tagName.equals(tag_embcover))	
		      			in_embcover = false;
		      	}
		      	else if(eventType == XmlPullParser.TEXT) {
		      		if (in_title) 			book_title = xpp.getText();
		      		else if (in_creator) 	author = xpp.getText();
		      		else if (in_descr) 		descr = xpp.getText();
		      		else if (in_embcover)	
		      			cover = xpp.getText();
		      	}
		      	eventType = xpp.next();
		      }

		      return true;
    }
    
    private boolean parseEPUB(String filename) 
    		  throws XmlPullParserException, IOException 
    {
		String opfName = getFullPath(unzip(filename, "META-INF/container.xml"));
		try {
			parseOpf(unzip(filename, opfName));
		} catch (Exception e) {
			String msg = e.getMessage();
			return false;
		}
		if (cover != null) {
			unzip(filename, "OEBPS/" + cover, "/sdcard/books/unzip/cover.jpg");
			return true;
		}
		
    	return true;
    }
    
    private boolean parseFB2(String filename) 
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
        			coverpage = xpp.getAttributeValue(0);
        			if (coverpage.charAt(0) == '#')
        				coverpage = coverpage.substring(1);
        		}
        		else if (tagName.equals(tag_binary)) {
        			cover = xpp.getAttributeValue("", "id");
        			if (coverpage.equals(cover))
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
        		if (in_book_title) 			book_title = xpp.getText();
        		else if (in_first_name)		first_name = xpp.getText();
        		else if (in_middle_name)	middle_name = xpp.getText();
        		else if (in_last_name)		last_name = xpp.getText();
        		else if (in_binary) {
        			jpg = ":" + xpp.getText();
        		}
//        		else if (in_coverpage && in_image)book.coverpage = "cp:" + xpp.getAttributeValue(0);
        	}
        	eventType = xpp.next();
        }

//        return book.book_title + " " + book.first_name + " " + book.middle_name + " " + book.last_name;
        return true;
    }
    
    // Определить кодировку файла UTF-8,Win-1251,...
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

    private String unzip(String zipFileName, String extFileName) {
  	  //File outputFile = null;
  	  String result = "";
    	  try {
    		  ZipFile zip = new ZipFile(zipFileName);
    		  ZipEntry entry = zip.getEntry(extFileName);
    	      InputStream is = zip.getInputStream(entry);

    	      byte buf[] = new byte[4096];
    	      do {
    	    	  int numread = is.read(buf, 0, 4096);
    		      if (numread <= 0)
    		    	  break;
    		      else {
    		    	  String data = new String(buf, 0, numread, "UTF-8");
    		    	  result += data;
    		      }
    	      } while (true);

    	      is.close();
    	  } catch (Exception e) {
//    		  String msg = e.getMessage();
    		  return null;
    	  }
    	  return result;
  }
  
    private boolean unzip(String zipFileName, String extFileName, String outFileName) {
    	
  	  	try {
  	  		ZipFile zip = new ZipFile(zipFileName);
  	  		ZipEntry entry = zip.getEntry(extFileName);
  	  		InputStream is = zip.getInputStream(entry);
  	      
  	  		File outputFile = new File(outFileName);
  	  		String outputPath = outputFile.getCanonicalPath();
  	  		String name = outputPath.substring(outputPath.lastIndexOf("/") + 1);
  	  		outputPath = outputPath.substring(0, outputPath.lastIndexOf("/"));
  	  		File outputDir = new File(outputPath);
  	  		outputDir.mkdirs();
  	  		outputFile = new File(outputPath, name);
  	  		outputFile.createNewFile();
  	  		FileOutputStream out = new FileOutputStream(outputFile);

  	  		final int BUF_SIZE = 4096;
  	  		byte buf[] = new byte[BUF_SIZE+1];
  	  		int numread;
  	  		while (0 < (numread = is.read(buf, 0, BUF_SIZE)))
				out.write(buf, 0, numread);
/*  	  		
  	  		do {
  	  			int numread = is.read(buf, 0, 4096);
  	  			if (numread <= 0)
  	  				break;
  	  			else
  	  				out.write(buf, 0, numread);
  	  		} while (true);
*/  	  		

  	  		is.close();
  	  		out.close();
  	  	} catch (Exception e) {
  	  		return false;
  	  	}
  	  	return true;
  }
  
    private String unzip(String filename) {
  	  File outputFile = null;
  	  try {
  		  ZipFile zip = new ZipFile(filename);
  	      Enumeration<? extends ZipEntry> zippedFiles = zip.entries();
  	      while (zippedFiles.hasMoreElements()) {
  	    	  ZipEntry entry = zippedFiles.nextElement();
  	          InputStream is = zip.getInputStream(entry);
  	          String name = entry.getName();
  	          String ext = getFileExt(name.toLowerCase());
  	          if (!ext.equals(".fb2")) 
  	        	  return null;
  	          outputFile = new File(filename + ".fb2");
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
//  		  return e.getMessage();
  		  return null;
  	  }
  	  return outputFile.getPath();
  }

}

