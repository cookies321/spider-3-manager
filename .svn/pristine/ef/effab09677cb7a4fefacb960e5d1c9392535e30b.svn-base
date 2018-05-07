package cn.jj.dao.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import cn.jj.service.JedisClient;
import cn.jj.utils.BeanToMapUtils;
import cn.jj.utils.CommonUtils;
import cn.jj.utils.MapToBeanUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Service
public class JedisClientSingle implements JedisClient{
	
	@Autowired
	private JedisPool jedisPool; 
	
	@Override
	public void addQueue(String Key, Object obj) {
		Jedis resource = null;
		try {
			resource = jedisPool.getResource();
			byte[] serialize = this.serialize(obj);
			resource.lpush(Key.getBytes(), serialize);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(resource!=null){
				resource.close();
			}
		}
		
	}
	
	@Override
	public String get(String key) {
		Jedis jedis = null;
		String string = null;
		try {
			jedis = jedisPool.getResource();
			string = jedis.get(key);
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(jedis!=null){
				jedis.close();
			}
		}
		return string;
	}

	@Override
	public String set(String key, String value) {
		Jedis jedis = null;
		String string = null;
		try {
			jedis = jedisPool.getResource();
			string = jedis.set(key, value);
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(jedis!=null){
				jedis.close();
			}
		}
		return string;
	}

	@Override
	public String hget(String hkey, String key) {
		Jedis jedis = null;
		String string = null;
		try {
			jedis = jedisPool.getResource();
			string = jedis.hget(hkey, key);
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(jedis!=null){
				jedis.close();
			}
		}
		return string;
	}

	@Override
	public long hset(String hkey, String key, String value) {
		Jedis jedis = null;
		Long result = null;
		try {
			jedis = jedisPool.getResource();
			result = jedis.hset(hkey, key, value);
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(jedis!=null){
				jedis.close();
			}
		}
		return result;
	}

	@Override
	public long incr(String key) {
		Jedis jedis = null;
		Long result = null;
		try {
			jedis = jedisPool.getResource();
			result = jedis.incr(key);
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(jedis!=null){
				jedis.close();
			}
		}
		return result;
	}

	@Override
	public long expire(String key, int second) {
		Jedis jedis = null;
		Long result = null;
		try {
			jedis = jedisPool.getResource();
			result = jedis.expire(key, second);
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(jedis!=null){
				jedis.close();
			}
		}
		return result;
	}

	@Override
	public long ttl(String key) {
		Jedis jedis = null;
		Long result = null;
		try {
			jedis = jedisPool.getResource();
			result = jedis.ttl(key);
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(jedis!=null){
				jedis.close();
			}
		}
		return result;
	}

	@Override
	public long del(String key) {
		Jedis jedis = null;
		Long result = null;
		try {
			jedis = jedisPool.getResource();
			result = jedis.del(key);
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(jedis!=null){
				jedis.close();
			}
		}
		return result;
	}
	
	@Override
	public long hdel(String hkey, String key) {
		Jedis jedis = null;
		Long result = null;
		try {
			jedis = jedisPool.getResource();
			result = jedis.hdel(hkey, key);
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(jedis!=null){
				jedis.close();
			}
		}
		return result;
	}
	

