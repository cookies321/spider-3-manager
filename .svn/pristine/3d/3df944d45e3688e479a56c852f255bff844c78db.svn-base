package com.holyrobot.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.holyrobot.common.Addressinfo;
import com.holyrobot.common.Hotelinfo;
import com.holyrobot.common.Roombasicinfo;
import com.holyrobot.common.Routeinfo;
import com.holyrobot.common.Sceinfo;
import com.holyrobot.common.Scepriceinfo;

/**
 * @Description: 房型，票价，好评率，评论等url任务公共类
 * @author 徐仁杰
 * @date 2017年12月7日 下午2:23:54
 */
public class Params implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 类型枚举类
	 */
	private Param type;

	/**
	 * 数据来源网站
	 */
	private Param dataSource;

	/**
	 * 请求链接
	 */
	private String url;
	
	/**
	 * 上一级的url
	 */
	private String parentUrl;
	
	/**
	 * 城市名称
	 */
	private String cityName;
	/**
	 * 基础信息关联uuid
	 */
	private String uuid;

	/**
	 * 请求类型
	 */
	private Param httpType;

	/**
	 * post请求参数
	 */
	private String postParams;

	/**
	 * 评论日期
	 */
	private String commentDate;

	/**
	 * 页面、json内容
	 */
	private String content;
	
	/**
	 * 请求中需要带的头部信息
	 */
	private Map<String,String> header;
	
	/**
	 * 放入队列的名称
	 */
	private String destinationName;
	
	public String getDestinationName() {
		return destinationName;
	}

	public void setDestinationName(String destinationName) {
		this.destinationName = destinationName;
	}

	/**
	 * 四级地址表
	 */
	private Addressinfo addressinfo;

	/**
	 * 门票详情
	 */
	private Sceinfo sceinfo;
	
	private Scepriceinfo scepriceinfo;
	
	public Scepriceinfo getScepriceinfo() {
		return scepriceinfo;
	}

	public void setScepriceinfo(Scepriceinfo scepriceinfo) {
		this.scepriceinfo = scepriceinfo;
	}

	/**
	 * 行程详情
	 */
	private Routeinfo routeInfo;
	
	/**
	 * 酒店详情
	 */
	private Hotelinfo hotelInfo;
	/**
	 * 酒店房型基本信息
	 */
	private Roombasicinfo roomInfo;

	/**
	 * 存放备用信息的other
	 */
	
	private String otherInformation;
	
	public Params() {
		header = new HashMap<String, String>();
	}
	
	public Hotelinfo getHotelInfo() {
		return hotelInfo;
	}

	public void setHotelInfo(Hotelinfo hotelInfo) {
		this.hotelInfo = hotelInfo;
	}
	
	public Addressinfo getAddressinfo() {
		return addressinfo;
	}

	public void setAddressinfo(Addressinfo addressinfo) {
		this.addressinfo = addressinfo;
	}


	public Routeinfo getRouteInfo() {
		return routeInfo;
	}


	public void setRouteInfo(Routeinfo routeInfo) {
		this.routeInfo = routeInfo;
	}


	public String getOtherInformation() {
		return otherInformation;
	}




	public void setOtherInformation(String otherInformation) {
		this.otherInformation = otherInformation;
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


	public Roombasicinfo getRoomInfo() {
		return roomInfo;
	}

	public void setRoomInfo(Roombasicinfo roomInfo) {
		this.roomInfo = roomInfo;
	}
	public Sceinfo getSceinfo() {
		return sceinfo;
	}

	public void setSceinfo(Sceinfo sceinfo) {
		this.sceinfo = sceinfo;
	}

	public Map<String, String> getHeader() {
		return header;
	}

	public void setHeader(String key,String value) {
		this.header.put(key, value);
	}

	public Param getDataSource() {
		return dataSource;
	}

	public void setDataSource(Param dataSource) {
		this.dataSource = dataSource;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getPostParams() {
		return postParams;
	}

	public void setPostParams(String postParams) {
		this.postParams = postParams;
	}
	

	public Param getHttpType() {
		return httpType;
	}

	public void setHttpType(Param httpType) {
		this.httpType = httpType;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getCommentDate() {
		return commentDate;
	}

	public void setCommentDate(String commentDate) {
		this.commentDate = commentDate;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Param getType() {
		return type;
	}

	public void setType(Param type) {
		this.type = type;
	}

}
