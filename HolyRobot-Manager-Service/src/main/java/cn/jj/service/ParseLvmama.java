package cn.jj.service;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.holyrobot.common.Addressinfo;
import com.holyrobot.common.Commentinfo;
import com.holyrobot.common.Hotelinfo;
import com.holyrobot.common.Pictureinfo;
import com.holyrobot.common.Roombasicinfo;
import com.holyrobot.common.Roomprice;
import com.holyrobot.common.Routeinfo;
import com.holyrobot.common.Routepriceinfo;
import com.holyrobot.common.Sceinfo;
import com.holyrobot.common.Scepriceinfo;
import com.holyrobot.data.Param;
import com.holyrobot.data.Params;

import cn.jj.utils.DateUtil;
import cn.jj.utils.KafkaUtils;
import cn.jj.utils.NumUtils;

@Repository
public class ParseLvmama {
	@Autowired
	private ProducerService produceService;

	@Autowired
	private JedisClient jedis;

	@Autowired
	private IDownLoadService downLoadService;

	static String creatorID="tyl13564205515";
	static SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd");
	static String startDate="";
	static String endDate="";
	static List<String> dateList=null;

	/**
	 * 顶级队列，用于存储种子链接和分页链接的数据
	 */
	private static final String topQueue = "topQueue";
	/**
	 * 高级队列，用于存储每种类型的详情数据
	 */
	private static final String highQueue="highQueue";
	/**
	 * 低级队列，用于存储详情页面下的链接,也即是默认的队列
	 */
	private static final String  lowQueue="lowQueue";

	static{
		//获取房型的开始日期
		startDate = df.format(new Date());
		//获取房型的结束日期（开始日期的后60天）
		endDate = DateUtil.getEndDate(startDate, 31);
		//获取从现在开始之后60天的每一天日期（格式为yyyy-MM-dd形式）
		dateList = DateUtil.findDates(startDate, endDate);
	}

