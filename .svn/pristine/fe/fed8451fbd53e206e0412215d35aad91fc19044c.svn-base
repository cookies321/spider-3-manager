package com.holyrobot.data;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import com.holyrobot.common.Addressinfo;
import com.holyrobot.common.Sceinfo;

/**
 * @Description: 门票
 * @author 徐仁杰
 * @date 2017年11月30日 上午10:03:12 
 */
public class Stroke implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;

	private String url;

	private List<String> urlList = new LinkedList<>();

	private String content;
	
	//一级主页url
	private String parentUrl;
	
	//二级分页url
	private String pageUrl;
	
	//输入目的地城市名称
	private String cityName;
	
	/**
	 * 景点对象
	 */
	private Sceinfo sceinfo;
	
	/**
	 * 四级地址集合
	 */
	private Addressinfo addressinfo;
	
	//其他有用信息
	private String otherinformation;
	
	
	public Sceinfo getSceinfo() {
		return sceinfo;
	}

	public void setSceinfo(Sceinfo sceinfo) {
		this.sceinfo = sceinfo;
	}

	public String getOtherinformation() {
		return otherinformation;
	}

	public void setOtherinformation(String otherinformation) {
		this.otherinformation = otherinformation;
	}
	
	public Addressinfo getAddressinfo() {
		return addressinfo;
	}

	public void setAddressinfo(Addressinfo addressinfo) {
		this.addressinfo = addressinfo;
	}

	public Stroke() {
		super();
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
		return "Stroke [name=" + name + ", url=" + url + ", urlList=" + urlList + ", content=" + content
				+ ", parentUrl=" + parentUrl + ", pageUrl=" + pageUrl + ", cityName=" + cityName + "]";
	}

	
	

}
