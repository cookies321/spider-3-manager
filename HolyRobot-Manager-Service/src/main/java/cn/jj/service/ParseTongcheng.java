package cn.jj.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.holyrobot.common.Addressinfo;
import com.holyrobot.common.Commentinfo;
import com.holyrobot.common.Pictureinfo;
import com.holyrobot.common.Sceinfo;
import com.holyrobot.common.Scepriceinfo;
import com.holyrobot.data.Param;
import com.holyrobot.data.Params;

import cn.jj.utils.DateUtil;
import cn.jj.utils.NumUtils;

@Repository
public class ParseTongcheng {
	@Autowired
	private ProducerService produceService;

	@Autowired
	private JedisClient jedis;

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
	 * @Description 同程门票价格
	 * @author 汤玉林
	 * @date 2018年3月7日 下午4:00:36
	 * @action parseTongchengScenicPrice
	 * @param params
	 */
	public void parseTongchengScenicPrice(Params params) {
		String uuid = params.getUuid();
		String content = params.getContent();
		System.out.println(params.getUrl());
		//解析价格json
		if (StringUtils.isNotBlank(content)) {
			try {
				JSONObject ticket = new JSONObject(content);
				JSONArray sceneryPrices = ticket.getJSONArray("SceneryPrices");
				System.out.println(sceneryPrices.length());
				if(sceneryPrices!=null&& sceneryPrices.length() >0){
					for(int i=0;i<sceneryPrices.length();i++){
						JSONArray channelList =sceneryPrices.getJSONObject(i).getJSONArray("ChannelPriceModelEntityList");
						System.out.println(channelList.length());
						if(channelList!=null &channelList.length()>0){
							for(int j = 0 ; j<channelList.length();j++){
								JSONArray channelEntityList =channelList.getJSONObject(j).getJSONArray("ChannelPriceEntityList");
								if(channelEntityList!=null && channelEntityList.length()>0){
									for(int k = 0 ; k < channelEntityList.length() ; k++){
										//获取价格
										JSONObject channel = channelEntityList.getJSONObject(k);
										Scepriceinfo holyrobotScepriceinfo = new Scepriceinfo();

										holyrobotScepriceinfo.setId(UUID.randomUUID().toString());
										holyrobotScepriceinfo.setScenicid(uuid);
										//获取门票类型（单票、套票等等）
										Object productUnitName = channel.get("ProductUnitName");
										if(JSONObject.NULL.equals(productUnitName)){
											productUnitName="";
										}
										holyrobotScepriceinfo.setTickettype(productUnitName.toString());
										//获取价格类型（成人、儿童、其他等）
										Object consumers = channel.get("ConsumersTypeName");
										if(JSONObject.NULL.equals(consumers)){
											consumers="";
										}
										holyrobotScepriceinfo.setPricetype(consumers.toString());
										//市场价
										Object amount =  channel.get("Amount");
										if(JSONObject.NULL.equals(amount)){
											amount="";
										}
										holyrobotScepriceinfo.setMarketingprice(amount.toString());
										//价格条目
										Object ticketName =  channel.get("TicketName");
										if(JSONObject.NULL.equals(ticketName)){
											ticketName="";
										}
										holyrobotScepriceinfo.setPriceitem(ticketName.toString());
										//价格条目
										Object amountAdvice =  channel.get("AmountAdvice");
										if(JSONObject.NULL.equals(amountAdvice)){
											amountAdvice="";
										}
										holyrobotScepriceinfo.setSaleprice(amountAdvice.toString());

										//预定时间
										Object priceTimeLimit =  channel.get("PriceTimeLimit");
										if(!JSONObject.NULL.equals(priceTimeLimit)){
											String priceTime= (String) priceTimeLimit;
											holyrobotScepriceinfo.setSalecondition(priceTime);
										}
										JSONArray ticketTagEntityList=null;
										//优惠信息
										try {
											ticketTagEntityList=channel.getJSONArray("TicketTagEntityList");
										} catch (Exception e) {

										}

										if(ticketTagEntityList!=null&&ticketTagEntityList.length()>=2){
											String discount="";
											for (int l = 0; l < ticketTagEntityList.length(); l++) {
												JSONObject discountObj = ticketTagEntityList.getJSONObject(l);
												int tagType = discountObj.getInt("TagType");
												if(tagType==2){
													discount += discountObj.getString("Name")+" ";
												}
											}
											holyrobotScepriceinfo.setDiscountinfo(discount);
										}
										/*if(!getTicketMode.equals("") && getTicketMode!=null && !getTicketMode.equals("null")){

											holyrobotScepriceinfo.setDiscountinfo((String) getTicketMode);
										}*/
										holyrobotScepriceinfo.setCreatedate(new Date());
										holyrobotScepriceinfo.setCreator("陈文奇");
										holyrobotScepriceinfo.setCreatorid("chenwenqi-13263625152");
										System.out.println(productUnitName+"\n"+consumers+"\n"+ticketName+"\n"+amountAdvice+"\n"+priceTimeLimit);
										jedis.insertAndGetId(holyrobotScepriceinfo);
									}

								}

							}
						}
					}
				}
			} catch (Exception e) {	
				e.printStackTrace();
				produceService.sendMessage(lowQueue, params);
			}
		}
	}

