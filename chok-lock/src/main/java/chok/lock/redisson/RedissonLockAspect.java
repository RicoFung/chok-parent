package chok.lock.redisson;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Scope
@Aspect
@Order(1)
public class RedissonLockAspect
{
	private final Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private RedissonClient redissonClient;

	@Pointcut("@annotation(chok.lock.redisson.RedissonLock)")
	private void lockPoint()
	{

	}

	@Around("lockPoint()")
	public Object around(ProceedingJoinPoint pjp) throws Throwable
	{
		Method method = ((MethodSignature) pjp.getSignature()).getMethod();
		RedissonLock redissonLock = method.getAnnotation(RedissonLock.class);
		String key = redissonLock.keyName();

		RLock lock = getLock(key, redissonLock);

		if (lock.tryLock())
		{
			try
			{
				if (log.isDebugEnabled())
					log.debug("get lock success [{}]", key);
				return pjp.proceed();
			}
			catch (Exception e)
			{
				log.error("execute locked method occured an exception", e);
			}
			finally
			{
				lock.unlock();
				if (log.isDebugEnabled())
					log.debug("release lock [{}]", key);
			}
		}
		else
		{
			if (log.isDebugEnabled())
				log.debug("{} [{}]", redissonLock.getLockFailMsg(), key);
			throw new Exception(redissonLock.getLockFailMsg());
		}
		return null;
	}

	private RLock getLock(String key, RedissonLock RedissonLock)
	{
		switch (RedissonLock.lockType())
		{
			case REENTRANT_LOCK:
				return redissonClient.getLock(key);

			case FAIR_LOCK:
				return redissonClient.getFairLock(key);

			case READ_LOCK:
				return redissonClient.getReadWriteLock(key).readLock();

			case WRITE_LOCK:
				return redissonClient.getReadWriteLock(key).writeLock();

			default:
				throw new RuntimeException("do not support lock type:" + RedissonLock.lockType().name());
		}
	}
}
