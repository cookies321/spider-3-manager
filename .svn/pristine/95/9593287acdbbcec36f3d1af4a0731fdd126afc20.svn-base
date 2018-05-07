package cn.jj.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class ParseTuniu {
	
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
	 * @Description 解析途牛酒店评论信息
	 * @author 赵乐
	 * @date 2018年3月7日 下午5:00:37
	 * @action parseTuniuHotelComment
	 * @param @param params
	 * @return void
	 */
	public void parseTuniuHotelComment(Params params) {
		// TODO Auto-generated method stub
		String uuid = params.getUuid();
		String content = params.getContent();
		if(StringUtils.isNotBlank(content)){
			try {
				JSONObject jsonObject=new JSONObject(content);
				JSONArray contents=jsonObject.getJSONArray("contents");
				for(int i=0;i<contents.length();i++){
					JSONObject obj=contents.getJSONObject(i);

					JSONObject compTextContent=obj.getJSONObject("compTextContent");
					Commentinfo holyrobotCommentinfo = new Commentinfo();
					//内容
					holyrobotCommentinfo.setContent(obj.toString());
					//评论日期
					String commentDate=compTextContent.getString("updateTime");
					holyrobotCommentinfo.setCommentdate(commentDate);
					//行政区划
					holyrobotCommentinfo.setAdminarea("中国,"+params.getCityName());
					holyrobotCommentinfo.setId(UUID.randomUUID().toString());
					holyrobotCommentinfo.setInfoid(uuid);
					holyrobotCommentinfo.setType(1);
					holyrobotCommentinfo.setCreatedate(new Date());
					holyrobotCommentinfo.setDatasource("Tuniu");
					holyrobotCommentinfo.setCreator("tyl");
					holyrobotCommentinfo.setCreatorid("tyl13564205515");

					//放入kafka队列
					String urlParams = KafkaUtils.parseJsonObject(holyrobotCommentinfo, 11,1);
					String sendPost = downLoadService.sendPost(KafkaUtils.KAFKAURL, urlParams);
					jedis.insertAndGetId(holyrobotCommentinfo);
					System.out.println("评论内容："+obj.toString());
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
	 * 
	 * @Description 解析途牛酒店房型信息
	 * @author 赵乐
	 * @date 2018年3月7日 下午4:59:25
	 * @action parseTuniuHotelRoom
	 * @param @param params
	 * @return void
	 */
	public void parseTuniuHotelRoom(Params params) {
		// TODO Auto-generated method stub
		String uuid = params.getUuid();
		String url = params.getUrl();
		String priceDate = url.substring(url.indexOf("checkindate")+12, url.indexOf("checkoutdate")-1);
		String content=params.getContent();
		int count=1;
		if(StringUtils.isNotBlank(content)){
			try{
				JSONObject jsonObject=new JSONObject(content);
				JSONArray rooms=null;
				try{
					rooms=jsonObject.getJSONArray("rooms");
				}catch(Exception e){
					rooms=null;
				}
				if(rooms!=null){
					//System.out.println(rooms);
					System.out.println("房间列表长度："+rooms.length());
					for(int i=0;i<rooms.length();i++){
						JSONObject obj=rooms.getJSONObject(i);
						Object roomName=null;
						try {
							roomName=obj.get("name");
						} catch (Exception e) {
							// TODO: handle exception
						}
						if(jsonObject.NULL.equals(roomName)){
							roomName="";
						}
						Object roomBedType=null;
						try {
							roomBedType=obj.get("roomBedType");
						} catch (Exception e) {
							// TODO: handle exception
						}
						if(jsonObject.NULL.equals(roomBedType)){
							roomBedType="";
						}

						Object roomBedSize=null;
						try {
							roomBedSize=obj.get("roomBedInfo");
						} catch (Exception e) {
							// TODO: handle exception
						}
						if(jsonObject.NULL.equals(roomBedSize)){
							roomBedSize="";
						}


						Object hasWindowShow=null;
						try {
							hasWindowShow=obj.get("hasWindowShow");
						} catch (Exception e) {
							// TODO: handle exception
						}
						if(jsonObject.NULL.equals(hasWindowShow)){
							hasWindowShow="";
						}


						JSONObject facilities=obj.getJSONObject("facilities");
						//String isaddbed=obj.getString("isExtraBedShow");
						Object isaddbed=facilities.get("加床");
						if(jsonObject.NULL.equals(isaddbed)){
							isaddbed="";
						}
						Integer peopleCount=facilities.getInt("最多入住人数");
						Object floor=null;
						try {
							floor=facilities.get("楼层");
						} catch (Exception e) {
							// TODO: handle exception
						}

						if(jsonObject.NULL.equals(floor)){
							floor="";
						}
						Roombasicinfo roomInfo=new Roombasicinfo();
						String roomInfoId=UUID.randomUUID().toString();
						//行政区划
						roomInfo.setAdminarea("中国,"+params.getCityName());
						roomInfo.setId(roomInfoId);
						roomInfo.setHotelid(uuid);
						roomInfo.setBedtype(roomBedType.toString());
						roomInfo.setBedsize(roomBedSize.toString());
						roomInfo.setFloor(floor.toString());
							
						roomInfo.setIsaddbed(isaddbed.toString());
						roomInfo.setRoomtype(roomName.toString());
						roomInfo.setCreatedate(new Date());
						roomInfo.setCreator("tyl");
						roomInfo.setCreatorid("tyl13564205515");
						roomInfo.setPeoplecount(peopleCount.toString());
						roomInfo.setDatasource("Tuniu");
						
						//放入kafka队列
						String urlParams = KafkaUtils.parseJsonObject(roomInfo, 3,1);
						String sendPost = downLoadService.sendPost(KafkaUtils.KAFKAURL, urlParams);
						//插入redis
						jedis.insertAndGetId(roomInfo);

						JSONArray picArray=null;
						try{
							picArray=obj.getJSONArray("roomPics");
						}catch(Exception e ){
							picArray=null;
						}
						//获取图片
						String roomPicUrl="";
						if(picArray!=null&&picArray.length()>0){
							JSONObject picObj=picArray.getJSONObject(0);
							roomPicUrl=picObj.getString("url");

							Pictureinfo pictureInfo=new Pictureinfo();
							pictureInfo.setId(UUID.randomUUID().toString());
							//行政区划
							pictureInfo.setAdminarea("中国,"+params.getCityName());
							pictureInfo.setInfoid(uuid);
							pictureInfo.setImgurl(roomPicUrl);
							pictureInfo.setSort(count++);
							pictureInfo.setType(5);
							pictureInfo.setDownload(0);
							pictureInfo.setCreatedate(new Date());
							pictureInfo.setCreator("tyl");
							pictureInfo.setDatasource("Tuniu");
							pictureInfo.setCreatorid("tyl13564205515");

							//放入kafka队列
							String urlParams1 = KafkaUtils.parseJsonObject(pictureInfo, 10,1);
							String sendPost2 = downLoadService.sendPost(KafkaUtils.KAFKAURL, urlParams1);
							
							//插入redis
							jedis.insertAndGetId(pictureInfo);
						}
						/*System.out.println("\n房间名称："+roomName+";床型："+roomBedType+";床大小："+roomBedSize+";楼层："+floor+";"
								+ "是否可加床："+isaddbed+";房间可住人数："+peopleCount);
						 */
						//获取价格
						JSONArray ratePlans=obj.getJSONArray("ratePlans");
						for(int k=0;k<ratePlans.length();k++){
							JSONObject roomPriceJson=ratePlans.getJSONObject(k);
							String productName=roomPriceJson.getString("ratePlanName");
							Integer price=roomPriceJson.getInt("averagePrice");
							//是否可预订
							JSONObject operation=roomPriceJson.getJSONObject("operation");
							String order=operation.getString("name");
							String breakfast=roomPriceJson.getString("breakfast");
							//是否可取消
							JSONObject cancleJson=roomPriceJson.getJSONObject("cancel");
							String cancle=cancleJson.getString("name");
							JSONObject optimalData=null;
							try{
								optimalData=roomPriceJson.getJSONObject("optimalData");
							}catch(Exception e ){
								optimalData=null;
							}
							String optimalFlag="";
							if(optimalData!=null){
								optimalFlag=optimalData.getString("optimalFlag");
							}

							String iswifi=roomPriceJson.getString("networkDesc");
							//支付方式
							String payMethod="";
							JSONObject tags=roomPriceJson.getJSONObject("tags");
							boolean prepay=tags.getBoolean("prepay");
							boolean guarantee=tags.getBoolean("guarantee");
							if(prepay){
								payMethod="在线付";
							}else if(guarantee){
								payMethod="担保";
							}else{
								payMethod="到店支付";
							}
							Roomprice roomPrice=new Roomprice();
							roomPrice.setId(UUID.randomUUID().toString());
							//关联房型id
							roomPrice.setRoomid(roomInfoId);
							roomPrice.setHotelid(uuid);
							//行政区划
							roomPrice.setAdminarea("中国,"+params.getCityName());
							roomPrice.setProductname(productName+(StringUtils.isNotBlank(optimalFlag)?"("+optimalFlag+")":""));
							roomPrice.setIscancled(cancle);
							roomPrice.setIshasbreakfast(breakfast);
							roomPrice.setIswifi(iswifi);
							roomPrice.setPrice(price.toString());
							roomPrice.setIsbooking(order);
							roomPrice.setIswindow(hasWindowShow.toString());
							roomPrice.setPaymethod(payMethod);
							roomPrice.setDate(priceDate);
							roomPrice.setCreatedate(new Date());
							roomPrice.setCreator("tyl");
							roomPrice.setCreatorid("tyl13564205515");
							roomPrice.setDatasource("Tuniu");
							
							//放入kafka队列
							String urlParams2 = KafkaUtils.parseJsonObject(roomPrice, 2,1);
							String sendPost2 = downLoadService.sendPost(KafkaUtils.KAFKAURL, urlParams2);
							//放入redis
							jedis.insertAndGetId(roomPrice);
							//System.out.println("产品名称："+productName+";是否可取消："+cancle+";是否含早餐："+breakfast+";是否含wifi："+iswifi
							//		+";价格："+price+";是否可预订："+order+";是否含窗户："+hasWindowShow+";支付方式："+payMethod);
						}
					}
				}
			}catch(Exception e){
				produceService.sendMessage(params.getDestinationName(), params);
			}
		}else {
			System.out.println("未获取到信息，重新放入队列");
			produceService.sendMessage(params.getDestinationName(), params);
		}
	}

	/**
	 * @Description 解析途牛酒店介绍信息
	 * @author 赵乐
	 * @date 2018年3月7日 下午4:58:26
	 * @action parseTuniuHotelIntroduction
	 * @param @param params
	 * @return void
	 */
	public void parseTuniuHotelIntroduction(Params params) {
		// TODO Auto-generated method stub
		String uuid = params.getUuid();
		String url = params.getUrl();
		String content=params.getContent();
		if(StringUtils.isNotBlank(content)){
			try {
				JSONObject jsonObject = new JSONObject(content);
				JSONObject hotel = jsonObject.getJSONObject("data").getJSONObject("hotel");
				if(!JSONObject.NULL.equals(hotel)){
					String introduction = hotel.getString("detail");
					System.out.println("酒店介绍："+introduction);
					Hotelinfo hotelinfo = new Hotelinfo();
					hotelinfo.setId(uuid);
					hotelinfo.setIntroduction(introduction);

					//放入kafka队列
					String urlParams = KafkaUtils.parseJsonObject(hotelinfo, 1,0);
					String sendPost = downLoadService.sendPost(KafkaUtils.KAFKAURL, urlParams);
					jedis.insertAndGetId(hotelinfo);
				}
			} catch (Exception e) {
				produceService.sendMessage(params.getDestinationName(), params);
			}
		}
	}


	/**
	 * 
	 * @Description 解析途牛酒店图片信息
	 * @author 赵乐
	 * @date 2018年3月7日 下午4:56:31
	 * @action parseTuniuHotelPicture
	 * @param @param params
	 * @return void
	 */
	public void parseTuniuHotelPicture(Params params) {
		// TODO Auto-generated method stub
		String uuid = params.getUuid();
		String url = params.getUrl();
		String content=params.getContent();
		if(StringUtils.isNotBlank(content)){
			try {
				JSONArray jsonArray =null;
				try {
					JSONObject jsonObject=new JSONObject(content);
					JSONObject jsonObject2 = jsonObject.getJSONObject("data");
					jsonArray = jsonObject2.getJSONArray("all");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(jsonArray!=null){
					int length = jsonArray.length();
					length =length>10?10:length;
					for (int i=0;i<length;i++) {
						JSONObject obj = (JSONObject) jsonArray.get(i);
						String imgsrc = obj.getString("big");

						Pictureinfo picture=new Pictureinfo();
						String pictureId=UUID.randomUUID().toString();
						picture.setId(pictureId);
						picture.setInfoid(uuid);
						//行政区划
						picture.setAdminarea("中国,"+params.getCityName());
						picture.setImgurl(imgsrc);
						picture.setSort(i+1);
						picture.setType(4);
						picture.setInfoid(uuid);
						picture.setDownload(0);
						picture.setCreatedate(new Date());
						picture.setCreator("tyl");
						picture.setCreatorid(creatorID);
						picture.setDatasource("Tuniu");
						
						//放入kafka队列
						String urlParams = KafkaUtils.parseJsonObject(picture, 10,1);
						String sendPost = downLoadService.sendPost(KafkaUtils.KAFKAURL, urlParams);
						
						jedis.insertAndGetId(picture);

						System.out.println("图片地址："+imgsrc);
					}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}


	/**
	 * 
	 * @Description 解析途牛酒店详情信息
	 * @author 赵乐
	 * @date 2018年3月7日 下午4:56:02
	 * @action parseTuniuHotelDetail
	 * @param @param params
	 * @return void
	 */
	public void parseTuniuHotelDetail(Params params) {
		// TODO Auto-generated method stub
		System.out.println("途牛房型");
		String uuid = params.getUuid();
		String url = params.getUrl();
		String content = params.getContent();
		//获取途牛房型床型url
		String price = params.getHotelInfo().getPrice();
		if (StringUtils.isNotBlank(price)&&Integer.parseInt(price)>0) {
			String productId=url.substring(url.lastIndexOf("/")+1, url.length());
			
			
			//获取途牛图片url的信息
			String pictureUrl="http://hotel.tuniu.com/ajax/hotelPictures?hotelId="+productId+"&from=detail";
			Params para=new Params();
			para.setUuid(uuid);
			para.setUrl(pictureUrl);
			para.setCityName(params.getCityName());
			para.setType(Param.TUNIU_HOTEL_PICTURE);
			para.setDataSource(Param.TUNIU);
			para.setHttpType(Param.GET);
			System.out.println("添加途牛图片url");
			
			para.setDestinationName("lowQueue");
			produceService.sendMessage(lowQueue,para);

			//添加途牛酒店房型链接
			Map<String,String> header=new HashMap<String, String>();
			header.put("X-Requested-With", "XMLHttpRequest");
			for(int i=0;i<dateList.size()-1;i++){
				String roomUrl="http://hotel.tuniu.com/ajax/hotelRooms?"
						+ "id="+productId+"&checkindate="+dateList.get(i)+"&checkoutdate="+dateList.get(i+1);
				Params par=new Params();
				par.setUuid(uuid);
				par.setUrl(roomUrl);
				par.setCityName(params.getCityName());
				par.setType(Param.TUNIU_HOTEL_ROOM);
				par.setDataSource(Param.TUNIU);
				par.setHttpType(Param.GET);
				par.setHeader("X-Requested-With", "XMLHttpRequest");
				
				par.setDestinationName("lowQueue");
				produceService.sendMessage(lowQueue,par);
			}
		}
		
		
		
		//途牛酒店评论数据获取
		String productId=url.substring(url.lastIndexOf("/")+1, url.length());
		if(StringUtils.isNotBlank(content)){
			try {
				//途牛酒店介绍数据获取
				String introductionUrl = "http://hotel.tuniu.com/ajax/getHotelStaticInfo?id="+productId;

				Params para=new Params();
				para.setUuid(uuid);
				para.setUrl(introductionUrl);
				para.setCityName(params.getCityName());
				para.setType(Param.TUNIU_HOTEL_INTRODUCTION);
				para.setDataSource(Param.TUNIU);
				para.setHttpType(Param.GET);
				
				para.setDestinationName("lowQueue");
				produceService.sendMessage(lowQueue,para);

				//获取酒店部分数据
				Hotelinfo basicInfo=params.getHotelInfo();
				//行政区划
				basicInfo.setAdminarea("中国,"+params.getCityName());
				basicInfo.setId(uuid);
				basicInfo.setUrlid(url);
				basicInfo.setCreatedate(new Date());
				basicInfo.setCreator("tyl");
				basicInfo.setCreatorid(creatorID);
				basicInfo.setDatasource("Tuniu");
				//存入redis
				System.out.println("存入酒店部分信息");

				//放入kafka队列
				String urlParams = KafkaUtils.parseJsonObject(basicInfo, 1,1);
				String sendPost = downLoadService.sendPost(KafkaUtils.KAFKAURL, urlParams);
				jedis.insertAndGetId(basicInfo);


				Addressinfo addressInfo=params.getAddressinfo();
				addressInfo.setId(UUID.randomUUID().toString());
				addressInfo.setInfoid(uuid);
				addressInfo.setType(2);
				addressInfo.setCountry("中国");
				addressInfo.setCreatedate(new Date());
				addressInfo.setCreator("tyl");
				addressInfo.setCreatorid(creatorID);
				addressInfo.setDatasource("Tuniu");
				System.out.println("存入途牛四级地址信息");

				//放入kafka队列
				String urlParams1 = KafkaUtils.parseJsonObject(addressInfo, 12,1);
				String sendPost1 = downLoadService.sendPost(KafkaUtils.KAFKAURL, urlParams1);
				jedis.insertAndGetId(addressInfo);

			} catch (Exception e) {
				e.printStackTrace();
				produceService.sendMessage(params.getDestinationName(), params);
			}
			
			try {
				Document doc=Jsoup.parse(content);
				Elements elements=doc.select("div.new-cominfo>div.comstati.clearfix>div.com-btns>a.JS-comwrite.comwrite");
				String commenthref=elements.attr("href");
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
					Params par=new Params();
					par.setUuid(uuid);
					par.setUrl(commentUrl);
					par.setCityName(params.getCityName());
					par.setHttpType(Param.GET);
					par.setType(Param.LVMAMA_HOTEL_COMMENT);
					par.setDataSource(Param.LVMAMA);
					
					par.setDestinationName("lowQueue");
					produceService.sendMessage(topQueue,par);
				}
			} catch (Exception e) {
				e.printStackTrace();
				produceService.sendMessage(params.getDestinationName(), params);
			}
		}
		
	}
	/**
	 * 
	 * @Description 解析途牛酒店分页链接
	 * @author 赵乐
	 * @date 2018年3月7日 下午3:37:53
	 * @action parseTuniuHotelPage
	 * @param @param params
	 * @return void
	 */
	public void parseTuniuHotelPage(Params params) {
		// TODO Auto-generated method stub
		//获取分页url
		String pageUrl = params.getUrl();
		String cityName = pageUrl.substring(pageUrl.indexOf("cityName")+9, pageUrl.length());
		//获取下载的信息
		String content = params.getContent();
		try {
			//获取酒店详情url
			JSONObject jsonObject=new JSONObject(content);
			boolean success=jsonObject.getBoolean("success");
			if(success){
				JSONObject data=jsonObject.getJSONObject("data");
				JSONArray datalist=data.getJSONArray("list");
				System.out.println("本页酒店url总数为："+datalist.length());
				for(int j=0;j<datalist.length();j++){
					Params para = new Params();
					JSONObject obj=datalist.getJSONObject(j);
					Integer num=obj.getString("url").indexOf("?");
					//获取酒店url
					String url="";
					if(num!=-1){
						url="http://hotel.tuniu.com"+obj.getString("url").substring(0,num);  
						//System.out.println(url+"------途牛酒店url");
					}else{
						url="http://hotel.tuniu.com"+obj.getString("url");
						//System.out.println(url+"------途牛酒店url");
					}
					/**
					 * 判断
					 */
					String string = jedis.get(url);
					if(StringUtils.isBlank(string)){
						jedis.set(url, url);
					}else{
						continue;
					}

					String hotelName=obj.getString("name");
					JSONObject levelInfo=obj.getJSONObject("levelInfo");
					String star=levelInfo.getString("name");
					String addr=obj.getString("address");
					JSONObject pos=obj.getJSONObject("pos");
					String lng=pos.getString("lng");
					String lat=pos.getString("lat");
					Double remarkScore=obj.getDouble("remarkScore");
					Integer remarkCount=obj.getInt("remarkCount");
					Integer price=obj.getInt("startPrice");

					para.setParentUrl(pageUrl);
					para.setUrl(url);
					Hotelinfo basicInfo=new Hotelinfo();
					basicInfo.setName(hotelName);
					basicInfo.setAddress(addr);
					basicInfo.setLongitude(lng);
					basicInfo.setLatitude(lat);
					basicInfo.setPrice(price==null?"":price.toString());
					basicInfo.setGrade(remarkScore.toString());
					basicInfo.setStar(star);
					basicInfo.setDatasource("Tuniu");
					basicInfo.setGradenum(remarkCount.toString());

					para.setHotelInfo(basicInfo);

					Addressinfo addressinfo = new Addressinfo();
					addressinfo.setCity(cityName);
					addressinfo.setDetailaddress(addr);
					addressinfo.setProvince(StringUtils.isNotBlank(cityName)?(cityName.equals("上海")?"上海":"海南"):"");
					
					para.setAddressinfo(addressinfo);
					
					para.setType(Param.TUNIU_HOTEL_DETAIL);
					para.setDataSource(Param.TUNIU);
					para.setCityName(params.getCityName());
					para.setHttpType(Param.GET);
					para.setDestinationName("highQueue");
					produceService.sendMessage(highQueue,para);
					
					
					
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			produceService.sendMessage(params.getDestinationName(), params);
		}
	}
	/**
	 * 
	 * @Description TODO
	 * @author 赵乐
	 * @date 2018年3月7日 下午3:37:10
	 * @action parseTuniuHotelFirst
	 * @param @param params
	 * @return void
	 */
	public void parseTuniuHotelFirst(Params params) {
		// TODO Auto-generated method stub
		SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd");
		String url = params.getUrl();
		String content = params.getContent();
		//抓取酒店总个数url，数据为异步
		if(url.startsWith("http://hotel.tuniu.com/ajax/destSuggestion")){
			if(StringUtils.isNotBlank(content)){
				try {
					content=content.substring(1, content.length()-1);//去除前后括号
					JSONObject jsonObject = new JSONObject(content);
					JSONArray suggestions = jsonObject.getJSONArray("suggestions");
					if(suggestions.length()>0){
						JSONObject obj = suggestions.getJSONObject(0);
						Integer cityCode = obj.getInt("cityCode");
						String cityName = obj.getString("cityName");
						String cityUrl = "http://hotel.tuniu.com/list?city="+cityCode+"&cityName="+cityName;
						
						//调用activeMQ消息机制
						Params para=new Params();
						para.setUrl(cityUrl);
						para.setCityName(cityName);
						para.setType(Param.TUNIU_HOTEL_FIRST);
						para.setDataSource(Param.TUNIU);
						para.setHttpType(Param.GET);
						para.setDestinationName("topQueue");
						produceService.sendMessage(topQueue,para);

					}
				} catch (Exception e) {
					// TODO: handle exception
				}
				
			}
		}else if(url.startsWith("http://hotel.tuniu.com/list")){
			
			Integer cityId=NumUtils.getInteger(url);
			String cityName = url.substring(url.indexOf("cityName")+9, url.length());
			String firstPageUrl="http://hotel.tuniu.com/ajax/list?search%5BcityCode%5D="+cityId+""
						+ "&search%5BcheckInDate%5D="+df.format(new Date())+"&search%5BcheckOutDate%5D="+DateUtil.getEndDate(df.format(new Date()), 1)+"&search%5Bkeyword%5D="
						+ "&suggest=&sort%5Bfirst%5D%5Bid%5D=recommend&sort%5Bfirst%5D%5Btype%5D=&sort%5Bsecond%5D="
						+ "&sort%5Bthird%5D=cash-back-after&page=1&returnFilter=0&cityName="+cityName;
			//调用activeMQ消息机制
			Params para=new Params();
			para.setUrl(firstPageUrl);
			para.setType(Param.TUNIU_HOTEL_FIRST);
			para.setCityName(cityName);
			para.setDataSource(Param.TUNIU);
			para.setHttpType(Param.GET);
			para.setDestinationName("topQueue");
			produceService.sendMessage(topQueue,para);
			
		}
		//获取第一页数据
		else if(url.startsWith("http://hotel.tuniu.com/ajax/list?search%5BcityCode%5D=") && url.contains("suggest")){
			String cityName = url.substring(url.indexOf("cityName")+9, url.length());
			String cityId=url.substring(url.indexOf("cityCode")+12, url.indexOf("checkInDate")-10);
			System.out.println(cityId);
			int total = 0; 
			if(StringUtils.isNotBlank(content)){
				try {
					JSONObject json = new JSONObject(content);
					JSONObject data=json.getJSONObject("data");
					total=data.getInt("total");
				} catch (Exception e) {
					produceService.sendMessage(params.getDestinationName(), params);
					return;
				}
				
			}
			Integer pageNum=total%20==0?total/20:(total/20)+1;
			for(int i=1;i<=pageNum;i++){
				String totalNumHref="http://hotel.tuniu.com/ajax/list?search%5BcityCode%5D="+cityId+""
						+ "&search%5BcheckInDate%5D="+df.format(new Date())+"&search%5BcheckOutDate%5D="+DateUtil.getEndDate(df.format(new Date()), 1)+"&search%5Bkeyword%5D="
						+ "&sort%5Bfirst%5D%5Bid%5D=recommend&sort%5Bfirst%5D%5Btype%5D=&sort%5Bsecond%5D="
						+ "&sort%5Bthird%5D=cash-back-after&page="+i+"&returnFilter=0&cityName="+cityName;
				System.out.println("分页链接："+totalNumHref);
				
				//调用activeMQ消息机制
				Params para=new Params();
				para.setUrl(totalNumHref);
				para.setType(Param.TUNIU_HOTEL_PAGE);
				para.setCityName(cityName);
				para.setDataSource(Param.TUNIU);
				para.setHttpType(Param.GET);
				para.setDestinationName("topQueue");
				produceService.sendMessage(topQueue,para);

			}
		}
	}
	/**
	 * 
	 * @Description 解析途牛行程评论信息
	 * @author 赵乐
	 * @date 2018年3月7日 下午3:17:33
	 * @action parseTuniuRouteComment
	 * @param @param params
	 * @return void
	 */
	public void parseTuniuRouteComment(Params params) {
		// TODO Auto-generated method stub
		String uuid = params.getUuid();
		String content = params.getContent();
		if(StringUtils.isNotBlank(content)){
			try {
				JSONObject comJSON = new JSONObject(content);
				String data = comJSON.get("data").toString();
				JSONObject jsonData = new JSONObject(data);
				String comList = jsonData.get("list").toString();
				JSONArray comArr = new JSONArray(comList);
				for (int k = 0; k < comArr.length(); k++) {
					try {
						JSONObject comment = new JSONObject(comArr.get(k).toString());
						//获取评论时间
						String commentDate = comment.get("remarkTime").toString();
						Commentinfo commentInfo = new Commentinfo();
						commentInfo.setId(UUID.randomUUID().toString());
						commentInfo.setInfoid(uuid);				
						//行政区划
						commentInfo.setAdminarea("中国,"+params.getCityName());
						commentInfo.setType(3);    //类型：1-景点，2-酒店，3-景点行程
						commentInfo.setCreatedate(new Date());
						commentInfo.setDatasource("Tuniu");
						commentInfo.setCreator("姚良良");
						commentInfo.setCreatorid("13783985208");
						commentInfo.setContent(comment.toString());
						commentInfo.setCommentdate(commentDate);
						//放入kafka队列
						String urlParams = KafkaUtils.parseJsonObject(commentInfo, 11,1);
						String sendPost = downLoadService.sendPost(KafkaUtils.KAFKAURL, urlParams);
						
						jedis.insertAndGetId(commentInfo);
					}catch(Exception e){

					}
				}
			} catch (Exception e) {
				produceService.sendMessage(params.getDestinationName(), params);
			}
		}else{
			System.out.println("未获取到评论，重新放入队列");
			produceService.sendMessage(params.getDestinationName(), params);
		}
	}
	/**
	 * 
	 * @Description 解析途牛行程价格信息
	 * @author 赵乐
	 * @date 2018年3月7日 下午3:17:52
	 * @action parseTuniuRoutePrice
	 * @param @param params
	 * @return void
	 */
	public void parseTuniuRoutePrice(Params params) {
		// TODO Auto-generated method stub
		String uuid = params.getUuid();
		String url = params.getUrl();
		if(url.endsWith("&bookCityCode=&departCityCode=&backCityCode=")){
			System.err.println("无效连接");
		}else{
			String content = params.getContent();
			if(StringUtils.isNotBlank(content)){
				try {
					//对JSON数据进行解析
					JSONObject json = new JSONObject(content);
					String data = json.get("data").toString();
					JSONObject dataJson = new JSONObject(data);
					String calendars = dataJson.get("calendars").toString();
					JSONArray calendarsArr = new JSONArray(calendars);
					//记录保存行程价钱的天数
					int k = 0;
					System.out.println(url+" 的行程价钱共有 "+calendarsArr.length()+" 条!");
					for (int i = 0; i < calendarsArr.length(); i++) {
						JSONObject priceDateJson = new JSONObject(calendarsArr.get(i).toString());
						String priceDate = priceDateJson.get("departDate").toString();
						String lowestPrice = priceDateJson.get("startPrice").toString();
						String dayOfWeek = DateUtil.dateToWeek(priceDate);
						//创建HolyrobotRoutepriceinfo对象并设置相关属性
						Routepriceinfo routepriceInfo = new Routepriceinfo();
						routepriceInfo.setId(UUID.randomUUID().toString());
						routepriceInfo.setRouteid(uuid);
						routepriceInfo.setDestination(params.getCityName());
						routepriceInfo.setPricedate(priceDate);
						routepriceInfo.setLowestprice(lowestPrice);
						routepriceInfo.setDayofweek(dayOfWeek);
						routepriceInfo.setCreator("姚良良");
						routepriceInfo.setCreatorid("13783985208");
						routepriceInfo.setCreatedate(new Date());
						routepriceInfo.setDatasource("Tuniu");
						

						//放入kafka队列
						String urlParams = KafkaUtils.parseJsonObject(routepriceInfo, 7,1);
						String sendPost = downLoadService.sendPost(KafkaUtils.KAFKAURL, urlParams);
						
						jedis.insertAndGetId(routepriceInfo);
						k++;
					}	
					System.out.println(url+" 的行程价钱共有 "+calendarsArr.length()+" 条,共获取"+k+"条!");
				} catch (Exception e) {
					produceService.sendMessage(params.getDestinationName(), params);
				}
			}else {
				System.out.println("未获取到信息，重新放入队列");
				produceService.sendMessage(params.getDestinationName(), params);
			}
		}
	}
	/**
	 * 
	 * @Description 解析途牛行程详情
	 * @author 赵乐
	 * @date 2018年3月7日 下午2:43:42
	 * @action parseTuniuRouteDetail
	 * @param @param params
	 * @return void
	 */
	public void parseTuniuRouteDetail(Params params) {
		// TODO Auto-generated method stub
		String uuid = params.getUuid();
		String url = params.getUrl();
		String content = params.getContent();
		//对页面实体进行Jsoup解析
		try {
			Document doc = Jsoup.parse(content);

			//创建HolyrobotRouteinfoWithBLOBs对象并设置相关属性
			Routeinfo routeInfo = new Routeinfo();
			routeInfo.setId(uuid);
			routeInfo.setUrlid(url);
			routeInfo.setDatasource("Tuniu");
			routeInfo.setCreator("姚良良");
			routeInfo.setCreatorid("13783985208");
			routeInfo.setDestination(params.getCityName());//通过种子链接传过来的目的地
			routeInfo.setCreatedate(new Date());
			//获取行程的名字,跟团或自助游或半自助游
			Elements nameEle = doc.select("div.product-body.product-tour>div.product-body-inner>div.resource>h1.resource-title>strong");
			if(nameEle.isEmpty()){
				//自驾游
				nameEle = doc.select("div.wrapper_bg>div.wrapper>div.product_info>div.product_name_bar>h1");
			}	
			if(nameEle.isEmpty()){
				nameEle = doc.select("div.product-body.product-tour>div.product-body-inner>div.m-head>div.m-title>h1.title>strong");
			}
			//当地游乐
			if(nameEle.isEmpty()){
				nameEle = doc.select("div.main_top>div.top_tit>h1");
			}
			if(nameEle.isEmpty()){
				System.out.println(url+"行程的名字获取失败!==================================================");
				
				System.out.println("数据获取不完整");
				produceService.sendMessage(params.getDestinationName(), params);
			}else{
				String name = nameEle.first().text();
				System.out.println("行程的名字为  :"+name);

				routeInfo.setName(name);

				//获取行程类型
				Elements teamTypeEle = doc.select("div.resource-title-sub>i.resource-tag");
				if(teamTypeEle.isEmpty()){
					teamTypeEle = doc.select("div.subtitle>i.tag");
				}
				if(!teamTypeEle.isEmpty()){
					String teamType = teamTypeEle.text();
					routeInfo.setTeamtype(teamType);
				}else{
					teamTypeEle = doc.select("div.product_name_tips>span.icon_style_driver");
					if(!teamTypeEle.isEmpty()){
						String teamType = teamTypeEle.text();
						if(teamType != null && !"".equals(teamType)){
							routeInfo.setTeamtype(teamType);
						}else{
							String teamTypeStr = teamTypeEle.attr("class");
							System.out.println(teamTypeStr);
							if("icon_style_driver".equals(teamTypeStr)){
								teamType = "自驾游";
								System.out.println(teamType);
								routeInfo.setTeamtype(teamType);
							}
						}						
					}else{
						//当地游乐
						teamTypeEle = doc.select("div.tours-sub-info.clearfix>div.ser_sm.fl>span.style_tour");
						if(!teamTypeEle.isEmpty()){
							String teamType = "当地玩乐";
							System.out.println(teamType);
							routeInfo.setTeamtype(teamType);
						}
						
					}
				}

				//获取行程价钱
				Elements priceEle = doc.select("div.resource-section-content>span.price-quantity>span.price-number");
				if(priceEle.isEmpty()){
					priceEle = doc.select("div.price_bar>p>span.sale_price");
				}
				if(priceEle.isEmpty()){
					priceEle = doc.select("div.head-section-content>span.price-quantity>span.price-number");
				}
				if(priceEle.isEmpty()){
					priceEle = doc.select("div.new_price>p.promotion>span.price");
				}if(priceEle.isEmpty()){
					priceEle = doc.select("div.tour-price>div.local-promotion-price>span.price");
				}
				if(!priceEle.isEmpty()){
					String price = priceEle.first().text();
					System.out.println("行程的价钱为:"+price);
					routeInfo.setPrice(price);
				}

				//获取产品编号
				String itemNo = url.substring(url.lastIndexOf("/")+1, url.length());
				System.out.println("行程的编号为:"+itemNo);
				routeInfo.setItemno(itemNo);

				//获取供应商
				Elements supplierNameEle = doc.select("div.resource-title-sub>span.reource-vendor");
				if(supplierNameEle.isEmpty()){
					supplierNameEle = doc.select("div.product_name_tips>span:nth-child(3)");
				}
				if(supplierNameEle.isEmpty()){
					supplierNameEle = doc.select("div.subtitle>span.vendor");
				}
				if(supplierNameEle.isEmpty()){
					supplierNameEle = doc.select("div.tours-sub-info.clearfix>div.ser_sm.fl>span:nth-child(3)");
				}
				if(!supplierNameEle.isEmpty()){
					String supplierName = supplierNameEle.text();
					routeInfo.setSuppliername(supplierName);
					System.out.println("供应商为: "+supplierName);
				}

				//获取出发地
				Elements departureEle = doc.select("div#J_ResourceDepartCity>div.resource-city-more-label>div.resource-city-more-selected");
				if(departureEle.isEmpty()){
					departureEle = doc.select("div#J_cityList>div.J_cityItem.u-checkbox.active");
				}
				if(departureEle.isEmpty()){
					departureEle = doc.select("dd#selectDepartDate>div.select_con>p.select_result");
				}
				if(departureEle.isEmpty()){
					departureEle = doc.select("div.resource-section-item.resource-city.resource-city-depart>div.resource-section-content>div.resource-city-more>div.resource-city-more-label>div.resource-city-more-selected");
				}
				if (!departureEle.isEmpty()) {
					String departure = departureEle.text().substring(0,2);
					routeInfo.setDeparture(departure);
				}			

				//获取特色服务
				Elements featureServiceEle = doc.select("div#J_basisFeature");
				if(!featureServiceEle.isEmpty()){
					String featureService = featureServiceEle.select("script").html();
					routeInfo.setFeatureservice(featureService);
				}else{
					featureServiceEle = doc.select("div#J_ResourceFeature>div.resource-section-content>div.resource-feature-content-inner>"
							+ "div.resource-feature-list");
					if(!featureServiceEle.isEmpty()){
						String featureService = featureServiceEle.text();
						routeInfo.setFeatureservice(featureService);
					}
				}

				//获取行程概要
				Elements itineraryOutlineEle = doc.select("div#J_ResourceJourney>div.resource-section-box>div.resource-section-item>"
						+ "div.resource-section-content>div.resource-section-content-inner");
				if(!itineraryOutlineEle.isEmpty()){
					String itineraryOutline = itineraryOutlineEle.text();// 行程概要
					routeInfo.setItineraryoutline(itineraryOutline);
				}

				//获取产品推荐
				Elements productRecommendEle = doc.select("div#J_ResourceRecommend>div.resource-section-content>div.resource-recommend-content-outer>"
						+ "div.resource-recommend-content-inner");
				if(productRecommendEle.isEmpty()){
					productRecommendEle = doc.select("div.recommend>ul.recommend_list");
				}
				if(productRecommendEle.isEmpty()){
					productRecommendEle = doc.select("div.section-box-body>div.J_productDetail_recommendReason");
				}
				if(productRecommendEle.isEmpty()){
					productRecommendEle = doc.select("div.tour-desc>div.d-content.jinglituijian");
				}
				if(!productRecommendEle.isEmpty()){
					String productRecommend = productRecommendEle.text();
					routeInfo.setProductrecommend(productRecommend);
				}			

				//获取优惠信息
				Elements reducedPriceEle = doc.select("div#J_Detail>div.detail-sections>div.J_DetailFavor.detail-favor>div.section-box");
				if(reducedPriceEle.isEmpty()){
					reducedPriceEle = doc.select("div#yhhd>div.block_content>ul.favor_list");					
				}
				if(!reducedPriceEle.isEmpty()){
					String reducedPrice = reducedPriceEle.first().text();
					routeInfo.setReducedprice(reducedPrice);
				}

				//获取产品特色（产品详情）
				Elements productFeatureEle = doc.select("div#J_Detail>div.detail-sections>div.J_DetailFeature.section-box.detail-feature");
				if(productFeatureEle.isEmpty()){
					productFeatureEle = doc.select("div#cpts");
				}
				if(productFeatureEle.isEmpty()){
					productFeatureEle = doc.select("div.section-box-body>div.J_productDetail_productIntroduce");
				}
				if(!productFeatureEle.isEmpty()){
					String productFeature = productFeatureEle.text();
					routeInfo.setProductfeature(productFeature);
				}

				//获取线路介绍
				Elements itineraryDetailsEle = doc.select("div#J_Detail>div.detail-sections>div.J_DetailRoute.section-box.detail-route.detail-route4");
				if(itineraryDetailsEle.isEmpty()){
					itineraryDetailsEle = doc.select("div#cpts");
				}
				if(itineraryDetailsEle.isEmpty()){
					itineraryDetailsEle = doc.select("div.J_DetailRoute.section-box.detail-route.detail-route3");
				}
				if(itineraryDetailsEle.isEmpty()){
					//定制游
					itineraryDetailsEle = doc.select("div#tjxc");
				}
				if(!itineraryDetailsEle.isEmpty()){
					String itineraryDetails = itineraryDetailsEle.html();
					routeInfo.setItinerarydetails(itineraryDetails);
				}

				//获取费用说明
				Elements expenseEle = doc.select("div#J_Detail>div.detail-sections>div.J_DetailFee.section-box.detail-upgrade");
				if(expenseEle.isEmpty()){
					expenseEle = doc.select("div#fysm");
				}
				if(expenseEle.isEmpty()){
					expenseEle = doc.select("div.J_PkgInstruction.section-box.product-box-instruction");
				}
				if(!expenseEle.isEmpty()){
					String expense = expenseEle.text();// 费用说明
					routeInfo.setExpense(expense);
				}

				//获取预定须知				
				Elements reserveInfoEle = doc.select("div#J_Detail>div.detail-sections>div.J_DetailPolicy.section-box.detail-policy");
				if(reserveInfoEle.isEmpty()){
					reserveInfoEle = doc.select("div#ydxz");
				}
				if(reserveInfoEle.isEmpty()){
					reserveInfoEle = doc.select("div.J_PkgInstruction.section-box.product-box-instruction>div.section-box-body>div.J_pkgInstruction_reserveNotice");
				}
				if(!reserveInfoEle.isEmpty()){
					String reserveInfo = reserveInfoEle.text();// 预定须知
					routeInfo.setReserveinfo(reserveInfo);
				}

				//获取评分
				Elements gradeEle = doc.select("div.resource-count>div.resource-statisfaction>a");
				if(gradeEle.isEmpty()){
					gradeEle = doc.select("div.atisfaction_bar>p>span");
				}
				if(gradeEle.isEmpty()){
					gradeEle = doc.select("div.product_static_bar.J_ProductStatic>div.static>p.degree");
				}
				//当地游玩
				if(gradeEle.isEmpty()){
					gradeEle = doc.select("div.local-promotion-price>span.right>span");
				}
				if(!gradeEle.isEmpty()){
					String grade = gradeEle.text();
					routeInfo.setGrade(grade);
				}	

				//获取评论个数
				/*Elements gradeNumEle = doc.select("div.resource-people>div.resource-people-item:nth-child(2)>a.resource-people-number");
				if(gradeNumEle.isEmpty()){
					gradeNumEle = doc.select("div.product_static_bar.J_ProductStatic>div.criticism>p.degree");
				}
				if(!gradeNumEle.isEmpty()){
					String gradeNum = gradeNumEle.text();
					if("新品上线".equals(gradeNum)){
						routeInfo.setGradenum("0");
					}else{
						routeInfo.setGradenum(gradeNum);
						System.out.println("点评人数: "+gradeNum);
					}
					
				}else{
					gradeNumEle = doc.select("div.atisfaction_bar>p:nth-child(2)");
					if(!gradeNumEle.isEmpty()){
						String gradeNumStr = gradeNumEle.text();
						String gradeNum = NumUtils.getInteger(gradeNumStr).toString();
						
						if("新品上线".equals(gradeNum)){
							routeInfo.setGradenum("0");
						}else{
							routeInfo.setGradenum(gradeNum);
							System.out.println("点评人数: "+gradeNum);
						}
					}
				}*/
				//游客点评中获取
				String gradeNum="0";
				
				Elements gradeNumEle = doc.select("div#J_DetailTab>div.detail-tab-box.fixed>div.detail-tab-list>div[data-rel=#J_Comment]");
				if(gradeNumEle.isEmpty()){
					gradeNumEle = doc.select("ul#pkg-detail-tab-bd>li[ref=dpjl]>a");
				}
				if(gradeNumEle.isEmpty()){
					//防止不适合游客点评模板
					Elements gradeNumEle1 = doc.select("div.resource-people>div.resource-people-item:nth-child(2)>a.resource-people-number");
					if(gradeNumEle1.isEmpty()){
						gradeNumEle1 = doc.select("div.product_static_bar.J_ProductStatic>div.criticism>p.degree");
					}
					if(!gradeNumEle1.isEmpty()){
						String gradeNum1 = gradeNumEle1.text();
						if("新品上线".equals(gradeNum1)){
							routeInfo.setGradenum("0");
						}else{
							routeInfo.setGradenum(gradeNum1);
							System.out.println("点评人数: "+gradeNum1);
						}
						
					}else{
						gradeNumEle1 = doc.select("div.atisfaction_bar>p:nth-child(2)");
						if(!gradeNumEle1.isEmpty()){
							String gradeNumStr = gradeNumEle1.text();
							String gradeNum1 = NumUtils.getInteger(gradeNumStr).toString();
							
							if("新品上线".equals(gradeNum1)){
								routeInfo.setGradenum("0");
							}else{
								routeInfo.setGradenum(gradeNum1);
								System.out.println("点评人数: "+gradeNum1);
							}
						}
					}
					
				}else{
					gradeNum=NumUtils.getInteger(gradeNumEle.text())+"";
					routeInfo.setGradenum(gradeNum);
				}
				
				
				//获取出游人数
				Elements beenNumEle = doc.select("div.resource-people>div.resource-people-item:nth-child(1)>a.resource-people-number");
				if(beenNumEle.isEmpty()){
					beenNumEle = doc.select("div.product_static_bar.J_ProductStatic>div.viewer>p.degree");
				}
				if(!beenNumEle.isEmpty()){
					String beenNum = beenNumEle.first().text();
					if("新品上线".equals(beenNum)){
						beenNum="0";
					}
					routeInfo.setBeennum(beenNum);
					System.out.println("出游人数: "+beenNum);
				}else{
					beenNumEle = doc.select("div.head-section-text>span:nth-child(2)");
					if(!beenNumEle.isEmpty()){
						String beenNumStr = beenNumEle.first().text();
						String beenNum = (NumUtils.getInteger(beenNumStr)).toString();
						if("新品上线".equals(beenNum)){
							beenNum="0";
						}
						routeInfo.setBeennum(beenNum);
						System.out.println("出游人数: "+beenNum);
					}
				}
				//获取产品编号，存入route的otherInformation中
				System.out.println("url:"+url);
				String bookCityCode="";
				try {
					String script=doc.select("script").html();
					String scriptEle=script.substring(script.indexOf("bookCityCode:"), script.indexOf("backCityCode:"));
					bookCityCode=scriptEle.substring(13, scriptEle.indexOf(","));
					System.out.println("行程价格编号："+bookCityCode);
				} catch (Exception e) {
					
				}
				
				params.setOtherInformation(bookCityCode);
				params.setRouteInfo(routeInfo);
				
				if(StringUtils.isBlank(routeInfo.getName())){
					System.out.println("数据获取不完整");
					produceService.sendMessage(params.getDestinationName(), params);
				
				}else{
					//放入kafka队列
					String urlParams = KafkaUtils.parseJsonObject(routeInfo, 6,1);
					String sendPost = downLoadService.sendPost(KafkaUtils.KAFKAURL, urlParams);
					jedis.insertAndGetId(routeInfo);

					//获取行程图片集合
					Elements pictureEle = doc.select("div.gallery-thumbs>div.gallery-nav-box>ul.gallery-nav-list>li.gallery-thumb>img");
					if(pictureEle.isEmpty()){
						pictureEle = doc.select("div#gallery>div.gallery_con>div.gallery_item.clearfix>div.gy-nav>div.gy-thumbs>ul.gy-thumb-list>li>a>img");
					}
					if(pictureEle.isEmpty()){
						pictureEle = doc.select("div.navs>div.thumbs>div.navs-box>ul.navs-list>li.thumb>img");
					}
					if(pictureEle.isEmpty()){
						pictureEle = doc.select("div.gallery_con>div.gallery_item.clearfix>div.gy-nav>div.gy-thumbs>ul.gy-thumb-list>li>a>img");
					}
					if(!pictureEle.isEmpty()){
						int n = 0;
						System.out.println("该行程的图片数量为 "+pictureEle.size()+" 张!");
						for (int j = 0; j < pictureEle.size(); j++){						
							String imgUrl = pictureEle.get(j).attr("src");
							if(imgUrl == null || "".equals(imgUrl)){
								System.out.println("图片信息获取失败!");
								continue;
							}
							//实例一个行程图片对象
							Pictureinfo pictureInfo = new Pictureinfo();
							//设置行程图片的固定属性
							pictureInfo.setId(UUID.randomUUID().toString());
							//行政区划
							pictureInfo.setAdminarea("中国,"+params.getCityName());
							pictureInfo.setCreator("姚良良");
							pictureInfo.setCreatorid("13783985208");
							pictureInfo.setCreatedate(new Date());
							pictureInfo.setInfoid(uuid);
							pictureInfo.setType(3);
							pictureInfo.setDownload(0);
							pictureInfo.setImgurl(imgUrl);
							pictureInfo.setSort(j+1);
							pictureInfo.setDatasource("Tuniu");

							//放入kafka队列
							String urlParams2 = KafkaUtils.parseJsonObject(pictureInfo, 5,1);
							String sendPost2 = downLoadService.sendPost(KafkaUtils.KAFKAURL, urlParams2);
							jedis.insertAndGetId(pictureInfo);
							n++;
						}
						System.out.println("该行程已保存图片 "+pictureEle.size()+"-"+n+" 张!");
					}else{
						System.out.println("没有图片");
					}
					//放入下一级链接
					try {
						tuniuRoutePriceURL(params);
						
						tuniuRouteCommentURL(params);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
						
			}	
		} catch (Exception e) {
			e.printStackTrace();
		}				
	}
	/**
	 * 
	 * @Description 获取途牛行程评论url
	 * @author 赵乐
	 * @date 2018年3月7日 下午3:08:35
	 * @action tuniuRouteCommentURL
	 * @param @param params
	 * @return void
	 */
	public void tuniuRouteCommentURL(Params params) {
		// TODO Auto-generated method stub
		try {
			String uuid = params.getUuid();
			String url = params.getUrl();
			String productId=url.substring(url.lastIndexOf("/")+1, url.length());
			//获取评论总数
			String gradeNum=params.getRouteInfo().getGradenum();
			int commentCount=0;
			if(StringUtils.isNotBlank(gradeNum)){
				try {
					commentCount=Integer.valueOf(gradeNum);
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			int totalPage=commentCount%10==0?commentCount/10:commentCount/10+1;
			for (int i = 1; i <= totalPage; i++) {
				String commentURL="http://www.tuniu.com/papi/tour/comment/product?page=1&productId="+productId+"&selectedType=0&stamp=040084942428522051510819092235";
				Params para = new Params();
				para.setType(Param.TUNIU_ROUTE_COMMENT);
				para.setHttpType(Param.GET);
				para.setCityName(params.getCityName());
				para.setDataSource(Param.TUNIU);
				para.setUrl(commentURL);
				para.setUuid(uuid);
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
	 * @Description 获取途牛行程价格url
	 * @author 赵乐
	 * @date 2018年3月7日 下午3:08:53
	 * @action tuniuRoutePriceURL
	 * @param @param params
	 * @return void
	 */
	public void tuniuRoutePriceURL(Params params) {
		// TODO Auto-generated method stub
		try {
			String uuid = params.getUuid();
			String url = params.getUrl();
			String productId=url.substring(url.lastIndexOf("/")+1, url.length());
			String cityCode=params.getOtherInformation().trim();//目前只存了一个编号，因为三个一致，若发现不一致，可在上一个方法中重新设置
			String priceUrl="";
			//获取种子链接传过来的城市名称
			String cityName=params.getCityName();
			if("海南".equals(cityName)){
				priceUrl = "http://www.tuniu.com/tour/api/calendar?productId="+productId+"&bookCityCode="+cityCode+"&departCityCode="+cityCode+"&backCityCode="+cityCode;
			}else if("上海".equals(cityName)){
				priceUrl = "http://www.tuniu.com/tour/api/calendar?productId="+productId+"&bookCityCode="+cityCode+"&departCityCode="+cityCode+"&backCityCode="+cityCode;
			}else{
				System.err.println("------行程价格链接为空--------"+cityName);
			}
			Params para = new Params();
			para.setType(Param.TUNIU_ROUTE_PRICE);
			para.setHttpType(Param.GET);
			para.setCityName(params.getCityName());
			para.setDataSource(Param.TUNIU);
			para.setUrl(priceUrl);
			para.setUuid(uuid);

			para.setDestinationName("lowQueue");
			produceService.sendMessage(lowQueue,para);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 
	 * @Description 解析途牛行程分页链接
	 * @author 赵乐
	 * @date 2018年3月7日 下午2:43:38
	 * @action parseTuniuRoutePage
	 * @param @param params
	 * @return void
	 */
	public void parseTuniuRoutePage(Params params) {
		// TODO Auto-generated method stub
		String url = params.getUrl();
		String content = params.getContent();
		String cityName = params.getCityName();
		Document document = Jsoup.parse(content);
		//获取行程详情
		Elements elements2 = document.select("ul.thebox.clearfix>li>div.theinfo>a.clearfix");
		if(!elements2.isEmpty()){
			for (Element element : elements2) {
				String href="http:"+element.attr("href");
				System.out.println(href+"-----途牛行程url");//http://www.tuniu.com/tour/210020107
				
				String string = jedis.get(href);
				if(StringUtils.isBlank(string)){
					jedis.set(href, href);
				}else{
					continue;
				}
				//调用activeMQ消息机制
				Params para=new Params();
				para.setUrl(href);
				para.setCityName(cityName);
				para.setParentUrl(url);
				para.setType(Param.TUNIU_ROUTE_DETAIL);
				para.setDataSource(Param.TUNIU);
				para.setHttpType(Param.GET);
				para.setDestinationName("highQueue");
				produceService.sendMessage(highQueue,para);
			}
		}else{
			System.err.println("解析详情信息标签为空，放回队里中再次请求");
			produceService.sendMessage(params.getDestinationName(), params);
		}

	}
	/**
	 * 
	 * @Description 解析途牛行程首页链接
	 * @author 赵乐
	 * @date 2018年3月7日 下午2:39:29
	 * @action parseTuniuRouteFirst
	 * @param @param params
	 * @return void
	 */
	public void parseTuniuRouteFirst(Params params) {
		// TODO Auto-generated method stub
		String url = params.getUrl();
		String content = params.getContent();
		String cityName=url.substring(url.indexOf("whole-sh-0-")+11,url.lastIndexOf("/"));
		Document document = Jsoup.parse(content);
		//获取分页链接
		Elements elements = document.select("div.page-bottom>a");
		if(!elements.isEmpty()){
			String text = elements.get(elements.size()-2).text();
			Integer pageNum = Integer.parseInt(text);
			for (int i=1;i<=pageNum;i++) {
				String pageHref=url+i;
				//把分页链接放回redis任务队列 http://s.tuniu.com/search_complex/whole-sh-0-%E4%B8%8A%E6%B5%B7/2
				
				//调用activeMQ消息机制
				Params para=new Params();
				para.setUrl(pageHref);
				para.setParentUrl(url);
				para.setCityName(cityName);
				para.setType(Param.TUNIU_ROUTE_PAGE);
				para.setDataSource(Param.TUNIU);
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
	/**
	 * 
	 * @Description 解析途牛门票评论信息
	 * @author 赵乐
	 * @date 2018年3月7日 下午2:21:28
	 * @action parseTuniuScenicComment
	 * @param @param params
	 * @return void
	 */
	public void parseTuniuScenicComment(Params params) {
		// TODO Auto-generated method stub
		String uuid = params.getUuid();
		String content = params.getContent();
		if(StringUtils.isNotBlank(content)){
			try {
				JSONObject jsonObject=new JSONObject(content);
				JSONArray contents=jsonObject.getJSONArray("contents");
				for(int i=0;i<contents.length();i++){
					JSONObject obj=contents.getJSONObject(i);

					JSONObject compTextContent=obj.getJSONObject("compTextContent");
					Commentinfo holyrobotCommentinfo = new Commentinfo();
					//内容
					holyrobotCommentinfo.setContent(obj.toString());
					//评论日期
					String commentDate=compTextContent.getString("updateTime");
					holyrobotCommentinfo.setCommentdate(commentDate);
					//行政区划
					holyrobotCommentinfo.setAdminarea("中国,"+params.getCityName());
					holyrobotCommentinfo.setId(UUID.randomUUID().toString());
					holyrobotCommentinfo.setInfoid(uuid);
					holyrobotCommentinfo.setType(1);
					holyrobotCommentinfo.setCreatedate(new Date());
					holyrobotCommentinfo.setDatasource("Tuniu");
					holyrobotCommentinfo.setCreator("tyl");
					holyrobotCommentinfo.setCreatorid("tyl13564205515");
					//放入kafka队列
					String urlParams = KafkaUtils.parseJsonObject(holyrobotCommentinfo, 11,1);
					String sendPost = downLoadService.sendPost(KafkaUtils.KAFKAURL, urlParams);
					
					jedis.insertAndGetId(holyrobotCommentinfo);
					System.out.println("评论内容："+obj.toString());
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
	 * 
	 * @Description 解析途牛门票详情信息
	 * @author 赵乐
	 * @date 2018年3月7日 下午1:44:54
	 * @action parseTuniuScenicDetail
	 * @param @param params
	 * @return void
	 */
	public void parseTuniuScenicDetail(Params params) {
		// TODO Auto-generated method stub
		String uuid = params.getUuid();
		String url = params.getUrl();
		String content = params.getContent();
		try {
			Document document=Jsoup.parse(content);
			//爬取景点详情信息
			Sceinfo holyrobotSceinfo = new Sceinfo();
			holyrobotSceinfo.setId(uuid);
			holyrobotSceinfo.setUrlid(url);
			//景点名称
			Elements nameEle=document.select("div.v2_ticket_proinf>div.v2_tp_text>div.v2_ct_title");
			String name=nameEle.get(0).ownText();
			holyrobotSceinfo.setName(name);
			//景点星级(门票不应该有星级，这个字段可以不要)
			Elements starEle=document.select("div.v2_ticket_proinf>div.v2_tp_text>div.v2_ct_title>span.v2_ct_lable");
			String star=starEle.text();
			//holyrobotSceinfo.setStarlevel(star);
			//景点地址
			Elements addressEle=document.select("div.v2_ticket_proinf>div.v2_tp_text>p.v2_detail_address>span");
			String address="";
			if(addressEle.size()>0){
				address=addressEle.get(0).text();
			}
			holyrobotSceinfo.setAddress(address);
			//景点开放时间
			Elements opentimeEle=document.select("div.v2_ticket_proinf>div.v2_tp_text>div.v2_open_time>p>span.con");
			String opentime=opentimeEle.text();
			holyrobotSceinfo.setOpentime(opentime);
			//景点价格
			Elements priceEle=document.select("div.v2_ticket_proinf>div.v2_tp_text>div.v2-price>span.v2-money");
			String price = priceEle.text();
			holyrobotSceinfo.setReferprice(price);
			//景点评分和评论人数
			Elements gradeEle=document.select("div.v2_ticket_proinf>div.v2_tp_text>div.v2_tp_btm>p.v2_tp_sat>span");
			String grade="";
			String gradeNum="";
			if(gradeEle.size()>0){
				grade=gradeEle.get(0).text();
				gradeNum=gradeEle.get(1).text();
			}
			holyrobotSceinfo.setGrade(grade);
			holyrobotSceinfo.setGradenum(gradeNum);
			//景点介绍
			String scenicIntroduction=document.select("div.v2_di.detail_infor").text();
			holyrobotSceinfo.setIntroduction(scenicIntroduction);
			//经纬度
			String script=document.select("script").html();
			String lng="";
			String lat="";
			try {
				if(script.indexOf("GuideMap")>-1){
					String guideMap=script.substring(script.indexOf("GuideMap"));
					String[] guideMapSplit=guideMap.substring(guideMap.indexOf("(")+1, guideMap.indexOf(")")).split(",");
					lng=guideMapSplit[4].replace("'", "");
					lat=guideMapSplit[3].replace("'", "");
				}
			} catch (Exception e) {
				e.printStackTrace();
				produceService.sendMessage(params.getDestinationName(), params);
			}
			holyrobotSceinfo.setLongitude(lng);
			holyrobotSceinfo.setLatitude(lat);
			//服务承诺
			Elements promiseElement=document.select("div.v2_ticket_proinf.clearfix>div.v2_tp_text>p.v2_tp_promise>span.tp_tips");
			String promise=promiseElement.text();
			holyrobotSceinfo.setServicecommitment(promise);
			//优惠政策
			Elements select2 = document.select("div.detail_infor>div.pro_man_recom>div.order_detail_imfor>dl.clearfix ");
			for (Element element : select2) {
				Elements select3 = element.select("dt.clearfix");
				Elements select4 = element.select("dd");
				String type=select3.text();
				String item=select4.toString().replaceAll("<dd>", "").replaceAll("</dd>", "").replaceAll("<div>", "").replaceAll("</div>", "").replaceAll("&nbsp", "").replaceAll("<br>", "").replaceAll(";", "");
				switch (type) {
				case "特殊人群政策：":
					holyrobotSceinfo.setFavouredpolicy(item);
					break;
				default:
					break;
				}
				
			}
			//行政区划
			holyrobotSceinfo.setAdminarea("中国,"+params.getCityName());
			holyrobotSceinfo.setDatatype("2");
			holyrobotSceinfo.setDatasource("Tuniu");
			holyrobotSceinfo.setCreator("徐仁杰");
			holyrobotSceinfo.setCreatorid("xurenjie-13621935220");
			holyrobotSceinfo.setCreatedate(new Date());
			
			if(StringUtils.isBlank(holyrobotSceinfo.getName())){
				System.out.println("数据不完整放回队列");
				produceService.sendMessage(params.getDestinationName(), params);
				
			}else{
				//放入kafka队列
				String urlParams3 = KafkaUtils.parseJsonObject(holyrobotSceinfo, 5,1);
				String sendPost3 = downLoadService.sendPost(KafkaUtils.KAFKAURL, urlParams3);
				jedis.insertAndGetId(holyrobotSceinfo);
				
				//四级地址表信息
				Elements select = document.select("div.v2_search_nav>p.v2_crumbs>a");
				String province="";
				String city="";
				if(select.size()>2){
					String provinceStr = select.get(2).text();
					province=provinceStr.substring(0,provinceStr.indexOf("景点门票"));
				}
				if("上海".equals(province)){
					city=province;
				}
				if("海南".equals(province)){
					if(select.size()>3){
						String cityStr = select.get(3).text();
						city=cityStr.substring(0,cityStr.indexOf("景点门票"));
					}else{
						city="海南";
					}
					
				}

				Addressinfo addressInfo=new Addressinfo();
				addressInfo.setId(UUID.randomUUID().toString());
				addressInfo.setInfoid(uuid);
				addressInfo.setProvince(province);
				addressInfo.setCity(city);
				addressInfo.setDetailaddress(address);
				addressInfo.setCountry("中国");
				addressInfo.setCreatedate(new Date());
				addressInfo.setCreator("徐仁杰");
				addressInfo.setCreatorid("xurenjie-13621935220");
				addressInfo.setDatasource("Tuniu");
				//放入kafka队列
				String urlParams = KafkaUtils.parseJsonObject(addressInfo, 12,1);
				String sendPost = downLoadService.sendPost(KafkaUtils.KAFKAURL, urlParams);
				
				jedis.insertAndGetId(addressInfo);

				//景点图片
				Elements picElements=document.select("div.rg-thumbs>div.es-carousel-wrapper>div.es-carousel>ul>li");
				int count=0;
				for(Element element:picElements){
					String picAddress=element.select("a>img").attr("data-large");
					Pictureinfo holyrobotPictureinfo = new Pictureinfo();
					holyrobotPictureinfo.setId(UUID.randomUUID().toString());
					holyrobotPictureinfo.setInfoid(uuid);
					//行政区划
					holyrobotPictureinfo.setAdminarea("中国,"+params.getCityName());
					holyrobotPictureinfo.setSort(count++);
					holyrobotPictureinfo.setType(2);
					holyrobotPictureinfo.setDownload(0);
					holyrobotPictureinfo.setImgurl(picAddress);
					holyrobotPictureinfo.setCreatedate(new Date());
					holyrobotPictureinfo.setCreator("tyl");
					holyrobotPictureinfo.setCreatorid("tyl13564205515");
					holyrobotPictureinfo.setDatasource("Tuniu");
					
					//放入kafka队列
					String urlParams1 = KafkaUtils.parseJsonObject(holyrobotPictureinfo, 10,1);
					String sendPost1 = downLoadService.sendPost(KafkaUtils.KAFKAURL, urlParams1);
					
					jedis.insertAndGetId(holyrobotPictureinfo);
				}

				//门票评论url
				String scenicId=url.substring(url.lastIndexOf("=")+1, url.length());

				if(StringUtils.isBlank(gradeNum)){
					gradeNum="0";
				}
				int commentCount=Integer.valueOf(gradeNum);
				int totalPage=commentCount%100==0?commentCount/100:commentCount/100+1;
				for(int i=1;i<=totalPage;i++){
					String commentURL="http://menpiao.tuniu.com/tn?r=ticket/scenic/newRemarkList&currentPage="+i+"&scenicId%5B%5D="+scenicId+"&pageLimit=100";
					Params para=new Params();
					para.setUuid(uuid);
					para.setUrl(commentURL);
					para.setCityName(params.getCityName());
					para.setType(Param.TUNIU_SCENIC_COMMENT);
					para.setDataSource(Param.TUNIU);
					para.setHttpType(Param.GET);
					para.setDestinationName("lowQueue");
					produceService.sendMessage(lowQueue,para);
					
				}
				
				//门票价格列表
				String priceType="";
				Elements priceElement=document.select("div.v2_line_box.line_box>div.line_cont>ul.content>li.lc_title");
				//其中priceElement中第一个元素不是所要的
				for(int i=0;i<priceElement.size();i++){
					if(priceElement.get(i).attr("class").contains("v2_pro_title")){
						priceType=priceElement.get(i).select("div.lct>p.ticket_type").text();
						priceElement.remove(i);
					}
					Element priceInfo=priceElement.get(i);
					Scepriceinfo holyrobotScepriceinfo = new Scepriceinfo();
					
					holyrobotScepriceinfo.setName(name);
					//价格类型
					holyrobotScepriceinfo.setPricetype(priceType);
					//价格条目
					String priceitem=priceInfo.select("div.lct.clearfix>p.l_name.name_color>a").attr("title");
					holyrobotScepriceinfo.setPriceitem(priceitem);
					//预定时间
					String saleCondition=priceInfo.select("div.lct.clearfix>p.l_time.v2_product_com").text();
					holyrobotScepriceinfo.setSalecondition(saleCondition);
					//市场价
					String marketingPrice=priceInfo.select("div.lct.clearfix>p.l_g_price.g_price_color").text();
					holyrobotScepriceinfo.setMarketingprice(marketingPrice);
					//途牛价
					String salePrice=priceInfo.select("div.lct.clearfix>p.l_price.price_color").text();
					holyrobotScepriceinfo.setSaleprice(salePrice);
					//折扣信息
					Elements ticket_detail=priceInfo.select("div.yhhd_detail>ul.yhhd_table>li");
					String discountInfo="";
					if(ticket_detail.size()>0){
						discountInfo=ticket_detail.get(1).select("span.col.col2").text();
					}
					holyrobotScepriceinfo.setDiscountinfo(discountInfo);
					//优惠政策
					String favouredPolicy = priceInfo.select("div#line_detail>div.oi_lists").text();
					
					holyrobotScepriceinfo.setFavouredpolicy(favouredPolicy);
					holyrobotScepriceinfo.setAdminarea("中国,"+params.getCityName());
					holyrobotScepriceinfo.setId(UUID.randomUUID().toString());
					holyrobotScepriceinfo.setScenicid(uuid);
					holyrobotScepriceinfo.setUrlid(url);
					holyrobotScepriceinfo.setCreator("徐仁杰");
					holyrobotScepriceinfo.setCreatorid("xurenjie-13621935220");
					holyrobotScepriceinfo.setCreatedate(new Date());
					holyrobotScepriceinfo.setDatasource("Tuniu");
					

					//放入kafka队列
					String urlParams1 = KafkaUtils.parseJsonObject(holyrobotScepriceinfo, 4,1);
					String sendPost1 = downLoadService.sendPost(KafkaUtils.KAFKAURL, urlParams1);
					
					jedis.insertAndGetId(holyrobotScepriceinfo);
				}
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(params.getContent());
			produceService.sendMessage(params.getDestinationName(), params);
		}
	}
	/**
	 * 
	 * @Description 解析途牛门票首页链接
	 * @author 赵乐
	 * @date 2018年3月7日 下午1:42:30
	 * @action parseTuniuScenicFirst
	 * @param @param params
	 * @return void
	 */
	public void parseTuniuScenicFirst(Params params) {
		// TODO Auto-generated method stub
		String content = params.getContent();
		String pageUrl=params.getUrl();
		Document doc = Jsoup.parse(content);
		Elements nextPageEle = doc.select("div.page-bottom>a.page-next");
		for (Element element : nextPageEle) {
			String nextHref = element.attr("href");

			//调用activeMQ消息机制
			Params para=new Params();
			para.setUrl(nextHref);
			para.setType(Param.TUNIU_SCENIC_FIRST);
			para.setDataSource(Param.TUNIU);
			para.setCityName(params.getCityName());
			para.setHttpType(Param.GET);
			para.setDestinationName("topQueue");
			produceService.sendMessage(topQueue,para);
		}
		Elements select = doc.select("dl.detail>dt>p.title.ticket>a");
		for (Element element : select) {
			String href = element.attr("href");
			System.out.println(href+"-----途牛门票链接");
			
			String string = jedis.get(href);
			if(StringUtils.isBlank(string)){
				jedis.set(href, href);
			}else{
				continue;
			}
			//调用activeMQ消息机制
			Params para=new Params();
			para.setUrl(href);
			para.setCityName(params.getCityName());
			para.setParentUrl(pageUrl);
			para.setType(Param.TUNIU_SCENIC_DETAIL);
			para.setDataSource(Param.TUNIU);
			para.setHttpType(Param.GET);
			para.setDestinationName("highQueue");
			produceService.sendMessage(highQueue,para);

		}
	}
	/**
	 * 
	 * @Description 解析途牛景点评论信息
	 * @author 赵乐
	 * @date 2018年3月7日 上午11:23:27
	 * @action parseTuniuStrokeComment
	 * @param @param params
	 * @return void
	 */
	public void parseTuniuStrokeComment(Params params) {
		// TODO Auto-generated method stub
		String uuid=params.getUuid();
		String str=params.getContent();
		if (StringUtils.isNotBlank(str)) {
			try {
				JSONObject object=new JSONObject(str);
				String success = object.get("success").toString();
				if("true".equals(success)){
					String data = object.get("data").toString();
					Document document = Jsoup.parse(data);
					Elements elementsDetail = document.select("div.item>div.detail");
					int size = elementsDetail.size();
					System.out.println(size+"每页评论条数");
					for(Element element:elementsDetail){
						Commentinfo commentinfo=new Commentinfo();
						String commentId=UUID.randomUUID().toString();
						commentinfo.setId(commentId);
						commentinfo.setInfoid(uuid);
						String content = element.toString();
						String newContent = new String(content.getBytes(),"utf-8");
						commentinfo.setContent(newContent);
						commentinfo.setType(1);
						commentinfo.setDatasource("Tuniu");
						commentinfo.setCreatedate(new Date());
						commentinfo.setCreator("赵乐");
						commentinfo.setCreatorid("15736708180");
						//行政区划
						commentinfo.setAdminarea("中国,"+params.getCityName());
						//放入kafka队列
						String urlParams = KafkaUtils.parseJsonObject(commentinfo, 11,1);
						String sendPost = downLoadService.sendPost(KafkaUtils.KAFKAURL, urlParams);

						jedis.insertAndGetId(commentinfo);
					}
				}
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
	 * @Description 解析途牛景点详情信息
	 * @author 赵乐
	 * @date 2018年3月7日 上午10:37:00
	 * @action parseTuniuStrokeDetail
	 * @param @param params
	 * @return void
	 */
	public void parseTuniuStrokeDetail(Params params) {
		// TODO Auto-generated method stub
		String uuid = params.getUuid();
		String url=params.getUrl();
		String content = params.getContent();
		//景点基础信息对象
		Sceinfo sceInfo = new Sceinfo();
		sceInfo.setId(uuid);
		sceInfo.setUrlid(url);
		Document document=Jsoup.parse(content);
		try {

			Elements elements = document.select("div.row>div.photo.wrapper>a");
			String name="";
			if(!elements.isEmpty()){
				System.out.println("图片的数目"+elements.size());
				for(int i = 0; i < elements.size(); i++){
					Pictureinfo pictureinfo=new Pictureinfo();
					String pictureurl=elements.get(i).select("img").attr("src");
					System.out.println(pictureurl+"图片链接");
					String uurid = UUID.randomUUID().toString();
					pictureinfo.setId(uurid);
					pictureinfo.setInfoid(uuid);
					pictureinfo.setImgurl(pictureurl);
					pictureinfo.setSort(i);
					pictureinfo.setType(1);
					pictureinfo.setDownload(0);
					//行政区划
					pictureinfo.setAdminarea("中国,"+params.getCityName());
					pictureinfo.setCreatedate(new Date());
					pictureinfo.setCreator("赵乐");
					pictureinfo.setCreatorid("15736708180");
					pictureinfo.setDatasource("Tuniu");
					System.out.println(pictureurl);

					//放入kafka队列
					String urlParams = KafkaUtils.parseJsonObject(pictureinfo, 10,1);
					String sendPost = downLoadService.sendPost(KafkaUtils.KAFKAURL, urlParams);
					jedis.insertAndGetId(pictureinfo);
				}
				Elements elementsName = elements.get(0).select("div.mask-coat>div.mask>h1.signal");
				//景点名称
				name=elementsName.isEmpty()?"":elementsName.text();
			}
			sceInfo.setName(name);
			//获取景点介绍信息
			Elements elementsIntroduction = document.select("div#view_bar>div.coat>p");
			String introduction=elementsIntroduction.isEmpty()?"":elementsIntroduction.text();
			sceInfo.setIntroduction(introduction);
			//景点地址
			Elements elementsContent = document.select("div#view_bar>div.route>div.content");
			String address="";
			if(!elementsContent.isEmpty()){
				for(Element element:elementsContent){
					Elements elementLeft = element.select("div.left");
					String left=elementLeft.isEmpty()?"":elementLeft.text();
					Elements elementRight = element.select("div.right");
					String right=elementRight.isEmpty()?"":elementRight.text();
					switch(left){
					case "地址":
						//景点地址
						address=right;
						sceInfo.setAddress(address);
						break;
					case "开放时间":
						//开放时间
						String openTime=right;
						sceInfo.setOpentime(openTime);
						break;
					case "交通":
						Elements elementsOtherInformation = elementRight.select("div.traffic");
						String otherInformation=elementsOtherInformation.isEmpty()?"":elementsOtherInformation.text();
						sceInfo.setOtherinformation(otherInformation);
						break;
					default:
						break;
					}
				}
				Elements elementLocation = elementsContent.select("div.left.location");
				String location=elementLocation.isEmpty()?"":elementLocation.attr("data-default-point");
				//经纬度
				String longitude="";
				String latitude="";
				if(!"".equals(location)){
					String locations[]=location.split(",");
					if(locations.length>1){
						longitude=locations[0];
						latitude=locations[1];
					}
				}
				sceInfo.setLongitude(longitude);
				sceInfo.setLatitude(latitude);
				Elements elementAdviceTime= elementsContent.select("div.far>div.right");
				String adviceTime=elementAdviceTime.isEmpty()?"":elementAdviceTime.text();
				System.out.println(adviceTime);
				sceInfo.setAdvicetime(adviceTime);
				//行政区划
				sceInfo.setAdminarea("中国,"+params.getCityName());
				//门票信息
				//优惠政策
				//备注信息
				//评论页码 pageNum
				Elements elementspageNum = document.select("div#interact>div.module.module2.comment-module>div.page>ul.part");
				String pageNum=elementspageNum.isEmpty()?"":elementspageNum.attr("data-pages");
				if(StringUtils.isBlank(pageNum)){
					pageNum="0";
				}
				System.out.println(pageNum);
				//把评论页码放在otherinformation中，方便后面取
				params.setOtherInformation(pageNum);
				String gradeNu=Integer.parseInt(pageNum)*5+"";
				sceInfo.setGradenum(gradeNu);
				sceInfo.setDatasource("Tuniu");
				sceInfo.setDatatype("1");
				sceInfo.setCreatedate(new Date());
				sceInfo.setCreator("赵乐");
				sceInfo.setCreatorid("15736708180");

			}
			if(StringUtils.isBlank(sceInfo.getName())){
				System.out.println("数据不完整,放回队列");
				produceService.sendMessage(params.getDestinationName(), params);
				
			}else{
				//放入kafka队列
				String urlParams1 = KafkaUtils.parseJsonObject(sceInfo, 5,1);
				String sendPost1 = downLoadService.sendPost(KafkaUtils.KAFKAURL, urlParams1);

				jedis.insertAndGetId(sceInfo);
				//地址表
				Addressinfo addressinfo = params.getAddressinfo();
				System.out.println(addressinfo);
				addressinfo.setId(UUID.randomUUID().toString());
				addressinfo.setInfoid(uuid);
				addressinfo.setDetailaddress(address);
				addressinfo.setCreatedate(new Date());
				addressinfo.setCreator("赵乐");
				addressinfo.setCreatorid("15736708180");
				addressinfo.setDatasource("Tuniu");

				//放入kafka队列
				String urlParams = KafkaUtils.parseJsonObject(addressinfo, 12,1);
				String sendPost = downLoadService.sendPost(KafkaUtils.KAFKAURL, urlParams);

				jedis.insertAndGetId(addressinfo);
				
				//放入评论链接
				tuniuStrokeCommentURL(params);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			produceService.sendMessage(params.getDestinationName(), params);
		}
	}
	/**
	 * 
	 * @Description 存放途牛景点评论的url
	 * @author 赵乐
	 * @date 2018年3月7日 上午11:19:22
	 * @action tuniuCommentURL
	 * @param @param params
	 * @return void
	 */
	public void tuniuStrokeCommentURL(Params params) {
		// TODO Auto-generated method stub
		String url=params.getUrl();
		String uuid = params.getUuid();
		//解析里面讲pageNum的值放在otherinformation中
		String pageNum=params.getOtherInformation();
		//截取scenInfourl中景点的poiId
		String poiId=url.substring(22, url.lastIndexOf("/guide"));
		for(int i=1;i<=Integer.valueOf(pageNum);i++){
			String commentUrl="http://www.tuniu.com/newguide/api/widget/render/?widget=ask.AskAndCommentWidget&params[poiId]="
					+poiId+"&params[page]="+i;
			Params para = new Params();
			para.setType(Param.TUNIU_STROKE_COMMENT);
			para.setDataSource(Param.TUNIU);
			para.setHttpType(Param.GET);
			para.setCityName(params.getCityName());
			para.setUrl(commentUrl);
			para.setUuid(uuid);
			para.setDestinationName("lowQueue");
			produceService.sendMessage(lowQueue,para);

		}
	}
	/**
	 * 
	 * @Description 解析途牛首页链接
	 * @author 赵乐
	 * @date 2018年3月7日 上午9:35:58
	 * @action parseTuniuFirst
	 * @param @param params
	 * @return void
	 */
	public void parseTuniuStrokeFirst(Params params) {
		// TODO Auto-generated method stub
		String content = params.getContent();
		Document doc = Jsoup.parse(content);
		String pageUrl=params.getUrl();
		//获取下一页的链接
		Elements nextPageEle = doc.select("div.pagination>div.page-bottom>a");
		for (Element element : nextPageEle) {
			if("下一页".equals(element.text())){
				String nextHref="http://www.tuniu.com"+element.attr("href");
				//scenic.setUrlList(nextHref);
				
				//调用activeMQ消息机制
				Params para=new Params();
				para.setUrl(nextHref);
				para.setParentUrl(pageUrl);
				para.setCityName(params.getCityName());
				para.setType(Param.TUNIU_STROKE_FIRST);
				para.setDataSource(Param.TUNIU);
				para.setHttpType(Param.GET);
				para.setDestinationName("topQueue");
				produceService.sendMessage(topQueue,para);
			}
		}
		/**
		 * 获取本页面信息
		 */
		//获取四级地址表信息
		Elements selectAddress = doc.select("div.breadbar_v1.cf>ul.first_level>li>a");
		int size = selectAddress.size();
		String cityName ="";
		String proviceName = "";
		String countName ="";
		if(size==5){
			cityName = selectAddress.get(size-1).text().replace("景点", "");
			proviceName = selectAddress.get(size-2).text();
			countName = selectAddress.get(size-3).text();
		}if(size==4){
			cityName = selectAddress.get(size-1).text().replace("景点", "");
			proviceName = selectAddress.get(size-1).text().replace("景点", "");
			countName = selectAddress.get(size-2).text();
		}
		Addressinfo addressInfo=new Addressinfo();
		addressInfo.setCity(cityName);
		addressInfo.setProvince(proviceName);
		addressInfo.setCountry(countName);
		addressInfo.setType(1);
		//获取景点详情的链接
		Elements select = doc.select("div.allSpots>ul>li>a.pic");
		for (Element element : select) {
			Params para=new Params();
			String href = "http://www.tuniu.com"+element.attr("href");
			System.out.println(href+"-----途牛景点链接");
			
			String urlKey = jedis.get(href);
			if(StringUtils.isBlank(urlKey)){
				jedis.set(href, href);
			}else{
				continue;
			}
			para.setUrl(href);
			para.setParentUrl(pageUrl);
			para.setCityName(params.getCityName());
			para.setType(Param.TUNIU_STROKE_DETAIL);
			para.setDataSource(Param.TUNIU);
			para.setHttpType(Param.GET);
			//四级地址表
			para.setAddressinfo(addressInfo);
			para.setDestinationName("highQueue");
			produceService.sendMessage(highQueue,para);
			
		}
		
	}
	

}
