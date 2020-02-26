package chok.lock.redisson;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

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
		// 获取被AOP方法
		Method method = ((MethodSignature) pjp.getSignature()).getMethod();
		// 获取注解的锁对象
		RedissonLock redissonLock = method.getAnnotation(RedissonLock.class);
		// 获取锁key
		String key = redissonLock.lockKey();
		// 获取等待锁的时间
		long waitTime = redissonLock.waitTime();
		// 获取持有锁的时间
		long leaseTime = redissonLock.leaseTime();
		// 获取加锁时间单位
		TimeUnit timeUnit = redissonLock.timeUnit();
		// 获取锁对象
		RLock lock = getLock(key, redissonLock);
		// 获取tryLock选项
		int tryLockOption = redissonLock.tryLockOption();
		
		switch (tryLockOption)
		{
			case 1: return tryLock1(key, redissonLock, lock, pjp);
			case 2: return tryLock2(key, waitTime, timeUnit, redissonLock, lock, pjp);
			case 3: return tryLock3(key, waitTime, leaseTime, timeUnit, redissonLock, lock, pjp);
		}
		return null;

	}
	
	private Object tryLock1(String key, RedissonLock redissonLock, RLock lock, ProceedingJoinPoint pjp) throws Throwable 
	{
		if (log.isDebugEnabled())
			log.debug("Use tryLockOption is 1: tryLock()");
		
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
				log.debug("{} [{}]", redissonLock.lockFailMsg(), key);
			throw new Exception(redissonLock.lockFailMsg());
		}
		return null;
	}
	
	private Object tryLock2(String key, long waitTime, TimeUnit timeUnit, RedissonLock redissonLock, RLock lock, ProceedingJoinPoint pjp) throws Throwable 
	{
		if (log.isDebugEnabled())
			log.debug("Use tryLockOption is 2: tryLock(waitTime, timeUnit)");
		
		if (lock.tryLock(waitTime, timeUnit))
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
				log.debug("{} [{}]", redissonLock.lockFailMsg(), key);
			throw new Exception(redissonLock.lockFailMsg());
		}
		return null;
	}
	
	private Object tryLock3(String key, long waitTime, long leaseTime, TimeUnit timeUnit, RedissonLock redissonLock, RLock lock, ProceedingJoinPoint pjp) throws Throwable 
	{
		if (log.isDebugEnabled())
			log.debug("Use tryLockOption is 3: tryLock(waitTime, leaseTime, timeUnit)");
		
		if (lock.tryLock(waitTime, leaseTime, timeUnit))
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
				if (lock.isHeldByCurrentThread())
				{
					lock.unlock();
				}
				else
				{
					throw new Exception("Business timeout ! lock [" + key + "] has been released !");
				}
				if (log.isDebugEnabled())
					log.debug("release lock [{}]", key);
			}
		}
		else
		{
			if (log.isDebugEnabled())
				log.debug("{} [{}]", redissonLock.lockFailMsg(), key);
			throw new Exception(redissonLock.lockFailMsg());
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
