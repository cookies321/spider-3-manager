/** 
 * Project Name:HolyRobot-Manager-Service 
 * File Name:ConsumerServiceImpl.java 
 * Package Name:cn.jj.dao.impl 
 * Date:2018年1月11日 下午2:40:53 
 * author 汤玉林
 */ 
package cn.jj.service;

import javax.jms.Destination;
import javax.jms.ObjectMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;


/**
 * @Description: TODO
 * @author 汤玉林
 * @date 2018年1月11日 下午2:40:53 
 */
@Service
public class ConsumerServiceImpl implements ConsumerService{

	@Autowired
	private JmsTemplate jmsTemplate;
	
	/**f
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
	
	@Override
	public ObjectMessage receive(){
		//默认队列
		ObjectMessage om = (ObjectMessage)jmsTemplate.receive(lowQueue);  
		if(om==null){
			om = (ObjectMessage) jmsTemplate.receive(highQueue);
			System.out.println("从高级队列" + highQueue + "收到了消息：\t"  
			        + om);  
			if(om==null){
				System.out.println("从顶级队列" + topQueue + "收到了消息：\t"  
				        + om);  
				om = (ObjectMessage) jmsTemplate.receive(topQueue);
			}
		}
	        System.out.println("从低级队列" + lowQueue + "收到了消息：\t"  
			        + om);  
			return om;  
	}

	
	@Override
	public ObjectMessage receive(String destination) {
		// TODO Auto-generated method stub
		return null;
	}

}