	/**
	 * @Description 同程门票评论解析
	 * @author 汤玉林
	 * @date 2018年3月7日 下午3:56:25
	 * @action parseTongchengScenicComment
	 * @param params
	 */
	public void parseTongchengScenicComment(Params params) {
		String uuid = params.getUuid();
		String content = params.getContent();
		//解析评论json
		if (StringUtils.isNotBlank(content)) {
			try {
				JSONObject comment = new JSONObject(content);
				JSONArray jsonArray=null;
				//获取评论内容
				try {
					jsonArray = comment.getJSONArray("dpList");
					System.out.println("评论条数："+jsonArray.length());
				} catch (Exception e) {
					
				}
				
				if(jsonArray!=null&&jsonArray.length()>0){

					for (int i=0;i<jsonArray.length();i++) {
						//获取评论内容
						String commentContent = jsonArray.getJSONObject(i).toString();
						//获取评论时间
						String  dpDate = (String)jsonArray.getJSONObject(i).get("dpDate");

						Commentinfo holyrobotCommentinfo= new Commentinfo();
						holyrobotCommentinfo.setType(1);
						holyrobotCommentinfo.setId(UUID.randomUUID().toString());
						holyrobotCommentinfo.setInfoid(uuid);
						holyrobotCommentinfo.setCommentdate(dpDate);
						holyrobotCommentinfo.setContent(commentContent);
						holyrobotCommentinfo.setCreatedate(new Date());
						holyrobotCommentinfo.setCreator("陈文奇");
						holyrobotCommentinfo.setDatasource("Tongcheng");
						holyrobotCommentinfo.setCreatorid("chenwenqi-13263625152");
						//System.out.println("评论内容："+commentContent);
						jedis.insertAndGetId(holyrobotCommentinfo);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				produceService.sendMessage(lowQueue, params);
			}
		}
		
	}

	/**
	 * @Description 同程门票想去人数和已去人数
	 * @author 汤玉林
	 * @date 2018年3月7日 下午3:54:13
	 * @action parseTongchengScenicBoowNowNew
	 * @param params
	 */
	public void parseTongchengScenicBoowNowNew(Params params) {
		String uuid = params.getUuid();
		String content = params.getContent();
		if(StringUtils.isNotBlank(content)){
			try {
				content = content.substring(1, content.length()-1);
				JSONObject jsonObject = new JSONObject(content);
				JSONArray noticejson=null;
				try {
					noticejson = jsonObject.getJSONArray("Notice");
				} catch (Exception e) {
					// TODO: handle exception
				}
				if(noticejson.length()>0){
					JSONObject notice = noticejson.getJSONObject(0);
					JSONArray jsonArray = null;
					try {
						jsonArray = notice.getJSONArray("BItem");
					} catch (Exception e) {
						// TODO: handle exception
					}
					if(jsonArray.length()>0){
						for (int i = 0; i < jsonArray.length(); i++) {
							JSONObject object = jsonArray.getJSONObject(i);
							String discountName = object.getString("Name");
							String cont = "";
							switch (discountName) {
							case "特惠政策":
								cont = object.getString("Cont");
								cont = cont.replaceAll("<span>", "").replaceAll("</span>", "");
								Scepriceinfo sceprice = new Scepriceinfo();
								sceprice.setId(UUID.randomUUID().toString());
								sceprice.setScenicid(uuid);
								sceprice.setPricetype(discountName);
								sceprice.setPriceitem(cont);
								sceprice.setCreatedate(new Date());
								sceprice.setCreator("汤玉林");
								sceprice.setCreatorid("tyl13564205515");

								jedis.insertAndGetId(sceprice);
								break;
							default:
								break;
							}
						}

					}
				}

			} catch (Exception e) {

			}
		}
		
	}

	/**
	 * @Description 同程门票评论第一页
	 * @author 汤玉林
	 * @date 2018年3月7日 下午3:50:58
	 * @action parseTongchengScenicCommentFirst
	 * @param params
	 */
	public void parseTongchengScenicCommentFirst(Params params) {
		String uuid = params.getUuid();
		String url = params.getUrl();
		String sid = url.substring(url.indexOf("sid")+4, url.length());
		String content = params.getContent();
		try {
			if (StringUtils.isNotBlank(content)) {
				JSONObject pcomment =new  JSONObject(content);
				Sceinfo sceInfo = new Sceinfo();
				sceInfo.setId(uuid);
				String grade= pcomment.getString("degreeLevel");
				String gradenum=pcomment.getString("totalNum");
				//获取好评率
				sceInfo.setGrade(grade+"%");
				//获取评论总数
				sceInfo.setGradenum(gradenum);
				System.out.println("评分："+grade+";评论人数："+gradenum);
				sceInfo.setCreatedate(new Date());
				jedis.insertAndGetId(sceInfo);
				JSONObject pageInfo = (JSONObject) pcomment.get("pageInfo");
				int totalPage = (int) pageInfo.get("totalPage");
				for (int i = 0; i < totalPage; i++) {
					String commentUrl = "https://www.ly.com/scenery/AjaxHelper/DianPingAjax.aspx?action=GetDianPingList&pageSize=20&sid="+sid+"&page="+i;
					Params commentParams = new Params();
					commentParams.setUuid(uuid);
					commentParams.setUrl(commentUrl);
					commentParams.setType(Param.TONGCHENG_SCENIC_COMMENT);
					commentParams.setDataSource(Param.TONGCHENG);
					commentParams.setHttpType(Param.GET);

					produceService.sendMessage(lowQueue, commentParams);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			produceService.sendMessage(lowQueue, params);
		}
		
	}

	/**
	 * @Description 同程门票详情解析
	 * @author 汤玉林
	 * @date 2018年3月7日 下午3:42:12
	 * @action parseTongchengScenicDetail
	 * @param params
	 */
	public void parseTongchengScenicDetail(Params params) {
		String url = params.getUrl();
		String uuid = params.getUuid();
		String content = params.getContent();
		//门票编号
		String sid = url.substring(url.lastIndexOf("_")+1, url.lastIndexOf("html")-1);
		Document doc=Jsoup.parse(content);
		Sceinfo sceInfo = new Sceinfo();
		sceInfo.setUrlid(url);
		sceInfo.setId(uuid);
		try {
			//获取景点图片
			Elements img_wElements = doc.select("div.info_l>div.img_w>img");
			if(!img_wElements.isEmpty()){
				String imgUrl = img_wElements.first().attr("nsrc");

				Pictureinfo holyrobotPictureinfo = new Pictureinfo();
				holyrobotPictureinfo.setInfoid(uuid);
				holyrobotPictureinfo.setId(UUID.randomUUID().toString());
				holyrobotPictureinfo.setImgurl("https://www.ly.com"+imgUrl);
				holyrobotPictureinfo.setSort(0);
				holyrobotPictureinfo.setType(2);
				holyrobotPictureinfo.setDownload(0);
				holyrobotPictureinfo.setCreatedate(new Date());
				holyrobotPictureinfo.setCreator("陈文奇");
				holyrobotPictureinfo.setCreatorid("chenwenqi-13263625152");

				jedis.insertAndGetId(holyrobotPictureinfo);
			}
			
			//循环图片
			Elements img_ulElements = doc.select("div.img_s_ul>ul>li");
			System.out.println("图片数："+img_ulElements.size());
			for(int i = 0 ;i< img_ulElements.size()-1;i++){
				Pictureinfo holyrobotPictureinfoi = new Pictureinfo();
				String imgUrl = img_ulElements.get(i).select("img").attr("nsrc");

				holyrobotPictureinfoi.setInfoid(uuid);
				holyrobotPictureinfoi.setId(UUID.randomUUID().toString());
				holyrobotPictureinfoi.setImgurl(imgUrl);
				holyrobotPictureinfoi.setSort(0);
				holyrobotPictureinfoi.setType(2);
				holyrobotPictureinfoi.setDownload(0);
				holyrobotPictureinfoi.setCreatedate(new Date());
				holyrobotPictureinfoi.setCreator("陈文奇");
				holyrobotPictureinfoi.setCreatorid("chenwenqi-13263625152");

				jedis.insertAndGetId(holyrobotPictureinfoi);
			}

			//获取景点名称
			Elements nameElements = doc.select("div.info_r>h3.s_name");
			sceInfo.setName(nameElements.first().text());

			//景点星级
			Elements starEle = doc.select("div.info_r>h3.s_name>span");
			String star="";
			if(!starEle.isEmpty()){
				star=starEle.text();
			}
			sceInfo.setStarlevel(star);
			String address="";
			//获取景点名称
			Elements comElements = doc.select("div.info_r>p.s_com");
			for(Element element :comElements){
				String leftKey =  element.text().substring(0, 4);
				String leftValue = element.select("span").text();
				switch (leftKey) {
				case "景点地址":
					// 景点地址
					address = leftValue;
					sceInfo.setAddress(address);
					break;
				case "开放时间":
					// 开放时间
					String openTime = doc.select("div.s-tShow").text();
					sceInfo.setOpentime(openTime);
					break;
				case "服务保障":
					// 景点类型
					String service = leftValue;
					sceInfo.setServicecommitment(service);
					break;
				default:
					break;
				}
			}

			//获取景点参考价格
			Elements spbElements = doc.select("div.s_price>span.s_p_b>s");
			sceInfo.setReferprice(spbElements.text());
			//获取景点简介
			Elements fconElements = doc.select("div.inf-f-con");
			sceInfo.setIntroduction(fconElements.text());
			//景点经纬度
			Elements mapLng = doc.select("input#mapLng");
			String lng="";
			if(!mapLng.isEmpty()){
				lng=mapLng.attr("value");
			}
			sceInfo.setLongitude(lng);
			Elements maplat = doc.select("input#mapLat");
			String lat="";
			if(!maplat.isEmpty()){
				lat=maplat.attr("value");
			}
			sceInfo.setLatitude(lat);


			System.out.println(nameElements.first().text()+"\n"+star+"\n"+spbElements.text()+"\n"+lat+";"+lng+"\n");

			sceInfo.setDatatype("2");
			sceInfo.setCreatedate(new Date());
			sceInfo.setCreator("陈文奇");
			sceInfo.setCreatorid("chenwenqi-13263625152");
			sceInfo.setDatasource("Tongcheng");

			jedis.insertAndGetId(sceInfo);

			String cityName=params.getCityName();

			Addressinfo addressInfo=new Addressinfo();
			addressInfo.setId(UUID.randomUUID().toString());
			addressInfo.setInfoid(uuid);
			addressInfo.setProvince(cityName);
			addressInfo.setCity(cityName);
			addressInfo.setType(1);
			addressInfo.setDetailaddress(address);
			addressInfo.setCountry("中国");
			addressInfo.setCreatedate(new Date());
			addressInfo.setCreator("陈文奇");
			addressInfo.setCreatorid("chenwenqi-13263625152");
			jedis.insertAndGetId(addressInfo);

			//获取评论第一页的数据,用于获取评分人数和评分
			String firstCommentURL="https://www.ly.com/scenery/AjaxHelper/DianPingAjax.aspx?action=GetDianPingList&pageSize=20&sid="+sid;
			Params para= new Params();
			para.setUuid(uuid);
			para.setUrl(firstCommentURL);
			para.setType(Param.TONGCHENG_SCENIC_COMMENT_FIRST);
			para.setDataSource(Param.TONGCHENG);
			para.setHttpType(Param.GET);
			
			produceService.sendMessage(lowQueue, para);
			
			//获取页面的优惠政策信息
			String bookKnowsNewURL = "https://www.ly.com/scenery/AjaxHelper/SceneryPriceFrame.aspx?action=GetSceneryBookKnowsNew&id="+sid;
			Params para2 = new Params();
			para2.setUuid(uuid);
			para2.setUrl(bookKnowsNewURL);
			para2.setType(Param.TONGCHENG_SCENIC_BOOKNOWNEW);
			para2.setDataSource(Param.TONGCHENG);
			para2.setHttpType(Param.GET);
			
			produceService.sendMessage(lowQueue, para2);
		
			
			String priceUrl = "https://www.ly.com/scenery/AjaxHelper/SceneryPriceFrame.aspx?action=GETNEWFRAMEFORLIST&ids="+sid;
			Params para3 = new Params();
			para3.setUuid(uuid);
			para3.setUrl(priceUrl);
			para3.setType(Param.TONGCHENG_SCENIC_PRICE);
			para3.setDataSource(Param.TONGCHENG);
			para3.setHttpType(Param.GET);

			produceService.sendMessage(lowQueue, para3);
			
		} catch (Exception e) {
			e.printStackTrace();
			produceService.sendMessage(highQueue, params);
		}
		
	}

	/**
	 * @Description 同程门票分页解析
	 * @author 汤玉林
	 * @date 2018年3月7日 下午3:35:11
	 * @action parseTongchengScenicPage
	 * @param params
	 */
	public void parseTongchengScenicPage(Params params) {
		String url = params.getUrl();
		String uuid = params.getUuid();
		String content = params.getContent();
		String keyword=url.substring(url.indexOf("kw")+3, url.length());
		if(StringUtils.isNotBlank(content)){
			try {
				Document doc = Jsoup.parse(content);
				Elements urlELe=doc.select("div.info_c>dl>dt>a.sce_name");
				if(!urlELe.isEmpty()){
					System.out.println("该页景点数："+urlELe.size());
					for (Element element : urlELe) {
						Params para = new Params();
						String href = "https://www.ly.com"+element.attr("href");
						para.setUrl(href);
						para.setUuid(uuid);
						para.setHttpType(Param.GET);
						para.setType(Param.TONGCHENG_SCENIC_DETAIL);
						para.setDataSource(Param.TONGCHENG);
						para.setCityName(keyword);
						System.out.println("景点url："+href);
						produceService.sendMessage(highQueue, para);
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
			
		}
		
		
	}

	/**
	 * @Description 同程门票首页
	 * @author 汤玉林
	 * @date 2018年3月7日 下午3:04:51
	 * @action parseTongchengScenicFirst
	 * @param params
	 */
	public void parseTongchengScenicFirst(Params params) {
		String url = params.getUrl();
		String uuid = params.getUuid();
		String content = params.getContent();
		Document doc=Jsoup.parse(params.getContent());
		if(StringUtils.isNotBlank(content)){
			
			String keyword=url.substring(url.indexOf("_")+5, url.lastIndexOf(".")-14);
			//获得景点总数
			String total=doc.select("div.sel_num>p>span.orange").text();
			int scenicTotal=0;
			if(StringUtils.isNotBlank(total)){
				scenicTotal=Integer.valueOf(total);
			}
			int pageTotal=scenicTotal%10==0?scenicTotal/10:scenicTotal/10+1;
			for (int i = 1; i <= pageTotal; i++) {
				String scenicUrl="https://www.ly.com/scenery/NewSearchList.aspx?&action=getlist&page="+i+"&kw="+keyword;
				Params para = new Params();
				para.setUuid(uuid);
				para.setUrl(scenicUrl);
				para.setHttpType(Param.GET);
				para.setType(Param.TONGCHENG_SCENIC_PAGE);
				para.setDataSource(Param.TONGCHENG);
				produceService.sendMessage(topQueue, para);
			}
		}
		
	}

	/**
	 * @Description 同程景点评论解析
	 * @author 汤玉林
	 * @date 2018年3月7日 下午2:58:16
	 * @action parseTongchengStrokeComment
	 * @param params
	 */
	public void parseTongchengStrokeComment(Params params) {
		String uuid = params.getUuid();
		String content = params.getContent();
		if(StringUtils.isNotBlank(content)){
			try {
				JSONObject jsonOb2=new JSONObject(content);
				JSONObject jsonResponse2 = jsonOb2.getJSONObject("response");
				JSONObject jsonBody2 = jsonResponse2.getJSONObject("body");
				//获取当前页的所有评论
				JSONArray jsonArray = jsonBody2.getJSONArray("dpList");
				//获取评论信息
				for(int j=0;j<jsonArray.length();j++){
					Commentinfo commentinfo = new Commentinfo();
					String commentinfoId=UUID.randomUUID().toString();
					commentinfo.setId(commentinfoId);
					commentinfo.setInfoid(uuid);
					commentinfo.setType(1);
					commentinfo.setCreatedate(new Date());
					commentinfo.setDatasource("Tongcheng");
					commentinfo.setCreator("赵乐");
					commentinfo.setCreatorid("15736708180");
					JSONObject jsonObjectComment = jsonArray.getJSONObject(j);
					//评论内容
					String commentContent = jsonObjectComment.toString();
					commentinfo.setContent(commentContent);
					jedis.insertAndGetId(commentinfo);
				}
			} catch (Exception e) {
				e.printStackTrace();
				produceService.sendMessage(lowQueue, params);
			}
		}else{
			System.out.println("未获取到评论，重新放入队列");
			produceService.sendMessage(lowQueue, params);
		}
		
	}

	/**
	 * @Description 同程景点评论第一页解析
	 * @author 汤玉林
	 * @date 2018年3月7日 下午2:53:31
	 * @action parseTongchengStrokeCommentFirst
	 * @param params
	 */
	public void parseTongchengStrokeCommentFirst(Params params) {
		String uuid = params.getUuid();
		String url = params.getUrl();
		String content = params.getContent();
		if(StringUtils.isNotBlank(content)){
			try {
				JSONObject jsonOb=new JSONObject(content);
				JSONObject jsonResponse = jsonOb.getJSONObject("response");
				JSONObject jsonBody = jsonResponse.getJSONObject("body");
				JSONObject jsonpageInfo = jsonBody.getJSONObject("pageInfo");
				//补充评论评论人数
				String commentNum = jsonpageInfo.get("totalCount").toString();

				Sceinfo sceinfo =new Sceinfo();
				sceinfo.setId(uuid);
				sceinfo.setGradenum(commentNum);
				jedis.insertAndGetId(sceinfo);

				//取评论总页数
				String pageNumstr = jsonpageInfo.get("totalPage").toString();

				Integer pageNum=Integer.parseInt(pageNumstr);
				for(int i=1;i<=pageNum;i++){
					System.out.println("评论分页第"+i+"页");
					String url2=url.replace("page:1", "page:"+i);

					Params params2=new Params();
					params2.setUuid(uuid);
					params2.setUrl(url2);
					params2.setDataSource(Param.TONGCHENG);
					params2.setHttpType(Param.GET);
					params2.setType(Param.TONGCHENG_STROKE_COMMENT);
					produceService.sendMessage(lowQueue, params2);
				}
			} catch (Exception e) {
				e.printStackTrace();
				produceService.sendMessage(lowQueue, params);
			}
		}else{
			System.out.println("未获取到评论，重新放入队列");
			produceService.sendMessage(lowQueue, params);
		}
		
	}

	/**
	 * @Description 同程景点详情
	 * @author 汤玉林
	 * @date 2018年3月7日 下午2:44:09
	 * @action parseTongchengStrokeDetail
	 * @param params
	 */
	public void parseTongchengStrokeDetail(Params params) {
		String uuId = params.getUuid();
		String url = params.getUrl();
		String content = params.getContent();
		//景点基础信息对象
		Sceinfo sceinfo = params.getSceinfo();
		Addressinfo addressinfo = params.getAddressinfo();
		if(StringUtils.isNotBlank(content)){

			Document document=Jsoup.parse(content);
			try {
				Elements elementsdls = document.select("div.con_left>div.lablebox>dl");
				if(!elementsdls.isEmpty()){
					for (Element elementsdl : elementsdls) {
						Elements elementsdt = elementsdl.select("dt");
						Elements elementsdd = elementsdl.select("dd");
						String dtstr=elementsdt.isEmpty()?"":elementsdt.text();
						String ddstr=elementsdd.isEmpty()?"":elementsdd.text();

						switch (dtstr){

						case "景区特色：":
							break;
						case "简介：":
							//景点介绍
							String introduction=ddstr;
							sceinfo.setIntroduction(introduction);
							break;
							//景点开放时间描述
						case "开放时间：":
							String openTime=ddstr;
							sceinfo.setOpentime(openTime);
							break;
						case "游玩时长：":
							//建议游玩时间描述
							String adviceTime=ddstr;
							sceinfo.setAdvicetime(adviceTime);
							break;
						case "门票信息：":
							String otherInformation=ddstr;
							sceinfo.setOtherinformation(otherInformation);
							break;
						default:
							break;
						}
					}
				}
				sceinfo.setId(uuId);
				sceinfo.setUrlid(url);
				sceinfo.setDatatype("1");
				sceinfo.setCreatedate(new Date());
				sceinfo.setCreator("赵乐");
				sceinfo.setCreatorid("15736708180");

				if(StringUtils.isBlank(sceinfo.getName())){
					System.out.println("数据获取不完整，放回队列");
					produceService.sendMessage(highQueue, params);
				}else{
					jedis.insertAndGetId(sceinfo);
					//景点评分个数
					//景点去过人数/出游人数
					//景点想去人数
					//补充四级地址表信息
					addressinfo.setId(UUID.randomUUID().toString());
					addressinfo.setInfoid(uuId);
					addressinfo.setCreatedate(new Date());
					addressinfo.setCreator("赵乐");
					addressinfo.setCreatorid("15736708180");
					System.out.println("插入四级地址表");
					jedis.insertAndGetId(addressinfo);
					//图片信息
					Elements imurlEle = document.select("div.con_left>div.cityinfo>img");
					String imUrl=imurlEle.attr("src");
					Pictureinfo pictureInfo=new Pictureinfo();
					pictureInfo.setId(UUID.randomUUID().toString());
					pictureInfo.setInfoid(uuId);
					pictureInfo.setImgurl(imUrl);
					pictureInfo.setSort(1);
					pictureInfo.setType(1);
					pictureInfo.setCreatedate(new Date());
					pictureInfo.setCreator("赵乐");
					pictureInfo.setCreatorid("15736708180");
					System.out.println("插入图片");
					jedis.insertAndGetId(pictureInfo);
					
					//放下一级链接
					Elements selectProductId = document.select("input#poiResourceID");
					String productId=selectProductId.isEmpty()?"":selectProductId.attr("value");
					//设置景点评论的首页url
					if(!StringUtils.isBlank(productId)){
						Params para=new Params();
						String commentUrl="https://www.ly.com/go/RainbowClientAjax/getdianping";
						String param="requsetParms={projectTag:\"jingqu\",productId:"+productId+",reqFrom:1,tagId:1,page:1,pageSize:10}&serviceName=getdianpinglist";
						para.setUuid(uuId);
						para.setUrl(commentUrl+"?"+param);
						para.setHttpType(Param.GET);
						para.setType(Param.TONGCHENG_STROKE_COMMENT_FIRST);
						para.setDataSource(Param.TONGCHENG);
						System.out.println("插入评论url");
						produceService.sendMessage(lowQueue, para);
					}
				}

			} catch (Exception e) {
				produceService.sendMessage(highQueue, params);
				e.printStackTrace();
			}
		}

	}
	/**
	 * @Description 同程景点分页解析
	 * @author 汤玉林
	 * @date 2018年3月7日 下午2:16:25
	 * @action parseTongchengStrokePage
	 * @param params
	 */
	public void parseTongchengStrokePage(Params params) {
		String uuid = params.getUuid();
		String url = params.getUrl();
		String content = params.getContent();
		if(StringUtils.isNotBlank(content)){
			//获取景点首页链接
			if(url.startsWith("https://www.ly.com/go/RainbowClientAjax/GetAladdin?_dAjax=callback")){
				JSONObject jsonObj = new JSONObject(content);
				JSONObject jsonResponse=jsonObj.getJSONObject("response");
				JSONObject jsonBody =jsonResponse.getJSONObject("body");
				String totalPage=jsonBody.get("totalPage").toString();
				if(totalPage!=null && !"".equals(totalPage)){
					Integer pageNum=Integer.parseInt(totalPage);

					for(int i=1;i<=pageNum;i++){
						String pageHref=url.replace("pageIndex:1", "pageIndex:"+i).replace("_dAjax=callback&", "");
						Params para = new Params();
						para.setUuid(uuid);
						para.setUrl(pageHref);
						para.setHttpType(Param.GET);
						para.setType(Param.TONGCHENG_STROKE_PAGE);
						para.setDataSource(Param.TONGCHENG);
						produceService.sendMessage(topQueue, para);
					}
				}
			}

			//获取景点分页链接
			if(url.startsWith("https://www.ly.com/go/RainbowClientAjax/GetAladdin?requsetParms")){
				//转换为json格式
				JSONObject jsonpageIndex = new JSONObject(content);
				JSONObject jsonResponse=jsonpageIndex.getJSONObject("response");
				JSONObject jsonBody =jsonResponse.getJSONObject("body");
				JSONArray jsonArray = jsonBody.getJSONArray("dataList");
				for(int j=0;j<jsonArray.length();j++){
					//景点基础信息对象
					Sceinfo sceInfo = new Sceinfo();
					//四级地址表信息
					Addressinfo addressinfo=new Addressinfo();

					//获取每条景点的url和部分详情
					JSONObject jsonObject = jsonArray.getJSONObject(j);
					String cityId=jsonObject.get("cityId").toString();
					//poiId
					String poiId=jsonObject.get("poiId").toString();
					//城市名称
					String cityName=jsonObject.get("cityName").toString();
					//景点名称
					String name=jsonObject.get("title").toString();
					sceInfo.setName(name);
					//景点星级
					String starLevel=jsonObject.get("level").toString();
					sceInfo.setStarlevel(starLevel);
					//景点评分
					String scorestr=jsonObject.get("score").toString();
					String grade="";
					if(!"".equals(scorestr)){
						Double score=Double.parseDouble(scorestr);
						grade=score*20+"";
					}
					sceInfo.setGrade(grade);
					//景点参考价格
					String price=jsonObject.get("price").toString();
					sceInfo.setReferprice(price);
					//景点地址
					String address=jsonObject.get("address").toString();
					sceInfo.setAddress(address);
					//景点类型
					String type=jsonObject.get("type2Name").toString();
					sceInfo.setType(type);

					//景点经度
					String longitude=jsonObject.get("lonBD").toString();
					sceInfo.setLongitude(longitude);;
					//景点纬度
					String latitude=jsonObject.get("latBD").toString();
					sceInfo.setLatitude(latitude);

					sceInfo.setDatasource("Tongcheng");

					//四级地址信息
					//"亚洲,中国,海南,海口,",
					String pathName=jsonObject.get("pathName").toString();
					String str[]=pathName.split(",");
					if(str.length>3){
						String province=str[2];
						String country=str[1];
						addressinfo.setCountry(country);
						addressinfo.setProvince(province);
					}

					//存入地址表详细地址
					addressinfo.setDetailaddress(address);
					addressinfo.setCity(cityName);
					addressinfo.setType(1);

					String urlScenic="";
					if(!"".equals(cityId) && !"".equals(poiId)){
						urlScenic="https://www.ly.com/go/scenery/"+cityId+"/"+poiId+".html";
						System.out.println(urlScenic+"景点url");
					}
					Params para = new Params();
					para.setUuid(uuid);
					para.setUrl(urlScenic);
					para.setSceinfo(sceInfo);
					para.setCityName(cityName);
					para.setAddressinfo(addressinfo);
					para.setHttpType(Param.GET);
					para.setType(Param.TONGCHENG_STROKE_DETAIL);
					para.setDataSource(Param.TONGCHENG);
					produceService.sendMessage(highQueue, para);
				}
			}
		}

	}
	/**
	 * @Description 同程景点首页解析
	 * @author 汤玉林
	 * @date 2018年3月7日 上午10:59:15
	 * @action parseTongchengStrokeFirst
	 * @param params
	 */
	public void parseTongchengStrokeFirst(Params params) {
		String uuid = params.getUuid();
		String url = params.getUrl();
		String content = params.getContent();
		if(StringUtils.isNotBlank(content)){
			if(url.startsWith("https://www.ly.com/go/countryProvince")){
				Document document=Jsoup.parse(content);
				//解析省份的城市景点url
				Elements  elementsIndex= document.select("div.must_pro>ul.prolist.clearfix>li>a");
				for(Element element:elementsIndex){
					// https://www.ly.com/go/scenery/133.html
					String cityUrlstr="https://www.ly.com"+element.attr("href");// /go/area/133.html
					String cityUrl=cityUrlstr.replace("area", "scenery");
					Params para = new Params();
					para.setUuid(uuid);
					para.setUrl(cityUrl);
					para.setHttpType(Param.GET);
					para.setType(Param.TONGCHENG_STROKE_FIRST);
					para.setDataSource(Param.TONGCHENG);
					produceService.sendMessage(topQueue, para);
				}
			}

			if(url.startsWith("https://www.ly.com/go/scenery")){
				//解析城市景点的url
				//cityUrl链接 例如：上海  https://www.ly.com/go/scenery/321.html
				//获取页面隐藏的信息 例如：<input id="path" type="hidden" value="3106,1,25,321," />
				Document document=Jsoup.parse(content);
				Elements selectInput = document.select("input#path");
				String pathHidden=selectInput.isEmpty()?"":selectInput.attr("value");
				Integer cityId=NumUtils.getInteger(url);
				String selectCityId="";
				if(!"".equals(pathHidden)){
					String str[]=pathHidden.split(",");
					if(str.length==4){
						selectCityId=str[3];
					}
				}
				if(!StringUtils.isBlank(selectCityId)){
					String firstHref="https://www.ly.com/go/RainbowClientAjax/GetAladdin?_dAjax=callback&requsetParms={cityId:"+cityId+",selectCityId:"+selectCityId+",fromSite:1,poiType:1,type2Id:999,pageIndex:1,pageSize:15}&serviceName=getpoiorderfilterchoice";
					Params para = new Params();
					para.setUuid(uuid);
					para.setUrl(firstHref);
					para.setHttpType(Param.GET);
					para.setType(Param.TONGCHENG_STROKE_PAGE);
					para.setDataSource(Param.TONGCHENG);
					produceService.sendMessage(topQueue, para);

				}
			}
		}


	}
	

}
