package cn.jj.service;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.holyrobot.common.Commentinfo;
import com.holyrobot.common.Hotelinfo;

import cn.jj.utils.KafkaUtils;
import cn.jj.utils.NumUtils;
import cn.jj.utils.PhantomjsUtils;

@Repository
public class CtripCommentByPhantomJs {
	
	@Autowired
	private JedisClient jedis;
	
	@Autowired
	private PhantomjsUtils phantomjsUtils;
	
	@Autowired
	private IDownLoadService downLoadService;
	
	/**
	 * 
	 * @Description 携程评论爬取（通过模拟浏览器）
	 * @author 汤玉林
	 * @date 2017年12月11日 上午11:56:30
	 * @action ctripCommentThirdByPhantomJs
	 */
	public void ctripCommentByPhantomJs(Hotelinfo hotel){
		String url=hotel.getUrlid();
		String uuId=hotel.getId();
		PhantomJSDriver driver= null;
		try {
			driver=(PhantomJSDriver) phantomjsUtils.getPhantomjs(false);
			driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
			driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
			driver.manage().timeouts().setScriptTimeout(10, TimeUnit.SECONDS);
			//链接放入浏览器
			driver.get(url);
			String html=driver.getPageSource();
			Document doc=Jsoup.parse(html);
			System.out.println(url);
			//总的评论数
			int commentCount=0;
			String comment=doc.select("li#commentTab>a").text();
			if(StringUtils.isNotBlank(comment)){
				try{
					commentCount=NumUtils.getInteger(comment);
				}catch(Exception e){
					commentCount=0;
				}
			}
			int pageTotal=commentCount%15==0?commentCount/15:commentCount/15+1;
			if(pageTotal>100){
				pageTotal=100;
			}
			for(int k=1;k<=pageTotal;k++){
				System.out.println("当前页："+k);
				try{
					//点击下一页
					WebElement searchButton = driver.findElement(By.cssSelector("div.c_page>a.c_down"));
					searchButton.click();
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}catch(Exception e){

				}
				html=driver.getPageSource();
				if(StringUtils.isNotBlank(html)){
					doc=Jsoup.parse(html);
					driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
					driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
					driver.manage().timeouts().setScriptTimeout(10, TimeUnit.SECONDS);
					Elements commentList=doc.select("div.comment_detail_list>div.comment_block");
					for(Element element:commentList){
						String commentDate=element.select("div.comment_main>div.comment_txt>div.comment_bar>p.comment_bar_info>span.time").text();
						Commentinfo commentInfo=new Commentinfo();
						commentInfo.setId(UUID.randomUUID().toString());
						commentInfo.setInfoid(uuId);
						commentInfo.setType(2);
						commentInfo.setCommentdate(commentDate);
						commentInfo.setContent(element.toString());
						commentInfo.setCreatedate(new Date());
						commentInfo.setCreator("tyl");
						commentInfo.setCreatorid("15736708180");
						commentInfo.setDatasource("Ctrip");

						jedis.insertAndGetId(commentInfo);
						
						String commentInfoObject = KafkaUtils.parseJsonObject(commentInfo, 11,1);
						String sendPost2 = downLoadService.sendPost(KafkaUtils.KAFKAURL, commentInfoObject);

						System.out.println("评论内容："+element.text());
					}
				}
			}

		} catch (Exception e) {
			System.out.println("报错了，小伙子！");
			driver.quit();
			driver=(PhantomJSDriver) phantomjsUtils.getPhantomjs(false);
			e.printStackTrace();
		}finally{
			driver.quit();
		}

	}
	
}
