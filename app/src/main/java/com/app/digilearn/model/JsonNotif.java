package com.app.digilearn.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class JsonNotif{

	@SerializedName("show_in_foreground")
	@Expose
	private String showInForeground;

	@SerializedName("sound")
	@Expose
	private String sound;

	@SerializedName("icon")
	@Expose
	private String icon;

	@SerializedName("body")
	@Expose
	private String body;

	@SerializedName("title")
	@Expose
	private String title;

	@SerializedName("click_action")
	@Expose
	private String clickAction;

	public void setShowInForeground(String showInForeground){
		this.showInForeground = showInForeground;
	}

	public String getShowInForeground(){
		return showInForeground;
	}

	public void setSound(String sound){
		this.sound = sound;
	}

	public String getSound(){
		return sound;
	}

	public void setIcon(String icon){
		this.icon = icon;
	}

	public String getIcon(){
		return icon;
	}

	public void setBody(String body){
		this.body = body;
	}

	public String getBody(){
		return body;
	}

	public void setTitle(String title){
		this.title = title;
	}

	public String getTitle(){
		return title;
	}

	public void setClickAction(String clickAction){
		this.clickAction = clickAction;
	}

	public String getClickAction(){
		return clickAction;
	}

	@Override
 	public String toString(){
		return 
			"JsonNotif{" + 
			"show_in_foreground = '" + showInForeground + '\'' + 
			",sound = '" + sound + '\'' + 
			",icon = '" + icon + '\'' + 
			",body = '" + body + '\'' + 
			",title = '" + title + '\'' + 
			",click_action = '" + clickAction + '\'' + 
			"}";
		}
}