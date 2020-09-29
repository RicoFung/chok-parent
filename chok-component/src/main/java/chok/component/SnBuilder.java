package chok.component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Component;

@Component
public class SnBuilder
{
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private RedisTemplate<String, Object> redisTemplate;
	
	public String[] buildBatch(String k, int d, int q)
	{
		List<String> snList = new ArrayList<String>();
		for (int i=0; i<q; i++)
		{
			String sn = build(k, d);
			if (null != sn)
			{
				snList.add(sn);
			}
		}
		String[] snArray = snList.toArray(new String[snList.size()]);
		return snArray;
	}
	
	public String build(String k, int d)
	{
		return build(k, d, 1, TimeUnit.DAYS);
	}
	
	public String build(String k, int d, long timeout, java.util.concurrent.TimeUnit timeUnit)
	{
		// 初始化业务流水号
		String bizSn = null;
		// 初始化流水号计数器
		RedisAtomicLong snCounter = new RedisAtomicLong(k, redisTemplate.getConnectionFactory());
		// 获取当前流水号
		Long nowSn = snCounter.longValue();
		// 初始化新流水号
		Long newSn = 0l;
		// 初始化流水号上限
		Double maxSn = Math.pow(10, d)-1;
		if (log.isDebugEnabled()) 
		{
			log.debug("key <== {}, nowSn <== {}, maxSn <== {}", k, nowSn, maxSn);
		}
		// 校验流水号上限，超出返回0，反之返回业务流水号
		if (nowSn < maxSn)
		{
			newSn = snCounter.incrementAndGet();
			if ((null == newSn || newSn.longValue() == 1))
			{
				// 初始设置过期时间
				snCounter.expire(timeout, timeUnit);
			}
			// 格式化按位数补0
			String suffixNum = String.format("%0"+d+"d", newSn);
			bizSn = k + suffixNum;
		}
		return bizSn;
	}
}