	@Override
	public List<Object> lrange(String key, int start, int end) {
		Jedis jedis = null;
		List<Object> obj = new ArrayList<>();
		try {
			jedis = jedisPool.getResource();
			List<byte[]> list = jedis.lrange(key.getBytes(), start, end);
			for (int i = 0; i < list.size(); i++) {
				byte[] bs = list.get(i);
				Object unserizlize = this.unserizlize(bs);
				obj.add(unserizlize);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(jedis!=null){
				jedis.close();
			}
		}
		return obj;
	}

	@Override
	public void addObjectToList(String Key, Object obj) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			jedis.lpush(Key.getBytes(), this.serialize(obj));
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(jedis!=null){
				jedis.close();
			}
		}
	}

	@Override
	public Object pollFromList(String key) {
		Jedis jedis = null;
		Object unserizlize = null;
		try {
			jedis = jedisPool.getResource();
			byte[] rpop = jedis.rpop(key.getBytes());
			if (Objects.nonNull(rpop)) {
				unserizlize = this.unserizlize(rpop);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(jedis!=null){
				jedis.close();
			}
		}
		return unserizlize;
	}

	@Override
	public byte[] serialize(Object obj) {
		ObjectOutputStream obi = null;
		ByteArrayOutputStream bai = null;
		try {
			bai = new ByteArrayOutputStream();
			obi = new ObjectOutputStream(bai);
			obi.writeObject(obj);
			byte[] byt = bai.toByteArray();
			return byt;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Object unserizlize(byte[] byt) {
		ObjectInputStream oii = null;
		ByteArrayInputStream bis = null;
		bis = new ByteArrayInputStream(byt);
		try {
			oii = new ObjectInputStream(bis);
			Object obj = oii.readObject();
			return obj;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Set<String> getKeys(String key) {
		Jedis jedis = null;
		Set<String> keys = null;
		try {
			jedis = jedisPool.getResource();
			keys = jedis.keys(key);
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(jedis!=null){
				jedis.close();
			}
		}
		return keys;
	}
	
	/**
	 * 多条件查询
	 */
	@Override
	public <T> List<T> selectByMultipleAttribute(Object record) {
	     List<T> list =  new LinkedList<>();
	     try {
	    	  Map<String,String> map =  new LinkedHashMap<>();
	    	  //得到多条件查询的map
	    	  map = BeanToMapUtils.selectfunction(record);
	    	  //得到当前参数的类名
	    	  String className = CommonUtils.getClassName(record.getClass());
	    	  //找到当前对象的类的所有的key
	    	  Jedis jedis = jedisPool.getResource();
	  		  Set<String> keysSet = jedis.keys(className+"-*");
	    	  //循环
	    	  for(String key :keysSet){
	    		  //根据key查找哈希结构的数据
	    		  Map<String,String> redisMap = jedis.hgetAll(key);
	    		  //调用工具类,判断当前redis中的哈希数据是否满足条件
	    		  Boolean flag =  CommonUtils.isOK(map,redisMap);
	    		  if(flag){
	    			  //满足条件,把这个map转化成对象
	    			  Object object = MapToBeanUtils.getEntityByMap(record.getClass(), redisMap);
	    			  //把转化成的对象加入到集合中
	    			  list.add((T) object);
	    		  }
	    	  }
	    	  jedis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	     if(list.size()==0){
	    	 System.out.println("未查询出结果");
	     }
	     return list;
	}
	
	/**
	 * 根据id查找特定对象(直接使用根据key,找到特定的map,并封装成对象返回)
	 */
	@Override
	public Object getBeaninfoById(Class clazz,String id) { 
		Object  object =  null;
		Map<String,String> map= new HashMap<>();
		Jedis jedis = jedisPool.getResource();
		//得到查询redis的key
		String key = CommonUtils.getClassName(clazz)+"-"+id;
		try {
		//得到map
		map = jedis.hgetAll(key);
		//转化成实体类
		object  = MapToBeanUtils.getEntityByMap(clazz, map);
		} catch (Exception e) {
			
			e.printStackTrace();
		}finally{
			jedis.close();
		}
		return object;
	}
	/**
	 * 插入单条记录,并得到id
	 */
	@Override
	public String insertAndGetId(Object record) {
		Map<String,String> map = new HashMap<>();
		Map<String,String> existMap =  new HashMap<>();
		Map<String,String> updateMap =  new HashMap<>();
		Jedis jedis = jedisPool.getResource();
		try {
			//把传过来的对象转化成map
			map = BeanToMapUtils.insertfunction(record);
			//判断是否有id
			String id = map.get("id");
			if("".equals(id)){
				//对象中没有id,自己设置一个id
				id = UUID.randomUUID().toString();
				map.put("id", id);
				//执行插入
				jedis.hmset(CommonUtils.getClassName(record.getClass())+"-"+id, map);
				return id;
			}else{
				//对象中有id
				//判断缓存中是否含有这条记录
				String key = CommonUtils.getClassName(record.getClass())+"-"+id;
				Set<String> set = jedis.keys(key);
				if(set.size()==0){
					//找不到,说明缓存数据库中没有相同的记录,直接插入
					jedis.hmset(key, map);
					return id;
				}else{
					//找的到,则先要删除从数据库中删除相同的记录
					//把需要更新的对象转化成map
					updateMap = BeanToMapUtils.selectfunction(record);
					existMap  = jedis.hgetAll(key);
					//删除旧的对象
					jedis.del(key);
					//整个两个map到一个map中
					updateMap = CommonUtils.updateMap(existMap, updateMap);
					//再执行插入
					jedis.hmset(key, updateMap);
					return id ;
				}
			}
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}finally{
			jedis.close();
		}
		return null;
	}
	/**
	 * 批量添加,返回插入的成功的记录数
	 */
	@Override
	public <T> long insertBatch(List<T> objectList) {
		long count = 0;
		try{
			for( Object object : objectList){
				//调用单条插入
				this.insertAndGetId(object);
				count++;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return count;
	}
	/**
	 * 根据id删除特定对象(这种就是直接根据key删除单条记录)
	 */
	@Override
	public int deleteById(Object record) {
		Jedis jedis = jedisPool.getResource();
		Map<String,String> map =  new LinkedHashMap<>();
		//把传过来的对象转化成map
		try {
			map = BeanToMapUtils.selectfunction(record);
			String id =  map.get("id");
			if(map.get("id") != null){
				//如果传入的对象有id,执行删除
				String key  = CommonUtils.getClassName(record.getClass())+"-"+id;
				jedis.del(key);
				return 1;
			}else{
				//传入的对象没有id,无法删除
				return 0 ;
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			jedis.close();
		}
		return 0;
	}

	/**
	 * 多条件删除(可以调用多条件查询,查询玩以后得到的list,把里面的数据一个个地删除)
	 */
	public void deleteByMultipleAttribute(Object record) {
		//调用多条件查询,得到符合条件的list
		List<Object> list = new LinkedList<>();
		list = this.selectByMultipleAttribute(record);
		try{
			//循环删除
			for(Object object:list){
				//调用单条删除的方法进行删除进行删除
				this.deleteById(object);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}

	/* (non-Javadoc)
	 * @see cn.jj.dao.JedisClient#close()
	 */
	@Override
	public void close() {
		
		
	}

	@Override
	public void flushDB() {
		// TODO Auto-generated method stub
		Jedis jedis = jedisPool.getResource();
		jedis.flushDB();
		jedis.close();
	}

	
	

}
