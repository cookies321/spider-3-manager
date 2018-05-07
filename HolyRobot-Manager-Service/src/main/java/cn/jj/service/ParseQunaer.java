package cn.jj.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
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
import com.holyrobot.common.Pictureinfo;
import com.holyrobot.common.Sceinfo;
import com.holyrobot.common.Scepriceinfo;
import com.holyrobot.data.Param;
import com.holyrobot.data.Params;

import cn.jj.utils.DateUtil;
import cn.jj.utils.NumUtils;

@Repository
public class ParseQunaer {
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
	 * 
	 * @Description 解析
	 * @author 赵乐
	 * @date 2018年3月8日 上午10:15:06
	 * @action parseQunaerStrokeComment
	 * @param @param params
	 * @return void
	 */
	public void parseQunaerStrokeComment(Params params) {
		// TODO Auto-generated method stub
		String uuid=params.getUuid();
		String contentJson = params.getContent();
		try {
			String data="";
			try {
				JSONObject comJSON = new JSONObject(contentJson);
				data = comJSON.get("data").toString();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			if(!"".equals(data)){
				Document doc = Jsoup.parse(data);
				Elements commemtEle = doc.select("ul#comment_box>li");
				if(!commemtEle.isEmpty()){
					for (Element element : commemtEle) {
						Elements dateCom = element.select("div.e_comment_main>div.e_comment_main_inner>div.e_comment_add_info>ul>li");
						String commentDate = dateCom.isEmpty()?"":dateCom.first().text().substring(0,10);
						Commentinfo commentInfo = new Commentinfo();
						commentInfo.setId(UUID.randomUUID().toString());
						commentInfo.setInfoid(uuid);				
						commentInfo.setType(1);    //类型：1-景点，2-酒店，3-景点行程
						commentInfo.setCreatedate(new Date());
						commentInfo.setDatasource("Qunar");
						commentInfo.setCreator("姚良良");
						commentInfo.setCreatorid("13783985208");							
						commentInfo.setContent(element.toString());
						commentInfo.setCommentdate(commentDate);
						jedis.insertAndGetId(commentInfo);
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
	 * @Description 解析去哪儿景点详情
	 * @author 赵乐
	 * @date 2018年3月8日 上午9:13:41
	 * @action parseQunaerStrokeDetail
	 * @param @param params
	 * @return void
	 */
	public void parseQunaerStrokeDetail(Params params) {
		// TODO Auto-generated method stub
		String url = params.getUrl();
		String content = params.getContent();
		String uuid=params.getUuid();
		String productid=NumUtils.getInteger(url)+"";
		//景点基础信息对象
		Sceinfo sceInfo = new Sceinfo();
		//景点四级地址
		Addressinfo addressinfo = new Addressinfo();

		Document doc = Jsoup.parse(content);
		try {

			//景点名称
			Elements elementsName = doc.select("div#js_mainleft>div.b_title.clrfix>h1.tit");
			if(elementsName.isEmpty()){
				System.out.println(url+" 景点的名字获取失败!=====================");
				produceService.sendMessage(params.getDestinationName(),params);
			}else{
				String nameStr = elementsName.text();
				//String name = nameStr.split("[a-z|A-z]")[0]; 
				sceInfo.setName(nameStr);

				//景点地址
				Elements addressEle = doc.select("div.e_summary_list.clrfix>table>tbody>tr>td.td_l>dl:nth-child(1)>dd>span");
				if(addressEle.isEmpty()){
					addressEle = doc.select("div.e_summary_list_box>div.e_summary_list.clrfix>dl>dd>span");
				}
				String address = addressEle.isEmpty()?"":addressEle.text();
				sceInfo.setAddress(address);
				//开发时间
				Elements openTimeEle = doc.select("div.e_summary_list.clrfix>table>tbody>tr>td.td_r>dl:nth-child(1)>dd>span");				
				String openTime = openTimeEle.isEmpty()?"":openTimeEle.text();
				sceInfo.setOpentime(openTime);
				//获取景点介绍信息
				Elements introductionEle = doc.select("div#gs>div.e_db_content_box>div.short>div.content>p");
				String introduction=introductionEle.isEmpty()?"":introductionEle.text();
				sceInfo.setIntroduction(introduction);
				//经纬度
				Elements latlngEle = doc.select("div.mapbox_outside>div.mapbox");
				if(!latlngEle.isEmpty()){
					String latlng = latlngEle.attr("latlng");
					if(latlng != null && !"".equals(latlng)){
						String []latlngArr = latlng.split(",");
						String longitude = latlngArr[0];
						String latitude = latlngArr[1];
						sceInfo.setLongitude(longitude);
						sceInfo.setLatitude(latitude);
					}
				}
				//建议游玩时间描述
				Elements adviceTimeEle= doc.select("div.txtbox>div.time");
				if(!adviceTimeEle.isEmpty()){
					String adviceTimeStr = adviceTimeEle.text();
					String adviceTime = adviceTimeStr.split("：")[1];
					sceInfo.setAdvicetime(adviceTime);
				}				
				//景点评分
				Elements gradeEle = doc.select("div.m_scorebox>div.scorebox.clrfix>span.cur_score");
				String grade = gradeEle.isEmpty()?"":gradeEle.text();
				sceInfo.setGrade(grade);
				//景点评分个数
				Elements gradeNumEle = doc.select("div.e_nav_txt_box>span>span.e_nav_comet_num");
				String gradeNum = gradeNumEle.isEmpty()?"":gradeNumEle.first().text();
				
				//门票信息
				Elements select = doc.select("div#mp>div.e_db_content_box.e_db_content_dont_indent>p");
				String selectStr = select.toString();
				sceInfo.setOtherinformation(selectStr);
				
				sceInfo.setId(uuid);
				sceInfo.setUrlid(url);
				sceInfo.setGradenum(gradeNum);
				sceInfo.setDatatype("1");
				sceInfo.setDatasource("Qunaer");
				sceInfo.setCreatedate(new Date());
				sceInfo.setCreator("姚良良");
				sceInfo.setCreatorid("13783985208");
				//持久化或更新景点基础信息
				if(StringUtils.isBlank(sceInfo.getName())){
					System.out.println("数据获取不完整");
					produceService.sendMessage(params.getDestinationName(), params);
				}else{
					jedis.insertAndGetId(sceInfo);
					
					//国家
					Elements countryEle = doc.select("div.e_crumbs>ul.clrfix>li.item.pull:nth-child(3)>a");
					String country = countryEle.isEmpty()?"":countryEle.text();
					addressinfo.setCountry(country);
					//省
					Elements provinceEle = doc.select("div.e_crumbs>ul.clrfix>li.item.pull:nth-child(4)>a");
					String province = provinceEle.isEmpty()?"":provinceEle.text();
					addressinfo.setProvince(province);
					//市
					Elements cityEle = doc.select("div.e_crumbs>ul.clrfix>li.item.pull:nth-child(5)>a");
					String city = cityEle.isEmpty()?"":cityEle.text();
					addressinfo.setId(UUID.randomUUID().toString());
					addressinfo.setCity(city);
					addressinfo.setInfoid(uuid);
					addressinfo.setType(1);
					addressinfo.setDetailaddress(address);
					addressinfo.setCreatedate(new Date());
					addressinfo.setCreator("姚良良");
					addressinfo.setCreatorid("83985208");
					jedis.insertAndGetId(addressinfo);
					//景点图片
					Elements pictureEle = doc.select("ul#idNum>li>div.imgbox>img");
					if(!pictureEle.isEmpty()){
						System.out.println("图片的数目"+pictureEle.size());
						int n = 0;
						for(int j = 1; j <= pictureEle.size(); j++){
							Pictureinfo pictureinfo=new Pictureinfo();
							String pictureurl=pictureEle.attr("src");
							String uurid = UUID.randomUUID().toString();
							pictureinfo.setId(uurid);
							pictureinfo.setInfoid(uuid);
							pictureinfo.setImgurl(pictureurl);
							pictureinfo.setSort(j);
							pictureinfo.setType(1);
							pictureinfo.setDownload(0);
							pictureinfo.setCreatedate(new Date());
							pictureinfo.setCreator("姚良良");
							pictureinfo.setCreatorid("13783985208");
							jedis.insertAndGetId(pictureinfo);
						}
					}
					//景点价格
					Elements priceEle = doc.select("div#mp>div.e_ticket_info_box>div.e_ticket_info>dl");
					if(!priceEle.isEmpty()){
						for (Element element : priceEle) {
							Scepriceinfo priceInfo = new Scepriceinfo();
							//价格条目
							Elements priceItemEle = element.select("dt");
							if(priceItemEle.isEmpty()){
								System.out.println("该价格信息获取失败!");
								continue;
							}					
							String priceItem = priceItemEle.text();
							priceInfo.setPriceitem(priceItem);
							//市场价
							Elements marketingPriceEle = element.select("dd.e_old_price");
							String marketingPriceStr = marketingPriceEle.isEmpty()?"":marketingPriceEle.text();
							String marketingPrice = NumUtils.getInteger(marketingPriceStr).toString();
							priceInfo.setMarketingprice(marketingPrice);
							//销售价
							Elements salePriceEle = element.select("dd.e_now_price");
							String salePriceStr = salePriceEle.isEmpty()?"":salePriceEle.text().substring(1);
							String salePrice = NumUtils.getInteger(salePriceStr).toString();
							priceInfo.setSaleprice(salePrice);
							priceInfo.setId(UUID.randomUUID().toString());
							priceInfo.setScenicid(uuid);
							priceInfo.setUrlid(url);
							priceInfo.setCreatedate(new Date());
							priceInfo.setCreator("姚良良");
							priceInfo.setCreatorid("13783985208");
							jedis.insertAndGetId(priceInfo);
						}
						//"http://travel.qunar.com/place/api/html/comments/poi/"+productid+"?poiList=true&sortField=1&rank=0&pageSize=10&page="+j
						if(!"".equals(gradeNum)){
							Integer total = Integer.parseInt(gradeNum);
							Integer page = total%50 == 0?total/50:total/50+1;
							//去哪儿景点评论url
							for(int i=1;i<=page;i++){
								String commentUrl = "http://travel.qunar.com/place/api/html/comments/poi/"+productid+"?poiList=true&sortField=1&rank=0&pageSize=50&page="+i;
								Params para=new Params();
								para.setUuid(uuid);
								para.setUrl(commentUrl);
								para.setHttpType(Param.GET);
								para.setType(Param.QUNAER_STROKE_COMMENT);
								para.setDataSource(Param.QUNAER);
								para.setDestinationName("lowQueue");
								produceService.sendMessage(lowQueue,para);
							}
						}

					}
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 
	 * @Description 解析去哪儿景点分页链接
	 * @author 赵乐
	 * @date 2018年3月8日 上午9:12:36
	 * @action parseQunaerStrokePage
	 * @param @param params
	 * @return void
	 */
	public void parseQunaerStrokePage(Params params) {
		// TODO Auto-generated method stub
		//获取每个页面景点的URL链接，最多10个/页
		String content = params.getContent();
		String url = params.getUrl();
		Document doc = Jsoup.parse(content);
		Elements elements = doc.select("div.listbox>ul.list_item.clrfix>li.item>a.imglink");
		//记录每页获取的URL数
		if(!elements.isEmpty()){
			for (Element element : elements) {
				Params para = new Params();
				
				String strokeUrl = element.attr("href");
				para.setUrl(strokeUrl);
				para.setParentUrl(url);
				para.setType(Param.QUNAER_STROKE_DETAIL);
				para.setDataSource(Param.QUNAER);
				para.setHttpType(Param.GET);
				para.setDestinationName("highQueue");
				produceService.sendMessage(highQueue,para);

			}
		}
	}
	/**
	 * 
	 * @Description 解析去哪儿景点首页链接
	 * @author 赵乐
	 * @date 2018年3月8日 上午9:11:22
	 * @action parseQunaerStrokeFirst
	 * @param @param params
	 * @return void
	 */
	public void parseQunaerStrokeFirst(Params params) {
		// TODO Auto-generated method stub
		//获取document
		String content = params.getContent();
		String url=params.getUrl();
		Document doc = Jsoup.parse(content);
		//判断首页url，取城市的景点链接
		if(!url.contains("jingdian")){
			//判断上海和海南
			if("hainan".equals(url.substring(url.lastIndexOf("-")+1, url.length()))){
				Elements cityEle = doc.select("div#placebottomNav>dl.line.clrfix:nth-child(3)>dd>a");
				if(!cityEle.isEmpty()){
					for (Element element : cityEle) {
						String sceinfoUrl = element.attr("href")+"-jingdian";
						
						//调用activeMQ消息机制
						Params para=new Params();
						para.setUrl(sceinfoUrl);
						para.setType(Param.QUNAER_STROKE_FIRST);
						para.setDataSource(Param.QUNAER);
						para.setHttpType(Param.GET);
						para.setDestinationName("topQueue");
						produceService.sendMessage(topQueue,para);
					}
				}
			}else{
				String sceinfoUrl = url+"-jingdian";
				
				//调用activeMQ消息机制
				Params para=new Params();
				para.setUrl(sceinfoUrl);
				para.setType(Param.QUNAER_STROKE_FIRST);
				para.setDataSource(Param.QUNAER);
				para.setHttpType(Param.GET);
				para.setDestinationName("topQueue");
				produceService.sendMessage(topQueue,para);
			}
			//根据城市景点链接，取分页链接
		}else{
			if(!url.contains("-1-")){
				//获取页面分页的最大页码数
				Elements aHrefNum = doc.select("div.b_paging>a");
				Integer total = 0;
				if(aHrefNum.isEmpty()){
					total = 1;
				}else{
					String str = aHrefNum.get(aHrefNum.size() - 2).text();
					total = Integer.valueOf(str);
					for (int j = 1; j <= total; j++) {
						//获取该城市所有请求页面的链接
						String pageUrl = url+"-1-"+j;
						
						//调用activeMQ消息机制
						Params para=new Params();
						para.setUrl(pageUrl);
						para.setType(Param.QUNAER_STROKE_PAGE);
						para.setDataSource(Param.QUNAER);
						para.setHttpType(Param.GET);
						para.setDestinationName("topQueue");
						produceService.sendMessage(topQueue,para);

					}
				}
			}
			
		}
	}

}
