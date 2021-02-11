package com.app.digilearn.model;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("com.robohorse.robopojogenerator")
public class ResponseWebview{

	@SerializedName("LinkUrl")
	private String linkUrl;

	public void setLinkUrl(String linkUrl){
		this.linkUrl = linkUrl;
	}

	public String getLinkUrl(){
		return linkUrl;
	}

	@Override
 	public String toString(){
		return 
			"ResponseWebview{" + 
			"linkUrl = '" + linkUrl + '\'' + 
			"}";
		}
}