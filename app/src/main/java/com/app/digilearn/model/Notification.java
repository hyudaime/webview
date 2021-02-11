package com.app.digilearn.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class Notification implements Parcelable {

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Notification(String id, String title, String message) {
		this.id = id;
		this.title = title;
		this.message = message;
	}

	@SerializedName("id")
	@Expose
	private String id;

	@SerializedName("title")
	@Expose
	private String title;

	@SerializedName("message")
	@Expose
	private String message;


	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.id);
		dest.writeString(this.title);
		dest.writeString(this.message);
	}

	protected Notification(Parcel in) {
		this.id = in.readString();
		this.title = in.readString();
		this.message = in.readString();
	}

	public static final Parcelable.Creator<Notification> CREATOR = new Parcelable.Creator<Notification>() {
		@Override
		public Notification createFromParcel(Parcel source) {
			return new Notification(source);
		}

		@Override
		public Notification[] newArray(int size) {
			return new Notification[size];
		}
	};
}