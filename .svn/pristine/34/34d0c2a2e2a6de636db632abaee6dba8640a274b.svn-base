package com.holyrobot.data;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import com.holyrobot.common.Sceinfo;


public class Scenic implements Serializable {

	private static final long serialVersionUID = 1L;

	private String url;

	private List<String> urlList = new LinkedList<>();

	private String content;
	
	//一级父类url
	private String parentUrl;
	
	//二级分页url
	private String pageUrl;
	
	/**
	 * 门票表对象
	 */
	private Sceinfo sceinfo;

	/**
	 * 图片url
	 */
	private String pictureURL;
	
	
	private String cityName;
	
	

	public String getPictureURL() {
		return pictureURL;
	}

	public void setPictureURL(String pictureURL) {
		this.pictureURL = pictureURL;
	}

	public Sceinfo getSceinfo() {
		return sceinfo;
	}

	public void setSceinfo(Sceinfo sceinfo) {
		this.sceinfo = sceinfo;
	}

	public Scenic() {
		super();
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public List<String> getUrlList() {
		return urlList;
	}
	//修改为add
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

	public String getPageUrl() {
		return pageUrl;
	}

	public void setPageUrl(String pageUrl) {
		this.pageUrl = pageUrl;
	}
	

	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	@Override
	public String toString() {
		return "Scenic [url=" + url + ", urlList=" + urlList + ", content=" + content + ", parentUrl=" + parentUrl
				+ ", pageUrl=" + pageUrl + "]";
	}
	
	

	

}
