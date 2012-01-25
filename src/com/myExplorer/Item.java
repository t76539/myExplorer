package com.myExplorer;

public class Item {
	private String name = "";
	private String path = "";
	private boolean dir = false;
	
	public Item(String name, String path, boolean dir) {
		this.name = name;
		this.path = path;
		this.dir = dir;
	}
	
	public String getName() { return name; }
	public String getPath() { return path; }
	public boolean isDir() { return dir; }
}
