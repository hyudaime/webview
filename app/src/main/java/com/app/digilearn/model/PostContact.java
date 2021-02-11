package com.app.digilearn.model;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("com.robohorse.robopojogenerator")
public class PostContact{

	@SerializedName("Email")
	private String email;

	@SerializedName("Phone")
	private String phone;

	@SerializedName("Name")
	private String name;

	public void setEmail(String email){
		this.email = email;
	}

	public String getEmail(){
		return email;
	}

	public void setPhone(String phone){
		this.phone = phone;
	}

	public String getPhone(){
		return phone;
	}

	public void setName(String name){
		this.name = name;
	}

	public String getName(){
		return name;
	}

	@Override
 	public String toString(){
		return 
			"PostContact{" + 
			"email = '" + email + '\'' + 
			",phone = '" + phone + '\'' + 
			",name = '" + name + '\'' + 
			"}";
		}
}