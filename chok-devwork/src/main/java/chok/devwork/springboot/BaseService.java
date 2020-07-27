package chok.devwork.springboot;

import java.util.List;
import java.util.Map;

import chok.devwork.Page;

public abstract class BaseService<T,PK> 
{
	public abstract BaseDao<T, PK> getEntityDao();
	
	public int add(T po) throws Exception
	{
		return getEntityDao().add(po);
	}
	
	public int addBatch(List<T> list) throws Exception
	{
		return getEntityDao().addBatch(list);
	}
	
	public int addBatch(List<T> list, int size) throws Exception
	{
		return getEntityDao().addBatch(list, size);
	}

	public int upd(T po) throws Exception
	{
		return getEntityDao().upd(po);
	}
	
	public int updBatch(List<T> list) throws Exception
	{
		return getEntityDao().updBatch(list);
	}
	
	public int updBatch(List<T> list, int size) throws Exception
	{
		return getEntityDao().updBatch(list, size);
	}

	public int del(PK[] ids) throws Exception
	{
		int r = 0;
		for(PK id:ids)
		{
			r += getEntityDao().del(id);
		}
		return r;
	}

	public T get(PK id)
	{
		return (T) getEntityDao().get(id);
	}
	
	public Object getDynamic(Map<String, Object> param)
	{
		return getEntityDao().getDynamic(param);
	}

	public List<T> query(Map<String, Object> m) 
	{
		return getEntityDao().query(m);
	}
	
	public List<T> queryDynamic(Map<String, Object> m) 
	{
		return getEntityDao().queryDynamic(m);
	}
	
	public List queryMap(Map<String, Object> m)
	{
		return getEntityDao().queryMap(m);
	}
	
	public int getCount(Map<String, Object> m) 
	{
		return getEntityDao().getCount(m);
	}

	/**
	 * 分页查询
	 * @param countPageEach 可点击页码个数 
	 * @param m 表单查询参数
	 * @return Page对象
	 */
	public Page<T> getPage(int countPageEach, Map<String, Object> m)
	{
		return getEntityDao().getPage(countPageEach, m);
	}
	
	public List queryMapPage(Map<String, Object> m)
	{
		return getEntityDao().queryMapPage(m);
	}
}
