package cn.jj.service;

import java.util.Map;
import java.util.UUID;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Repository;

import cn.jj.utils.ValidateUtil;

import com.holyrobot.data.Param;
import com.holyrobot.data.Params;

@Repository
public class MessageListenner2 implements MessageListener {
	
	@Autowired
	private IDownLoadService downLoadService;
	
	@Autowired
	private IParseService parseService;
	
	@Autowired
	private ProducerService producerService;
	
	@Autowired  
    ThreadPoolTaskExecutor threadPoolTaskExecutor;

	@Override
	public void onMessage(final Message message) {
		threadPoolTaskExecutor.execute(new Runnable() {
			
			@Override
			public void run() {
				ObjectMessage objMsg=(ObjectMessage) message;
				try {
					Params params = (Params) objMsg.getObject();
					
					System.out.println("监听器从lowQueue中取出"+params.getUrl());
					String url = params.getUrl();
					Param httpType = params.getHttpType();
					Map<String, String> header = params.getHeader();
					String content="";
					switch (httpType) {
					case GET:
						try { 
							content = downLoadService.httpClientGet(url,header);
							if(ValidateUtil.valid(content)){
								producerService.sendMessage(params.getDestinationName(), params);
							}
							
						} catch (Exception e) {
							producerService.sendMessage(params.getDestinationName(), params);
						}
						break;
						
					case POST:
						try { 
							String param = params.getPostParams();
							content = downLoadService.post(url,param,header);
							if(ValidateUtil.valid(content)){
								producerService.sendMessage(params.getDestinationName(), params);
							}
							
						} catch (Exception e) {
							producerService.sendMessage(params.getDestinationName(), params);
						}
						break;
					default:
						break;
					}
					if(!ValidateUtil.valid(content)) {
						params.setContent(content);
						parseService.parse(params);
					}
					
				} catch (JMSException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		
				
			}
		});
				
	}


}
