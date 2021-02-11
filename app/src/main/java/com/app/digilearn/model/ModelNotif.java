package com.app.digilearn.model;

import com.google.gson.annotations.SerializedName;

public class ModelNotif{

	@SerializedName("Content")
	private String content;

	@SerializedName("Title")
	private String title;

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	@SerializedName("Link")
	private String link;

	public String getContent(){
		return content;
	}

	public String getTitle(){
		return title;
	}
}