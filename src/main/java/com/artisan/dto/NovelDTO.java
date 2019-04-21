package com.artisan.dto;


import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

public class NovelDTO {
	
	private String title;
	private String author;
	private String wordCount;
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date publishDate;
	
	
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getWordCount() {
		return wordCount;
	}
	public void setWordCount(String wordCount) {
		this.wordCount = wordCount;
	}
	public Date getPublishDate() {
		return publishDate;
	}
	public void setPublishDate(Date publishDate) {
		this.publishDate = publishDate;
	}
	
	
	

}
