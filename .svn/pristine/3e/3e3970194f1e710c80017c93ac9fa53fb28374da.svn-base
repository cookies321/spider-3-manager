package cn.jj.service;

import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
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
public class ParseCtrip {

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
	 * @Description 解析行程酒店房型价格
	 * @author 赵乐
	 * @date 2018年3月6日 下午5:00:46
	 * @action parseCtripHotelRoom
	 * @param @param params
	 * @return void
	 */
	public void parseCtripHotelRoom(Params params) {
		// TODO Auto-generated method stub
		String url = params.getUrl();
		String cityName = params.getCityName();
		String priceDate=url.substring(url.indexOf("startDate")+10, url.indexOf("depDate")-1);
		String str=params.getContent();
		String uuId=params.getUuid();
		String roomHtml="";
		int count=0;
		try {
			System.out.println(params.getUrl());
			str=URLDecoder.decode(str.replace("%", "%25"), "UTF-8");
			JSONObject jsonObject=new JSONObject(str);
			roomHtml=jsonObject.getString("html");
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			Document roomDoc=Jsoup.parse(roomHtml);
			Elements tbody=roomDoc.select("table#J_RoomListTbl>tbody");
			for(Element element1:tbody){
				//去除没用的tr
				Elements tr=element1.select("tr");
				for(int i=0;i<tr.size();i++){
					if(tr.get(i).text().contains("展开全部")||tr.get(i).attr("class").contains("all-order")){
						tr.remove(i);
					}
				}
				System.out.println("该酒店价格长度为："+tr.size());
				//获取房型信息
				for(int i=2;i<tr.size();++i){
					String room_type="";
					String bedtype="";
					String floor="";
					String bedSize="";
					String peopleCount="";
					String roomPic="";
					String addBed="";
					String hasWindow="";
					int isaddBed=0;
					//找出相同的brid的元素
					String sameBrid=tr.get(i).attr("brid");
					//System.out.println(sameBrid);
					if(StringUtils.isBlank(sameBrid)){
						try{
							sameBrid=tr.get(i+1).attr("brid");
						}catch(Exception e){
							sameBrid="";
						}
					}
					//获取相同的房型，去除tr中的没用的一行
					Elements sametr=element1.select("tr[brid="+sameBrid+"]");
					for(int k=0;k<sametr.size();k++){
						if(sametr.get(k).text().contains("展开全部")||sametr.get(k).attr("class").contains("all-order")){
							sametr.remove(k);
						}
					}
					Element roomDetails=tr.get(i+sametr.size());
					Elements es1=roomDetails.select("div.hrd-info");
					if(es1.size()>0){
						Elements details=roomDetails.select("div.hrd-info>div.hrd-info-base>ul.hrd-info-base-list>li");
						for (Element element : details) {
							String text = element.text();
							if(text.contains("楼层")){
								floor=text;
							}else if(text.contains("床型")){
								bedSize=text;
							}else if(text.contains("窗")){
								hasWindow=text;
							}else if(text.contains("加床")){
								addBed=text;
							}
						}
						/*floor=details.get(1).text();
						try{
							bedSize=details.get(2).text();
						}catch(Exception e){
							bedSize="";
						}
						if(details.size()>3){
							addBed=details.get(3).text();
						}else{
							addBed="无";
						}
						if(!addBed.contains("不可加床")){
							isaddBed=1;
						}*/
					}else{
						Elements details=roomDetails.select("tr.clicked>td>div.searchresult_caption.basefix>div.searchresult_caplist_box>ul.searchresult_caplist");
						if(details.size()>0){
							details =details.get(0).select("li");
						}
						for (Element element : details) {
							String text = element.text();
							if(text.contains("楼层")){
								floor=text;
							}else if(text.contains("床型")){
								bedSize=text;
							}else if(text.contains("窗")){
								hasWindow=text;
							}else if(text.contains("加床")){
								addBed=text;
							}
						}

						/*floor=details.get(1).text();
						try{
							bedSize=details.get(2).text();
						}catch(Exception e){
							bedSize="";
						}
						if(details.size()>3){
							addBed=details.get(3).text();
						}else{
							addBed="无";
						}
						if(!addBed.contains("不可加床")){
							isaddBed=1;
						}*/
					}
					Roombasicinfo roomInfo=new Roombasicinfo();
					String roomInfoId=UUID.randomUUID().toString();

					//开始获取每种产品的价格信息
					for(int j=0;j<sametr.size();j++){
						String price="";
						String productName="";
						String breakfast="";
						String wifi="";
						String cancle="";
						String isbooking="";
						String payMethod="";
						//获取每一个相同tr下的所有td,共有9列
						Elements sametd=sametr.get(j).select("td");
						//第一列特殊处理
						if(j==0){
							//获取第一个td列里面的a
							Elements td1=sametd.get(0).select("a");
							room_type=td1.get(1).ownText();
							roomPic=td1.get(0).select("img").attr("_src");
							//获取第二个td下第一个子元素的值
							Elements td2=sametd.get(1).select("span");
							productName=td2.get(0).text();
							if(StringUtils.isBlank(productName)){
								productName=td2.attr("style").substring(td2.attr("style").indexOf("(")+1, td2.attr("style").indexOf(")"));
							}
							//获取第二个td下文本值
							bedtype=sametd.get(2).text();
							breakfast=sametd.get(3).text();
							wifi=sametd.get(4).text();
							String peopleTitle=sametd.get(5).select("span").get(0).attr("title");
							if(StringUtils.isNotBlank(peopleTitle)){
								peopleCount=NumUtils.getInteger(peopleTitle).toString();
							}else{
								peopleCount="0";
							}
							Elements td5=sametd.get(6).select("span");
							cancle=td5.get(0).text();
							Elements td6=sametd.get(7).select("p");
							if(!td6.isEmpty()){
								try {
									price=NumUtils.getInteger(td6.get(1).text()).toString();
								} catch (Exception e) {

								}
							}
							Elements td7=sametd.get(8).select("div.book_type>a.J_hotel_order");
							if(td7.size()<=0){
								td7=sametd.get(8).select("div.book_type>a.btns_base22.btns_base22_dis");
								isbooking="订完";
							}else{
								isbooking="预定";
							}
						}
						if(j>=1){
							Elements td2=sametd.get(0).select("span");
							productName=td2.get(0).text();
							if(StringUtils.isBlank(productName)){
								productName=td2.attr("style").substring(td2.attr("style").indexOf("(")+1, td2.attr("style").indexOf(")"));
							}
							//获取第二个td下文本值
							bedtype=sametd.get(1).text();
							breakfast=sametd.get(2).text();
							wifi=sametd.get(3).text();
							if(sametd.get(4).select("span").size()>0){
								String peopleTitle=sametd.get(4).select("span").get(0).attr("title");
								peopleCount=NumUtils.getInteger(peopleTitle).toString();
							}else{
								peopleCount="0";
							}
							Elements td5=sametd.get(5).select("span");
							cancle=td5.get(0).text();
							Elements td6=sametd.get(6).select("p");
							if(!td6.isEmpty()){
								try {
									price=NumUtils.getInteger(td6.get(1).text()).toString();
								} catch (Exception e) {

								}
							}
							//System.out.println(price);
							Elements td7=sametd.get(7).select("div.book_type>a.J_hotel_order");
							if(td7.size()<=0){
								td7=sametd.get(7).select("div.book_type>a.btns_base22.btns_base22_dis");
								isbooking="订完";
							}else{
								isbooking="预定";
								Elements payMethodEle=sametd.get(7).select("div.btns_base22_skin01>span.payment_txt");
								if(payMethodEle.isEmpty()){
									payMethodEle=sametd.get(7).select("div.btns_base22_skin04>span.payment_txt");
								}
								payMethod=payMethodEle.text();
							}
						}

						//酒店价格
						Roomprice roomPrice=new Roomprice();
						String roomPriceId=UUID.randomUUID().toString();
						roomPrice.setId(roomPriceId);
						roomPrice.setHotelid(uuId);
						roomPrice.setRoomid(roomInfoId);
						roomPrice.setProductname(productName);
						roomPrice.setIscancled(cancle);
						roomPrice.setIshasbreakfast(breakfast);
						roomPrice.setIswifi(wifi);
						roomPrice.setPrice(price);
						roomPrice.setIsbooking(isbooking);
						roomPrice.setDate(priceDate);
						roomPrice.setPaymethod(payMethod);
						roomPrice.setAdminarea("中国,"+cityName);
						roomPrice.setCreatedate(new Date());
						roomPrice.setCreator("tyl");
						roomPrice.setCreatorid("tyl13564205515");
						roomPrice.setDatasource("Ctrip");
						String roomPriceObject = KafkaUtils.parseJsonObject(roomPrice, 2,1);
						String sendPost3 = downLoadService.sendPost(KafkaUtils.KAFKAURL, roomPriceObject);

						//System.out.println("插入房型价格信息，产品名称："+productName+";价格："+price);
						jedis.insertAndGetId(roomPrice);
						//System.out.println("产品名称："+productName+";是否可取消："+cancle+";是否含早餐:"+breakfast+";是否有wifi："+wifi+";价格："+price);
						//记住当前的tr值，下次从当前位置开始
						++i;
					}
					//酒店房型
					roomInfo.setId(roomInfoId);
					roomInfo.setHotelid(uuId);
					roomInfo.setBedtype(bedtype);
					roomInfo.setBedsize(bedSize);
					roomInfo.setFloor(floor);
					roomInfo.setIsaddbed(addBed);
					roomInfo.setRoomtype(room_type);
					roomInfo.setPeoplecount(Integer.valueOf(peopleCount).toString());
					roomInfo.setAdminarea("中国,"+cityName);
					roomInfo.setCreatedate(new Date());
					roomInfo.setCreator("tyl");
					roomInfo.setDatasource("Ctrip");
					roomInfo.setCreatorid("tyl13564205515");

					String roomInfoObject = KafkaUtils.parseJsonObject(roomInfo, 3,1);
					String sendPost3 = downLoadService.sendPost(KafkaUtils.KAFKAURL, roomInfoObject);
					jedis.insertAndGetId(roomInfo);
					/*System.out.println("房间名称："+room_type+";床的类型："+bedtype+";床的大小："+bedSize+";"
					+ "楼层："+floor+";是否可加床："+isaddBed+";可住人数："+peopleCount);*/
					//获取图片
					if(StringUtils.isNotBlank(roomPic)){
						Pictureinfo pictureInfo=new Pictureinfo();
						pictureInfo.setId(UUID.randomUUID().toString());
						pictureInfo.setInfoid(roomInfoId);
						pictureInfo.setImgurl(roomPic);
						pictureInfo.setSort(count++);
						pictureInfo.setType(5);
						pictureInfo.setDownload(0);
						pictureInfo.setAdminarea("中国,"+cityName);
						pictureInfo.setCreatedate(new Date());
						pictureInfo.setCreator("tyl");
						pictureInfo.setDatasource("Ctrip");
						pictureInfo.setCreatorid("tyl13564205515");

						String pictureInfoObject = KafkaUtils.parseJsonObject(pictureInfo, 10,1);
						String sendPost4 = downLoadService.sendPost(KafkaUtils.KAFKAURL, pictureInfoObject);
						jedis.insertAndGetId(pictureInfo);
					}			

				}
			}
		} catch (Exception e) {
			e.printStackTrace();  
			produceService.sendMessage(params.getDestinationName(), params);
		}
	}
	/**
	 * 
	 * @Description 解析行程酒店详情信息
	 * @author 赵乐
	 * @date 2018年3月6日 下午4:25:49
	 * @action parseCtripHotelDetail
	 * @param @param params
	 * @return void
	 */
	public void parseCtripHotelDetail(Params params) {
		// TODO Auto-generated method stub
		String uuId = params.getUuid();
		String url = params.getUrl();
		String content = params.getContent();
		String cityName = params.getCityName();
		Document doc=Jsoup.parse(content);
		int count=1;
		if(doc!=null){
			try{
				Elements hotelinfo1=doc.select(".special_info");
				if(hotelinfo1.size()<=0){
					hotelinfo1=doc.select("div#htlDes>p>span[itemprop=description]");
				}
				Elements hotelinfo2=doc.select("div#htlDes");
				//放入队列中,为了获取模拟浏览器获取评论
				Hotelinfo ho = new Hotelinfo();
				ho.setId(uuId);
				ho.setUrlid(url);
				produceService.sendMessage("hotelCommentQueue",ho);
				
				//通过种子链接传过来的值获取
				Hotelinfo basicInfo=params.getHotelInfo();
				basicInfo.setId(uuId);
				basicInfo.setUrlid(url);
				basicInfo.setIntroduction(hotelinfo1.text()+" "+hotelinfo2.text());
				System.out.println("酒店介绍："+hotelinfo2.text());
				basicInfo.setAdminarea("中国,"+cityName);
				basicInfo.setCreatedate(new Date());
				basicInfo.setCreator("tyl");
				basicInfo.setDatasource("Ctrip");
				basicInfo.setCreatorid(creatorID);
				
				
				String basicInfoObject = KafkaUtils.parseJsonObject(basicInfo, 1,1);
				String sendPost = downLoadService.sendPost(KafkaUtils.KAFKAURL, basicInfoObject);
				//插入详情信息
				jedis.insertAndGetId(basicInfo);
				//获取地址信息address
				
				Addressinfo addressInfo = params.getAddressinfo();
				addressInfo.setId(UUID.randomUUID().toString());
				addressInfo.setInfoid(uuId);
				addressInfo.setCreatedate(new Date());
				addressInfo.setCreator("tyl");
				addressInfo.setDatasource("Ctrip");
				addressInfo.setCreatorid(creatorID);

				String addressInfoObject = KafkaUtils.parseJsonObject(addressInfo, 12,1);
				String sendPost2 = downLoadService.sendPost(KafkaUtils.KAFKAURL, addressInfoObject);
				jedis.insertAndGetId(addressInfo);

				Elements picList=doc.select("div#topPicList>div>div");
				int picSize=0;
				if(picList.size()>10){
					picSize=10;
				}
				for(int i=0;i<picSize;i++){
					Element pic = picList.get(i);
					String picAttr="http:"+pic.attr("_src");
					if(StringUtils.isNotBlank(picAttr)){
						Pictureinfo picture=new Pictureinfo();
						String pictureId=UUID.randomUUID().toString();
						picture.setId(pictureId);
						picture.setInfoid(uuId);
						picture.setImgurl(picAttr);
						picture.setSort(count++);
						picture.setType(4);
						picture.setDownload(0);
						picture.setAdminarea("中国,"+cityName);
						picture.setCreatedate(new Date());
						picture.setCreator("tyl");
						picture.setCreatorid(creatorID);
						picture.setDatasource("Ctrip");
						String pictureObject = KafkaUtils.parseJsonObject(picture, 10,1);
						String sendPost3 = downLoadService.sendPost(KafkaUtils.KAFKAURL, pictureObject);

						jedis.insertAndGetId(picture);
					}

				}
			}catch(Exception e){
				e.printStackTrace();
			}

			String productId=url.substring(url.lastIndexOf("/")+1, url.lastIndexOf("."));
			for(int i=0;i<dateList.size()-1;i++){
				//获取酒店房型床型url
				String roomUrl="http://hotels.ctrip.com/Domestic/tool/AjaxHote1RoomListForDetai1.aspx?psid="
						+ "&MasterHotelID="+productId+"&hotel="+productId+"&EDM=F&roomId=&IncludeRoom=&city=&"
						+ "showspothotel=T&supplier=&IsDecoupleSpotHotelAndGroup=F&contrast=0&brand=0&"
						+ "startDate="+dateList.get(i)+"&depDate="+dateList.get(i+1)+"&IsFlash=F&RequestTravelMoney=F&hsids="
						+ "&IsJustConfirm=&contyped=0&priceInfo=-1&equip=&filter=&productcode=&couponList=&abForHuaZhu=&defaultLoad=T&TmFromList=F"
						+ "&eleven=a6345e7ad8ce57141b04b776ecf949c3dff39104214a542a3a808b772c5b2ded&callback=CASSzozqvcbWPNaFOux&_=1509431461626";
				Params para=new Params();
				//携程房型的url
				para.setUuid(uuId);
				para.setUrl(roomUrl);
				para.setCityName(cityName);
				para.setType(Param.CTRIP_HOTEL_ROOM);
				para.setDataSource(Param.CTRIP);
				para.setHttpType(Param.GET);
				para.setHeader("Referer", url);
				//System.out.println("房型床型url：" + roomUrl);
				para.setDestinationName("lowQueue");
				produceService.sendMessage(lowQueue,para);
			}

		}
	}
	/**
	 * 
	 * @Description 解析携程酒店分页练级获取酒店种子
	 * @author 赵乐
	 * @date 2018年3月6日 下午3:34:44
	 * @action parseCtripHotelPage
	 * @param @param params
	 * @return void
	 */
	public void parseCtripHotelPage(Params params) {
		// TODO Auto-generated method stub
		String url=params.getUrl();
		//获取下载的内容
		String content = params.getContent();
		String cityName2 = params.getCityName();
		String cityId=url.substring(url.indexOf("cityId")+7, url.indexOf("cityPY")-1);
		String cityPY=url.substring(url.indexOf("cityPY")+7, url.indexOf("cityCode")-1);
		System.out.println("cityId:"+cityId+";cityPY:"+cityPY);
		String cityName="";
		if(cityPY.contains("shanghai")){
			cityName="上海";
		}else if(cityPY.contains("sanya")){
			cityName="三亚";
		}else if(cityPY.contains("haikou")){
			cityName="海口";
		}else if(cityPY.contains("qionghai")){
			cityName="琼海";
		}else if(cityPY.contains("wenchang")){
			cityName="文昌";
		}else if(cityPY.contains("wanning")){
			cityName="万宁";
		}else if(cityPY.contains("danzhou")){
			cityName="儋州";
		}else if(cityPY.contains("dongfang")){
			cityName="东方";
		}else if(cityPY.contains("wuzhishan")){
			cityName="五指山";
		}
		try {
			//解析json中的hotelList(为html)
			String hotelList="";
			if(content.indexOf("hotelList")>0){
				hotelList=content.replace("\\", "").substring(content.indexOf("hotelList")+12, content.indexOf("paging")-2);
			} 
			Document doc=Jsoup.parse(hotelList);
			//获得最低价格
			Elements element=doc.select("span.J_price_lowList");

			content=content.substring(content.indexOf("hotelMapStreetJSON")-1, content.indexOf("biRecord")-2);
			content="{"+content+"}";
			content=content.replace("\\", "");
			JSONObject json = new JSONObject(content);
			JSONArray array=json.getJSONArray("hotelPositionJSON");
			System.out.println("该页酒店url总数为："+array.length());
			for(int j=0;j<array.length();j++){

				JSONObject obj=array.getJSONObject(j);
				String url2=obj.get("url").toString().substring(0,obj.get("url").toString().indexOf("?"));
				String href="http://hotels.ctrip.com"+url2;
				//存入url用于去重
				String string = jedis.get(href);
				if(StringUtils.isNotBlank(string)){
					continue;
				}else{
					jedis.set(href, href);
				}

				Params para = new Params();
				para.setParentUrl(url);;
				para.setUrl(href);
				//酒店价格
				String price=element.get(j).text();

				//酒店详情对象
				Hotelinfo basicInfo=new Hotelinfo();
				basicInfo.setName(obj.getString("name"));
				basicInfo.setAddress(obj.getString("address"));
				basicInfo.setLongitude(obj.getString("lon"));
				basicInfo.setLatitude(obj.getString("lat"));
				basicInfo.setPrice(price);
				basicInfo.setGrade(obj.getString("score"));
				basicInfo.setStar(obj.getString("stardesc"));
				basicInfo.setDatasource("Ctrip");
				basicInfo.setGradenum(obj.getString("dpcount"));

				para.setHotelInfo(basicInfo);

				//酒店四级地址表对象
				Addressinfo addressInfo=new Addressinfo();
				addressInfo.setCity(cityName);
				addressInfo.setDetailaddress(obj.getString("address"));
				addressInfo.setProvince(cityName.equals("上海")?"上海":"海南");
				addressInfo.setType(2);
				addressInfo.setCountry("中国");
				addressInfo.setDatasource("Ctrip");

				para.setAddressinfo(addressInfo);
				//加入redis
				para.setCityName(cityName2);
				para.setType(Param.CTRIP_HOTEL_DETAIL);
				para.setDataSource(Param.CTRIP);
				para.setHttpType(Param.GET);
				para.setDestinationName("highQueue");
				produceService.sendMessage(highQueue,para);

			}
		} catch (Exception e) {
			produceService.sendMessage(params.getDestinationName(), params);
		}


	}
	/**
	 * 
	 * @Description TODO
	 * @author 赵乐
	 * @date 2018年3月6日 下午2:59:56
	 * @action parseCtripHotelFirst
	 * @param @param params
	 * @return void
	 */
	public void parseCtripHotelFirst(Params params) {
		// TODO Auto-generated method stub
		//抓取酒店分页链接
		String uuid = params.getUuid();
		String cityName = params.getCityName();
		String url = params.getUrl();
		String content = params.getContent();
		if(url.startsWith("http://hotels.ctrip.com/hotel/")){
			Document document = Jsoup.parse(content);
			Elements elements = document.select("div#page_info>div.c_page_list.layoutfix>a");
			String strss=url.substring(url.lastIndexOf("/")+1, url.length());
			Integer cityId=NumUtils.getInteger(strss);
			String cityPY=strss.replace(cityId+"", "");
			Elements elementscityLat = document.select("input#cityLat");
			//经纬度
			String cityLat ="";
			String cityLng ="";
			for (Element element : elementscityLat) {
				cityLat = element.attr("value");
			}
			Elements elementscityLng = document.select("input#cityLng");
			for (Element element : elementscityLng) {
				cityLng = element.attr("value");
			}
			//总页数
			Integer pageNum=1;
			for (Element element : elements) {
				if("nofollow".equals(element.attr("rel"))){
					String pageNumstr = element.text();
					pageNum=Integer.parseInt(pageNumstr);
				}
			}

			for(int k=1;k<=pageNum;k++){
				String pageHref="http://hotels.ctrip.com/Domestic/Tool/AjaxHotelList.aspx?__VIEWSTATEGENERATOR=DB1FBB6D&cityName=%25E4%25B8%2589%25E4%25BA%259A&"
						+ "StartTime=2017-09-26&DepTime=2017-09-27"
						+ "&txtkeyword=&Resource=&Room=&Paymentterm=&BRev=&Minstate=&PromoteType=&PromoteDate=&operationtype=NEWHOTELORDER&PromoteStartDate=&PromoteEndDate=&OrderID=&RoomNum=&IsOnlyAirHotel=F&"
						+ "cityId="+cityId+""
						+ "&cityPY="+cityPY+"&cityCode=0899&"
						+ "cityLat="+cityLat+"&cityLng="+cityLng+""
						+ "&positionArea=&positionId=&keyword=&hotelId=&htlPageView=0&hotelType=F&hasPKGHotel=F&requestTravelMoney=F&isusergiftcard=F&useFG=F&HotelEquipment=&priceRange=-2&hotelBrandId=&promotion=F&prepay=F&IsCanReserve=F&OrderBy=99&OrderType=&k1=&k2=&CorpPayType=&viewType=&checkIn=2017-09-26&checkOut=2017-09-27&DealSale=&ulogin=&hidTestLat=0%257C0&AllHotelIds=386917%252C486003%252C1486685%252C1682691%252C3362163%252C1185042%252C436673%252C346766%252C346690%252C396523%252C345067%252C345522%252C345078%252C6007926%252C345071%252C345553%252C353871%252C345101%252C532834%252C1601597%252C470083%252C5296769%252C3735187%252C345074%252C430163&psid=&HideIsNoneLogin=T&isfromlist=T&ubt_price_key=htl_search_result_promotion&showwindow=&defaultcoupon=&isHuaZhu=False&hotelPriceLow=&htlFrom=hotellist&unBookHotelTraceCode=&showTipFlg=&hotelIds=386917_1_1%2C486003_2_1%2C1486685_3_1%2C1682691_4_1%2C3362163_5_1%2C1185042_6_1%2C436673_7_1%2C346766_8_1%2C346690_9_1%2C396523_10_1%2C345067_11_1%2C345522_12_1%2C345078_13_1%2C6007926_14_1%2C345071_15_1%2C345553_16_1%2C353871_17_1%2C345101_18_1%2C532834_19_1%2C1601597_20_1%2C470083_21_1%2C5296769_22_1%2C3735187_23_1%2C345074_24_1%2C430163_25_1"
						+ "&markType=0&zone=&location=&type=&brand=&group=&feature=&equip=&star=&sl="
						+ "&s=&l=&price=&a=0&keywordLat=&keywordLon=&contrast=0&page="+k+"&contyped=0&productcode=";
				System.out.println(pageHref+"分页url++++");

				//调用activeMQ消息机制
				Params para=new Params();
				para.setUrl(pageHref);
				para.setCityName(cityName);
				para.setType(Param.CTRIP_HOTEL_PAGE);
				para.setDataSource(Param.CTRIP);
				para.setHttpType(Param.GET);
				para.setDestinationName("topQueue");
				produceService.sendMessage(topQueue,para);

			}

		}
	}
	/**
	 * 
	 * @Description 解析携程行程评论信息
	 * @author 赵乐
	 * @date 2018年3月6日 下午2:33:35
	 * @action parseCtripRouteComment
	 * @param @param params
	 * @return void
	 */
	public void parseCtripRouteComment(Params params) {
		// TODO Auto-generated method stub
		String url=params.getUrl();
		String uuid = params.getUuid();
		String content=params.getContent();
		String cityName = params.getCityName();
		//跟团游和半自助游的解析
		if(url.contains("http://vacations.ctrip.com/bookingnext/Comment/Search")){
			try {
				//解析JSON数据
				JSONObject comjson = new JSONObject(content);
				String dataStr = comjson.get("data").toString();
				JSONObject jsonData = new JSONObject(dataStr);
				String commentsInfoList = jsonData.get("CommentsInfoList").toString();
				JSONArray commentArr = new JSONArray(commentsInfoList);
				System.out.println(commentArr);
				//循环取出评论的相关信息
				for (int k = 0; k < commentArr.length(); k++) {
					try {
						JSONObject comMsgJson = new JSONObject(commentArr.get(k).toString());
						String commentDate = comMsgJson.get("CommentDate").toString();

						Commentinfo commentInfo = new Commentinfo();
						commentInfo.setId(UUID.randomUUID().toString());
						commentInfo.setInfoid(uuid);
						commentInfo.setType(3);    //类型：1-景点，2-酒店，3-景点行程
						commentInfo.setContent(comMsgJson.toString());
						commentInfo.setCommentdate(commentDate);
						commentInfo.setAdminarea("中国,"+cityName);
						commentInfo.setDatasource("Ctrip");
						commentInfo.setCreatedate(new Date());
						commentInfo.setCreator("姚良良");
						commentInfo.setCreatorid("13783985208");
						String commentInfoObject = KafkaUtils.parseJsonObject(commentInfo, 11,1);
						String sendPost = downLoadService.sendPost(KafkaUtils.KAFKAURL, commentInfoObject);

						jedis.insertAndGetId(commentInfo);
						System.out.println("跟团游评论信息---"+comMsgJson.toString());
					} catch (Exception e) {
						e.printStackTrace();
					}finally {
						int m = k+1;
						System.out.println("当前页行程评论共"+commentArr.length()+"条，已抓取"+m+"条!");
					}
				}
			} catch (Exception e) {
				produceService.sendMessage(params.getDestinationName(), params);
			}

			//自由行的解析
		}else if(url.contains("http://online.ctrip.com/restapi/soa2/12447/json/GetCommentInfoList")){
			//解析JSON数据
			try {
				JSONObject comObj = new JSONObject(content);
				String dataStr = comObj.get("Data").toString();
				JSONObject jsonData = new JSONObject(dataStr);
				String commentsInfoList = jsonData.get("CommentInfoList").toString();
				JSONArray commentArr = new JSONArray(commentsInfoList);
				//循环取出评论的相关信息
				for (int k = 0; k < commentArr.length(); k++) {
					try {
						JSONObject comMsgJson = new JSONObject(commentArr.get(k).toString());
						String commentDate = comMsgJson.get("SubmitTime").toString();

						Commentinfo commentInfo = new Commentinfo();
						commentInfo.setType(3);    //类型：1-景点，2-酒店，3-景点行程
						commentInfo.setId(UUID.randomUUID().toString());
						commentInfo.setInfoid(uuid);
						commentInfo.setContent(comMsgJson.toString());
						commentInfo.setCommentdate(commentDate);
						commentInfo.setAdminarea("中国,"+cityName);
						commentInfo.setDatasource("Ctrip");
						commentInfo.setCreatedate(new Date());
						commentInfo.setCreator("姚良良");
						commentInfo.setCreatorid("13783985208");
						String commentInfoObject = KafkaUtils.parseJsonObject(commentInfo, 11,1);
						String sendPost = downLoadService.sendPost(KafkaUtils.KAFKAURL, commentInfoObject);
						jedis.insertAndGetId(commentInfo);
						System.out.println("自由行评论信息--"+comMsgJson.toString());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				produceService.sendMessage(params.getDestinationName(), params);
			}
		}

	}

	/**
	 * 
	 * @Description 获取携程评论首页url
	 * @author 赵乐
	 * @date 2018年3月6日 下午2:25:39
	 * @action parseCtripRouteCommentFirst
	 * @param @param params
	 * @return void
	 */
	public void parseCtripRouteCommentFirst(Params params) {
		// TODO Auto-generated method stub
		String uuid=params.getUuid();
		String url = params.getUrl();
		String cityName = params.getCityName();
		String content = params.getContent();
		//跟团游和半自助游的解析
		if(url.contains("http://vacations.ctrip.com/bookingnext/Comment/Search")){
			String itemNo=url.substring(url.indexOf("pkg")+4, url.indexOf("&pageIndex"));
			System.out.println("评论url编号：(测试截取是否正确)"+itemNo);
			try {
				//解析JSON数据
				JSONObject jsonObject = new JSONObject(content);
				String data = jsonObject.get("data").toString();
				JSONObject dataJson = new JSONObject(data);
				Integer commentUsers = Integer.valueOf(dataJson.get("CommentUsers").toString());
				if(commentUsers == 0 || "".equals(commentUsers)){
					System.out.println("该行程暂无评论!");
				}
				//计算总页数
				Integer pageCount = commentUsers%5==0?commentUsers/5:commentUsers/5+1;
				for (int j = 1; j <= pageCount; j++) {
					//拼接跟团游的评论请求URL
					String comUrl = "http://vacations.ctrip.com/bookingnext/Comment/Search?pkg="+itemNo+"&pageIndex="+j;
					Params paramComment=new Params();
					paramComment.setCityName(cityName);
					paramComment.setType(Param.CTRIP_ROUTE_COMMENT);
					paramComment.setHttpType(Param.GET);
					paramComment.setDataSource(Param.CTRIP);
					paramComment.setUrl(comUrl);
					paramComment.setHeader("Referer", url);
					System.out.println("url--"+url);
					System.out.println("跟团游评论url--"+comUrl);
					paramComment.setUuid(uuid);

					paramComment.setDestinationName("lowQueue");
					produceService.sendMessage(lowQueue,paramComment);

				}

			} catch (Exception e) {
				produceService.sendMessage(params.getDestinationName(), params);
			}
			//自由行的解析
		}else if(url.contains("http://online.ctrip.com/restapi/soa2/12447/json/GetCommentInfoList")){
			try {
				String postParam=params.getPostParams();
				String itemNo=postParam.substring(postParam.indexOf("ProductId")+12, postParam.indexOf("PageIndex")-2);
				System.out.println("评论url编号：(测试截取是否正确)"+itemNo);
				//解析JSON数据
				JSONObject jsonObject = new JSONObject(content);
				String data = jsonObject.get("Data").toString();
				if(data == null || "".equals(data) || "null".equals(data)){
					System.out.println("该行程暂无评论!");
				}
				JSONObject dataJson = new JSONObject(data);
				String commentSummaryInfo = dataJson.get("CommentSummaryInfo").toString();
				JSONObject commentInfoJson = new JSONObject(commentSummaryInfo);
				Integer commentUsers = Integer.valueOf(commentInfoJson.get("TotalAmount").toString());
				if(commentUsers == 0 || "".equals(commentUsers)){
					System.out.println("该行程暂无评论!");
				}
				//计算总页数
				Integer pageCount = commentUsers%5==0?commentUsers/5:commentUsers/5+1;
				for (int j = 1; j <= pageCount; j++) {
					String commentUrl = "http://online.ctrip.com/restapi/soa2/12447/json/GetCommentInfoList";
					//封装请求头参数
					String urlParam = "{\"version\":70400,\"platformId\":4,\"channelCode\":0,\"CommentLevel\":0,\"ProductId\":"+itemNo+",\"PageIndex\":"+j+",\"PageSize\":5}";

					Params paramComment=new Params();
					paramComment.setCityName(cityName);
					paramComment.setType(Param.CTRIP_ROUTE_COMMENT);
					paramComment.setHttpType(Param.POST);
					paramComment.setDataSource(Param.CTRIP);
					paramComment.setUrl(commentUrl);
					paramComment.setPostParams(urlParam);
					paramComment.setUuid(uuid);
					paramComment.setHeader("Content-Type", "application/json; charset=UTF-8");
					System.out.println("自由行评论"+commentUrl+urlParam);

					paramComment.setDestinationName("lowQueue");
					produceService.sendMessage(lowQueue,paramComment);

				}
			} catch (Exception e) {
				produceService.sendMessage(params.getDestinationName(), params);
			}
		}
	}
	/**
	 * 
	 * @Description 解析携程费用说明和服务说明信息
	 * @author 赵乐
	 * @date 2018年3月6日 下午2:23:14
	 * @action parseCtripExpenseAndReserveinfo
	 * @param @param params
	 * @return void
	 */
	public void parseCtripExpenseAndReserveinfo(Params params) {
		// TODO Auto-generated method stub
		String uuid = params.getUuid();
		String content = params.getContent();
		Routeinfo routeinfo = params.getRouteInfo();
		if (StringUtils.isNotBlank(content)) {
			try {
				JSONObject jsonObject=new JSONObject(content);
				JSONObject jsonObjectData = jsonObject.getJSONObject("data");
				JSONArray jsonArrayExpense = jsonObjectData.getJSONArray("FeeInfos");
				JSONObject jsonObjectReserveinfo = jsonObjectData.getJSONObject("OrderingNeedToKnowInfo");
				routeinfo.setId(uuid);
				routeinfo.setExpense(jsonArrayExpense.toString());
				routeinfo.setReserveinfo(jsonObjectReserveinfo.toString());

				String routeinfoObject = KafkaUtils.parseJsonObject(routeinfo, 6,1);
				String sendPost = downLoadService.sendPost(KafkaUtils.KAFKAURL, routeinfoObject);

				System.out.println("插入携程中费用信息和介绍信息");
				jedis.insertAndGetId(routeinfo);
			}catch (Exception e) {
				
			}	
		}else {
			System.out.println("未获取到信息，重新放入队列");
			produceService.sendMessage(params.getDestinationName(), params);
		}
	}
	/**
	 * 
	 * @Description 接卸携程行程的出发地信息
	 * @author 赵乐
	 * @date 2018年3月6日 下午2:19:00
	 * @action parseCtripRouteDeparture
	 * @param @param params
	 * @return void
	 */
	public void parseCtripRouteDeparture(Params params) {
		// TODO Auto-generated method stub
		String uuid = params.getUuid();
		String url = params.getUrl();
		String productID=url.substring(url.indexOf("ProductID")+10,url.indexOf("&StartCity"));
		String content = params.getContent();
		String departure="";
		if (StringUtils.isNotBlank(content)) {
			try {
				JSONObject jsonObject=new JSONObject(content);
				JSONArray jsonArray = jsonObject.getJSONArray("departureCity");
				for(int i=0;i<jsonArray.length();i++){
					JSONObject jsonObject2 = jsonArray.getJSONObject(i);
					Integer productIdNum=(Integer) jsonObject2.getInt("productId");
					String productId=productIdNum+"";
					System.out.println(productId);
					if(productID.equals(productId)){
						departure=jsonObject2.getString("name");
						Routeinfo routeinfo = new Routeinfo();
						routeinfo.setId(uuid);
						routeinfo.setDeparture(departure);
						String routeinfoObject = KafkaUtils.parseJsonObject(routeinfo, 6,0);
						String sendPost = downLoadService.sendPost(KafkaUtils.KAFKAURL, routeinfoObject);

						jedis.insertAndGetId(routeinfo);
						break;
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
				produceService.sendMessage(params.getDestinationName(), params);
			}
		}else {
			System.out.println("未获取到信息，重新放入队列");
			produceService.sendMessage(params.getDestinationName(), params);
		}
	}
	/**
	 * 
	 * @Description 获取携程行程的价格信息
	 * @author 赵乐
	 * @date 2018年3月6日 下午2:15:18
	 * @action parseCtripRoutePrice
	 * @param @param params
	 * @return void
	 */
	public void parseCtripRoutePrice(Params params) {
		// TODO Auto-generated method stub
		String uuId = params.getUuid();
		String cityName = params.getCityName();
		String content = params.getContent();
		if (StringUtils.isNotBlank(content)) {
			try {
				//对JSON数据进行解析
				JSONObject json = new JSONObject(content);
				JSONObject dataJson = new JSONObject(json.get("data").toString());
				JSONArray jsonArray = new JSONArray(dataJson.get("ProductCalendarDailyList").toString());
				for (int j = 0; j < jsonArray.length(); j++) {

					Routepriceinfo routePrice = new Routepriceinfo();
					JSONObject priceDateJson = new JSONObject(jsonArray.get(j).toString());
					String date = priceDateJson.get("Date").toString();
					String priceDate = date.substring(0,10);
					String lowestPrice = priceDateJson.get("MinPrice").toString();
					String dayOfWeek = DateUtil.dateToWeek(priceDate);
					//创建HolyrobotRoutepriceinfo对象并设置相关属性

					routePrice.setId(UUID.randomUUID().toString());
					routePrice.setRouteid(uuId);
					routePrice.setCreator("姚良良");
					routePrice.setCreatorid("tyl13564205515");
					routePrice.setCreatedate(new Date());
					routePrice.setPricedate(priceDate);
					routePrice.setLowestprice(lowestPrice);
					routePrice.setDayofweek(dayOfWeek);
					routePrice.setDatasource("Ctrip");
					routePrice.setDestination("中国,"+cityName);
					String routePriceObject = KafkaUtils.parseJsonObject(routePrice, 7,1);
					String sendPost2 = downLoadService.sendPost(KafkaUtils.KAFKAURL, routePriceObject);
					System.out.println("插入行程价格信息");
					jedis.insertAndGetId(routePrice);
				}
			} catch (Exception e) {
				produceService.sendMessage(params.getDestinationName(), params);
			}
		}else {
			System.out.println("未获取到信息，重新放入队列");
			produceService.sendMessage(params.getDestinationName(), params);
		}
	}
	
	
	/**
	 * @Description TODO
	 * @author 汤玉林
	 * @date 2018年3月9日 下午3:01:39
	 * @action parseCtripRouteProductOutline
	 * @param params
	 */
	public void parseCtripRouteProductOutline(Params params) {
		String url=params.getUrl();
		String uuId=params.getUuid();
		String cityName = params.getCityName();
		String content = params.getContent();
		Document doc = Jsoup.parse(content);
		Routeinfo routeInfo = params.getRouteInfo();
		try {
			//获取产品特色（产品详情）
			Elements productFeatureEle = doc.select("div#divTourSchedule>div.detail_content.first_detail_content");
			if(productFeatureEle.isEmpty()){
				
			}else{
				String productFeature = productFeatureEle.text();
				routeInfo.setProductfeature(productFeature);
			}

			//获取线路介绍
			Elements itineraryDetailsDetailEle = doc.select("div.multiple_route>div.multiple_route_mod");	//获取图文模式
			//定义一个StringBuffer用于拼接图文和日历模式
			StringBuffer itineraryDetails = new StringBuffer();
			if (itineraryDetailsDetailEle.size()>0) {
				for (Element element : itineraryDetailsDetailEle) {
					itineraryDetails.append(element.text());
				}
			}
			routeInfo.setItinerarydetails(itineraryDetails.toString());	
		} catch (Exception e) {
			// TODO: handle exception
		}
			String productID = params.getOtherInformation().split(",")[0];
			String departureCity = params.getOtherInformation().split(",")[1];
			String saleCity = params.getOtherInformation().split(",")[2];
	
			String hrefExpenseAndReserveInfo="http://vacations.ctrip.com/bookingnext/Product/DescriptionInfo?";
			String param="ProductID="+productID+"&StartCity="+departureCity+"&SalesCity="+saleCity;

			Params para3=new Params();
			para3.setUuid(uuId);
			para3.setCityName(cityName);
			para3.setRouteInfo(routeInfo);
			para3.setType(Param.CTRIP_ROUTE_EXPENSEANDRESERVEINFO);
			para3.setHttpType(Param.POST);
			para3.setDataSource(Param.CTRIP);
			para3.setUrl(hrefExpenseAndReserveInfo+param);
			para3.setPostParams("");
			para3.setHeader("Referer", url);
			para3.setDestinationName("lowQueue");

			produceService.sendMessage(lowQueue,para3);
			
			/*String routeInfoObject = KafkaUtils.parseJsonObject(routeInfo, 6,0);
			String sendPost = downLoadService.sendPost(KafkaUtils.KAFKAURL, routeInfoObject);
			jedis.insertAndGetId(routeInfo);*/
		
		
	}
	

	/**
	 * 
	 * @Description 解析携程行程的详细信息(两个模板)
	 * @author 赵乐
	 * @date 2018年3月6日 下午1:40:04
	 * @action parseCtripRouteDetail
	 * @param @param params
	 * @return void
	 */
	public void parseCtripRouteDetail(Params params) {

		Routeinfo routeInfo=params.getRouteInfo();
		
		/*if(routeInfo.getTeamtype().equals("自由行")){
			parseCtripRouteZYXDetail(params);
		}else if(routeInfo.getTeamtype().equals("酒店+景点")){
			System.out.println("类型为酒店+景点");
		}else{
			parseCtripRouteNotZYXDetail(params);
		}*/
			
		if(routeInfo.getTeamtype().equals("酒店+景点")){
			System.out.println("类型为酒店+景点");
		}else{
			parseCtripRouteNotZYXDetail(params);
		}


	}

	/**
	 * @Description TODO
	 * @author 汤玉林
	 * @date 2018年3月9日 下午1:43:42
	 * @action parseCtripRouteNotZYXDetail
	 * @param params
	 */
	private void parseCtripRouteNotZYXDetail(Params params) {
		String url=params.getUrl();
		String uuId=params.getUuid();
		String cityName = params.getCityName();
		String content = params.getContent();
		Document doc = Jsoup.parse(content);
		try {
			Routeinfo routeInfo=params.getRouteInfo();
			routeInfo.setId(uuId);
			routeInfo.setUrlid(url);
			//获取行程的名字（作为判断是否是成功页面的标识）
			Elements selectName = doc.select("div.product_scroll_wrap.new_scroll_wrap>h1");
			if(selectName.isEmpty()){

				System.err.println("非自由行，出现错误，名字未取到");

			}
			String name=selectName.isEmpty()?"":selectName.text();

			//获取行程的线路编号（区分同一Url中A线、B线、C线（如果没有线路区别，则为空））
			Elements routeTypeEle = doc.select("ul#js_detail_tab>li>dl.current>dt.basefix>a.inner_current");
			if(!routeTypeEle.isEmpty()){
				String routeType = routeTypeEle.text();
				routeInfo.setRoutetype(routeType);
			}else{
				System.out.println("该行程为单线路!");
			}
			//获取价格	
			String html = doc.html();
			String price="";
			String effectDate="";
			String expireDate="";
			try {
				int indexOf = html.indexOf("{\"minPrice\":");
				if(indexOf!=-1){
					price = html.substring(html.indexOf("{\"minPrice\":")+12,html.indexOf(", \"effectDate\""));
				}else{
					indexOf = html.indexOf("ProductMinPrice");
					price = html.substring(html.indexOf("ProductMinPrice"), html.indexOf("ProductMinPriceDate"));
					price = price.substring(17, price.indexOf(",")-1);
				}

			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if(!"".equals(price)){
				routeInfo.setPrice(price);
				int effectDateIndexOf = html.indexOf("effectDate");
				int expireDateIndexOf = html.indexOf("expireDate");
				//获取出发地链接中的参数日期
				effectDate=html.substring(effectDateIndexOf+13,html.indexOf("\",", effectDateIndexOf));
				expireDate=html.substring(expireDateIndexOf+13,html.indexOf("\"}", expireDateIndexOf));
			}

			//获取行程概要
			Elements itineraryOutlineEle = doc.select("div#simple_route_div");
			if(!itineraryOutlineEle.isEmpty()){
				String itineraryOutline =itineraryOutlineEle.text();// 行程概要
				routeInfo.setItineraryoutline(itineraryOutline);
			}
			//获取产品推荐
			Elements productRecommendEle = doc.select("div.pm_face_recommend>div.pm_recommend");
			if(productRecommendEle.isEmpty()){
				productRecommendEle = doc.select("div.product_scroll_wrap.new_scroll_wrap>div.pm_recommend");
				if(productRecommendEle.isEmpty()){
					productRecommendEle = doc.select("div.pm_face_recommend>div.pm_recommend.pm_recom_cur>ul");
				}
			}
			if(!productRecommendEle.isEmpty()){
				String productRecommend = productRecommendEle.text();
				routeInfo.setProductrecommend(productRecommend);
			}			

			//获取产品特色（产品详情）
			Elements productFeatureEle = doc.select("div#js_detail>div.product_feature");
			if(productFeatureEle.isEmpty()){
				productFeatureEle = doc.select("div#divTourSchedule>div.detail_content.first_detail_content");
			}
			if(!productFeatureEle.isEmpty()){
				String productFeature = productFeatureEle.text();
				routeInfo.setProductfeature(productFeature);
			}

			//获取产品概要
			Elements productOutlineEle = doc.select("div.abc_content>div.abc_detail_col>dl.abc_detail");
			if(!productOutlineEle.isEmpty()){
				String productOutline = productOutlineEle.html();
				routeInfo.setProductoutline(productOutline);
			}				

			//获取线路介绍
			Elements itineraryDetailsDetailEle = doc.select("div#js_detail_travelCtrip");	//获取图文模式
			Elements itineraryDetailsSimpleEle = doc.select("div#simpleJourneyBox");		//获取日历模式
			//定义一个StringBuffer用于拼接图文和日历模式
			StringBuffer itineraryDetails = new StringBuffer();
			//如果没有图文和日历模式则查找单模式
			if(itineraryDetailsDetailEle.isEmpty() && itineraryDetailsSimpleEle.isEmpty()){
				Elements itineraryDetailsEle = doc.select("div#divTourSchedule>div.detail_content:nth-child(2)");
				if(!itineraryDetailsEle.isEmpty()){
					itineraryDetails.append(itineraryDetailsEle.html());											
				}
			}else{
				//如果有图文或日历模式则对其进行拼接保存
				if(!itineraryDetailsDetailEle.isEmpty()){
					String itineraryDetailsDetail = itineraryDetailsDetailEle.html();
					itineraryDetails.append(itineraryDetailsDetail);
				}
				if(!itineraryDetailsDetailEle.isEmpty()){
					String itineraryDetailsSimple = itineraryDetailsSimpleEle.html();
					itineraryDetails.append(itineraryDetailsSimple);
				}															
			}
			routeInfo.setItinerarydetails(itineraryDetails.toString());	

			//获取评分
			Elements gradeEle = doc.select("div#js_main_price_wrap>div.comment_wrap>a.score");
			if(gradeEle.isEmpty()){
				gradeEle = doc.select("div#js-min-price-change>div.comment_wrap>a.score");
			}
			if(!gradeEle.isEmpty()){
				String gradeStr = gradeEle.text().toString();
				String grade=NumUtils.getDouble(gradeStr)*20+"";
				routeInfo.setGrade(grade);
			}	
			//获取评论个数
			Elements gradeNumEle = doc.select("div#js_main_price_wrap>div.comment_wrap>a.comment_num");
			if(gradeNumEle.isEmpty()){
				gradeNumEle = doc.select("div#js-min-price-change>div.comment_wrap>a.comment_num");
			}
			if(!gradeNumEle.isEmpty()){
				String gradeNumStr = gradeNumEle.text();
				String gradeNum = NumUtils.getInteger(gradeNumStr)+"";
				routeInfo.setGradenum(gradeNum);
				System.out.println("点评人数: "+gradeNum);
			}
			//获取出游人数
			Elements beenNumEle = doc.select("div#js_main_price_wrap>div.comment_wrap>span");
			if(beenNumEle.isEmpty()){
				beenNumEle = doc.select("span#orderPersonCount");
			}
			if(!beenNumEle.isEmpty()){
				String beenNum = NumUtils.getInteger(beenNumEle.text())+"";
				routeInfo.setBeennum(beenNum);
				System.out.println("出游人数: "+beenNum);
			}
			routeInfo.setCreatedate(new Date());
			routeInfo.setCreator("姚良良");
			routeInfo.setCreatorid("13783985208");
			routeInfo.setDatasource("Ctrip");

			if(StringUtils.isBlank(name) ||StringUtils.isBlank(routeInfo.getName())){
				System.out.println("模板不匹配");
				//produceService.sendMessage(params.getDestinationName(), params);
				parseCtripRouteZYXDetail(params);
			}else{
			/*	String routeInfoObject = KafkaUtils.parseJsonObject(routeInfo, 6,0);
				String sendPost = downLoadService.sendPost(KafkaUtils.KAFKAURL, routeInfoObject);

				//放入redis
				String insertAndGetId = jedis.insertAndGetId(routeInfo);*/
				if(true){

					String routepriceURL = params.getOtherInformation();
					//http://vacations.ctrip.com/tour-mainsite-vacations/api/product/Calendar?PRO=7193115&DepartureCity=32&SaleCity=316
					String productID="";
					String departureCity="";
					String saleCity="";
					if(!"".equals(routepriceURL)){
						try {
							productID=routepriceURL.substring(routepriceURL.indexOf("PRO=")+4,routepriceURL.indexOf("&DepartureCity"));
							departureCity=routepriceURL.substring(routepriceURL.indexOf("DepartureCity")+14,routepriceURL.indexOf("&SaleCity"));
							saleCity=routepriceURL.substring(routepriceURL.indexOf("SaleCity")+9,routepriceURL.length());
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					//获取行程中的出发地
					String departure ="";
					try {
						int index=content.indexOf("StartCityName:\"");
						if(index>0){
							departure = content.substring(index+15,content.indexOf("\",",index+15));
						}else{
							index=content.indexOf("StartCityName: \'");
							if(index>0){
								departure = content.substring(index+16,content.indexOf("\',",index+16));
							}
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					routeInfo.setDeparture(departure);
					/*//获取行程中的出发地
					if(!"".equals(productID)&&!"".equals(departureCity)&&!"".equals(saleCity)&&!"".equals(effectDate)&&!"".equals(expireDate)){
						Params para=new Params();

						para.setUuid(uuId);
						String departureurl="http://vacations.ctrip.com/bookingnext/CalendarV2/CalendarInfo?ProductID="+productID+"&StartCity="+departureCity+"&SalesCity="+saleCity+"&EffectDate="+effectDate+"&ExpireDate="+expireDate;
						para.setType(Param.CTRIP_ROUTE_DEPARTURE);
						para.setHttpType(Param.GET);
						para.setDataSource(Param.CTRIP);
						para.setCityName(cityName);
						para.setUrl(departureurl);
						para.setHeader("Referer", url);
						para.setDestinationName("lowQueue");

						produceService.sendMessage(lowQueue,para);

					}*/

					//获取费用说明和预定须知信息的url链接
					String hrefExpenseAndReserveInfo="http://vacations.ctrip.com/bookingnext/Product/DescriptionInfo?";
					String param="ProductID="+productID+"&StartCity="+departureCity+"&SalesCity="+saleCity;

					Params para=new Params();
					para.setUuid(uuId);
					para.setCityName(cityName);
					para.setType(Param.CTRIP_ROUTE_EXPENSEANDRESERVEINFO);
					para.setHttpType(Param.POST);
					para.setDataSource(Param.CTRIP);
					para.setUrl(hrefExpenseAndReserveInfo+param);
					para.setPostParams("");
					para.setHeader("Referer", url);
					para.setDestinationName("lowQueue");

					produceService.sendMessage(lowQueue,para);

					Elements pictureEle = doc.select("div#js_photoviewer>div.attraction_photo_s>div.small_photo_wrap>ul>li>a>img");
					if(pictureEle.isEmpty()){
						pictureEle = doc.select("div#js_photoviewer>div.attraction_photo_small>div.small_photo_wrap>ul>li>a>img");
					}
					if(!pictureEle.isEmpty()){
						int n = 0;
						System.out.println("该行程的图片数量为 "+pictureEle.size()+" 张!");
						for (int j = 0; j < pictureEle.size(); j++){				
							String imgUrl = pictureEle.get(j).attr("_src");
							if(imgUrl == null || "".equals(imgUrl)){
								System.out.println("图片信息获取失败!");
								continue;
							}
							//实例一个行程图片对象
							Pictureinfo pictureInfo = new Pictureinfo();
							String pictureId=UUID.randomUUID().toString();
							//设置行程图片的固定属性
							pictureInfo.setId(pictureId);
							pictureInfo.setInfoid(uuId);
							pictureInfo.setType(3);
							pictureInfo.setDownload(0);
							pictureInfo.setImgurl(imgUrl);
							pictureInfo.setSort(j+1);
							pictureInfo.setAdminarea("中国,"+cityName);
							pictureInfo.setCreatedate(new Date());
							pictureInfo.setCreator("姚良良");
							pictureInfo.setDatasource("Ctrip");
							pictureInfo.setCreatorid("13783985208");
							String pictureInfoObject = KafkaUtils.parseJsonObject(pictureInfo, 10,1);
							String sendPost2 = downLoadService.sendPost(KafkaUtils.KAFKAURL, pictureInfoObject);
							//放入redis
							jedis.insertAndGetId(pictureInfo);
						}
						System.out.println("该行程已保存图片 "+pictureEle.size()+"-"+n+" 张!");
					}	
					//放回下一级链接
					ctripRoutePriceUrl(params);
					ctripRouteCommentTotalURL(params);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			produceService.sendMessage(params.getDestinationName(), params);
		}				

	}
	/**
	 * @Description TODO
	 * @author 汤玉林
	 * @date 2018年3月9日 下午1:43:11
	 * @action parseCtripRouteZYXDetail
	 * @param params
	 */
	private void parseCtripRouteZYXDetail(Params params) {
		String url=params.getUrl();
		String uuId=params.getUuid();
		String cityName = params.getCityName();
		String content = params.getContent();
		Document doc = Jsoup.parse(content);
		try {
			Routeinfo routeInfo=params.getRouteInfo();
			routeInfo.setId(uuId);
			routeInfo.setUrlid(url);
			//获取行程的名字（作为判断是否是成功页面的标识）
			Elements selectName = doc.select("div.detail_main_title>h2");
			if(selectName.isEmpty()){
				System.err.println("自由行,出现错误，名字未取到");

			}
			String name=selectName.isEmpty()?"":selectName.text();

			//获取行程的线路编号(自由行应该不分ABC线，目前没有发现)
			Elements routeTypeEle = doc.select("ul#js_detail_tab>li>dl.current>dt.basefix>a.inner_current");
			if(!routeTypeEle.isEmpty()){
				String routeType = routeTypeEle.text();
				routeInfo.setRoutetype(routeType);
			}else{
				System.out.println("该行程为单线路!");
			}
			//获取价格	
			String html = doc.html();
			String price="";

			try {
				int	indexOf = html.indexOf("ProductMinPrice");
				price = html.substring(html.indexOf("ProductMinPrice"), html.indexOf("ProductMinPriceDate"));
				price = price.substring(17, price.indexOf(",")-1);

			} catch (Exception e1) {
				e1.printStackTrace();
			}
			if(StringUtils.isNotBlank(price)){
				routeInfo.setPrice(price);
			}

			//获取产品推荐
			Elements productRecommendEle = doc.select("div.pm_face_recommend>div.pm_recommend.pm_recom_cur>ul");
			if(productRecommendEle.isEmpty()){

			}else{
				String productRecommend = productRecommendEle.text();
				routeInfo.setProductrecommend(productRecommend);
			}	

			String productid=url.substring(url.indexOf("/p")+2, url.lastIndexOf("s"));
			String salesCity=url.substring(url.lastIndexOf("s")+1,url.lastIndexOf("."));

			//获取评分
			Elements gradeEle = doc.select("div#js-min-price-change>div.comment_wrap>a.score");
			if(gradeEle.isEmpty()){

			}else{
				String gradeStr = gradeEle.text().toString();
				String grade=NumUtils.getDouble(gradeStr)*20+"";
				routeInfo.setGrade(grade);
			}	
			//获取评论个数
			Elements gradeNumEle = doc.select("div#js-min-price-change>div.comment_wrap>a.comment_num");
			if(gradeNumEle.isEmpty()){

			}else{
				String gradeNumStr = gradeNumEle.text();
				String gradeNum = NumUtils.getInteger(gradeNumStr)+"";
				routeInfo.setGradenum(gradeNum);
				System.out.println("点评人数: "+gradeNum);
			}
			//获取出游人数
			Elements beenNumEle = doc.select("span#orderPersonCount");
			if(beenNumEle.isEmpty()){

			}else{
				String beenNum = NumUtils.getInteger(beenNumEle.text())+"";
				routeInfo.setBeennum(beenNum);
				System.out.println("出游人数: "+beenNum);
			}
			routeInfo.setCreatedate(new Date());
			routeInfo.setCreator("姚良良");
			routeInfo.setCreatorid("13783985208");
			routeInfo.setDatasource("Ctrip");

			if(StringUtils.isBlank(name) ||StringUtils.isBlank(routeInfo.getName())){
				System.out.println("数据不完整，不适合当前自由行模板");
				//produceService.sendMessage(params.getDestinationName(), params);
			}else{
				/*String routeInfoObject = KafkaUtils.parseJsonObject(routeInfo, 6,0);
				String sendPost = downLoadService.sendPost(KafkaUtils.KAFKAURL, routeInfoObject);

				//放入redis
				String insertAndGetId = jedis.insertAndGetId(routeInfo);*/
				
				String routepriceURL = params.getOtherInformation();
				//http://vacations.ctrip.com/tour-mainsite-vacations/api/product/Calendar?PRO=7193115&DepartureCity=32&SaleCity=316
				String productID="";
				String departureCity="";
				String saleCity="";
				if(!"".equals(routepriceURL)){
					try {
						productID=routepriceURL.substring(routepriceURL.indexOf("PRO=")+4,routepriceURL.indexOf("&DepartureCity"));
						departureCity=routepriceURL.substring(routepriceURL.indexOf("DepartureCity")+14,routepriceURL.indexOf("&SaleCity"));
						saleCity=routepriceURL.substring(routepriceURL.indexOf("SaleCity")+9,routepriceURL.length());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				//获取行程中的出发地
				String departure ="";
				try {
					int index=content.indexOf("StartCityName:\"");
					if(index>0){
						departure = content.substring(index+15,content.indexOf("\",",index+15));
					}else{
						index=content.indexOf("StartCityName: \'");
						if(index>0){
							departure = content.substring(index+16,content.indexOf("\',",index+16));
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				routeInfo.setDeparture(departure);
		/*		//获取行程中的出发地
				if(!"".equals(productID)&&!"".equals(departureCity)&&!"".equals(saleCity)&&!"".equals(effectDate)&&!"".equals(expireDate)){
					Params para2=new Params();

					para2.setUuid(uuId);
					String departureurl="http://vacations.ctrip.com/bookingnext/CalendarV2/CalendarInfo?ProductID="+productID+"&StartCity="+departureCity+"&SalesCity="+saleCity+"&EffectDate="+effectDate+"&ExpireDate="+expireDate;
					para2.setType(Param.CTRIP_ROUTE_DEPARTURE);
					para2.setHttpType(Param.GET);
					para2.setDataSource(Param.CTRIP);
					para2.setUrl(departureurl);
					para2.setHeader("Referer", url);
					para2.setDestinationName("lowQueue");

					produceService.sendMessage(lowQueue,para2);

				}*/

				//获取费用说明和预定须知信息的url链接
		/*		String hrefExpenseAndReserveInfo="http://vacations.ctrip.com/bookingnext/Product/DescriptionInfo?";
				String param="ProductID="+productID+"&StartCity="+departureCity+"&SalesCity="+saleCity;

				Params para3=new Params();
				para3.setUuid(uuId);
				para3.setCityName(cityName);
				para3.setType(Param.CTRIP_ROUTE_EXPENSEANDRESERVEINFO);
				para3.setHttpType(Param.POST);
				para3.setDataSource(Param.CTRIP);
				para3.setUrl(hrefExpenseAndReserveInfo+param);
				para3.setPostParams("");
				para3.setHeader("Referer", url);
				para3.setDestinationName("lowQueue");
				produceService.sendMessage(lowQueue,para3);*/
				
				//获取产品概要
				String productOutlineUrl = "http://taocan.ctrip.com/freetravel/ProductDetail/Schedule?ProductId="+productid;				
				Params para=new Params();

				para.setUuid(uuId);
				para.setCityName(cityName);
				para.setRouteInfo(routeInfo);
				para.setType(Param.CTRIP_ROUTE_PRODUCTOUTLINE);
				para.setOtherInformation(productID+","+departureCity+","+saleCity);
				para.setHttpType(Param.GET);
				para.setDataSource(Param.CTRIP);
				para.setUrl(productOutlineUrl);
				para.setDestinationName("lowQueue");
				produceService.sendMessage(lowQueue, para);
				
				Elements pictureEle = doc.select("div#js_photoviewer>div.attraction_photo_s>div.small_photo_wrap>ul>li>a>img");
				if(pictureEle.isEmpty()){
					pictureEle = doc.select("div#js_photoviewer>div.attraction_photo_small>div.small_photo_wrap>ul>li>a>img");
				}
				if(!pictureEle.isEmpty()){
					int n = 0;
					System.out.println("该行程的图片数量为 "+pictureEle.size()+" 张!");
					for (int j = 0; j < pictureEle.size(); j++){				
						String imgUrl = pictureEle.get(j).attr("_src");
						if(imgUrl == null || "".equals(imgUrl)){
							System.out.println("图片信息获取失败!");
							continue;
						}
						//实例一个行程图片对象
						Pictureinfo pictureInfo = new Pictureinfo();
						String pictureId=UUID.randomUUID().toString();
						//设置行程图片的固定属性
						pictureInfo.setId(pictureId);
						pictureInfo.setInfoid(uuId);
						pictureInfo.setType(3);
						pictureInfo.setDownload(0);
						pictureInfo.setImgurl(imgUrl);
						pictureInfo.setSort(j+1);
						pictureInfo.setAdminarea("中国,"+cityName);
						pictureInfo.setCreatedate(new Date());
						pictureInfo.setCreator("姚良良");
						pictureInfo.setDatasource("Ctrip");
						pictureInfo.setCreatorid("13783985208");
						String pictureInfoObject = KafkaUtils.parseJsonObject(pictureInfo, 10,1);
						String sendPost2 = downLoadService.sendPost(KafkaUtils.KAFKAURL, pictureInfoObject);
						//放入redis
						jedis.insertAndGetId(pictureInfo);
					}
					System.out.println("该行程已保存图片 "+pictureEle.size()+"-"+n+" 张!");
				}	
				//放回下一级链接
				ctripRoutePriceUrl(params);
				ctripRouteCommentTotalURL(params);
				
			}

		} catch (Exception e) {
			e.printStackTrace();
			produceService.sendMessage(params.getDestinationName(), params);
		}				

	}
	/**
	 * 
	 * @Description 获取携程行程的评论url
	 * @author 赵乐
	 * @date 2018年3月6日 下午2:00:48
	 * @action ctripRouteCommentTotalURL
	 * @param @param params
	 * @return void
	 */
	public void ctripRouteCommentTotalURL(Params params) {
		// TODO Auto-generated method stub
		try {
			//获得行程类型
			String teamType=params.getRouteInfo().getTeamtype();
			String url = params.getUrl();
			String uuId=params.getUuid();
			String cityName = params.getCityName();
			String itemNo=url.substring(url.indexOf("/p")+2, url.lastIndexOf("s")); 
			Params para=new Params();
			if("跟团游".equals(teamType) || "半自助游".equals(teamType)){
				//拼接跟团游的评论请求URL
				String commentUrl = "http://vacations.ctrip.com/bookingnext/Comment/Search?pkg="+itemNo+"&pageIndex=1";
				//route.setCommentURL(commentUrl);
				para.setType(Param.CTRIP_ROUTE_COMMENT_FIRST);
				para.setHttpType(Param.GET);
				para.setDataSource(Param.CTRIP);
				para.setUrl(commentUrl);
				para.setHeader("Referer", url);
				para.setCityName(cityName);
				para.setUuid(uuId);

			}else if("自由行".equals(teamType)){
				String commentUrl = "http://online.ctrip.com/restapi/soa2/12447/json/GetCommentInfoList";
				String param="{\"version\":70400,\"platformId\":4,\"channelCode\":0,\"CommentLevel\":0,\"ProductId\":"+itemNo+",\"PageIndex\":1,\"PageSize\":5}";

				para.setType(Param.CTRIP_ROUTE_COMMENT_FIRST);
				para.setHttpType(Param.POST);
				para.setDataSource(Param.CTRIP);
				para.setUrl(commentUrl);
				para.setPostParams(param);
				para.setUuid(uuId);
				para.setCityName(cityName);
				
				para.setHeader("Content-Type", "application/json; charset=UTF-8");

			}else{
				System.out.println("暂时没有此类型");
			}

			if(params.getHttpType()!=null){
				System.out.println("存入评论首页url");
				para.setDestinationName("lowQueue");
				produceService.sendMessage(lowQueue,para);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 
	 * @Description 获取行程价格url
	 * @author 赵乐
	 * @date 2018年3月6日 下午2:01:22
	 * @action ctripRoutePriceUrl
	 * @param @param params
	 * @return void
	 */
	public void ctripRoutePriceUrl(Params params) {
		// TODO Auto-generated method stub
		try {
			String uuId = params.getUuid();
			String cityName = params.getCityName();
			String routepriceURL = params.getOtherInformation();
			//调用activeMQ消息机制
			Params para=new Params();
			para.setUuid(uuId);
			para.setCityName(cityName);
			para.setType(Param.CTRIP_ROUTE_PRICE);
			para.setHttpType(Param.GET);
			para.setDataSource(Param.CTRIP);
			para.setUrl(routepriceURL);

			para.setDestinationName("lowQueue");
			produceService.sendMessage(lowQueue,para);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @Description 解析携程分页url获取种子链接
	 * @author 赵乐
	 * @date 2018年3月6日 上午10:41:09
	 * @action parseCtripRoutePage
	 * @param @param params
	 * @return void
	 */
	public void parseCtripRoutePage(Params params) {
		// TODO Auto-generated method stub
		String url = params.getUrl();
		String content = params.getContent();
		String cityName = params.getCityName();
		Document document = Jsoup.parse(content);

		//分页链接
		Elements elements = document.select("div#_prd>div.main_mod.product_box.flag_product");
		if(!elements.isEmpty()){
			//循环取出每个模块中的相关数据
			for (Element element : elements) {

				try {
					//获取行程URL链接节点
					Elements routeUrl = element.select("div.product_main>h2.product_title>a");
					String href = "";
					String name = "";
					if(!routeUrl.isEmpty()){
						href = "http:"+routeUrl.attr("href");
						name = routeUrl.text();
						System.out.println("名称为:"+name+",链接为: "+href);							
					}else{
						System.out.println("名字获取失败!");
						continue;
					}

					//存入url用于去重
					String string = jedis.get(href);
					if(StringUtils.isNotBlank(string)){
						continue;
					}else{
						jedis.set(href, href);
					}

					//调用activeMQ消息机制
					Params para=new Params();
					para.setType(Param.CTRIP_ROUTE_DETAIL);
					para.setDataSource(Param.CTRIP);
					para.setHttpType(Param.GET);
					para.setDestinationName("highQueue");

					//型号基础对象
					Routeinfo routeInfo=new Routeinfo();

					para.setUrl(href);
					para.setParentUrl(url);
					
					routeInfo.setDatasource("Ctrip");
					routeInfo.setCreator("姚良良");
					routeInfo.setCreatorid("13783985208");
					routeInfo.setCreatedate(new Date());
					//获取行程的名字
					routeInfo.setName(name);
					//获取行程编号
					String productid=href.substring(href.indexOf("/p")+2, href.lastIndexOf("s"));
					routeInfo.setItemno(productid);
					String salesCity=href.substring(href.lastIndexOf("s")+1,href.lastIndexOf("."));

					Elements textareaEle = element.select("textarea");
					String textarea = textareaEle.text();
					JSONObject textareaJSON = new JSONObject(textarea);
					String departureCity = textareaJSON.get("DepartureId").toString();	//出发地城市ID

					String destination=href.substring(href.indexOf("kwd=")+4,href.length());
					//获取行程	，拼接行程价格的URL
					String routepriceURL = "http://vacations.ctrip.com/tour-mainsite-vacations/api/product/Calendar?PRO="+productid+"&DepartureCity="+departureCity+"&SaleCity="+salesCity;
					//存放行程价格的url链接
					para.setOtherInformation(routepriceURL);//后面的行程价格url直接从这里获取
					
					//设置目的地
					para.setCityName(URLDecoder.decode(destination, "UTF-8"));
					
					destination=URLDecoder.decode(destination, "UTF-8");
					routeInfo.setDestination(destination);
					//获取行程类型(跟团游、自由行或者其他)
					Elements teamTypeEle = element.select("div.product_pic>em");
					if(!teamTypeEle.isEmpty()){
						String teamType = teamTypeEle.first().text();
						routeInfo.setTeamtype(teamType);
						System.out.println("旅行类型为:"+teamType);
					}
					//获取供应商
					Elements supplierNameEle = element.select("div.basefix>div.product_l>p.product_retail");
					if(!supplierNameEle.isEmpty()){
						String supplierNameStr = supplierNameEle.text();
						String supplierName = supplierNameStr.substring(4);
						routeInfo.setSuppliername(supplierName);
						System.out.println("供应商为: "+supplierName);
					}
					para.setRouteInfo(routeInfo);
					produceService.sendMessage(highQueue,para);

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}			
			}								
		}else{
			System.err.println("解析详情信息标签为空，放回到队列中再次请求");
			produceService.sendMessage(params.getDestinationName(), params);
		}
	}



	/**
	 * 
	 * @Description 解析携程行程首页链接
	 * @author 赵乐
	 * @date 2018年3月6日 上午10:28:08
	 * @action parseCtripRouteFirst
	 * @param @param params
	 * @return void
	 */
	public void parseCtripRouteFirst(Params params) {
		// TODO Auto-generated method stub
		String url = params.getUrl();
		String content = params.getContent();
		String cityName = params.getCityName();
		Document document = Jsoup.parse(content);
		if(!url.contains("/p")){
			//取页码总也数码数
			Elements elements = document.select("div#_pg>a");
			if(!elements.isEmpty()&&elements.size()>1){
				Element elementStr = elements.get(elements.size()-2);
				Integer pageNum=Integer.parseInt(elementStr.text());
				for (int i=1;i<=pageNum;i++) {
					String pageHref=url+"/p"+i;

					//调用activeMQ消息机制
					Params para=new Params();
					para.setUrl(pageHref);
					para.setCityName(cityName);
					para.setType(Param.CTRIP_ROUTE_PAGE);
					para.setDataSource(Param.CTRIP);
					para.setHttpType(Param.GET);
					para.setDestinationName("topQueue");
					produceService.sendMessage(topQueue,para);
				}
			}else{
				//如果为空,把链接放回到队列中再次请求
				System.err.println("解析分页链接标签为空，放回到队列中再次请求"+url);
				produceService.sendMessage(params.getDestinationName(), params);
			}
		}
	}



	/**
	 * 
	 * @Description 解析携程门票评论信息
	 * @author 赵乐
	 * @date 2018年3月6日 上午9:53:25
	 * @action parseCtripScenicComment
	 * @param @param params
	 * @return void
	 */
	public void parseCtripScenicComment(Params params) {
		// TODO Auto-generated method stub
		String content=params.getContent();
		String uuId=params.getUuid();
		String cityName = params.getCityName();
		if (StringUtils.isNotBlank(content)) {
			try {
				JSONObject json = new JSONObject(content);
				String comment = json.get("Comment").toString();
				JSONArray jsonArray = new JSONArray(comment);
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject object=jsonArray.getJSONObject(i);
					String commentDate=object.getString("date");
					Commentinfo holyrobotCommentinfo = new Commentinfo();
					String commentinfoId=UUID.randomUUID().toString();
					holyrobotCommentinfo.setId(commentinfoId);
					holyrobotCommentinfo.setInfoid(uuId);
					holyrobotCommentinfo.setContent(object.toString());
					holyrobotCommentinfo.setCommentdate(commentDate);
					holyrobotCommentinfo.setType(1);
					holyrobotCommentinfo.setDatasource("Ctrip");
					holyrobotCommentinfo.setAdminarea("中国,"+cityName);
					holyrobotCommentinfo.setCreator("徐仁杰");
					holyrobotCommentinfo.setCreatorid("xurenjie-13621935220");
					holyrobotCommentinfo.setCreatedate(new Date());

					String holyrobotCommentinfoObject = KafkaUtils.parseJsonObject(holyrobotCommentinfo, 11,1);
					String sendPost = downLoadService.sendPost(KafkaUtils.KAFKAURL, holyrobotCommentinfoObject);
					jedis.insertAndGetId(holyrobotCommentinfo);
					System.out.println("评论插入成功");
				}
			} catch (Exception e) {
				e.printStackTrace();
				produceService.sendMessage(params.getDestinationName(), params);
			}
		}else {
			System.out.println("未获取到信息，重新放入队列");
			produceService.sendMessage(params.getDestinationName(), params);
		}
	}
	
	/**
	 * 
	 * @Description 解析携程门票图片信息
	 * @author 赵乐
	 * @date 2018年3月6日 上午9:53:22
	 * @action parseCtripScenicPicture
	 * @param @param params
	 * @return void
	 */
	public void parseCtripScenicTicketStatute(Params params) {
		//获取票价的URL的id
		Scepriceinfo scepriceinfo = params.getScepriceinfo();
		String url = params.getUrl();
		String content = params.getContent();
		
		scepriceinfo.setFavouredpolicy(content);
		
		String holyrobotPictureinfoObject = KafkaUtils.parseJsonObject(scepriceinfo, 4,1);
		String sendPost = downLoadService.sendPost(KafkaUtils.KAFKAURL, holyrobotPictureinfoObject);
		
		jedis.insertAndGetId(scepriceinfo);
	}

	/**
	 * 
	 * @Description 解析携程门票图片信息
	 * @author 赵乐
	 * @date 2018年3月6日 上午9:53:22
	 * @action parseCtripScenicPicture
	 * @param @param params
	 * @return void
	 */
	public void parseCtripScenicPicture(Params params) {
		// TODO 
		String content=params.getContent();
		String uuId = params.getUuid();
		String cityName = params.getCityName();
		if (StringUtils.isNotBlank(content)) {
			try {
				JSONArray jsonArray = new JSONArray(content);
				JSONArray imgArray = null;
				JSONObject json = null;
				for (int i = 0; i < jsonArray.length(); i++) {
					Pictureinfo holyrobotPictureinfo = new Pictureinfo();
					String dataArray = jsonArray.get(i).toString();
					json = new JSONObject(dataArray);
					String imgData = json.get("Ga").toString();
					imgArray = new JSONArray(imgData);
					// 图片排序
					Integer imgId = json.getInt("ImgID");
					// 图片地址
					String img = imgArray.get(0).toString();
					json = new JSONObject(img);
					String pictureInfoId=UUID.randomUUID().toString();
					holyrobotPictureinfo.setId(pictureInfoId);
					holyrobotPictureinfo.setInfoid(uuId);
					holyrobotPictureinfo.setSort(imgId);
					holyrobotPictureinfo.setType(2);
					holyrobotPictureinfo.setDownload(0);
					holyrobotPictureinfo.setAdminarea("中国,"+cityName);
					holyrobotPictureinfo.setImgurl(json.getString("Url").toString());
					holyrobotPictureinfo.setCreator("徐仁杰");
					holyrobotPictureinfo.setDatasource("Ctrip");
					holyrobotPictureinfo.setCreatorid("xurenjie-13621935220");
					holyrobotPictureinfo.setCreatedate(new Date());

					String holyrobotPictureinfoObject = KafkaUtils.parseJsonObject(holyrobotPictureinfo, 10,1);
					String sendPost = downLoadService.sendPost(KafkaUtils.KAFKAURL, holyrobotPictureinfoObject);
					jedis.insertAndGetId(holyrobotPictureinfo);
					System.out.println("图片插入成功");
				}
			} catch (Exception e) {
				e.printStackTrace();
				produceService.sendMessage(params.getDestinationName(),params);
			}
		}else {
			System.out.println("未获取到信息，重新放入队列");
			produceService.sendMessage(params.getDestinationName(),params);
		}
	}




	/**
	 * 
	 * @Description 解析携程门票详情信息
	 * @author 赵乐
	 * @date 2018年3月6日 上午8:42:07
	 * @action parseCtripScenicDetail
	 * @param @param params
	 * @return void
	 */
	public void parseCtripScenicDetail(Params params) {
		// TODO Auto-generated method stub
		String url = params.getUrl();
		String uuId=params.getUuid();
		String cityName2 = params.getCityName();
		
		String content = params.getContent();

		Document document=Jsoup.parse(content);
		try {
			// 景点详情信息
			Sceinfo holyrobotSceinfo = new Sceinfo();
			holyrobotSceinfo.setId(uuId);
			holyrobotSceinfo.setUrlid(url);
			//景点名称
			Elements nameEle=document.select("div.media-right>h2.media-title");
			String name=nameEle.text();
			holyrobotSceinfo.setName(name);
			//景点星级
			Elements starEle=document.select("div.media-right>span.media-grade>strong");
			String star=starEle.text();
			holyrobotSceinfo.setStarlevel(star);
			// 推荐价格
			Elements select = document.select("div#media-wrapper>div.media-price>div.price-box>span");
			String text = select.text();
			holyrobotSceinfo.setReferprice(text);
			// 开放时间
			Elements select2 = document.select("div#media-wrapper>div.media-right>ul>li.time>span.j-limit");
			String text2 = select2.text();
			holyrobotSceinfo.setOpentime(text2);
			// 服务承诺
			Elements select4 = document.select("div#J-MediaLabel>div.jmp.pop-content");
			String text3 = select4.text();
			holyrobotSceinfo.setServicecommitment(text3);
			// 景点简介
			Elements select3 = document.select("div#J-Jdjj>div.feature-wrapper");
			String text4 = select3.text();
			holyrobotSceinfo.setIntroduction(text4);
			//景点评论人数
			Elements gradeElements=document.select("div.grade>a.mark");
			Integer gradeNum=NumUtils.getInteger(gradeElements.text());
			if(gradeNum==null){
				gradeNum=0;
			}
			holyrobotSceinfo.setGradenum(gradeNum.toString());

			//景点评分
			Elements gradeEle=document.select("div.grade>i");
			String grade=gradeEle.text();
			holyrobotSceinfo.setGrade(grade);
			//景点地址
			String address="";
			Elements addressEle=document.select("div.layoutfix>div.media-right>ul>li>span");
			if(addressEle.size()>0){
				address=addressEle.get(0).text();
			}
			holyrobotSceinfo.setAddress(address);
			System.out.println("url:"+url);

			// 景点经纬度
			String lng="";
			String lat="";
			String script = document.select("script").html();
			try {
				String lnglat=script.substring(script.indexOf("position:")+1, script.indexOf("cityName"));
				lng=lnglat.substring(10, lnglat.indexOf("|"));
				lat=lnglat.substring(lnglat.indexOf("|")+1, lnglat.indexOf(",")-1);

			} catch (Exception e) {
				e.printStackTrace();
			}
			holyrobotSceinfo.setLongitude(lng);
			holyrobotSceinfo.setLatitude(lat);
			String cityName="";
			String productId="";
			//城市名称
			try {
				String cityNameScript= script.substring(script.indexOf("cityName"), script.indexOf("jsPathRoot"));
				cityName=cityNameScript.substring(10, cityNameScript.lastIndexOf(",")-1);
				String productStr=script.substring(script.indexOf("productid:")+11, script.indexOf("address"));
				productId=productStr.substring(0, productStr.indexOf(","));
			} catch (Exception e) {
				e.printStackTrace();
			}
			//景点productID，存入otherinformation字段中用于取评论
			params.setOtherInformation(productId);
			
			// 门票价格部分
			Elements select13 = document.select("dl.c-wrapper-info>dd");
			for (Element element:select13) {
				String type = element.select("strong").text();
				String textValue = element.select("div").text();
				
				switch (type) {
				case "优待政策":
					holyrobotSceinfo.setFavouredpolicy(textValue);
					
					break;
				default:
					break;
				}
			}
			holyrobotSceinfo.setAdminarea("中国,"+cityName2);
			holyrobotSceinfo.setDatasource("Ctrip");
			holyrobotSceinfo.setDatatype("2");
			holyrobotSceinfo.setCreator("徐仁杰");
			holyrobotSceinfo.setCreatorid("xurenjie-13621935220");
			holyrobotSceinfo.setCreatedate(new Date());

			if(StringUtils.isBlank(holyrobotSceinfo.getName())){
				System.out.println("数据不完整，放回队列");
				produceService.sendMessage(params.getDestinationName(), params);

			}else{
				//取评论人数
				params.setSceinfo(holyrobotSceinfo);

				String holyrobotSceinfoObject = KafkaUtils.parseJsonObject(holyrobotSceinfo, 13,1);
				String sendPost = downLoadService.sendPost(KafkaUtils.KAFKAURL, holyrobotSceinfoObject);

				jedis.insertAndGetId(holyrobotSceinfo);

				Addressinfo addressInfo = new Addressinfo();
				addressInfo.setId(UUID.randomUUID().toString());
				addressInfo.setInfoid(uuId);
				addressInfo.setDetailaddress(address);
				addressInfo.setCity(cityName);
				addressInfo.setProvince(params.getCityName());//种子链接传过来的值是省份
				addressInfo.setType(1);
				addressInfo.setCountry("中国");
				addressInfo.setCreator("徐仁杰");
				addressInfo.setCreatorid("xurenjie-13621935220");
				addressInfo.setDatasource("Ctrip");
				addressInfo.setCreatedate(new Date());

				String addressInfoObject = KafkaUtils.parseJsonObject(addressInfo, 12,1);
				String sendPost2 = downLoadService.sendPost(KafkaUtils.KAFKAURL, addressInfoObject);
				jedis.insertAndGetId(addressInfo);
				
				//门票部分
				Elements select5 = document.select("div#booking-wrapper>div#J-Ticket>table.ticket-table>tbody>tr.ticket-info");
				
				if(select5.size()<=0){
					select5=document.select("div#booking-wrapper>div#J-Activity>table.ttd-table>tbody>tr");
				}
				String priceType = "";
				for (Element element : select5) {
					Scepriceinfo holyrobotScepriceinfo = new Scepriceinfo();
					String priceinfoId=UUID.randomUUID().toString();
					holyrobotScepriceinfo.setId(priceinfoId);
					holyrobotScepriceinfo.setScenicid(uuId);
					holyrobotScepriceinfo.setUrlid(url);
					holyrobotScepriceinfo.setName(name);
					
					// 价格类型
					Elements select6 = element.select("td.ticket-type>span");
					String str = select6.text();
					
					if (StringUtils.isNotBlank(str)) {
						priceType = str;
					}
					holyrobotScepriceinfo.setPricetype(priceType);
					// 门票类型
					Elements select7 = element.select("td.ticket-title-wrapper>span");
					String text6 = select7.text();
					holyrobotScepriceinfo.setTickettype(text6);
					// 价格条目
					Elements select8 = element.select("td.ticket-title-wrapper>a");
					if(select8.size()<=0){
						select8 = element.select("td.ttd-title>a");
					}
					String text7 = select8.text();
					holyrobotScepriceinfo.setPriceitem(text7);
					// 市场价
					Elements select9 = element.select("td.del-price>strong");
					if(select9.size()<=0){
						select9 = element.select("td>span.del-price");
					}
					String text8 = select9.text();
					holyrobotScepriceinfo.setMarketingprice(text8);
					// 销售条件（预定时间）
					Element select10 = null;
					
					if (StringUtils.isNoneBlank(str)) {
						
						select10 = element.select("td").get(2);
						
					} else {
						select10 = element.select("td").get(1);
					}
					String text9 = select10.text();
					holyrobotScepriceinfo.setSalecondition(text9);
					// 销售价
					Elements select11 = element.select("td>span.ctrip-price>strong");

					String text10 = select11.text();
					holyrobotScepriceinfo.setSaleprice(text10);
					// 折扣信息
					Elements select12 = element.select("td>div.icon-wrapper.pop-wrapper");
					String text11 = select12.text();
					holyrobotScepriceinfo.setDiscountinfo(text11);
					
					holyrobotSceinfo.setAdminarea("中国,"+cityName);
					holyrobotScepriceinfo.setCreator("徐仁杰");
					holyrobotScepriceinfo.setCreatorid("xurenjie-13621935220");
					holyrobotScepriceinfo.setDatasource("Ctrip");
					holyrobotScepriceinfo.setCreatedate(new Date());
					
					
				/*	String holyrobotScepriceinfoObject = KafkaUtils.parseJsonObject(holyrobotScepriceinfo, 4,0);
					String sendPost3 = downLoadService.sendPost(KafkaUtils.KAFKAURL, holyrobotScepriceinfoObject);
					
					jedis.insertAndGetId(holyrobotScepriceinfo);*/
					
					//获取门票下的详细信息，（票价中的优惠信息）
					String dataId =select5.attr("data-id");
					if(StringUtils.isBlank(dataId)){
						//dataId=select5.select("td.ttd-title>a").attr("data-id");
						dataId="";	
					}else{
						String ticketStatuteUrl="http://piao.ctrip.com/Thingstodo-Booking-ShoppingWebSite/api/TicketStatute?resourceID="+dataId;
						
						//调用activeMQ消息机制
						Params para=new Params();
						para.setUuid(priceinfoId);
						para.setUrl(ticketStatuteUrl);
						para.setScepriceinfo(holyrobotScepriceinfo);
						para.setType(Param.CTRIP_SCENIC_TICKETSTATUTE);
						para.setDataSource(Param.CTRIP);
						para.setHttpType(Param.GET);
						para.setDestinationName("lowQueue");
						produceService.sendMessage(lowQueue,para);
					}

				}
				//放图片链接
				ctripScenicPictureURL(params);
				//放评论链接
				ctripScenicCommentURL(params);
			}
		} catch (Exception e) {
			e.printStackTrace();
			produceService.sendMessage(params.getDestinationName(), params);
		}
	}

	/**
	 * 
	 * @Description 解析携程门票评论url
	 * @author 赵乐
	 * @date 2018年3月6日 上午9:07:10
	 * @action ctripScenicCommentURL
	 * @param @param params
	 * @return void
	 */
	public void ctripScenicCommentURL(Params params) {
		// TODO Auto-generated method stub
		String url=params.getUrl();
		String uuId=params.getUuid();
		String cityName = params.getCityName();
		String scenicSpotId=url.substring(url.lastIndexOf("/")+2, url.lastIndexOf("."));
		//获得评论总数
		String gradeNum=params.getSceinfo().getGradenum();
		System.out.println(gradeNum);
		int commentCount=Integer.valueOf(gradeNum);
		//评论产品id
		String productId=params.getOtherInformation();
		int totalPage=commentCount%10==0?commentCount/10:commentCount/10+1;
		totalPage=totalPage>100?100:totalPage;
		for(int i=1;i<=totalPage;i++){
			String commentUrl="http://piao.ctrip.com/Thingstodo-Booking-ShoppingWebSite/api/TicketDetailApi/action/GetUserComments?productId="
					+ productId + "&scenicSpotId=" + scenicSpotId + "&page=" +i;

			//调用activeMQ消息机制
			Params para=new Params();
			para.setUuid(uuId);
			para.setUrl(commentUrl);
			para.setCityName(cityName);
			para.setType(Param.CTRIP_SCENIC_COMMENT);
			para.setDataSource(Param.CTRIP);
			para.setHttpType(Param.GET);
			para.setDestinationName("lowQueue");
			produceService.sendMessage(lowQueue,para);

		}
	}


	/**
	 * 
	 * @Description 解析携程门票图片信息
	 * @author 赵乐
	 * @date 2018年3月6日 上午9:06:25
	 * @action ctripScenicPictureURL
	 * @param @param params
	 * @return void
	 */
	public void ctripScenicPictureURL(Params params) {
		// TODO Auto-generated method stub
		String uuId=params.getUuid();
		String url=params.getUrl();
		String cityName = params.getCityName();
		String scenicSpotId=url.substring(url.lastIndexOf("/")+2, url.lastIndexOf("."));
		String pictureUrl = "http://piao.ctrip.com/Thingstodo-Booking-ShoppingWebSite/api/TicketDetailApi/action/GetMultimedia?scenicSpotId="+scenicSpotId;

		//调用activeMQ消息机制
		Params para=new Params();
		para.setUuid(uuId);
		para.setUrl(pictureUrl);
		para.setCityName(cityName);
		para.setType(Param.CTRIP_SCENIC_PICTURE);
		para.setDataSource(Param.CTRIP);
		para.setHttpType(Param.GET);
		para.setDestinationName("lowQueue");
		produceService.sendMessage(lowQueue,para);
	}


	/**
	 * 
	 * @Description 解析携程门票首页链接获取分页链接
	 * @author 赵乐
	 * @date 2018年3月5日 下午4:42:17
	 * @action parseCtripScenicFirst
	 * @param @param params
	 * @return void
	 */
	public void parseCtripScenicFirst(Params params) {
		// TODO Auto-generated method stub
		String content = params.getContent();

		String pageUrl=params.getUrl();
		String cityName="";

		if(pageUrl.contains("s-tickets")){
			if(pageUrl.contains("u-_c9_cf_ba_a3")){
				cityName="上海";
			}else if(pageUrl.contains("u-_ba_a3_c4_cf")){
				cityName="海南";
			}
		}
		Document doc = Jsoup.parse(content);
		Elements nextPageEle = doc.select("div.pkg_page.basefix>a.down");
		for (Element element : nextPageEle) {
			String href = element.attr("href");
			if("".equals(href)){
				produceService.sendMessage(params.getDestinationName(), params);
			}else if(href.equals("###")) {
				System.out.println("没有下一页链接了");
			}else {
				String nextHref = "http://piao.ctrip.com" + href;
				System.out.println("门票分页链接"+nextHref);
				//调用activeMQ消息机制
				Params para=new Params();
				para.setUrl(nextHref);
				para.setParentUrl(pageUrl);
				para.setCityName(cityName);
				para.setType(Param.CTRIP_SCENIC_FIRST);
				para.setHttpType(Param.GET);
				para.setDataSource(Param.CTRIP);
				para.setDestinationName("topQueue");
				produceService.sendMessage(topQueue,para); 

			}
		}

		Elements select = doc.select("div#searchResultContainer>div>div.search_ticket_caption");
		System.err.println("分页url："+pageUrl+";当前页的门票链接数："+select.size());

		//解析失败，放回队列
		if(select.size()==0){
			produceService.sendMessage(params.getDestinationName(), params);
		}else{
			for (Element element : select) {
				String href ="http://piao.ctrip.com" + element.select("a.search_ticket_img").attr("href");
				System.out.println(href+"------携程门票链接");

				//存入url用于去重
				String string = jedis.get(href);
				if(StringUtils.isNotBlank(string)){
					continue;
				}else{
					jedis.set(href, href);
				}

				//调用activeMQ消息机制
				Params para=new Params();
				para.setUrl(href);
				para.setParentUrl(pageUrl);
				para.setCityName(cityName);
				para.setType(Param.CTRIP_SCENIC_DETAIL);
				para.setHttpType(Param.GET);
				para.setDataSource(Param.CTRIP);
				para.setDestinationName("highQueue");
				produceService.sendMessage(highQueue,para);
			}
		}

	}

	/**
	 * 
	 * @Description 解析行程景点评论信息
	 * @author 赵乐
	 * @date 2018年3月5日 下午4:07:29
	 * @action parseCtripStrokeComment
	 * @param @param params
	 * @return void
	 */
	public void parseCtripStrokeComment(Params params) {
		// TODO Auto-generated method stub
		String uuid=params.getUuid();
		String content = params.getContent();
		String cityName = params.getCityName();
		if (StringUtils.isNotBlank(content)) {
			//下面解析的是网页端的评论url返回的数据，暂时不用
			try {

				Document document = Jsoup.parse(content);
				Elements elements = document.select("div.comment_ctrip>div.comment_single>ul");
				System.err.println("当前评论url："+params.getUrl());
				if(!elements.isEmpty()){
					for(int i=0;i<elements.size();i++){
						Element element=elements.get(i);
						System.out.println("评论内容："+element.text());
						Commentinfo commentinfo=new Commentinfo();
						//主键ID
						String uuId=UUID.randomUUID().toString();
						commentinfo.setId(uuId);
						commentinfo.setInfoid(uuid);
						commentinfo.setType(1);
						commentinfo.setDatasource("Ctrip");
						commentinfo.setAdminarea("中国,"+cityName);
						commentinfo.setContent(element.toString());
						commentinfo.setCreatedate(new Date());
						commentinfo.setCreator("赵乐");
						commentinfo.setDatasource("Ctrip");
						commentinfo.setCreatorid("15736708180");
						
						//放入kafka队列
						String urlParams = KafkaUtils.parseJsonObject(commentinfo, 11,1);
						String sendPost = downLoadService.sendPost(KafkaUtils.KAFKAURL, urlParams);

						jedis.insertAndGetId(commentinfo);
					}

				}else{
					produceService.sendMessage(params.getDestinationName(), params);
				}
			} catch (Exception e) {
				System.out.println("未获取到信息，重新放入队列");
				produceService.sendMessage(params.getDestinationName(), params);
			}
		}
	}
	/**
	 * 
	 * @Description 解析携程景点想去人数和去过人数
	 * @author 赵乐
	 * @date 2018年3月5日 下午4:07:11
	 * @action parseCtripStrokeWantAndBeen
	 * @param @param params
	 * @return void
	 */
	public void parseCtripStrokeWantAndBeen(Params params) {
		// TODO Auto-generated method stub
		String content=params.getContent();
		Sceinfo sceinfo = params.getSceinfo();
		String beenNum = "";
		String wantToNum = "";
		if(StringUtils.isNotBlank(content)){
			try {
				if(content.contains("{")){
					JSONObject jsonObject=new JSONObject(content);
					try {
						if(jsonObject!=null){
							beenNum = jsonObject.get("WentTimes").toString();
							//景点想去人数
							wantToNum = jsonObject.get("WantTimes").toString();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				//补全sceinfo信息，插入redis
				String uuId=params.getUuid();

				sceinfo.setId(uuId);
				sceinfo.setWanttonum(wantToNum);
				sceinfo.setBeennum(beenNum);

				System.out.println("想去人数和去过人数"+wantToNum+beenNum);
				String urlParams = KafkaUtils.parseJsonObject(sceinfo, 5,1);
				String sendPost = downLoadService.sendPost(KafkaUtils.KAFKAURL, urlParams);

				jedis.insertAndGetId(sceinfo);
			} catch (Exception e) {
				produceService.sendMessage(params.getDestinationName(), params);
			}
		}else{
			System.out.println("未获取到信息，重新放入队列");
			produceService.sendMessage(params.getDestinationName(), params);
		}

	}


	/**
	 * 
	 * @Description 解析携程的种子链接获取景点的详情信息
	 * @author 赵乐
	 * @date 2018年3月5日 下午2:43:16
	 * @action parseCtripStrokeDetail
	 * @param @param params
	 * @return void
	 */
	public void parseCtripStrokeDetail(Params params) {
		// TODO Auto-generated method stub
		String uuid=params.getUuid();
		String href=params.getUrl();
		String content = params.getContent();
		String cityName = params.getCityName();
		//获取附加信息,请求评论分页url的参数
		String resourceId=href.substring(href.lastIndexOf("/")+1,href.lastIndexOf("."));
		String districtIdAndName=href.substring(href.indexOf("sight/")+6,href.lastIndexOf("/"));
		String districtId=NumUtils.getInteger(districtIdAndName)+"";
		String districteName=districtIdAndName.substring(0,districtIdAndName.indexOf(districtId));

		Document document=Jsoup.parse(content);

		try {
			//景点基础信息对象
			Sceinfo sceInfo = new Sceinfo();
			sceInfo.setId(uuid);
			sceInfo.setUrlid(href);

			//景点名称
			Elements elementsName = document.select("div.dest_toptitle.detail_tt>div.cf>div.f_left>h1>a");
			String name= elementsName.isEmpty() ? "":elementsName.text();
			sceInfo.setName(name);
			//景点地址
			Elements elementsAddress = document.select("div.s_sight_infor>p.s_sight_addr");
			String  addressstr=elementsAddress.isEmpty()? "":elementsAddress.text();
			String address="";
			if(!"".equals(addressstr)){
				address=addressstr.substring(addressstr.indexOf("地址：")+3, addressstr.length());
			}
			sceInfo.setAddress(address);
			//景点介绍
			Elements elementsIntroduction = document.select("div.detailcon.detailbox_dashed>div.toggle_l>div.text_style>p");
			String introduction=elementsIntroduction.isEmpty()? "":elementsIntroduction.text();
			sceInfo.setIntroduction(introduction);
			//经纬度
			Elements elementsByTag = document.getElementsByTag("meta");
			String script="";
			if(!elementsByTag.isEmpty()){
				for(Element element:elementsByTag){
					if("location".equals(element.attr("name"))){
						script=element.attr("content");
						break;
					}
				}
			}//province=海南;city=文昌;coord=110.778151528462,19.6880616289976
			String longitude ="";
			String latitude = "";
			String provice="";
			String city="";
			if(!"".equals(script)){
				String	scriptss=script.split(";coord=")[1];
				//景点经度
				longitude = scriptss.split(",")[0];
				sceInfo.setLongitude(longitude);
				//景点纬度
				latitude = scriptss.split(",")[1];
				sceInfo.setLatitude(latitude);
				//四级地址信息
				String scriptssAdress=script.split(";coord=")[0];
				String provinceAdress=scriptssAdress.split(";")[0];
				String cityAdress=scriptssAdress.split(";")[1];
				provice=provinceAdress.substring(provinceAdress.indexOf("province=")+9, provinceAdress.length());
				city=cityAdress.substring(cityAdress.indexOf("city=")+5, cityAdress.length());
				//规范四级地址表
				if("中国".equals(provice) && "上海".equals(city)){
					provice="上海";
				}else if("上海".equals(provice)){
					city="上海";
				}else if(!("上海".equals(provice) || "中国".equals(provice) || "海南".equals(provice))){
					city=provice;
					provice="海南";
				}
				System.out.println(city);
			}
			//设置四级地址表
			Addressinfo addressinfo=new Addressinfo();
			String addressId=UUID.randomUUID().toString();
			addressinfo.setId(addressId);
			addressinfo.setInfoid(uuid);
			addressinfo.setType(1);
			addressinfo.setCountry("中国");
			addressinfo.setProvince(provice);
			addressinfo.setCity(city);
			addressinfo.setDetailaddress(address);
			addressinfo.setCreatedate(new Date());
			addressinfo.setCreator("赵乐");
			addressinfo.setDatasource("Ctrip");
			addressinfo.setCreatorid("15736708180");

			String addressObject = KafkaUtils.parseJsonObject(addressinfo, 12,1);
			String sendPost = downLoadService.sendPost(KafkaUtils.KAFKAURL, addressObject);


			jedis.insertAndGetId(addressinfo);
			Elements elements = document.select("div.s_sight_infor>ul.s_sight_in_list>li");
			if(!elements.isEmpty()){
				for(Element element:elements){
					String key=element.select("span.s_sight_classic").text().trim();
					Elements valuetr=element.select("span.s_sight_con");
					String value=valuetr.text().trim();
					switch (key) {
					case "类        型：":
						//景点类型
						Elements elementsType = valuetr.select("a");
						String type="";
						if(elementsType.size()>0){
							for(int i=0;i<elementsType.size();i++){
								System.out.println(elementsType.get(i).text());
								type=type+elementsType.get(i).text()+" ";
							}
						}
						System.out.println(type+"----景点类型");
						sceInfo.setType(type);
						break;
					case "等        级：":
						//景点星级
						String starLevel=value;
						sceInfo.setStarlevel(starLevel);
						break;
					case "游玩时间：":
						//建议游玩时间描述
						String adviceTime =value;
						sceInfo.setAdvicetime(adviceTime);
						break;
					default:
						break;
					}

				}
			}
			//景点开放时间描述
			String openTime="";
			//其他信息，存其他有用信息  --门票信息
			String ticketinformation="";
			Elements elementsOpenTime = document.select("div.s_sight_infor>dl.s_sight_in_list");
			if(elementsOpenTime.size()>0){
				for(Element element:elementsOpenTime){
					String left = element.select("dt").isEmpty()?"":element.select("dt").text();
					String right =element.select("dd").isEmpty()?"":element.select("dd").text();
					switch(left){
					case "开放时间：":
						openTime=right;
						break;
					case "门票信息：":
						ticketinformation=right;
						break;
					default:
						break;
					}
				}
			}
			//景点开放时间描述
			sceInfo.setOpentime(openTime);
			//其他信息，存其他有用信息  --门票信息
			sceInfo.setTicketinformation(ticketinformation);
			//景点评分，百分制
			Elements elementGradeAndGradeNum = document.select("div.detailtop.cf.normalbox>ul.detailtop_r_info>li");
			//Elements elementsGrade = document.select("div.detailtop.cf.normalbox>ul.detailtop_r_info>li>span.score>b");
			Elements elementsGrade = elementGradeAndGradeNum.select("span.score>b");
			String grade="";
			if(!elementsGrade.isEmpty()){
				String text = elementsGrade.text().trim();
				Double gra = Double.parseDouble(text);
				grade=(gra*20)+"";
			}
			sceInfo.setGrade(grade);
			//景点评分个数
			//Elements elementsGradeNum = document.select("div.detailtop.cf.normalbox>ul.detailtop_r_info>li>span.pl_num>dfn#hrefyyDp>span.f_orange");
			Elements elementsGradeNum = elementGradeAndGradeNum.select("span.pl_num>dfn#hrefyyDp>span.f_orange");
			String gradeNum=elementsGradeNum.isEmpty()? "":elementsGradeNum.text();
			sceInfo.setGradenum(gradeNum);
			
			sceInfo.setAdminarea("中国,"+cityName);
			sceInfo.setDatatype("1");
			sceInfo.setDatasource("Ctrip");
			sceInfo.setCreatedate(new Date());
			sceInfo.setCreator("赵乐");
			sceInfo.setCreatorid("15736708180");

			if(StringUtils.isBlank(sceInfo.getName())||StringUtils.isBlank(href)){
				System.err.println(document);
				produceService.sendMessage(params.getDestinationName(),params);

			}else{

/*				String sceInfoObject = KafkaUtils.parseJsonObject(sceInfo, 5,0);
				String message2 = downLoadService.sendPost(KafkaUtils.KAFKAURL, sceInfoObject);

				String insertAndGetId = jedis.insertAndGetId(sceInfo);*/

				//获取点评的 poiId返回后，传入评论方法的参数
				Elements elementspoiId = elementGradeAndGradeNum.select("span.pl_num>span.c_tipswrap>a.b_orange_m");
				String poiIdhref=elementspoiId.isEmpty()?"":elementspoiId.attr("href");
				// /dianping/edit/80273.html
				String poiId="";
				if(!"".equals(poiIdhref)){
					poiId=poiIdhref.substring(poiIdhref.lastIndexOf("/")+1, poiIdhref.lastIndexOf("."));
				}

				//获取评论的总页码数
				Elements elementspageNum = document.select("div.ttd_pager.cf>div.pager_v1>span>b");
				String pageNum=elementspageNum.isEmpty()?"":elementspageNum.text();

				//存入请求评论信息所需要的信息
				int totalComment=0;
				if(StringUtils.isBlank(gradeNum)){
					gradeNum="0";
				}
				totalComment=Integer.valueOf(gradeNum);
				//下面的是获取app端的评论url
				/*int page=totalComment>1000?2:(totalComment/500+1);
				for(int i=1;i<=page;i++){
					String commentUrl="https://m.ctrip.com/restapi/soa2/10491/json/GetCommentListAndHotTagList?_fxpcqlniredt=09031048111851431123";
					String param="{\"CommentResultInfoEntity\":{\"BusinessId\":\""+poiId+"\",\"BusinessType\":11,\"PageIndex\":"+i+","
							+ "\"PageSize\":500,\"StarType\":0,\"TouristType\":0,\"SortType\":3,\"ImageFilter\":false,"
							+ "\"CommentTagId\":0,\"ChannelType\":7,\"VideoImageWidth\":700,\"VideoImageHeight\":392},"
							+ "\"head\":{\"cver\":\"1.0\",\"lang\":\"01\",\"auth\":null,\"extension\":[{\"name\":\"protocal\","
							+ "\"value\":\"http\"}]},\"contentType\":\"json\"}";
					Params params = new Params();
					params.setType(Param.CTRIP_STROKE_COMMENT);
					params.setDataSource(Param.CTRIP);
					params.setUrl(commentUrl);
					params.setPostParams(param);
					params.setUuid(uuid);
					params.setHttpType(Param.POST);
					StrokeSpiderStart.queue.add(MISSION_PARAMS, params);
				}*/
				//下面的是获取网页端的评论url，暂时不用
				Integer page=totalComment%10==0?totalComment/10:totalComment/10+1;
				page=page>100?100:page;
				for(int i=1;i<=page;i++){
					String commentUrl="http://you.ctrip.com/destinationsite/TTDSecond/SharedView/AsynCommentView?"+
							"poiID="+poiId+"&districtId="+districtId+"&districtEName="+districteName+
							"&pagenow="+i+"&order=3.0&star=0.0&tourist=0.0&resourceId="+resourceId+"&resourcetype=2";

					//调用activeMQ消息机制
					Params para=new Params();
					para.setUrl(commentUrl);
					para.setUuid(uuid);
					para.setCityName(cityName);
					para.setType(Param.CTRIP_STROKE_COMMENT);
					para.setHttpType(Param.GET);
					para.setDataSource(Param.CTRIP);
					para.setDestinationName("lowQueue");
					produceService.sendMessage(lowQueue,para); 
				}
				// 景点图片
				Elements elements2 = document.select("div#detailCarousel>div.carousel-inner>div.item>a");
				if(!elements2.isEmpty()){
					System.out.println("图片的数目"+elements2.size());
					for(int i = 0; i < elements2.size(); i++){
						Pictureinfo pictureinfo=new Pictureinfo();
						String pictureurl=elements2.get(i).select("img").attr("src");
						//Pictureurl="http://you.ctrip.com/"+Pictureurl;
						System.out.println(pictureurl+"图片链接");
						String pictureid = UUID.randomUUID().toString();
						pictureinfo.setId(pictureid);
						pictureinfo.setInfoid(uuid);
						pictureinfo.setImgurl(pictureurl);
						pictureinfo.setSort(i);
						pictureinfo.setType(1);
						pictureinfo.setDownload(0);
						pictureinfo.setAdminarea("中国,"+cityName);
						pictureinfo.setCreatedate(new Date());
						pictureinfo.setCreator("赵乐");
						pictureinfo.setDatasource("Ctrip");
						pictureinfo.setCreatorid("15736708180");

						String pictureinfoObject = KafkaUtils.parseJsonObject(pictureinfo, 10,1);
						String message3 = downLoadService.sendPost(KafkaUtils.KAFKAURL, pictureinfoObject);

						jedis.insertAndGetId(pictureinfo);
					}
				}
				//获取携程景点想去人数和去过人数的url,用于后面解析获取BeenNum,WantToNum
//				ctripWantToURL(params);
				
				String url=params.getUrl();
				String uuId = params.getUuid();
				String strokeInfo_pageId = url.substring(url.lastIndexOf("/")+1,url.lastIndexOf("."));
				String wantToUrl="http://you.ctrip.com/Destinationsite/SharedComm/ShowGowant?";
				String param="Resource="+strokeInfo_pageId+"&pageType=Sight";

				//调用activeMQ消息机制
				Params para=new Params();
				para.setUrl(wantToUrl);
				para.setPostParams(param);
				para.setUuid(uuId);
				para.setCityName(cityName);
				para.setSceinfo(sceInfo);
				para.setType(Param.CTRIP_STROKE_WANTANDBEEN);
				para.setHttpType(Param.POST);
				para.setDataSource(Param.CTRIP);
				para.setDestinationName("lowQueue");
				produceService.sendMessage(lowQueue,para); 
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			produceService.sendMessage(params.getDestinationName(),params);
		}
	}
	/**
	 * 
	 * @Description 存入携程景点想去人数和去过人数url
	 * @author 赵乐
	 * @date 2018年3月5日 下午3:41:14
	 * @action ctripWantToURL
	 * @param @param params
	 * @return void
	 */
	/*public void ctripWantToURL(Params params) {
		// TODO Auto-generated method stub
		String url=params.getUrl();
		String uuId = params.getUuid();
		String cityName = params.getCityName();
		String strokeInfo_pageId = url.substring(url.lastIndexOf("/")+1,url.lastIndexOf("."));
		String wantToUrl="http://you.ctrip.com/Destinationsite/SharedComm/ShowGowant";
		String param="Resource="+strokeInfo_pageId+"&pageType=Sight";

		//调用activeMQ消息机制
		Params para=new Params();
		para.setUrl(wantToUrl);
		para.setUuid(uuId);
		para.setCityName(cityName);
		para.setType(Param.CTRIP_STROKE_WANTANDBEEN);
		para.setHttpType(Param.POST);
		para.setDataSource(Param.CTRIP);
		para.setDestinationName("lowQueue");
		produceService.sendMessage(lowQueue,para); 
	}
*/

	/**
	 * 
	 * @Description 解析携程景点分页链接获取携程景点种子
	 * @author 赵乐
	 * @date 2018年3月5日 下午2:27:54
	 * @action parseCtripStrokePage
	 * @param @param params
	 * @return void
	 */
	public void parseCtripStrokePage(Params params) {
		// TODO Auto-generated method stub
		String url = params.getUrl();
		String content = params.getContent();
		String cityName = params.getCityName();
		Document document = Jsoup.parse(content);
		//获取景点详情链接
		Elements select = document.select("div.list_mod2>div.rdetailbox>dl>dt>a");
		if(!select.isEmpty()){
			for (Element element : select) {
				String str = element.attr("href");
				if(StringUtils.isNotBlank(str)){
					String href = "http://you.ctrip.com" + str;

					String string = jedis.get(href);
					if(StringUtils.isBlank(string)){
						jedis.set(href, href);
					}else{
						continue;
					}

					//调用activeMQ消息机制
					Params para=new Params();
					para.setUrl(href);
					para.setParentUrl(url);
					para.setCityName(cityName);
					para.setType(Param.CTRIP_STROKE_DETAIL);
					para.setHttpType(Param.GET);
					para.setDataSource(Param.CTRIP);
					para.setDestinationName("highQueue");
					produceService.sendMessage(highQueue,para); 

				}
			}
		}else{
			System.out.println("获取分页链接标签失败，放回队列重新请求");
			produceService.sendMessage(params.getDestinationName(),params);
		}
	}

	/**
	 * 
	 * @Description 解析携程景点获取种子部分的首个url
	 * @author 赵乐
	 * @date 2018年3月5日 上午11:45:05
	 * @action parseCtripStrokeFirst
	 * @param @param params
	 * @return void
	 */
	public void parseCtripStrokeFirst(Params params) {
		// TODO Auto-generated method stub
		String url = params.getUrl();
		String content = params.getContent();
		String cityName = params.getCityName();
		Document document = Jsoup.parse(content);
		//爬取输入省份下的城市景点链接，如海南下面的三亚
		if (url.startsWith("http://you.ctrip.com/countrysightlist")) {
			//判断是不是分页链接
			if(!url.contains("/p")){
				//不是分页链接，即为首页链接，解析取区分页
				String hrefstr=url.substring(0,url.lastIndexOf("."));
				//城市页码总页数
				Elements elements = document.select("div.ttd_pager.cf>div.pager_v1>span>b");
				if(!elements.isEmpty()){
					for (Element element : elements) {
						String pageNumstr = element.text();
						if(StringUtils.isNotBlank(pageNumstr)){
							Integer pageNum=Integer.parseInt(pageNumstr);
							for(int i=1;i<=pageNum;i++){
								String href = hrefstr+"/p"+i+".html";
								//StrokeStart.jedis.addStr(StrokeStart.KEY_URL, href);
								//调用activeMQ消息机制
								
								Params para=new Params();
								para.setCityName(cityName);
								para.setUrl(href);
								para.setParentUrl(url);
								para.setType(Param.CTRIP_STROKE_FIRST);
								para.setHttpType(Param.GET);
								para.setDataSource(Param.CTRIP);
								para.setDestinationName("topQueue");
								produceService.sendMessage(topQueue,para); 

								System.out.println("放入城市分页链接到topQueue"+href);
							}
						}
					}
				}
			}else{
				//获取当前城市页面的所有城市------通过改变url获取城市的景点url
				Elements elements2 = document.select("div.list_mod1>dl>dt>a");
				if(!elements2.isEmpty()){
					for (Element element : elements2) {
						String href=element.attr("href");
						if(StringUtils.isNotBlank(href)){
							String newurl = "http://you.ctrip.com" + href.replace("place", "sight");
							//StrokeStart.jedis.addStr(StrokeStart.KEY_URL, url);

							//调用activeMQ消息机制
							Params para=new Params();
							para.setCityName(cityName);
							para.setUrl(newurl);
							para.setParentUrl(url);
							para.setType(Param.CTRIP_STROKE_FIRST);
							para.setHttpType(Param.GET);
							para.setDataSource(Param.CTRIP);
							para.setDestinationName("topQueue");
							produceService.sendMessage(topQueue,para);

							System.out.println("城市跳转链接"+url);
						}
					}
				}else{
					System.out.println("解析景点详情标签为空，放回队里重新请求");
					produceService.sendMessage(topQueue, params);
				}
			}

		}
		//爬取景点的分页链接
		if (url.startsWith("http://you.ctrip.com/sight/")) {
			//获取景点的总页数
			String hrefstr=url.substring(0,url.lastIndexOf("."));
			Elements pageNumEle = document.select("div.ttd_pager.cf>div.pager_v1>span>b");
			if(!pageNumEle.isEmpty()){
				for (Element element : pageNumEle) {
					String pageNumstr = element.text();
					if(StringUtils.isNotBlank(pageNumstr)){
						Integer pageNum=Integer.parseInt(pageNumstr);
						for(int i=1;i<=pageNum;i++){
							String href = hrefstr+"/s0-p"+i+".html";

							//调用activeMQ消息机制
							Params para=new Params();
							para.setUrl(href);
							para.setParentUrl(url);
							para.setCityName(cityName);
							para.setType(Param.CTRIP_STROKE_PAGE);
							para.setHttpType(Param.GET);
							para.setDataSource(Param.CTRIP);
							para.setDestinationName("topQueue");
							produceService.sendMessage(topQueue,para);

							System.out.println("景点分页链接"+href);
						}
					}
				}
			}else{
				Elements elementsName = document.select("div.list_mod2>div.rdetailbox>dl>dt>a");
				if(!elementsName.isEmpty()){
					String href = hrefstr+"/s0-p1.html";

					//调用activeMQ消息机制
					Params para=new Params();
					para.setUrl(href);
					para.setParentUrl(url);
					para.setCityName(cityName);
					para.setType(Param.CTRIP_STROKE_PAGE);
					para.setHttpType(Param.GET);
					para.setDataSource(Param.CTRIP);
					para.setDestinationName("topQueue");
					produceService.sendMessage(topQueue,para);

					System.out.println("景点分页链接"+href);
				}else{
					System.out.println("解析景点详情标签为空，放回队里重新请求");
					produceService.sendMessage(topQueue, params);
				}
			}
		}


	}


}
