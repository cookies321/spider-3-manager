package com.holyrobot.data;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import com.holyrobot.common.Routeinfo;


/**
 * @Description: 行程
 * @author 徐仁杰
 * @date 2017年11月30日 上午10:03:01
 */
public class Route implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 测试用
	 */
	private String name;

	/**
	 * 页面内容
	 */
	private String content;

	/**
	 * 当前正在请求的url链接
	 */
	private String url;

	/**
	 * 收集到的链接
	 */
	private List<String> urlList = new LinkedList<>();
	
	
	//城市名称
	private String cityName;
	
	//一级主页url
	private String parentUrl;
	//二级分页url
	private String pageUrl;

	public Route() {
		super();
	}

	/**
	 * 存放行程价格的url链接
	 */
	private String routepriceURL;
	
	/**
	 * 存放行程信息
	 */
	private Routeinfo routeInfo;


	public String getRoutepriceURL() {
		return routepriceURL;
	}

	public void setRoutepriceURL(String routepriceURL) {
		this.routepriceURL = routepriceURL;
	}

	public Routeinfo getRouteInfo() {
		return routeInfo;
	}

	public void setRouteInfo(Routeinfo routeInfo) {
		this.routeInfo = routeInfo;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
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

	public void setUrlList(String url) {
		this.urlList.add(url);
	}

	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
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

	@Override
	public String toString() {
		return "Route [name=" + name + ", content=" + content + ", url=" + url + ", urlList=" + urlList + ", cityName="
				+ cityName + ", parentUrl=" + parentUrl + ", pageUrl=" + pageUrl + "]";
	}


}
