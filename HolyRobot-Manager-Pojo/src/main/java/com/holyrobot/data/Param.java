package com.holyrobot.data;

public enum Param {
	/**
	 * 驴妈妈景点获取种子url的首链接
	 */
	LVMAMA_STROKE_FIRST,
	/**
	 * 驴妈妈景点获取种子url的分页链接
	 */
	LVMAMA_STROKE_PAGE,
	/**
	 * 驴妈妈景点详情种子url
	 */
	LVMAMA_STROKE_DETAIL,
	/**
	 * 驴妈妈景点评论url枚举类型
	 */
	LVMAMA_STROKE_COMMENT,
	/**
	 * 驴妈妈景点评论枚举类型
	 */
	LVMAMA_STROKE_COMMENTPAGE,
	/**
	 * 驴妈妈想去人数和去过人数枚举类
	 */
	LVMAMA_STROKE_WANTANDBEEN,
	/**
	 * 驴妈妈酒店首页url
	 */
	LVMAMA_HOTEL_FIRST,
	/**
	 * 驴妈妈酒店获取种子的分页url
	 */
	LVMAMA_HOTEL_PAGE,
	/**
	 * 驴妈妈酒店详情
	 */
	LVMAMA_HOTEL_DETAIL,
	/**
	 * 驴妈妈酒店房型价格枚举类
	 */
	LVMAMA_HOTEL_ROOM,
	/**
	 * 驴妈妈酒店评论枚举类
	 */
	LVMAMA_HOTEL_COMMENT,
	/**
	 * 驴妈妈门票首页链接
	 */
	LVMAMA_SCENIC_FIRST,
	/**
	 * 驴妈妈门票获取种子的分页链接
	 */
	LVMAMA_SCENIC_PAGE,
	/**
	 * 驴妈妈门票详情枚举类
	 */
	LVMAMA_SCENIC_DETAIL,
	/**
	 * 驴妈妈门票优惠政策
	 */
	LVMAMA_SCENIC_FOFAVOUREDPOLICY,
	/**
	 * 驴妈妈门票评论枚举类
	 */
	LVMAMA_SCENIC_COMMENT,
	/**
	 * 驴妈妈门票评论分页类型
	 */
	LVMAMA_SCENIC_COMMENTPAGE,
	/**
	 * 驴妈妈行程首页链接类型
	 */
	LVMAMA_ROUTE_FIRST,
	/**
	 * 驴妈妈行程分页链接类型
	 */
	LVMAMA_ROUTE_PAGE,
	/**
	 * 驴妈妈行程详细种子类型
	 */
	LVMAMA_ROUTE_DETAIL,
	/**
	 * 驴妈妈行程价格枚举类
	 */
	LVMAMA_ROUTE_PRICE,
	/**
	 * 驴妈妈行程评论分页枚举类
	 */
	LVMAMA_ROUTE_COMMENTPAGE,
	/**
	 * 驴妈妈行程评论枚举类
	 */
	LVMAMA_ROUTE_COMMENT,
	
