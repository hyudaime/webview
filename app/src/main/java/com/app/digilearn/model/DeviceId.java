package com.app.digilearn.model;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("com.robohorse.robopojogenerator")
public class DeviceId{

	@SerializedName("DeviceID")
	private String deviceID;

	public void setDeviceID(String deviceID){
		this.deviceID = deviceID;
	}

	public String getDeviceID(){
		return deviceID;
	}

	@Override
 	public String toString(){
		return 
			"DeviceId{" + 
			"deviceID = '" + deviceID + '\'' + 
			"}";
		}
}