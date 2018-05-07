package com.holyrobot.data;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import com.holyrobot.common.Addressinfo;
import com.holyrobot.common.Hotelinfo;
import com.holyrobot.common.Pictureinfo;


public class Hotel implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;

	private String url;

	private List<String> urlList = new LinkedList<>();

	private String content;
	
	//一级主页url
	private String parentUrl;
	
	//二级分页url
	private String pageUrl;
	
	//酒店详情实体类
	private Hotelinfo hotelbasicInfo;
	
	//地址详情
	private Addressinfo addressInfo;
	
	//图片
	private Pictureinfo pictureInfo;
	
	/**
	 * 城市名称
	 */
	private String cityName;
	
	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	public Hotel() {
		super();
	}


	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public void setUrlList(List<String> urlList) {
		this.urlList = urlList;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	

	public String getPageUrl() {
		return pageUrl;
	}

	public void setPageUrl(String pageUrl) {
		this.pageUrl = pageUrl;
	}

	public List<String> getUrlList() {
		return urlList;
	}

	public void setUrlList(String url) {
		this.urlList.add(url);
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getParentUrl() {
		return parentUrl;
	}

	public void setParentUrl(String parentUrl) {
		this.parentUrl = parentUrl;
	}

	public Hotelinfo getHotelbasicInfo() {
		return hotelbasicInfo;
	}

	public void setHotelbasicInfo(Hotelinfo hotelbasicInfo) {
		this.hotelbasicInfo = hotelbasicInfo;
	}

	public Addressinfo getAddressInfo() {
		return addressInfo;
	}

	public void setAddressInfo(Addressinfo addressInfo) {
		this.addressInfo = addressInfo;
	}

	public Pictureinfo getPictureInfo() {
		return pictureInfo;
	}

	public void setPictureInfo(Pictureinfo pictureInfo) {
		this.pictureInfo = pictureInfo;
	}

	@Override
	public String toString() {
		return "Hotel [name=" + name + ", url=" + url + ", urlList=" + urlList
				+ ", content=" + content + ", parentUrl=" + parentUrl
				+ ", pageUrl=" + pageUrl + ", hotelbasicInfo=" + hotelbasicInfo
				+ ", addressInfo=" + addressInfo + ", pictureInfo="
				+ pictureInfo + ", cityName=" + cityName + "]";
	}
	

}