	/**
	 * 携程获取景点种子的首页url类型
	 */
	CTRIP_STROKE_FIRST,
	/**
	 * 携程获取景点的分页url类型
	 */
	CTRIP_STROKE_PAGE,
	/**
	 * 携程获取景点的种子详情
	 */
	CTRIP_STROKE_DETAIL,
	/**
	 * 携程想去人数和去过人数枚举类
	 */
	CTRIP_STROKE_WANTANDBEEN,
	/**
	 * 携程景点评论枚举类
	 */
	CTRIP_STROKE_COMMENT,
	/**
	 * 携程门票首页链接
	 */
	CTRIP_SCENIC_FIRST,
	/**
	 * 携程门票分页链接
	 */
	CTRIP_SCENIC_PAGE,
	/**
	 * 携程景点详情信息
	 */
	CTRIP_SCENIC_DETAIL,
	/**
	 * 携程门票详细信息链接
	 */
	CTRIP_SCENIC_TICKETSTATUTE,
	/**
	 * 携程门票评论枚举类
	 */
	CTRIP_SCENIC_COMMENT,
	/**
	 * 携程酒店首页链接url
	 */
	CTRIP_HOTEL_FIRST,
	/**
	 * 携程酒店分页url
	 */
	CTRIP_HOTEL_PAGE,
	/**
	 * 携程酒店详情信息
	 */
	CTRIP_HOTEL_DETAIL,
	/**
	 * 携程酒店房型枚举类
	 */
	CTRIP_HOTEL_ROOM,
	/**
	 * 携程门票图片枚举类
	 */
	CTRIP_SCENIC_PICTURE,
	/**
	 * 携程行程首页链接
	 */
	CTRIP_ROUTE_FIRST,
	/**
	 * 携程行程的分页链接
	 */
	CTRIP_ROUTE_PAGE,
	/**
	 * 携程行程的详情信息
	 */
	CTRIP_ROUTE_DETAIL,
	/**
	 * 携程行程产品概要（自由行）
	 */
	CTRIP_ROUTE_PRODUCTFEATURE,
	/**
	 * 行程产品概要（自由行）
	 */
	CTRIP_ROUTE_PRODUCTOUTLINE,
	/**
	 * 携程行程评论第一页url
	 */
	CTRIP_ROUTE_COMMENT_FIRST,
	/**
	 * 携程行程价格枚举类
	 */
	CTRIP_ROUTE_PRICE,
	/**
	 * 携程行程出发城市枚举类
	 */
	CTRIP_ROUTE_DEPARTURE,
	/**
	 * 携程行程费用和预定须知枚举类
	 */
	CTRIP_ROUTE_EXPENSEANDRESERVEINFO,
	/**
	 * 携程行程评论枚举类
	 */
	CTRIP_ROUTE_COMMENT,
	/**
	 * 途牛景点首页链接
	 */
	TUNIU_STROKE_FIRST,
	/**
	 * 途牛景点分页链接
	 */
	TUNIU_STROKE_PAGE,
	/**
	 * 途牛景点详情信息
	 */
	TUNIU_STROKE_DETAIL,
	/**
	 * 途牛景点评论枚举类
	 */
	TUNIU_STROKE_COMMENT,
	/**
	 * 途牛酒店首页链接
	 */
	TUNIU_HOTEL_FIRST,
	/**
	 * 途牛酒店分页链接
	 */
	TUNIU_HOTEL_PAGE,
	/**
	 * 途牛酒店详情
	 */
	TUNIU_HOTEL_DETAIL,
	/**
	 * 途牛酒店房型床型枚举类
	 */
	TUNIU_HOTEL_ROOM,
	/**
	 * 途牛酒店评论枚举类
	 */
	TUNIU_HOTEL_COMMENT,
	/**
	 * 途牛酒店的介绍
	 */
	TUNIU_HOTEL_INTRODUCTION,
	/**
	 * 途牛酒店的价格
	 */
	TUNIU_HOTEL_PICTURE,
	/**
	 * 途牛景点首页链接
	 */
	TUNIU_SCENIC_FIRST,
	/**
	 * 途牛景点分页链接
	 */
	TUNIU_SCENIC_PAGE,
	/**
	 * 途牛景点详情
	 */
	TUNIU_SCENIC_DETAIL, 
	/**
	 * 途牛门票评论枚举类
	 */
	TUNIU_SCENIC_COMMENT, 
	/**
	 * 途牛行程首页链接
	 */
	TUNIU_ROUTE_FIRST,
	/**
	 * 途牛行程分页链接
	 */
	TUNIU_ROUTE_PAGE,
	/**
	 * 途牛行程详情
	 */
	TUNIU_ROUTE_DETAIL,
	/**
	 * 途牛行程价格枚举类
	 */
	TUNIU_ROUTE_PRICE, 
	/**
	 * 途牛行程评论枚举类
	 */
	TUNIU_ROUTE_COMMENT,
	/**
	 * 同程酒店房型床型枚举类
	 */
	TONGCHENG_HOTEL_ROOM, 
	/**
	 * 同程酒店评论枚举类
	 */
	TONGCHENG_HOTEL_COMMENT, 
	/**
	 * 同程景点首页
	 */
	TONGCHENG_STROKE_FIRST,
	/**
	 * 同程景点分页
	 */
	TONGCHENG_STROKE_PAGE, 
	/**
	 * 同程景点详情
	 */
	TONGCHENG_STROKE_DETAIL, 
	/**
	 * 同城景点评论首页链接枚举类
	 */
	TONGCHENG_STROKE_COMMENT_FIRST,
	/**
	 * 同城景点评论链接枚举类
	 */
	TONGCHENG_STROKE_COMMENT,
	/**
	 * 同程门票首页
	 */
	TONGCHENG_SCENIC_FIRST, 
	/**
	 * 同程门票分页
	 */
	TONGCHENG_SCENIC_PAGE, 
	/**
	 * 同程门票详情
	 */
	TONGCHENG_SCENIC_DETAIL, 
	/**
	 * 同程门票已去和想去人数
	 */
	TONGCHENG_SCENIC_BOOKNOWNEW, 
	/**
	 * 同程门票价格链接枚举类
	 */
	TONGCHENG_SCENIC_PRICE, 
	/**
	 * 同程门票评论第一页
	 */
	TONGCHENG_SCENIC_COMMENT_FIRST,
	/**
	 * 同程门票评论url枚举类
	 */
	TONGCHENG_SCENIC_COMMENT, 
	/**
	 * 同城行程价格链接枚举类
	 */
	TONGCHENG_ROUTE_PRICE,
	/**
	 * 同城行程第一页评论链接枚举类
	 */
	TONGCHENG_ROUTE_COMMENT_FIRST,
	/**
	 * 同城行程评论链接枚举类
	 */
	TONGCHENG_ROUTE_COMMENT,
	/**
	 * 去哪儿景点首页链接
	 */
	QUNAER_STROKE_FIRST,
	/**
	 * 去哪儿景点分页链接
	 */
	QUNAER_STROKE_PAGE,
	/**
	 * 去哪儿景点详情信息
	 */
	QUNAER_STROKE_DETAIL,
	/**
	 * 去哪儿评论链接枚举类
	 */
	QUNAER_STROKE_COMMENT,
	/**
	 * 去哪儿门票首页链接
	 */
	QUNAER_SECNIC_FIRST,
	/**
	 * 去哪儿门票分页信息
	 */
	QUNAER_SECNIC_PAGE,
	/**
	 * 去哪儿门票详情
	 */
	QUNAER_SECNIC_DETAIL,
	
