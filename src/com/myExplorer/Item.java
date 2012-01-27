package com.myExplorer;

public class Item {
	private String name = "";
	private String path = "";
	private boolean dir = false;
	public Book book = null;
	
	public Item(String name, String path, boolean dir, Book book) {
		this.name = name;
		this.path = path;
		this.dir = dir;
		this.book = book;
	}
	
	public String getName() { return name; }
	public String getPath() { return path; }
	public boolean isDir() { return dir; }
	public boolean isBook() { return book != null; }
}
