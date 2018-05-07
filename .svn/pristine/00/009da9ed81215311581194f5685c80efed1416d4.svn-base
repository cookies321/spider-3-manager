package com.holyrobot.data;

import java.io.Serializable;

/**
 * @Author: 周陈
 * @Description: 爬虫入口API传输对象
 */
public class ReceiverData implements Serializable {
	private static final long serialVersionUID = 771208872458900148L;
	/**
	 * data对象类型
	 * 1.酒店详情；2.酒店价格；3.酒店房型；
	 * 4.景点价格；5.景点详情；
	 * 6.行程详情；7.行程价格；
	 * 8.机票详情；9.机票价格;
	 * 10.图片;11.评论；12.地址。
	 */
	private Integer type;

	/**
	 * 1、完整数据；0、不完整数据
	 */
	private Integer flag;
	/**
	 * 对应酒店、景点、行程、机票数据
	 */
	private Object data;

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	private String version;
	
	
	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Integer getFlag() {
		return flag;
	}

	public void setFlag(Integer flag) {
		this.flag = flag;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}
}