	/**
	 * 去哪儿门票评论链接枚举类
	 */
	QUNAER_SCENIC_COMMENT,
	/**
	 * 去哪儿行程首页链接
	 */
	QUNAER_ROUTE_FIRST,
	/**
	 * 去哪儿行程分页链接
	 */
	QUNAER_ROUTE_PAGE,
	/**
	 * 去哪儿行程详情
	 */
	QUNAER_ROUTE_DETAIL,
	
	/**
	 * 去哪儿行程价格链接枚举类
	 */
	QUNAER_ROUTE_PRICE, 
	/**
	 * 去哪儿行程评论第一页链接枚举类
	 */
	QUNAER_ROUTE_COMMENT_FIRST, 
	/**
	 * 去哪儿行程评论链接枚举类
	 */
	QUNAER_ROUTE_COMMENT,
	/**
	 * 去哪儿酒店首页链接
	 */
	QUNAER_HOTEL_FIRST,
	/**
	 * 去哪儿酒店分页链接
	 */
	QUNAER_HOTEL_PAGE,
	/**
	 * 去哪儿酒店详情
	 */
	QUNAER_HOTEL_DETAIL,
	
	/**
	 * 去哪儿酒店房型链接枚举类
	 */
	QUNAER_HOTEL_ROOM, 
	/**
	 * 去哪儿酒店详情介绍链接枚举类
	 */
	QUNAER_HOTEL_INTRODUCTION,
	/**
	 * 去哪儿酒店图片链接枚举类
	 */
	QUNAER_HOTEL_PICTURE, 
	/**
	 * 去哪儿酒店评论链接枚举类
	 */
	QUNAER_HOTEL_COMMENT, 
	/**
	 * 去哪儿酒店房间价格链接枚举类
	 */
	QUNAER_HOTEL_ROOM_PRICE,
	/**
	 * get请求类型
	 */
	GET,
	/**
	 * post请求类型
	 */
	POST,
	/**
	 * 携程
	 */
	CTRIP,
	/**
	 * 驴妈妈
	 */
	LVMAMA,
	/**
	 * 途牛
	 */
	TUNIU,
	/**
	 * 同程
	 */
	TONGCHENG,
	/**
	 * 去哪儿
	 */
	QUNAER, 

}