	/**
	 * 
	 * @Description 解析驴妈妈酒店评论信息
	 * @author 汤玉林
	 * @date 2018年1月22日 上午9:08:06
	 * @action parseLvmamaHotelComment
	 * @param params
	 */
	public void parseLvmamaHotelComment(Params params) {
		String uuId=params.getUuid();
		String cityName = params.getCityName();
		try{
			Document doc2=Jsoup.parse(params.getContent());
			Elements commentlist=doc2.select("div.comment-li");
			if(commentlist.size()>0){
				for(Element comment:commentlist){
					Commentinfo commentInfo=new Commentinfo();
					String str = comment.toString();
					System.out.println(str);
					commentInfo.setId(UUID.randomUUID().toString());
					commentInfo.setInfoid(uuId);
					commentInfo.setType(2);
					commentInfo.setDatasource("Lvmama");
					commentInfo.setAdminarea("中国,"+cityName);
					commentInfo.setContent(comment.toString());
					commentInfo.setCreator("汤玉林");
					commentInfo.setCreatedate(new Date());
					commentInfo.setCreatorid("tyl13564205515");

					String commentInfoObject = KafkaUtils.parseJsonObject(commentInfo, 11,1);
					String sendPost4 = downLoadService.sendPost(KafkaUtils.KAFKAURL, commentInfoObject);					
					jedis.insertAndGetId(commentInfo);
				}
			}else {
				System.out.println("未获取到评论，重新放入队列");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @Description 获得驴妈妈酒店评论url
	 * @author 汤玉林
	 * @date 2018年1月22日 上午9:08:29
	 * @action parseLvmamaHotelCommentURL
	 * @param params
	 */
	public void parseLvmamaHotelCommentURL(Params params) {
		String uuid = params.getUuid();
		String url=params.getUrl();
		String cityName = params.getCityName();
		String content = params.getContent();
		Document doc=Jsoup.parse(content);
		//commentURL
		try {
			Elements elements=doc.select("div.new-cominfo>div.comstati.clearfix>div.com-btns>a.JS-comwrite.comwrite");
			String commenthref=elements.attr("href");
			//获得评论的placeID
			//获得评论的placeID
			String placeid=commenthref.substring(commenthref.lastIndexOf("=")+1,commenthref.length());
			//String commentCount=hotel.getHotelbasicInfo().getGradenum();

			//获取评论总数(从网页内部获取总数)
			String commentCount = doc.select("ul.ul-hor>li[date_id=comments]>b").text();
			int totalNum=0;
			if(StringUtils.isNotBlank(commentCount)){
				totalNum=NumUtils.getInteger(commentCount);
			}
			int pageTotal=totalNum%10==0?totalNum/10:(totalNum/10+1);


			for(int i=1;i<=pageTotal;i++){
				String commentURL="http://hotels.lvmama.com/lvmm_dest_front/comment/newPaginationOfComments";
				String param="type=all&currentPage="+i+"&totalCount="+totalNum+"&placeId="+placeid+"&productId=&placeIdType=PLACE&isPicture=&isBest=&isPOI=Y&isELong=Y";

				/**
				 * post修改为get
				 */
				String commentUrl=commentURL+"?"+param;
				Params para=new Params();
				para.setUuid(uuid);
				para.setUrl(commentUrl);
				para.setCityName(cityName);
				para.setHttpType(Param.GET);
				para.setType(Param.LVMAMA_HOTEL_COMMENT);
				para.setDataSource(Param.LVMAMA);
				para.setDestinationName("lowQueue");
				produceService.sendMessage(para);
			}
		} catch (Exception e) {
			produceService.sendMessage(params.getDestinationName(), params);
		}

	}

	/**
	 * 
	 * @Description 获得驴妈妈酒店房型url
	 * @author 汤玉林
	 * @date 2018年1月22日 上午9:08:51
	 * @action parseLvmamaHotelRoomURL
	 * @param params
	 */
	public void parseLvmamaHotelRoomURL(Params params) {
		String uuid = params.getUuid();
		String url=params.getUrl();
		String content = params.getContent();
		String cityName = params.getCityName();
		Document doc=Jsoup.parse(content);
		String hotelCode=url.substring(url.lastIndexOf("/")+1, url.lastIndexOf("."));
		//roomURL
		for(int i=0;i<dateList.size()-1;i++){
			String roomURL="http://hotels.lvmama.com/prod/hotel/showNewHotelGoods.do";
			String urlParam="startDateStr="+dateList.get(i)+"&endDateStr="+dateList.get(i+1)+""
					+ "&productId="+hotelCode+"&getTopTwo=false&getPropInfo=false&notSell=N"
					+ "&removeTimePriceTabel=removeTimePriceTabel";
			//url传入Params
			/**
			 * post修改为get类型
			 */
			String roomUrl=roomURL+"?"+urlParam;
			Params param=new Params();
			param.setUuid(uuid);
			param.setUrl(roomUrl);
			param.setCityName(cityName);
			param.setType(Param.LVMAMA_HOTEL_ROOM);
			param.setDataSource(Param.LVMAMA);
			param.setHttpType(Param.GET);
			param.setDestinationName("lowQueue");

			produceService.sendMessage(param);
		}

	}

	/**
	 * 
	 * @Description 解析驴妈妈酒店房型信息
	 * @author 汤玉林
	 * @date 2018年1月22日 上午9:09:16
	 * @action parseLvmamaHotelRoom
	 * @param params
	 */
	public void parseLvmamaHotelRoom(Params params) {
		String uuId = params.getUuid();
		String url = params.getUrl();
		String cityName = params.getCityName();
		String priceDate = url.substring(url.indexOf("startDateStr")+13, url.indexOf("endDateStr")-1);
		Document doc = Jsoup.parse(params.getContent());
		try {
			//酒店房间列表
			Elements hotelRoom=doc.select("div.room_list");
			int count=0;
			if(hotelRoom.size()>0){
				for(Element element:hotelRoom){
					//房间图片
					Elements imgElement=element.select("div.room_list_l>img");
					//房间名称
					if(imgElement.isEmpty()){
						imgElement=element.select("img.roomList-img");
					}
					String picturesrc=imgElement.attr("src");
					//房间名称
					Elements room_list_r=element.select("div.room_list_r");
					if(room_list_r.isEmpty()){
						room_list_r=element.select("dl>dt");
					}
					String roomName=room_list_r.select("h4").text();

					String bedtype="";
					String floor="";
					String isaddBed="";
					int peoplecount=0;
					String iswifi="";

					Elements room_info=room_list_r.select("p.room_info");

					String[] room_infos;
					if(!room_info.isEmpty()){
						room_infos=room_info.text().split(" ");
						for(int s=0;s<room_infos.length;s++){
							if(room_infos[s].contains("楼层")){
								floor=room_infos[s];
							}
							if(room_infos[s].contains("床型")){
								bedtype=room_infos[s];
							}
							if(room_infos[s].contains("可以加床 ")){
								isaddBed=room_infos[s];
							}
							if(room_infos[s].contains("最大入住人数")){
								peoplecount=NumUtils.getInteger(room_infos[s]);
							}
							if(room_infos[s].contains("宽带：免费")){
								iswifi=room_infos[s];
							}
						}
					}else{ 
						if(room_info.isEmpty()){
							room_info=room_list_r.select("p.room-info");
						}
						room_infos = room_info.text().replace("|", "").split(" ");
						for(int s=0;s<room_infos.length;s++){
							if(room_infos[s].contains("位于")){						
								floor=room_infos[s];			//楼层
							}
							if((room_infos[s].contains("大")||room_infos[s].contains("双")) && room_infos[s].contains("床")){						
								bedtype=room_infos[s];			//床型
							}
							if(room_infos[s].contains("床") && room_infos[s].contains("加床")){
								isaddBed=room_infos[s];			//是否可以加床
							}
							if(room_infos[s].contains("最大入住")){
								peoplecount=NumUtils.getInteger(room_infos[s]);
							}
							if(room_infos[s].contains("宽带")){
								iswifi=room_infos[s];
							}
						}
					}
					Roombasicinfo roomInfo=new Roombasicinfo();
					String roomInfoId=UUID.randomUUID().toString();
					roomInfo.setId(roomInfoId);
					roomInfo.setHotelid(uuId);
					roomInfo.setBedtype(bedtype);
					roomInfo.setFloor(floor);
					roomInfo.setIsaddbed(isaddBed);
					roomInfo.setIswifi(iswifi);
					roomInfo.setPeoplecount(Integer.valueOf(peoplecount).toString());
					roomInfo.setRoomtype(roomName);
					roomInfo.setAdminarea("中国,"+cityName);
					roomInfo.setCreatorid("tyl13564205515");
					roomInfo.setCreator("汤玉林");
					roomInfo.setDatasource("Lvmama");
					roomInfo.setCreatedate(new Date());
					//存放房型信息
					System.out.println("插入酒店房型"+bedtype);

					String parseJsonObject3 = KafkaUtils.parseJsonObject(roomInfo, 3,1);
					String sendPost3 = downLoadService.sendPost(KafkaUtils.KAFKAURL, parseJsonObject3);
					jedis.insertAndGetId(roomInfo);

					Pictureinfo pictureInfo=new Pictureinfo();
					String pictureId=UUID.randomUUID().toString();
					pictureInfo.setImgurl(picturesrc);
					pictureInfo.setSort(count++);
					pictureInfo.setType(4);
					pictureInfo.setDownload(0);
					pictureInfo.setId(pictureId);
					pictureInfo.setInfoid(roomInfoId);
					//存入图片
					pictureInfo.setCreatedate(new Date());
					pictureInfo.setAdminarea("中国,"+cityName);
					pictureInfo.setCreator("汤玉林");
					pictureInfo.setDatasource("Lvmama");
					pictureInfo.setCreatorid("tyl13564205515");

					String pictureInfoObject = KafkaUtils.parseJsonObject(pictureInfo, 10,1);
					String sendPost4 = downLoadService.sendPost(KafkaUtils.KAFKAURL, pictureInfoObject);
					jedis.insertAndGetId(pictureInfo);
					//System.out.println("房型："+roomName+";楼层："+floor+";床型："+bedtype+";加床与否："+isaddBed);

					//不同房型产品列表
					Elements room_tables=element.select("div.room_box>table>tbody>tr.room_table");
					if(room_tables.isEmpty()){
						room_tables=element.select("dl>dd>table>tbody>tr");
					}
					for(int l=0;l<room_tables.size();l++){
						String isbooking="";
						String available="无";
						//下面是每一个tr里面的数据
						Elements tds=room_tables.get(l).select("td");
						String productName=tds.get(0).text();
						String hasBreakfast=tds.get(2).text();
						String productWindow=tds.get(3).text();
						String iscancled=tds.get(4).text();
						String price=tds.get(5).select("span.room_price>big.J_room_rate").text();
						if(StringUtils.isBlank(price)){
							price=tds.get(5).select("dfn>big.J_room_rate").text();
						}
						//预定
						Elements booking=tds.get(6).select("span.J_yuding.btn.btn-orange.btn-sm");
						if(booking.isEmpty()){
							booking=tds.get(6).select("a.btn.btn-orange.btn-sm.btn-disabled");
						}
						if(!booking.isEmpty()){
							isbooking = booking.text();
							if("预订".equals(isbooking)){
								available = "有";
							}
						}
						Roomprice roomPrice=new Roomprice();
						//存放房间价格信息
						String roompriceId=UUID.randomUUID().toString();
						roomPrice.setId(roompriceId);
						roomPrice.setHotelid(uuId);
						roomPrice.setRoomid(roomInfoId);
						roomPrice.setProductname(productName);
						roomPrice.setIsbooking(isbooking);
						roomPrice.setIscancled(iscancled);
						roomPrice.setIshasbreakfast(hasBreakfast);
						roomPrice.setIswifi(iswifi);
						roomPrice.setIswindow(productWindow);
						roomPrice.setPrice(price);
						roomPrice.setAvailablenum(available);
						roomPrice.setDate(priceDate);
						
						roomPrice.setAdminarea("中国,"+cityName);
						roomPrice.setCreatedate(new Date());
						roomPrice.setCreator("汤玉林");
						roomPrice.setDatasource("Lvmama");
						roomPrice.setCreatorid("tyl13564205515");
						System.out.println("插入房间价格信息"+price);
						jedis.insertAndGetId(roomPrice);

						String roomPriceObject = KafkaUtils.parseJsonObject(roomPrice, 2,1);
						String sendPost = downLoadService.sendPost(KafkaUtils.KAFKAURL, roomPriceObject);
					}
				}
			}
		} catch (Exception e) {
			produceService.sendMessage(params.getDestinationName(), params);
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * @Description 解析驴妈妈酒店详情信息
	 * @author 汤玉林
	 * @date 2018年1月22日 上午9:10:30
	 * @action parseLvmamaHotelDetail
	 * @param params
	 */
	public void parseLvmamaHotelDetail(Params params) {
		String uuid = params.getUuid();
		String url=params.getUrl();
		String content = params.getContent();
		String cityName = params.getCityName();
		Document doc=Jsoup.parse(content);
		//parse hotel detail
		try {
			//经纬度
			Elements script=doc.select("script");
			String scriptext=script.html();
			/**
			 * String index out of range: -1
			 */
			String mapString="";
			String destBuMap="";
			try {
				destBuMap=scriptext.substring(scriptext.indexOf("DestBuMap"), scriptext.indexOf("publicCallBack"));
				mapString = destBuMap.substring(destBuMap.indexOf("[")+1, destBuMap.indexOf("]"));
			} catch (Exception e) {
				try {
					destBuMap = scriptext.substring(scriptext.indexOf("var baiduArray = ")+18);
					mapString = destBuMap.substring(0,destBuMap.indexOf("]"));
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
			String lat="";
			String lng="";
			if(StringUtils.isNotBlank(mapString)){
				JSONObject jsonMap=new JSONObject(mapString);
				lat=Double.toString(jsonMap.getDouble("latitude"));
				lng=Double.toString(jsonMap.getDouble("longitude"));
			}
			//酒店介绍
			Elements elements=doc.select("div.product_detailL>div.product_jd>div.xq_img");
			if(elements.isEmpty()){
				elements=doc.select("div.pd-section>div.pd-section-content");
			}
			String hotelDetails=elements.text();

			Hotelinfo basicInfo=params.getHotelInfo();

			basicInfo.setId(uuid);
			basicInfo.setUrlid(url);
			basicInfo.setLatitude(lat);
			basicInfo.setLongitude(lng);
			basicInfo.setIntroduction(hotelDetails);
			basicInfo.setAdminarea("中国,"+cityName);
			basicInfo.setCreatedate(new Date());;
			basicInfo.setCreator("汤玉林");
			basicInfo.setDatasource("Lvmama");
			basicInfo.setCreatorid(creatorID);

			String parseJsonObject3 = KafkaUtils.parseJsonObject(basicInfo, 1,1);
			String sendPost3 = downLoadService.sendPost(KafkaUtils.KAFKAURL, parseJsonObject3);
			jedis.insertAndGetId(basicInfo);

			//种子链接传过来的地址信息
			Addressinfo addressinfo=params.getAddressinfo();
			addressinfo.setId(UUID.randomUUID().toString());
			addressinfo.setInfoid(uuid);
			addressinfo.setCreatedate(new Date());
			addressinfo.setCreator("汤玉林");
			addressinfo.setDatasource("Lvmama");
			addressinfo.setCreatorid(creatorID);

			String addressinfoObject = KafkaUtils.parseJsonObject(addressinfo, 12,1);
			String sendPost4 = downLoadService.sendPost(KafkaUtils.KAFKAURL, addressinfoObject);
			jedis.insertAndGetId(addressinfo);

			//酒店图片
			Elements imgs=doc.select("div.ticket_img_scroll>div.datu.fl>ul.pic_mod_ul>li");
			if(imgs.isEmpty()){
				imgs = doc.select("ul.focusReam-focus>li");
			}
			int count=0;
			int imgSize=0;
			if(imgs.size()>10){
				imgSize=10;
			}
			for(int i=0;i<imgSize;i++){
				String imgsrc=imgs.get(i).select("img").attr("src");
				Pictureinfo picture=new Pictureinfo();
				String pictureId=UUID.randomUUID().toString();
				picture.setId(pictureId);
				picture.setInfoid(pictureId);
				picture.setImgurl(imgsrc);
				picture.setSort(count++);
				picture.setType(4);
				picture.setDownload(0);
				picture.setCreatedate(new Date());
				picture.setAdminarea("中国,"+cityName);
				picture.setCreator("汤玉林");
				picture.setDatasource("Lvmama");
				picture.setCreatorid(creatorID);

				String pictureObject = KafkaUtils.parseJsonObject(addressinfo, 10,1);
				String sendPost5 = downLoadService.sendPost(KafkaUtils.KAFKAURL, pictureObject);
				jedis.insertAndGetId(picture);
			}
		} catch (Exception e) {
			produceService.sendMessage(params.getDestinationName(), params);
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @Description 解析驴妈妈酒店分页url
	 * @author 汤玉林
	 * @date 2018年1月22日 上午9:10:56
	 * @action parseLvmamaHotelPage
	 * @param params
	 */
	public void parseLvmamaHotelPage(Params params) {
		String pageUrl = params.getUrl();
		String content = params.getContent();
		String cityName2 = params.getCityName();
		Document document = Jsoup.parse(content);
		//获取分页链接中部分信息
		String cityName=pageUrl.substring(pageUrl.indexOf("mdd=")+4,pageUrl.length()-5);

		Integer lastindex=pageUrl.lastIndexOf("/")+1;
		//C20171012O20171013P1U9
		String strss=pageUrl.substring(lastindex,pageUrl.indexOf("?"));
		String str[]=strss.split("P");
		String str1=str[0];
		String str2=str[1];
		String cityId=str2.substring(str2.indexOf("U"),str2.length());
		//获取父类url
		String parentUrl="http://s.lvmama.com/hotel/"+cityId+str1+"?mmd="+cityName;
		//获取酒店url的a标签
		Elements hotelList=document.select("div.mainLeft.fl>div.prdLi");
		int count=0;
		//遍历url的a标签集合，获取酒店详情部分信息
		for(Element e:hotelList){
			//获取酒店详情url
			Elements element=e.select("a.proImg");
			String hotelUrl=element.attr("href");

			//存入url用于去重
			String string = jedis.get(hotelUrl);
			if(StringUtils.isNotBlank(string)){
				continue;
			}else{
				jedis.set(hotelUrl, hotelUrl);
			}
			System.out.println(hotelUrl+"酒店url-----");
			//设置酒店详情url
			Params para=new Params();
			//设置一级url 主页url
			para.setParentUrl(parentUrl);

			para.setUuid(UUID.randomUUID().toString());
			para.setUrl(hotelUrl);
			para.setCityName(cityName2);
			para.setHttpType(Param.GET);
			para.setType(Param.LVMAMA_HOTEL_DETAIL);
			para.setDataSource(Param.LVMAMA);
			try{
				String stardesc="";
				//获取酒店星级的信息
				Elements hotelStar=e.select("p.proTit>span.djjd_tagsclasses");
				if(hotelStar.size()>0){
					stardesc=hotelStar.attr("title");
				}else{
					hotelStar=e.select("dt.proTit>span.djjd_tagsclasses");
					stardesc=hotelStar.text();
					if(hotelStar.size()<=0){
						hotelStar=e.select("dt.proTit>span.hotel_stars");
						stardesc=hotelStar.attr("class");
						if(stardesc.contains("hotel_stars05")){
							stardesc="五星级";
						}
						else if(stardesc.contains("hotel_stars04")){
							stardesc="四星级";
						}
						else if(stardesc.contains("hotel_stars03")){
							stardesc="三星级";
						}
						else if(stardesc.contains("hotel_stars02")){
							stardesc="二星级";
						}
						else if(stardesc.contains("hotel_stars01")){
							stardesc="一星级";
						}
					}
				}
				//获取酒店名称
				String hotelName=e.attr("name").replace("．", "");
				String address="";
				//酒店地址
				Elements hotelAddress=e.select("div.proInfo>dl.prdDtl.address>dd");
				if(hotelAddress.size()>0){
					address=hotelAddress.get(0).ownText();
				}else {
					hotelAddress=e.select("dd.proInfo-address>i");
					address=hotelAddress.text();
				}
				//酒店价格
				Elements hotelPrice=e.select("div.price_inf>p.price>span.num");
				if(hotelPrice.size()<=0){
					hotelPrice=e.select("div.priceInfo-price>dfn>span.num");
				}
				String price=hotelPrice.text();
				//酒店评分
				Elements hotelGoodCommentRate=e.select("div.price_inf>p.evaluate>span.num");
				if(hotelGoodCommentRate.size()<=0){
					hotelGoodCommentRate=e.select("div.priceInfo>div>ul.product-number>li>a>b");
				}
				String score=hotelGoodCommentRate.text();
				//酒店的评分个数
				String commentsCount="";
				Elements hotelCommentsCount=e.select("div.price_inf>p.eval_count>a.num");
				if(hotelCommentsCount.size()<=0){
					Elements selects = e.select("div.priceInfo>div>ul.product-number>li>a");
					if(selects.size()>1){
						Element elementCommentsCount = selects.get(1);
						commentsCount=elementCommentsCount.text();
					}
				}
				//设置酒店实体类
				Hotelinfo basicInfo=new Hotelinfo();
				basicInfo.setName(hotelName);
				basicInfo.setAddress(address);
				basicInfo.setPrice(price);
				basicInfo.setGrade(score);
				basicInfo.setStar(stardesc);
				basicInfo.setDatasource("Lvmama");
				basicInfo.setGradenum(commentsCount);

				para.setHotelInfo(basicInfo);
				//设置地址实体类
				Addressinfo addressInfo=new Addressinfo();
				addressInfo.setDetailaddress(address);
				addressInfo.setCity(cityName);
				addressInfo.setProvince(cityName.equals("上海")?"上海":"海南");
				addressInfo.setType(2);
				addressInfo.setCountry("中国");

				para.setAddressinfo(addressInfo);
				para.setDestinationName("highQueue");

				produceService.sendMessage(highQueue,para);

			}catch(Exception x){

			}
		}
	}

	/**
	 * 
	 * @Description 解析驴妈妈酒店首页信息
	 * @author 汤玉林
	 * @date 2018年1月22日 上午9:11:15
	 * @action parseLvmamaHotelFirst
	 * @param params
	 */
	public void parseLvmamaHotelFirst(Params params) {
		String url = params.getUrl();
		String cityName = params.getCityName();
		String content = params.getContent();
		if(StringUtils.isNotBlank(content)){
			try {

				Document document = Jsoup.parse(content);
				//http://s.lvmama.com/hotel/U9C20171201O20171202?keyword=&mdd=%E4%B8%8A%E6%B5%B7
				//取总条数标签
				Elements totalHotel=document.select("div.request.clearfix>p.result>b");
				//计算总页数
				int totalPage=Integer.parseInt(totalHotel.text())%20==0?Integer.parseInt(totalHotel.text())/20:(Integer.parseInt(totalHotel.text())/20)+1;

				//通过截取url，拼接分页url
				Integer lastindex=url.lastIndexOf("/")+1;
				String cityId=url.substring(lastindex,url.indexOf("C", lastindex));
				String mmd=url.substring(url.indexOf("mdd=")+4,url.length());
				//获取分页链接
				for(int j=1;j<=totalPage;j++){
					String pageUrl="http://s.lvmama.com/hotel/C20171011O20171012P"+j+""+cityId+"?keyword=&mdd="+mmd+"#list";

					Params para= new Params();
					para.setUrl(pageUrl);
					para.setCityName(cityName);
					para.setHttpType(Param.GET);
					para.setType(Param.LVMAMA_HOTEL_PAGE);
					para.setDataSource(Param.LVMAMA);
					para.setDestinationName("topQueue");
					produceService.sendMessage(topQueue,para);
					System.out.println(url+"分页url++++++");
				}
			} catch (Exception e) {
				produceService.sendMessage(topQueue,params);
			}
		}
	}

	/**
	 * 
	 * @Description 解析行程评论url，或者行程评论信息
	 * @author 赵乐
	 * @date 2018年1月19日 下午4:43:05
	 * @action parseLvmamaRouteCommentPage
	 * @param @param params
	 * @return void
	 */
	public void parseLvmamaRouteCommentPage(Params params) {
		// TODO Auto-generated method stub
		String uuId=params.getUuid();
		String cityName = params.getCityName();
		Document doc=Jsoup.parse(params.getContent());
		try {
			//获取每条评论的DIV
			Elements comEles = doc.select("div.comment-li");
			if(!comEles.isEmpty()){
				for (Element element : comEles) {
					Commentinfo commentInfo = new Commentinfo();
					String commentDate=element.select("div.com-userinfo>p>em").text();
					String id=UUID.randomUUID().toString();
					commentInfo.setId(id);
					commentInfo.setInfoid(uuId);
					commentInfo.setType(3);//类型：1-景点，2-酒店，3-景点行程
					commentInfo.setCommentdate(commentDate);
					//获取评论的内容div
					commentInfo.setContent(element.toString());
					commentInfo.setAdminarea("中国,"+cityName);
					commentInfo.setDatasource("Lvmama");
					commentInfo.setCreator("姚良良");
					commentInfo.setCreatorid("13783985208");
					commentInfo.setCreatedate(new Date());

					String commentInfoObject = KafkaUtils.parseJsonObject(commentInfo, 11,1);
					String sendPost = downLoadService.sendPost(KafkaUtils.KAFKAURL, commentInfoObject);
					//存入redis
					jedis.insertAndGetId(commentInfo);
				}
			}else {
				produceService.sendMessage(lowQueue, params);
				System.out.println("未获取到评论信息，重新放入队列");
			}
		} catch (Exception e) {
			produceService.sendMessage(lowQueue, params);
			e.printStackTrace();
		}
	}

	//通过解析驴妈妈详情获取行程的评论url
	public void parseLvmamaRouteComment(Params params) {
		// TODO Auto-generated method stub
		String uuId=params.getUuid();
		String cityName = params.getCityName();
		String content = params.getContent();
		if (StringUtils.isNotBlank(content)) {
			try {
				Document doc = Jsoup.parse(params.getContent());
				//获取评论总数
				int totalComment=0;
				//通过对象的gradenum属性获得评论总数
				//通过对象的gradenum属性获得评论总数
				Elements elements = doc.select("a#allCmt>span");
				if(elements.isEmpty()){
					//自由行
					elements = doc.select("li#taocan_Cmt>a>span");

				}
				totalComment=elements.isEmpty()?0:NumUtils.getInteger(elements.text());
				//产品id(通过上面解析价格的时候获取的)
				String productId = params.getOtherInformation();
				//总页数
				int totalPage=totalComment%10==0?totalComment/10:totalComment/10+1;
				System.out.println("该行程的评论页数为："+totalPage+";链接为："+params.getUrl());
				for(int i=1;i<=totalPage;i++){
					String commentUrL="http://dujia.lvmama.com/vst_front/comment/newPaginationOfComments"
							+ "?type=all&currentPage="+i+"&totalCount="+totalComment+"&placeId=&productId="+productId+"&placeIdType=&isPicture=&isBest=&isPOI=Y&isELong=N";

					Params para=new Params();
					para.setType(Param.LVMAMA_ROUTE_COMMENTPAGE);
					para.setHttpType(Param.GET);
					para.setDataSource(Param.LVMAMA);
					para.setCityName(cityName);
					para.setUrl(commentUrL);
					para.setUuid(uuId);
					para.setDestinationName("lowQueue");

					produceService.sendMessage(lowQueue,para);

				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		}

	}

	/**
	 * 
	 * @Description 解析驴妈妈行程价格url，获取行程价格信息
	 * @author 赵乐
	 * @date 2018年1月19日 下午4:37:33
	 * @action parseLvmamaRoutePrice
	 * @param @param params
	 * @return void
	 */
	public void parseLvmamaRoutePrice(Params params) {
		// TODO Auto-generated method stub
		String url=params.getUrl();
		String uuid = params.getUuid();
		String content=params.getContent();
		String cityName = params.getCityName();
		if(!StringUtils.isBlank(content)){
			try {

				JSONArray jsonArray = new JSONArray(content);
				System.out.println(url+" 的行程价格安排共有"+jsonArray.length()+"天!");
				int count = 0;

				for (int j = 0; j < jsonArray.length(); j++) {
					Routepriceinfo routePrice = new Routepriceinfo();
					String str = jsonArray.get(j).toString();
					JSONObject json = new JSONObject(str);
					//获取日期
					String priceDate = json.get("departureDate").toString();
					//获取星期
					String dayOfWeek = json.get("weekOfDate").toString();
					//获取最低价格
					String lowestPrice = json.get("lowestSaledPriceYuan").toString();
					String id=UUID.randomUUID().toString();
					routePrice.setId(id);
					routePrice.setRouteid(uuid);
					routePrice.setPricedate(priceDate);
					routePrice.setDayofweek(dayOfWeek);
					routePrice.setLowestprice(lowestPrice);
					routePrice.setDestination("中国,"+cityName);
					routePrice.setCreator("姚良良");
					routePrice.setCreatorid("13783985208");
					routePrice.setDatasource("Lvmama");
					routePrice.setCreatedate(new Date());
					String parseJsonObject = KafkaUtils.parseJsonObject(routePrice, 7,1);
					String sendPost = downLoadService.sendPost(KafkaUtils.KAFKAURL, parseJsonObject);
					
					jedis.insertAndGetId(routePrice);

				}	
				System.out.println(url+" 的行程价格安排共保存了"+count+"天!");
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("解析失败");
				produceService.sendMessage(lowQueue, params);
			}
		}else {
			produceService.sendMessage(lowQueue, params);
			System.out.println("未获取到评论信息，重新放入队列");
		}
	}
	//解析驴妈妈详情获取行程价格的url
	public void parseLvmamaRoutePriceUrl(Params params) {
		String url=params.getUrl();
		String cityName = params.getCityName();
		System.out.println("url链接为："+url);
		String uuId=params.getUuid();
		Integer productId = 0;
		Integer startDistrictId = -1;
		if(url.indexOf("-")!=-1){
			String[] array = url.split("-");
			int size = array.length;
			try {
				if (size > 1) {
					System.out.println("正常链接");
					String productIdStr = array[0];
					String productIdStr1 = array[1];
					productId = NumUtils.getInteger(productIdStr);
					if(productId==null){
						productId = NumUtils.getInteger(productIdStr1);
					}
					System.out.println(productId);
					String startDistrictIdStr = array[1];
					startDistrictId = NumUtils.getInteger(startDistrictIdStr);
					System.out.println("出发地城市ID为 "+startDistrictId);
				} else {
					String productIdStr = array[0];
					productId = NumUtils.getInteger(productIdStr);
					System.out.println(productId);
					System.out.println("没有区号");
				}
			} catch (Exception e) {
				productId=0;
			}
		}else{
			productId=Integer.valueOf(url.substring(url.lastIndexOf("/")+1, url.length()));
		}
		//行程编号,后面的评论链接需要用到
		params.setOtherInformation(productId.toString());
		String routePriceURL="http://dujia.lvmama.com/package/travelDateList.json?productId=" + productId + "&startDistrictId="
				+ startDistrictId + "&businessType=DestBu";

		System.out.println("行程价格url-----"+routePriceURL);
		//将行程价格url放入redis
		Params para=new Params();
		para.setType(Param.LVMAMA_ROUTE_PRICE);
		para.setHttpType(Param.GET);
		para.setDataSource(Param.LVMAMA);
		para.setCityName(cityName);
		para.setUrl(routePriceURL);
		para.setUuid(uuId);
		para.setDestinationName("lowQueue");

		produceService.sendMessage(lowQueue,para);
	}

	//解析驴妈妈行程详情信息
	public void parseLvmamaRouteDetail(Params params) {
		// TODO Auto-generated method stub
		String url = params.getUrl();
		String cityName = params.getCityName();
		Document doc=Jsoup.parse(params.getContent());
		try {
			//实例一个行程对象
			Routeinfo routeInfo = new Routeinfo();
			//设置行程routeInfo的固定属性
			String uuId=UUID.randomUUID().toString();
			routeInfo.setId(uuId);
			routeInfo.setUrlid(url);
			routeInfo.setDestination(cityName);
			//获取名字
			Elements nameEle = doc.select("div.product_top_r>div.product_top_tit>h1");
			if(nameEle.isEmpty()){
				nameEle = doc.select("div.product_top.clearfix>div.product_top_r>h1.product_top_tit");
				if(nameEle.isEmpty()){
					nameEle = doc.select("h1.detail_product_tit");
					if(nameEle.isEmpty()){
						nameEle=doc.select("div.detail>div.textWrap>p.nchtitle>a");
					}
				}
			}
			if(nameEle.isEmpty()){
				System.out.println(url+" 获取名称失败,请重新获取!");
			}else{					
				String name = nameEle.first().text();
				System.out.println("名称为:"+name);
				routeInfo.setName(name);
			}
			//获取行程的线路编号（区分同一Url中A线、B线、C线（如果没有线路区别，则为空））
			Elements routeTypeEle = doc.select("div#product-travel>div.instance_box>div.clearfix>ul.instance_tab.fl>li.active:nth-child(1)");
			if(!routeTypeEle.isEmpty()){
				String routeType = routeTypeEle.text();
				routeInfo.setRoutetype(routeType);
			}
			//获取价格	
			//跟团游和半自助和当地游价格模板
			Elements priceEle = doc.select("div.product_top_price_box>dl.product_info>dd>p.product_top_price>span.price_num>dfn");
			if(priceEle.isEmpty()){
				priceEle = doc.select("span.product_price");
				if(priceEle.isEmpty()){
					//自由行价格模板
					priceEle = doc.select("div.product_top_price_box>p.product_top_price>span.price_num>dfn");
					/*if(priceEle.isEmpty()){
						priceEle = doc.select("div.nchline-price>span.nchline-price-style>em>b");
					}*/
				}
			}
			if (!priceEle.isEmpty()) {
				String priceStr = priceEle.first().text();// 驴妈妈价格
				if (StringUtils.isNoneBlank(priceStr)) {
					routeInfo.setPrice(NumUtils.getInteger(priceStr).toString());
					System.out.println("价钱为:"+priceStr);
				}
			}
			//获取产品编号
			Elements itemNoEle = doc.select("div.product_info1");
			if(itemNoEle.isEmpty()){
				itemNoEle = doc.select("p.detail_product_num>span");
			}
			String[] split = itemNoEle.text().split("：");
			if (split.length > 1) {
				String itemNo = NumUtils.getInteger(split[1]).toString();// 产品编号
				routeInfo.setItemno(itemNo);
			} else {
				String[] split2 = url.split("-");
				String itemNo = NumUtils.getInteger(split2[0]).toString();
				routeInfo.setItemno(itemNo);
			}
			//获取行程类型(跟团游、自由行或者其他)
			/*Elements teamTypeElement = doc.select("div.product-type>div.group_icon");
			if(teamTypeElement.isEmpty()){
				teamTypeElement=doc.select("div.product_top_l>span.product_top_type");
				if(teamTypeElement.isEmpty()){
					teamTypeElement=doc.select("div.main-detail>div.sign");
				}
			}
			String teamTypeClass=teamTypeElement.attr("class");
			String teamType="";
			if(teamTypeClass.contains("zyx")||teamTypeClass.contains("freetour")){
				teamType="自由行";
			}else if (teamTypeClass.contains("gty")){
				teamType="跟团游";
			}else if (teamTypeClass.contains("ddy")){
				teamType="当地游";
			}else if (teamTypeClass.contains("bzz")){
				teamType="半自助";
			}*/

			//跟团游类型和当地游适合模板
			String teamType="";
			Elements teamTypeElement = doc.select("div.detail_top_all.clearfix>p.detail_product_num>span.dpn_group");
			if(teamTypeElement.isEmpty()){
				//自由行
				teamTypeElement=doc.select("div.product_top_box.clearfix>div.product_top_l>span.product_top_type.product_type_zyx");
				if(teamTypeElement.isEmpty()){
					teamTypeElement=doc.select("div.product_top.clearfix>i.line_icon.product_zyx");
					if(!teamTypeElement.isEmpty()){
						teamType="自由行";
					}else{
						//自由行的另一种模板
						teamTypeElement=doc.select("div#tour-type");
						if(!teamTypeElement.isEmpty()){
							teamType="自由行";
						}else{
							//半自助
							teamTypeElement=doc.select("div.detail_top_all.clearfix>p.detail_product_num>span.dpn_half");
							if(!teamTypeElement.isEmpty()){
								teamType=teamTypeElement.text();
							}
						}
					}
				}else{
					teamType=teamTypeElement.text();
				}
			}else{
				teamType=teamTypeElement.text();
			}
			routeInfo.setTeamtype(teamType);
			//获取出发地
			Elements elements = doc.select("div.product_top_booking>dl.product_info.clearfix");
			for(int i=0;i<elements.size();i++){
				String textdt = elements.get(i).select("dt").text();
				String textdd = elements.get(i).select("dd").text();
				switch (textdt) {
				case "出发城市：":
					String departure = textdd;
					routeInfo.setDeparture(departure);
					break;

				default:
					break;
				}

			}
			//获取行程概要
			Elements itineraryOutlineEle = doc.select("div.product_top_r>dl.product_info.clearfix.product_info_itinerary_summary>dd");
			if(itineraryOutlineEle.isEmpty()){
				System.err.println("行程概要为空");
			}
			if(!itineraryOutlineEle.isEmpty()){
				String itineraryOutline = itineraryOutlineEle.text();// 行程概要
				routeInfo.setItineraryoutline(itineraryOutline);
			}
			//获取产品推荐
			Elements productRecommendEle = doc.select("div.product_manager_recommendation>ul.pmr_content");
			if(productRecommendEle.isEmpty()){
				productRecommendEle=doc.select("div.pm-recomman-body>ul");
			}
			String productRecommend = productRecommendEle.text();
			routeInfo.setProductrecommend(productRecommend);
			//获取优惠信息
			Elements reducedPriceEle = doc.select("div.product_top_r>dl.product_info2.mt5>dd>span.tags101");
			if(reducedPriceEle.isEmpty()){
				reducedPriceEle=doc.select("dl.product_info>dd>span");
				if(reducedPriceEle.isEmpty()){
					reducedPriceEle=doc.select("div.nchline-promotion-flag>span.nchline-promotion-comment>samp");
				}
			}
			String reducedPrice = reducedPriceEle.attr("tip-content");
			routeInfo.setReducedprice(reducedPrice);
			//获取产品特色（产品详情）
			Elements productFeatureEle = doc.select("div#product-detail");
			if(!productFeatureEle.isEmpty()){
				String productFeature = productFeatureEle.text();
				routeInfo.setProductfeature(productFeature);
			}
			//获取线路介绍
			Elements itineraryDetailsEle = doc.select("div#product-travel");
			if(itineraryDetailsEle.isEmpty()){
				itineraryDetailsEle = doc.select("ul.xingcheng");
			}
			if(!itineraryDetailsEle.isEmpty()){
				String itineraryDetails = itineraryDetailsEle.html();
				routeInfo.setItinerarydetails(itineraryDetails);
			}
			//跟团游的费用说明
			Elements expenseEle = doc.select("div.product-module.pd-cost");
			if(expenseEle.isEmpty()){
				//自由行费用说明
				expenseEle = doc.select("div#product-cost");
			}
			if(!expenseEle.isEmpty()){
				String expense = expenseEle.toString();// 费用说明
				routeInfo.setExpense(expense);
			}
			//跟团游和当地游和半自助获取预定须知				
			Elements reserveInfoEle = doc.select("div.product-module.pd-notice");
			if(reserveInfoEle.isEmpty()){
				reserveInfoEle = doc.select("div#product-preorder-note");
			}
			//自由行预定须知
			if(!reserveInfoEle.isEmpty()){
				String reserveInfo = reserveInfoEle.text();// 预定须知
				routeInfo.setReserveinfo(reserveInfo);
			}
			//获取评分
			//跟团游和当地游和半自助游
			Elements gradeEle = doc.select("div.product_top_dp>div.product_top_dp_left>span");
			if(gradeEle.isEmpty()){
				//自由行评分
				gradeEle=doc.select("p.product_top_dp>span");
			}	
			String grade = gradeEle.text();
			routeInfo.setGrade(grade);
			//获取评论个数
			/*Elements gradeNumEle = doc.select("div.com-count>em>a.f60");
			if(gradeNumEle.isEmpty()){
				gradeNumEle=doc.select("ul.float_nav_list clearfix>li.product-customer-review>span.float_nav_num");
			}*/
			//去过人数
			Elements beenNumEle = doc.select("a#appraise>span>i");
			if(!(beenNumEle.isEmpty() || beenNumEle.size()<2)){
				String beenNum=beenNumEle.get(1).text();
				routeInfo.setBeennum(beenNum);
			}

			//跟团游和当地游和半自助游点评人数（从驴友点评中获取）
			String gradeNum ="";
			Elements gradeNumEle = doc.select("div.float-nav>div.float-nav-main>ul.float-nav-list>li[data-flag=pd-comment]>a>em");
			if(!gradeNumEle.isEmpty()){
				gradeNum=NumUtils.getInteger(StringUtils.isNotBlank(gradeNumEle.text())?gradeNumEle.text():"0").toString();

			}else{
				//自由行点评人数（从驴友点评中获取）
				gradeNumEle = doc.select("ul#float_nav>li[data-flag=product-customer-review]>span.float_nav_num");
				if(!gradeNumEle.isEmpty()){
					gradeNum=NumUtils.getInteger(StringUtils.isNotBlank(gradeNumEle.text())?gradeNumEle.text():"0").toString();

				}
			}

			gradeNum = NumUtils.getInteger(StringUtils.isNotBlank(gradeNumEle.text())?gradeNumEle.text():"0").toString();
			System.err.println("评论人数："+gradeNum);
			if(StringUtils.isBlank(gradeNum)){
				gradeNum="0";
			}
			routeInfo.setGradenum(gradeNum);

			routeInfo.setDatasource("Lvmama");
			routeInfo.setCreator("姚良良");
			routeInfo.setCreatorid("13783985208");
			routeInfo.setCreatedate(new Date());
			//行程对象放入route中，后面需要用到里面的属性
			params.setRouteInfo(routeInfo);
			//放入redis

			if(StringUtils.isBlank(routeInfo.getName()) ){

				System.out.println("获取行程信息不完全，放回原队列");
				//RouteSpiderStart.queueRedis.add(RouteSpiderStart.KEY_ROUTE, route);
			}else{
				String parseJsonObject = KafkaUtils.parseJsonObject(routeInfo, 6,1);
				String sendPost = downLoadService.sendPost(KafkaUtils.KAFKAURL, parseJsonObject);
				jedis.insertAndGetId(routeInfo);
				//获取行程图片
				Elements pictureEle = doc.select("div.img_scroll_all>div.img_scroll_box>ul.img_scroll_list>li>img");
				if(pictureEle.isEmpty()){
					pictureEle=doc.select("div.product-pic>ul>li>img");
					if(pictureEle.isEmpty()){
						pictureEle=doc.select("div.product_top_img>div.scrollImg>div.scrollImg_wrap>ul>li>img");
					}
				}
				System.out.println("该行程的图片数量为 "+pictureEle.size()+" 张!");
				for (int j = 0; j < pictureEle.size(); j++){
					String imgUrl = pictureEle.get(j).attr("src");
					//实例一个行程图片对象
					Pictureinfo pictureInfo = new Pictureinfo();
					String id=UUID.randomUUID().toString();
					pictureInfo.setId(id);
					pictureInfo.setInfoid(uuId);
					pictureInfo.setImgurl(imgUrl);
					pictureInfo.setSort(j+1);
					//设置行程图片的固定属性
					pictureInfo.setType(3);
					pictureInfo.setDownload(0);
					pictureInfo.setAdminarea("中国,"+cityName);
					pictureInfo.setCreatedate(new Date());
					pictureInfo.setCreator("姚良良");
					pictureInfo.setDatasource("Lvmama");
					pictureInfo.setCreatorid("13783985208");

					String pictureInfoObject = KafkaUtils.parseJsonObject(pictureInfo, 10,1);
					String message2 = downLoadService.sendPost(KafkaUtils.KAFKAURL, pictureInfoObject);
					//放入redis
					jedis.insertAndGetId(pictureInfo);
				}
				//插入成功之后放入价格url
				parseLvmamaRoutePriceUrl(params);
				//插入成功之后放入评论url
				parseLvmamaRouteComment(params);
			}
		} catch (Exception e) {
			produceService.sendMessage(highQueue, params);
			e.printStackTrace();

		}	
	}

	//解析驴妈妈行程首页url
	public void parseLvmamaRouteFirst(Params params) {
		// TODO Auto-generated method stub
		//解析页面信息
		String content = params.getContent();
		try {
			if (StringUtils.isNotBlank(content)) {
				Document document = Jsoup.parse(content);
				//爬取分页链接
				if(params.getUrl().endsWith("&k=0#list")){
					//获取城市目的地
					String href=params.getUrl();
					String cityNeme=href.substring(href.indexOf("keyword=")+8,href.indexOf("&k"));
					//获取分页的总页数
					Elements aHrefNum = document.select("div.pagebox>a");
					String str = aHrefNum.get(aHrefNum.size() - 2).text();
					Integer num = Integer.valueOf(str);
					for (int i = 1; i <= num; i++) {
						//获取所有请求页面的链接
						String pagehref="http://s.lvmama.com/route/H8P" + i + "?keyword="+cityNeme+"&tabType=route";
						//把分页链接放回redis任务队列
						Params para=new Params();
						para.setUrl(pagehref);
						para.setHttpType(Param.GET);
						para.setType(Param.LVMAMA_ROUTE_FIRST);
						para.setDataSource(Param.LVMAMA);
						para.setDestinationName("topQueue");
						System.out.println(pagehref);
						produceService.sendMessage(topQueue,para);
					}
				}
				//爬取行程详情信息
				if(params.getUrl().endsWith("&tabType=route")){
					Elements elements = document.select("div.product-left>a");
					//二级分页url
					String pageUrl=params.getUrl();
					//一级主页url
					String parentUrl=pageUrl.substring(0,pageUrl.indexOf("P"))+
							pageUrl.substring(pageUrl.indexOf("?"),pageUrl.indexOf("&"))+"&k=0#list";
					//获取城市名称中文
					String cityName=pageUrl.substring(pageUrl.indexOf("keyword=")+8,pageUrl.indexOf("&tabType"));
					System.out.println(cityName);
					//遍历获取行程的部分基础信息
					for (Element element : elements) {
						String routeUrl=element.attr("href");
						//存入url用于去重
						String string = jedis.get(routeUrl);
						if(StringUtils.isNotBlank(string)){
							continue;
						}else{
							jedis.set(routeUrl, routeUrl);
						}

						Params para =new Params();
						System.out.println(routeUrl+"------驴妈妈行程url");
						para.setType(Param.LVMAMA_ROUTE_DETAIL);
						para.setDataSource(Param.LVMAMA);
						para.setHttpType(Param.GET);
						para.setParentUrl(parentUrl);
						para.setUrl(routeUrl);
						para.setCityName(cityName);
						para.setDestinationName("highQueue");

						produceService.sendMessage(highQueue,para);
					}
				}

			}else{
				produceService.sendMessage(params.getDestinationName(), params);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}


	}
	/**
	 * 
	 * @Description 解析驴妈妈门票评论url获取评论信息
	 * @author 赵乐
	 * @date 2018年1月19日 上午11:30:00
	 * @action parseLvmamaScenicCommentPage
	 * @param @param params
	 * @return void
	 */
	public void parseLvmamaScenicCommentPage(Params params) {
		// TODO Auto-generated method stub
		String content = params.getContent();
		String uuId=params.getUuid();
		String cityName = params.getCityName();
		Document doc = Jsoup.parse(content);
		try {
			Elements elements = doc.select("div.comment-li");
			if(elements.size()>0){
				for (Element element : elements) {
					Commentinfo commentinfo = new Commentinfo();
					commentinfo.setId(UUID.randomUUID().toString());
					commentinfo.setInfoid(uuId);
					String comment = element.html();
					Elements timeEle = element.select("div.com-userinfo>p>em");
					String time = timeEle.text();
					commentinfo.setContent(comment);
					commentinfo.setCommentdate(time);
					commentinfo.setType(1);
					commentinfo.setDatasource("Lvmama");
					commentinfo.setAdminarea("中国,"+cityName);
					commentinfo.setCreatedate(new Date());
					commentinfo.setCreator("徐仁杰");
					commentinfo.setCreatorid("xurenjie-13621935220");

					String commentinfoObject = KafkaUtils.parseJsonObject(commentinfo, 11,1);
					String message1 = downLoadService.sendPost(KafkaUtils.KAFKAURL, commentinfoObject);

					jedis.insertAndGetId(commentinfo);
				}
			}else{
				produceService.sendMessage(lowQueue, params);
				System.out.println("未获取到评论，重新放入队列");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 
	 * @Description 解析驴妈妈门票评论url获取评论分页链接
	 * @author 赵乐
	 * @date 2018年1月19日 上午9:32:33
	 * @action parseLvmamaScenicComment
	 * @param @param params
	 * @return void
	 */
	public void parseLvmamaScenicComment(Params params) {
		// TODO Auto-generated method stub
		String uuid = params.getUuid();
		String cityName = params.getCityName();
		String url = params.getUrl();
		//获得产品id
		String productId=url.substring(url.indexOf("-")+1, url.length());
		//获取评论总数
		String gradeNum=params.getSceinfo().getGradenum();
		int commentTotal=Integer.valueOf(gradeNum);
		int totalPage=commentTotal%10==0?commentTotal/10:commentTotal/10+1;
		for(int i=1;i<=totalPage;i++){
			String commentUrl = "http://ticket.lvmama.com/vst_front/comment/newPaginationOfComments?type=all&currentPage="+i+""
					+ "&totalCount="+gradeNum+"&placeId="+productId+"&productId=&placeIdType=PLACE&isPicture=&isBest=&isPOI=Y&isELong=N";
			Params para=new Params();
			para.setUuid(uuid);
			para.setUrl(commentUrl);
			para.setCityName(cityName);
			para.setType(Param.LVMAMA_SCENIC_COMMENTPAGE);
			para.setDataSource(Param.LVMAMA);
			para.setHttpType(Param.GET);
			para.setDestinationName("lowQueue");

			produceService.sendMessage(para);

		}

	}
	/**
	 * 
	 * @Description 解析驴妈妈门票中异步的优惠政策
	 * @author 赵乐
	 * @date 2018年1月19日 上午9:27:44
	 * @action parseLvmamaScenicDetail
	 * @param @param params
	 * @return void
	 */
	public void parseLvmamaScenicFavouredpolicy(Params params){
		String url = params.getUrl();
		String content = params.getContent();
		Scepriceinfo scepriceinfo = params.getScepriceinfo();
		scepriceinfo.setFavouredpolicy(content);
		
		String scepriceinfoObject = KafkaUtils.parseJsonObject(scepriceinfo, 4,1);
		String message5 = downLoadService.sendPost(KafkaUtils.KAFKAURL, scepriceinfoObject);

		jedis.insertAndGetId(scepriceinfo);
		
		
	}
	
	
	/**
	 * 
	 * @Description 解析驴妈妈门票景点详情url获取详细信息
	 * @author 赵乐
	 * @date 2018年1月19日 上午9:27:44
	 * @action parseLvmamaScenicDetail
	 * @param @param params
	 * @return void
	 */
	public void parseLvmamaScenicDetail(Params params) {
		// TODO Auto-generated method stub
		//新建uurid
		String uuid = params.getUuid();
		String url = params.getUrl();
		String cityName = params.getCityName();
		Integer productId=NumUtils.getInteger(url);

		// 获取门票页面
		String html = params.getContent();
		Document doc = Jsoup.parse(html);
		// 门票详情操作
		Sceinfo sceinfo =params.getSceinfo();
		sceinfo.setUrlid(url);
		sceinfo.setId(uuid);
		sceinfo.setType("2");
		try {
			//景点名称
			Elements nameEle=doc.select("div.dtitle>div.titbox>h1.tit");
			String name=nameEle.text();
			sceinfo.setName(name);

			// 经纬度
			String scriptStr = doc.html();
			int lngIndex = scriptStr.indexOf("coordinate: { lng:");
			int latIndex = scriptStr.indexOf("},//地图中心");
			String longitude="";
			String latitude="";
			try {
				if(lngIndex!=-1||latIndex!=-1){
					String[] coordinate = scriptStr.substring(lngIndex, latIndex).split(",");
					longitude = coordinate[0];
					latitude = coordinate[1];
				}else{
					lngIndex=0;
					latIndex=0;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(!"".equals(longitude) && !"".equals(latitude)){
				sceinfo.setLatitude(NumUtils.getDouble(latitude).toString());
				sceinfo.setLongitude(NumUtils.getDouble(longitude).toString());
			}


			Elements select = doc.select("div.overview>div.dtitle.clearfix>span.xorder>span.price>dfn>i");
			// 价格
			String referPrice = select.isEmpty() ? "" : select.text();
			sceinfo.setReferprice(referPrice);
			Elements select2 = doc.select("div.overview>div.dtitle.clearfix>div.titbox>span.mp_star>i");
			// 景点星级
			String starLevel = select2.isEmpty() ? "" : select2.text();
			sceinfo.setStarlevel(starLevel);
			Elements select3 = doc.select(
					"div.overview>div.dcontent.clearfix>div.dinfo>div.sec-info>div.sec-inner>dl.dl-hor.service_list>dd");
			// 服务保障
			String serviceCommitment = select3.isEmpty() ? "" : select3.text();
			sceinfo.setServicecommitment(serviceCommitment);

			//景点地址
			Elements addressEle=doc.select("div.sec-info>div.sec-inner>dl.dl-hor>dd>p.linetext");
			String address= addressEle.isEmpty()?"":addressEle.text();
			sceinfo.setAddress(address);

			//评论人数
			Elements commentEle=doc.select("div.tab-dest>ul.ul-hor>li>span#totalCmt");
			String commentStr=commentEle.text();
			Integer commentCount=0;
			if(StringUtils.isNotBlank(commentStr)){
				commentCount=NumUtils.getInteger(commentStr);
			}
			sceinfo.setGradenum(commentCount+"");

			//评分
			Elements scoreEle=doc.select("div.c_09c>span>i");
			String score=scoreEle.text();
			if(StringUtils.isBlank(score)){
				score="";
			}
			sceinfo.setGrade(score);
			
			//预定须知部分
			Elements select13 = doc.select("div.dcontent>div.dactive>div.darea");
			//免票政策
			for (Element element : select13) {
				String select5 = element.select("h5").text();
				Elements select6 = element.select("p");
				switch (select5) {

				case "免票政策":
					
					sceinfo.setTicketinformation(select13.text());;

					break;
				case "优惠政策":
					sceinfo.setFavouredpolicy(select13.text());

					break;
				default:
					break;
				}
			}
			sceinfo.setAdminarea("中国,"+cityName);
			sceinfo.setDatasource("Lvmama");
			sceinfo.setCreator("徐仁杰");
			sceinfo.setCreatorid("xurenjie-13621935220");
			sceinfo.setCreatedate(new Date());

			//判断爬取的景点名称是否是空
			if(StringUtils.isNotBlank(sceinfo.getName())){

				String parseJsonObject = KafkaUtils.parseJsonObject(sceinfo, 5,1);
				String message1 = downLoadService.sendPost(KafkaUtils.KAFKAURL, parseJsonObject);
				String insertAndGetId = jedis.insertAndGetId(sceinfo);
				//插入成功
				if(StringUtils.isNotBlank(insertAndGetId)){
					Addressinfo addressInfo = new Addressinfo();
					addressInfo.setId(UUID.randomUUID().toString());
					addressInfo.setInfoid(uuid);
					addressInfo.setDetailaddress(address);
					addressInfo.setCity(params.getCityName());
					addressInfo.setProvince(params.getCityName());
					addressInfo.setType(1);
					addressInfo.setCountry("中国");
					addressInfo.setCreator("徐仁杰");
					addressInfo.setDatasource("Lvmama");
					addressInfo.setCreatorid("xurenjie-13621935220");
					addressInfo.setCreatedate(new Date());

					String addressObject = KafkaUtils.parseJsonObject(addressInfo, 12,1);
					String message2 = downLoadService.sendPost(KafkaUtils.KAFKAURL, addressObject);
					jedis.insertAndGetId(addressInfo);

					// 景点图片
					Elements select4 = doc.select("div.ticket_img_scroll>div.xtu.fr>dl.pic_tab_dl>dd>img");
					if (!select4.isEmpty()) {
						for (int i = 0; i < select4.size(); i++) {
							Pictureinfo pictureinfo = new Pictureinfo();
							String imgUrl = select4.get(i).absUrl("src");
							String id=UUID.randomUUID().toString();
							pictureinfo.setId(id);
							pictureinfo.setInfoid(uuid);
							pictureinfo.setImgurl(imgUrl);
							pictureinfo.setSort(i);
							pictureinfo.setType(2);
							pictureinfo.setDownload(0);
							pictureinfo.setAdminarea("中国,"+cityName);
							pictureinfo.setCreator("徐仁杰");
							pictureinfo.setDatasource("Lvmama");
							pictureinfo.setCreatorid("xurenjie-13621935220");
							pictureinfo.setCreatedate(new Date());

							String pictureinfoObject = KafkaUtils.parseJsonObject(pictureinfo, 10,1);
							String message3 = downLoadService.sendPost(KafkaUtils.KAFKAURL, pictureinfoObject);
							jedis.insertAndGetId(pictureinfo);

						}
					}

					// 景点门票价格部分
					Elements select5 = doc
							.select("div.dcontent.dorder-list>div.dpro-list>table.ptable.table-full>tbody.ptbox.short");
					if (!select5.isEmpty()) {
						for (Element element : select5) {
							// 价格类型（成人、儿童、其他等）
							Elements select6 = element.select("tr>td.ptdname>div.ptname>h5");
							String priceType = select6.isEmpty() ? "" : select6.text();
							Elements priceListElements = element.select("tr>td>div.ptdlist>div.pdlist-inner>dl.ptditem");
							for (Element element2 : priceListElements) {
								Scepriceinfo scepriceinfo = new Scepriceinfo();
								String id=UUID.randomUUID().toString();
								scepriceinfo.setId(id);
								scepriceinfo.setScenicid(uuid);
								scepriceinfo.setUrlid(url);
								scepriceinfo.setName(name);
								scepriceinfo.setPricetype(priceType);
								// 门票类型（单票、套票等）
								Elements select7 = element2.select("dt.pdname");
								String ScenicType = select6.isEmpty() ? "" : select7.text();
								if (StringUtils.isNoneBlank(ScenicType) && ScenicType.contains("[") && ScenicType.contains("]")) {
									int indexOf = ScenicType.indexOf("[");
									int indexOf2 = ScenicType.indexOf("]");
									String substring = ScenicType.substring(indexOf + 1, indexOf2);
									scepriceinfo.setTickettype(substring);
								}
								// 价格条目
								Elements select8 = element2.select("dt.pdname");
								String priceItem = select6.isEmpty() ? "" : select8.text();
								scepriceinfo.setPriceitem(priceItem);
								// 市场价
								Elements select9 = element2.select("dd.pdprice");
								String marketingPrice = select6.isEmpty() ? "" : select9.text();
								scepriceinfo.setMarketingprice(marketingPrice);
								// 销售条件（预定时间）
								Elements select10 = element2.select("dd.pdAdvbookingTime");
								String saleCondition = select6.isEmpty() ? "" : select10.text();
								scepriceinfo.setSalecondition(saleCondition);
								// 销售价
								Elements select11 = element2.select("dd.pdlvprice");
								String salePrice = select6.isEmpty() ? "" : select11.text();
								scepriceinfo.setSaleprice(salePrice);
								// 折扣信息
								Elements select12 = element2.select("dd.pdprefer");
								String discountInfo = select6.isEmpty() ? "" : select12.text();
								scepriceinfo.setDiscountinfo(discountInfo);
								
								scepriceinfo.setAdminarea("中国,"+cityName);
								scepriceinfo.setCreator("徐仁杰");
								scepriceinfo.setDatasource("Lvmama");
								scepriceinfo.setCreatorid("xurenjie-13621935220");
								scepriceinfo.setCreatedate(new Date());
								//新增优惠信息
								//异步信息
								//获取suppGoodsId，和branchType
								Elements selectsuppGoodsId = element2.select("dt.pdname>a");
								
								String suppGoodsId = selectsuppGoodsId.attr("data");
								String branchType = selectsuppGoodsId.attr("data1");
								
								if(productId!=null && StringUtils.isNotBlank(suppGoodsId)){
									String priceinfoFavouredpolicy="http://ticket.lvmama.com/scenic_front/scenic/asyncLoadingTicketDetail.do?"
											+ "key=%25E6%2588%2590%25E4%25BA%25BA%25E7%25A5%25A8&productId="+productId+"&suppGoodsId="+suppGoodsId+
											"&branchType="+branchType;
									
									Params para=new Params();
									para.setUrl(priceinfoFavouredpolicy);
									para.setHttpType(Param.GET);
									para.setScepriceinfo(scepriceinfo);
									para.setType(Param.LVMAMA_SCENIC_FOFAVOUREDPOLICY);
									para.setDataSource(Param.LVMAMA);
									para.setDestinationName("lowQueue");
									produceService.sendMessage(lowQueue,para);
									
								}
								/*String scepriceinfoObject = KafkaUtils.parseJsonObject(scepriceinfo, 4,1);
								String message5 = downLoadService.sendPost(KafkaUtils.KAFKAURL, scepriceinfoObject);

								jedis.insertAndGetId(scepriceinfo);*/

							}
						}
					}
					
					//放入评论链接
					parseLvmamaScenicComment(params);
					
					
				}else{
					produceService.sendMessage(highQueue, params);
				}
			}else{
				produceService.sendMessage(highQueue,params);
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("解析出错，放回队列");
			produceService.sendMessage(highQueue,params);
		}
	}

	/**
	 * 
	 * @Description 解析驴妈妈门票首页链接获取分页链接
	 * @author 赵乐
	 * @date 2018年1月19日 上午9:17:15
	 * @action parseLvmamaScenicFirst
	 * @param @param params
	 * @return void
	 */
	public void parseLvmamaScenicFirst(Params params) {
		// TODO Auto-generated method stub
		//二级分页url
		String pageUrl=params.getUrl();
		String cityName=pageUrl.substring(pageUrl.indexOf("keyword=")+8,pageUrl.lastIndexOf("&"));
		try {
			cityName = URLDecoder.decode(cityName, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		//获取document
		String content = params.getContent();
		Document doc = Jsoup.parse(content);
		//获取下一页按钮链接url,即分页url
		Elements nextPageEle = doc.select("div.paging.orangestyle>div.pagebox>a.nextpage");
		for (Element element : nextPageEle) {
			//将驴妈妈门票下一页url信息存入队列
			String nextHref = element.attr("href");
			Params para=new Params();
			para.setUrl(nextHref);
			para.setHttpType(Param.GET);
			para.setType(Param.LVMAMA_SCENIC_FIRST);
			para.setDataSource(Param.LVMAMA);
			para.setDestinationName("topQueue");
			produceService.sendMessage(topQueue,para);
		}
		//获取门票的部分信息
		try {
			// 取到产品模块element对象集合
			Elements products = doc.select("div.product-regular.clearfix");
			for (Element element : products) {
				// 名称
				Elements elements = element.select(
						"div.product-section>h3.product-ticket-title>a.name");
				if (!elements.isEmpty()) {
					String name = elements.first().text();
					// a链接
					String href = elements.first().attr("href");

					//存入url用于去重
					String string = jedis.get(href);
					if(StringUtils.isNotBlank(string)){
						continue;
					}else{
						jedis.set(href, href);
					}
					//存放种子url
					Params param=new Params();
					param.setUrl(href);
					param.setParentUrl(pageUrl);
					param.setCityName(cityName);
					param.setHttpType(Param.GET);
					param.setType(Param.LVMAMA_SCENIC_DETAIL);
					param.setDataSource(Param.LVMAMA);

					//设置景点基础信息对象 存入部分详细信息
					Sceinfo sceInfo=new Sceinfo();
					sceInfo.setCreatedate(new Date());
					sceInfo.setName(name);

					// 左侧模块对象集合
					Elements informationElements = element.select("div.product-section>dl.product-details.clearfix");
					if (!informationElements.isEmpty()) {
						for (Element infoLeft : informationElements) {
							String leftKey = infoLeft.select("dt").text();// dt标签中的值
							String leftValue = infoLeft.select("dd").text();// dd标签中的值
							switch (leftKey) {
							case "景点地址":
								// 景点地址
								String address = leftValue;
								sceInfo.setAddress(address);
								break;
							case "营业时间":
								// 开放时间
								String openTime = leftValue;
								sceInfo.setOpentime(openTime);
								break;
							case "主题":
								// 景点类型
								String type = leftValue;
								sceInfo.setType(type);
								break;
							case "景点特色":
								// 景点介绍
								String introduction = leftValue;
								sceInfo.setIntroduction(introduction);
								break;
							default:
								break;
							}
						}
					}
					param.setSceinfo(sceInfo);
					param.setDestinationName("highQueue");

					produceService.sendMessage(highQueue,param);
				}
			}
		} catch (Exception e) {
			produceService.sendMessage(topQueue,params);
			System.out.println("解析出错");
		}

	}
	/**
	 * 
	 * @Description 解析驴妈妈评论分页url获取评论信息
	 * @author 赵乐
	 * @date 2018年1月11日 下午2:13:00
	 * @action parseLvmamaStrokeComment
	 * @param @param params
	 * @return void
	 */
	public void parseLvmamaStrokeComment(Params params) {
		// TODO Auto-generated method stub
		String uuid=params.getUuid();
		String content =params.getContent();
		String cityName = params.getCityName();
		//下面的解析是app端的数据
		if (StringUtils.isNotBlank(content)) {
			try {
				JSONObject jsonObject = new JSONObject(content);
				JSONObject data=jsonObject.getJSONObject("data");

				//格式变化
				JSONArray jsonArray = data.getJSONArray("list");
				if(jsonArray.length()>0){

					for(int i=0;i<jsonArray.length();i++){

						Commentinfo commentinfo=new Commentinfo();
						String uuId=UUID.randomUUID().toString();
						commentinfo.setId(uuId);
						commentinfo.setInfoid(uuid);
						commentinfo.setType(1);
						commentinfo.setDatasource("Lvmama");
						commentinfo.setAdminarea("中国,"+cityName);
						commentinfo.setContent(jsonArray.get(i).toString());
						commentinfo.setCreatedate(new Date());
						commentinfo.setCreator("赵乐");
						commentinfo.setCreatorid("15736708180");
						
						String urlParams = KafkaUtils.parseJsonObject(commentinfo, 11,1);
						String sendPost = downLoadService.sendPost(KafkaUtils.KAFKAURL, urlParams);
						//实例化
						jedis.insertAndGetId(commentinfo);
					}
				}else{
					produceService.sendMessage(params.getDestinationName(), params);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("未获取到评论信息，重新放入队列");
		}
		//下面的解析网页端的数据，暂时不用
		/*try {
			JSONObject jsonObject = new JSONObject(content);
			JSONObject jsonObjectData = jsonObject.getJSONObject("data");
			String html=null;
			try {
				html = jsonObjectData.getString("html");
			} catch (JSONException e) {
				html=null;
				System.err.println("当前评论内容为空的url为："+params.getUrl());
				System.out.println("当前内容错误的个数："+OtherSpiderStart.count++);
				OtherSpiderStart.queue.add(OtherSpiderStart.OTHER_MISSION_KEY, params);
			}
			if(html!=null){
				Document doc = Jsoup.parse(html);
				Elements elements = doc.select("div.comment-li");
				String uuId=params.getUuid();
				System.out.println("当前评论url为："+params.getUrl());
				for (Element element : elements) {
					Commentinfo commentinfo = new Commentinfo();
					String comment=element.toString();
					System.out.println("评论内容"+element.text());
					String id=UUID.randomUUID().toString();
					commentinfo.setId(id);
					commentinfo.setInfoid(uuId);
					commentinfo.setContent(comment);
					commentinfo.setCreatedate(new Date());
					commentinfo.setCreator("赵乐");
					commentinfo.setCreatorid("15736708180");
					redisService.insertAndGetId(commentinfo);
				}
			}
		} catch (JSONException e) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}*/
	}

	/**
	 * 
	 * @Description 解析驴妈妈评论的url获取评论的分页url
	 * @author 赵乐
	 * @date 2018年1月11日 下午2:06:48
	 * @action parseLvmamaStrokeToCommentPageUrl
	 * @param @param params
	 * @return void
	 */
	public void parseLvmamaStrokeToCommentPageUrl(Params params) {
		// TODO Auto-generated method stub
		String content = params.getContent();
		String uuId=params.getUuid();
		String url = params.getUrl();
		Sceinfo sceinfo = params.getSceinfo();
		String cityName = params.getCityName();
		String dest_id=url.substring(url.indexOf("d_id=")+5,url.length());
		if (StringUtils.isNotBlank(content)) {
			try {
				JSONObject jsonObject = new JSONObject(content);
				System.out.println("strokeCommentUrl---"+jsonObject);
				if(jsonObject!=null){
					//获取评分和评论人数
					String score="";
					JSONArray jsonArray=null;
					try {
						jsonArray = jsonObject.getJSONArray("com_dimen");
					} catch (JSONException e) {
						jsonArray=null;
					}
					if(jsonArray!=null && jsonArray.length()==5){
						JSONObject jsonObjectCom_dimen = jsonArray.getJSONObject(4);
						//评分
						score= jsonObjectCom_dimen.get("formatAvgScore").toString();
						//评论人数
					}
					Integer totalNum = jsonObject.getInt("count_pub");
					//设置params中sceinfo的评分和评论人数
					
					sceinfo.setGrade(score);
					sceinfo.setGradenum(totalNum+"");
					sceinfo.setCreatedate(new Date());
					sceinfo.setCreator("赵乐");
					sceinfo.setDatasource("Lvmama");
					sceinfo.setCreatorid("15736708180");
					String urlParams = KafkaUtils.parseJsonObject(sceinfo, 5,1);
					String sendPost = downLoadService.sendPost(KafkaUtils.KAFKAURL, urlParams);

					jedis.insertAndGetId(sceinfo);

					//评论总页数
					System.err.println("评论总条数-----"+totalNum);
					//网页端每页请求10条，app端每页请求500条数据
					Integer pageNum=totalNum%500==0?totalNum/500:(totalNum/500)+1;
					System.out.println(pageNum);
					for (int i=1;i<=pageNum;i++) {
						//下面的是app端的评论url数据
						String commentPageUrl="https://m.lvmama.com/other/router/rest.do?method=api.com.cmt.getCmtCommentList"
								+ "&version=2.0.0&currentPage="+i+"&isELong=N&pageSize=500&placeId="+dest_id+"&firstChannel=TOUCH&secondChannel=LVMM&iuf=1513238444478511647";
						Params para=new Params();

						System.out.println(commentPageUrl+"评论分页url");
						para.setType(Param.LVMAMA_STROKE_COMMENTPAGE);
						para.setDataSource(Param.LVMAMA);
						para.setUrl(commentPageUrl);
						para.setHeader("signal", "ab4494b2-f532-4f99-b57e-7ca121a137ca");
						para.setUuid(uuId);
						para.setCityName(cityName);
						para.setHttpType(Param.GET);
						para.setDestinationName("lowQueue");

						//下面的为网页端的评论url数据，暂时不用
						/*
						String commentPageUrl="http://www.lvmama.com/lvyou/home/ajaxGetCommentPage?page="+i+"&list_type=CommentBestNew&type=N&dest_id="+dest_id;

						Params para=new Params();
						System.out.println(commentPageUrl+"评论分页url");
						para.setType(Param.LVMAMA_STROKE_COMMENTPAGE);
						para.setDataSource(Param.LVMAMA);
						para.setUrl(commentPageUrl);
						para.setUuid(uuId);
						para.setHttpType(Param.GET);*/
						//加入消息队列
						System.out.println("把评论分页url加入reids");
						produceService.sendMessage(lowQueue,para);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("未获取到信息，重新放入队列");
		}
	}

	/**
	 * 
	 * @Description 解析驴妈妈景点想去人数和去过人数url获取信息
	 * @author 赵乐
	 * @date 2018年1月11日 下午2:00:44
	 * @action parseLvmamaStrokeWantAndBeen
	 * @param 
	 * @return void
	 */
	public void parseLvmamaStrokeWantAndBeen(Params params) {
		// TODO Auto-generated method stub
		String uuId=params.getUuid();
		//详情页链接
		String parentUrl = params.getParentUrl();
		String content=params.getContent();
		Sceinfo sceinfo = params.getSceinfo();
		if (StringUtils.isNotBlank(content)) {
			try {
				//去过人数
				String count_been="";
				//想去人数
				String count_want="";
				JSONObject jsonObject = new JSONObject(content);
				JSONObject jsonObjectData = jsonObject.getJSONObject("data");
				if(jsonObjectData!=null){
					count_been = jsonObjectData.getString("count_been");
					count_want = jsonObjectData.getString("count_want");
				}
				System.out.println(count_been+"----"+count_want);
				//设置params中sceinfo的想去人数和去过人数的值
				sceinfo.setWanttonum(count_want);
				sceinfo.setBeennum(count_been);
				sceinfo.setCreatedate(new Date());
				sceinfo.setCreator("赵乐");
				sceinfo.setCreatorid("15736708180");
				
				//放入评论链接，请求评论人数
				
				String cityName = params.getCityName();
				String dest_id = parentUrl.substring(parentUrl.indexOf("-") + 1, parentUrl.lastIndexOf("."));
				String commentUrl="http://www.lvmama.com/lvyou/ajax/ajaxGetComDataNew?d_id="+dest_id;

				Params para=new Params();
				para.setType(Param.LVMAMA_STROKE_COMMENT);
				para.setDataSource(Param.LVMAMA);
				para.setCityName(cityName);
				para.setUrl(commentUrl);
				para.setUuid(uuId);
				para.setSceinfo(sceinfo);
				para.setHttpType(Param.GET);
				para.setDestinationName("lowQueue");
				produceService.sendMessage(lowQueue,para);

			} catch (JSONException e) {
				e.printStackTrace();

			}
		} else {
			System.out.println("未获取到信息，重新放入队列");
		}
	}
	/**
	 * 
	 * @Description 解析lvmama景点想去人数和去过人数
	 * @author 赵乐
	 * @date 2018年1月11日 下午1:45:34
	 * @action parseLvmamaWantToURL
	 * @param @param params
	 * @return void
	 */
	/*public void parseLvmamaToWantAndBeenURL(Params params) {
		// TODO Auto-generated method stub
		String uuid = params.getUuid();
		String url=params.getUrl();
		String dest_id = url.substring(url.indexOf("-") + 1, url.lastIndexOf("."));
		String wantoURL="http://www.lvmama.com/lvyou/ajax/AjaxGetCount?dest_type=VIEWSPOT&dest_id="+dest_id;

		Params para=new Params();
		para.setType(Param.LVMAMA_STROKE_WANTANDBEEN);
		para.setDataSource(Param.LVMAMA);
		para.setUrl(wantoURL);
		para.setUuid(uuid);
		para.setHttpType(Param.GET);
		para.setDestinationName("lowQueue");
		//加入消息队列
		produceService.sendMessage(lowQueue,para);
	}*/
	/**
	 * 
	 * @Description TODO
	 * @author 赵乐
	 * @date 2018年1月11日 上午11:14:32
	 * @action parseLvmamaStrokeCommentUrl
	 * @param @param params
	 * @return void
	 */
	/*public void parseLvmamaStrokeToCommentUrl(Params params) {
		// TODO Auto-generated method stub
		String uuid=params.getUuid();
		String url=params.getUrl();
		String cityName = params.getCityName();
		String dest_id = url.substring(url.indexOf("-") + 1, url.lastIndexOf("."));
		String commentUrl="http://www.lvmama.com/lvyou/ajax/ajaxGetComDataNew?d_id="+dest_id;

		Params para=new Params();
		para.setType(Param.LVMAMA_STROKE_COMMENT);
		para.setDataSource(Param.LVMAMA);
		para.setCityName(cityName);
		para.setUrl(commentUrl);
		para.setUuid(uuid);
		para.setHttpType(Param.GET);
		para.setDestinationName("lowQueue");
		produceService.sendMessage(lowQueue,para);
	}*/

	/**
	 * 
	 * @Description 解析驴妈妈景点的详情链接
	 * @author 赵乐
	 * @date 2018年1月11日 上午11:46:54
	 * @action parseLvmamaStrokeDetail
	 * @param @param params
	 * @return void
	 */
	public void parseLvmamaStrokeDetail(Params params) {
		// TODO Auto-generated method stub
		//新建uuid
		String uuid= params.getUuid();
		//城市名称
		String cityName = params.getCityName();
		String url = params.getUrl();

		Document document=Jsoup.parse(params.getContent());
		// 景点基础信息对象
		Sceinfo sceinfo = new Sceinfo();
		sceinfo.setId(uuid);
		sceinfo.setUrlid(url);
		sceinfo.setType("1");
		sceinfo.setDatasource("驴妈妈");
		sceinfo.setCreatedate(new Date());
		sceinfo.setCreator("赵乐");
		sceinfo.setCreatorid("15736708180");
		//存放四级地址表信息
		Addressinfo addressinfo=new Addressinfo();
		addressinfo.setInfoid(uuid);
		try {
			// 景点名称
			Elements select = document.select("div.dz_state_model>div.vtop-name-box>h2");
			String Name = select.isEmpty()?"":select.text();

			Elements selectType = document.select("div.dz_state_model>div.vtop-name-box>i.vcomon-icon");
			String type="";
			if(!selectType.isEmpty()){
				type=selectType.text();
			}

			// 景点地址
			Elements elsementdt = document.select(
					"div.vbottom-box.clearfix>div.vtop-comment-box.fl>dl.vtop-address-box.vtop-height.clearfix>dt");
			String Address = "";
			String OpenTime = "";
			String AdviceTime = "";
			String TicketInformation = "";
			if (!elsementdt.isEmpty()) {
				for (Element element : elsementdt) {
					switch (element.text().trim()) {
					case "地　　址：":
						Address = element.nextElementSibling().text();
						Address = Address.substring(0, Address.length() - 4);
						break;
						// 景点开放时间描述
					case "开放时间：":
						OpenTime = element.nextElementSibling().text();
						System.out.println(OpenTime);
						break;
						// 建议游玩时间描述
					case "游玩时间：":
						AdviceTime = element.nextElementSibling().text().trim();
						System.out.println(AdviceTime);
						break;
						// 其他信息，存其他有用信息
					case "门票说明：":
						TicketInformation = element.nextElementSibling().text();
						System.out.println(TicketInformation);
						break;
					default:
						break;
					}
				}
			}
			// 景点介绍(标签现在取不到了)
			Elements select2 = document
					.select("div#common>div.mainList.poi_heightbox.js-content>dl.poi_bordernone>dd>p>font");
			String introduction = "";
			if (!select2.isEmpty()) {
				introduction = select2.text().trim();
			}
			String html = document.html();
			Integer indexOfcoordinate = html.indexOf("coordinate:");
			Integer indexOfzoom = html.indexOf("zoom:");
			// 景点经度
			String Longitude = "";
			// 景点纬度
			String Latitude = "";

			if (indexOfcoordinate != -1 && indexOfzoom != -1) {
				try {
					String coordinate = html.substring(indexOfcoordinate + 14, indexOfzoom - 15);
					int lag = coordinate.indexOf("lng: ");
					int lat = coordinate.indexOf(", lat: ");
					Longitude = coordinate.substring(lag + 6, lat - 1);
					Latitude = coordinate.substring(lat + 8, coordinate.length() - 2);
				} catch (Exception e) {
				}
			}
			System.out.println("经纬度" + Longitude + Latitude);
			// 景点参考价格
			String ReferPrice = "";
			Elements elements = document.select("div.dz_state_model>div.vtop-name-box>span.price-box.fr>em.big-price");
			if (elements.size() > 0) {
				ReferPrice = elements.get(0).text();
			}
			System.out.println(ReferPrice);
			// 插入景点基础表信息
			sceinfo.setName(Name);
			sceinfo.setType(type);
			sceinfo.setAddress(Address);
			sceinfo.setIntroduction(introduction);
			sceinfo.setLongitude(Longitude);
			sceinfo.setLatitude(Latitude);
			sceinfo.setAdvicetime(AdviceTime);
			sceinfo.setOpentime(OpenTime);
			sceinfo.setReferprice(ReferPrice);
			//新增门票信息
			sceinfo.setTicketinformation(TicketInformation);
			sceinfo.setAdminarea("中国,"+cityName);
			
			sceinfo.setDatatype("1");
			sceinfo.setDatasource("Lvmama");
			sceinfo.setCreatedate(new Date());
			sceinfo.setCreator("赵乐");
			sceinfo.setCreatorid("15736708180");

			if(StringUtils.isBlank(Name)){
				//放回队
				produceService.sendMessage(params.getDestinationName(), params);
			}else{
				//插入景点
				/*String urlParams = KafkaUtils.parseJsonObject(sceinfo, 5,0);
				String sendPost = downLoadService.sendPost(KafkaUtils.KAFKAURL, urlParams);
				String insertAndGetId = jedis.insertAndGetId(sceinfo);*/

				//添加四级表信息
				Elements elementsAddressList = document.select("span.crumbs_nav>span.crumbs_list>a.crumbs_down_a");
				if(elementsAddressList.size()!=0){
					Integer index=0;
					String uurid = UUID.randomUUID().toString();
					if("上海".equals(cityName)){
						for(int i=0;i<elementsAddressList.size();i++){
							String adressstr=elementsAddressList.get(i).text();
							if(adressstr.equals(cityName)){
								index=i;
								break;
							}
						}
						addressinfo.setId(uurid);
						addressinfo.setType(1);
						addressinfo.setCountry(elementsAddressList.get(index-1).text());;
						addressinfo.setProvince(cityName);
						addressinfo.setCity(cityName);
						addressinfo.setDetailaddress(Address);
						addressinfo.setCreatedate(new Date());
						addressinfo.setCreator("赵乐");
						addressinfo.setCreatorid("15736708180");
						String addressinfoObject = KafkaUtils.parseJsonObject(addressinfo, 12,1);
						String sendPost2 = downLoadService.sendPost(KafkaUtils.KAFKAURL, addressinfoObject);

						jedis.insertAndGetId(addressinfo);

					}if("海南".equals(cityName)){
						for(int i=1;i<elementsAddressList.size();i++){
							String adressstr=elementsAddressList.get(i).text();
							if(adressstr.equals(cityName)){
								index=i;
								break;
							}
						}
						addressinfo.setId(uurid);
						addressinfo.setType(1);
						addressinfo.setCountry(elementsAddressList.get(index-1).text());
						addressinfo.setProvince(cityName);
						addressinfo.setCity(elementsAddressList.get(index+1).text());
						addressinfo.setDetailaddress(Address);
						addressinfo.setCreatedate(new Date());
						addressinfo.setCreator("赵乐");
						addressinfo.setDatasource("Lvmama");
						addressinfo.setCreatorid("15736708180");

						String addressinfoObject = KafkaUtils.parseJsonObject(addressinfo, 12,1);
						String sendPost2 = downLoadService.sendPost(KafkaUtils.KAFKAURL, addressinfoObject);

						jedis.insertAndGetId(addressinfo);
						System.out.println(addressinfo+"放入四级地址表信息");
					}
				}
				//获取图片信息
				Elements elementImgUrls = document.select("div.dz_state_model>div.vbottom-box.clearfix>div.vtop-banner.fl>ul.vtop-banner-big-list>li>a>img");
				System.out.println(elementImgUrls+"图片elementImgUrls");

				if(!elementImgUrls.isEmpty()){
					System.out.println(elementImgUrls.size()+"图片的个数");
					for(int i=0;i<elementImgUrls.size();i++){
						String imgurl=elementImgUrls.get(i).attr("src");
						Pictureinfo pictureinfo=new Pictureinfo();
						pictureinfo.setInfoid(uuid);
						String uurid = UUID.randomUUID().toString();
						pictureinfo.setId(uurid);
						pictureinfo.setImgurl(imgurl);
						pictureinfo.setType(1);
						pictureinfo.setDownload(0);
						pictureinfo.setSort(i);
						pictureinfo.setAdminarea("中国,"+cityName);
						pictureinfo.setCreatedate(new Date());
						pictureinfo.setCreator("赵乐");
						pictureinfo.setDatasource("Lvmama");
						pictureinfo.setCreatorid("15736708180");

						String pictureinfoObject = KafkaUtils.parseJsonObject(pictureinfo, 10,1);
						String sendPost2 = downLoadService.sendPost(KafkaUtils.KAFKAURL, pictureinfoObject);


						jedis.insertAndGetId(pictureinfo);
					}
				}
				//解析驴妈妈景点想去和去过人数
				//parseLvmamaToWantAndBeenURL(params);
				
				//驴妈妈景点评论
				//parseLvmamaStrokeToCommentUrl(params);
				
				String dest_id = url.substring(url.indexOf("-") + 1, url.lastIndexOf("."));
				String wantoURL="http://www.lvmama.com/lvyou/ajax/AjaxGetCount?dest_type=VIEWSPOT&dest_id="+dest_id;

				Params para=new Params();
				para.setType(Param.LVMAMA_STROKE_WANTANDBEEN);
				para.setDataSource(Param.LVMAMA);
				para.setUrl(wantoURL);
				para.setParentUrl(url);
				para.setUuid(uuid);
				para.setSceinfo(sceinfo);
				para.setHttpType(Param.GET);
				para.setDestinationName("lowQueue");
				//加入消息队列
				produceService.sendMessage(lowQueue,para);
				
				
			}
		} catch (Exception e) {
			produceService.sendMessage(params.getDestinationName(), params);
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @Description 解析驴妈妈景点的分页链接
	 * @author 赵乐
	 * @date 2018年1月11日 上午11:29:44
	 * @action parseLvmamaStrokePage
	 * @param @param params
	 * @return void
	 */
	public void parseLvmamaStrokePage(Params params) {
		// TODO Auto-generated method stub
		//获取父类url
		String parentUrl = params.getParentUrl();
		//获取二级分页url

		String cityName = params.getCityName();
		String pageUrl = params.getUrl();

		String dest_id=pageUrl.substring(pageUrl.indexOf("dest_id")+8,pageUrl.indexOf("&base_id="));

		//json格式的请求结果，不能直接解析
		JSONObject jsonObject = null;
		try {
			jsonObject = new JSONObject(params.getContent());
			String stringdata = jsonObject.getString("data");
			//获取document
			Document doc = Jsoup.parse(stringdata); 
			Elements elements = doc.select("dl.ticket_price.line_price.com_dest_2>dt>a");
			//遍历景点部分信息
			for(Element element:elements){
				Params para=new Params();
				String url=element.attr("href");
				System.out.println(url+"----url链接");

				//存入url用于去重
				String string = jedis.get(url);
				if(StringUtils.isNotBlank(string)){
					continue;
				}else{
					jedis.set(url, url);
				}

				para.setUrl(url);
				para.setParentUrl(pageUrl);
				para.setCityName(cityName);
				para.setHttpType(Param.GET);
				para.setDataSource(Param.LVMAMA);
				para.setType(Param.LVMAMA_STROKE_DETAIL);
				para.setDestinationName("highQueue");
				//放入到activeMQ中
				produceService.sendMessage(highQueue,para);

			}
		} catch (Exception e) {
			produceService.sendMessage(params.getDestinationName(),params);

		}

	}
	/**
	 * 
	 * @Description 解析驴妈妈种子链接的首个url
	 * @author 赵乐
	 * @date 2018年1月11日 上午11:15:19
	 * @action parseLvmamaStrokeFirst
	 * @param @param params
	 * @return void
	 */
	public void parseLvmamaStrokeFirst(Params params) {
		String url = params.getUrl();
		String cityName = params.getCityName();
		String content = params.getContent();
		if(StringUtils.isNotBlank(content)){
			try {
				//获取document
				Document document = Jsoup.parse(content);
				//获取总条数
				Elements totalNumEle = document.select("span#total_num");
				//获取dest_id和base_id,拼接分页pageUrl
				Integer dest_id=NumUtils.getInteger(url);

				String htmlstr = document.select("script").html();
				String base_idstr =htmlstr.substring(htmlstr.lastIndexOf("base_id"),htmlstr.indexOf("request_uri"));
				Integer bsas_id = NumUtils.getInteger(base_idstr);
				//遍历分页pageUrl
				for (Element element : totalNumEle) {
					Integer totalNum=Integer.parseInt(element.text());
					Integer pageNum=totalNum%10==0?totalNum/10:(totalNum/10)+1;
					for(int i=1;i<=pageNum;i++){
						String pageUrl="http://www.lvmama.com/lvyou/ajax/getNewViewList?page_num="+i
								+"&dest_id="+dest_id+"&base_id="+bsas_id;
						//调用activeMQ消息机制
						Params para=new Params();
						para.setUrl(pageUrl);
						para.setParentUrl(url);
						para.setCityName(cityName);
						para.setType(Param.LVMAMA_STROKE_PAGE);
						para.setHttpType(Param.GET);
						para.setDataSource(Param.LVMAMA);
						para.setDestinationName("topQueue");
						produceService.sendMessage(topQueue,para); 
					}
				}
			} catch (Exception e) {
				produceService.sendMessage(topQueue, params);
			}
		}else {
			produceService.sendMessage(topQueue, params);
		}
	}



}
