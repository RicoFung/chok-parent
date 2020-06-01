package chok.devwork.springboot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;

import chok.devwork.Page;


public abstract class BaseDao<T,PK>
{
	/**
	 * 用于返回命名空间的全路径Class.getName()
	 * @return Class
	 */
	protected abstract Class<T> getEntityClass();
	protected abstract SqlSession getSqlSession();
	private String _statement = null;
	private static int DEFAULT_OFFSET = 1;
	private static int DEFAULT_LIMIT = 5;
	
	/**
	 * 返回命名空间的值
	 * @return String
	 */
	private String getSqlNamespace()
	{
		return getEntityClass().getName();
	}

	/**
	 * 获取需要操作sql的id，当getEntityClass().getName()无法满足时，可以重载此方法
	 * @param statementName SQL的ID(不包含namespace)
	 * @return String
	 */
	protected String getStatementName(String statementName)
	{
		if(_statement == null)
		{
			_statement = getSqlNamespace() + ".";
		}
		return _statement + statementName;
	}
	
	public int add(T po)
	{
		return this.getSqlSession().insert(getStatementName("add"), po);
	}
	
	public int upd(T po)
	{
		return this.getSqlSession().update(getStatementName("upd"), po);
	}

	public int del(PK id)
	{
		return this.getSqlSession().delete(getStatementName("del"), id);
	}

	@SuppressWarnings("unchecked")
	public T get(PK id)
	{
		return (T) this.getSqlSession().selectOne(getStatementName("get"), id);
	}
	
	public Map<String, Object> getMap(Map<String, Object> m)
	{
		return getMap("getMap", m);
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Object> getMap(String statementName, Map<String, Object> m)
	{
		return (Map<String, Object>) this.getSqlSession().selectOne(getStatementName(statementName), m);
	}
	
	public T getOnSelectFields(String[] selectFields, String pkKey, PK pkValue)
	{
		return getOnSelectFields("getOnSelectFields", "selectFields", selectFields, pkKey, pkValue);
	}
	
	@SuppressWarnings("unchecked")
	public T getOnSelectFields(String statementName, String selectFieldsKey, String[] selectFieldsValue, String pkKey, PK pkValue)
	{
		Map<String, Object> param = new HashMap<String, Object>();
		param.put(selectFieldsKey, selectFieldsValue);
		param.put(pkKey, pkValue);
		return (T) this.getSqlSession().selectOne(getStatementName(statementName), param);
	}
	
	public Map<String, Object> getMapOnSelectFields(String[] selectFields, String pkKey, PK pkValue)
	{
		return getMapOnSelectFields("getMapOnSelectFields", "selectFields", selectFields, pkKey, pkValue);
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Object> getMapOnSelectFields(String statementName, String selectFieldsKey, String[] selectFieldsValue, String pkKey, PK pkValue)
	{
		Map<String, Object> param = new HashMap<String, Object>();
		param.put(selectFieldsKey, selectFieldsValue);
		param.put(pkKey, pkValue);
		return (Map<String, Object>) this.getSqlSession().selectOne(getStatementName(statementName), param);
	}
	
	public List<T> query(Map<String, Object> m)
	{
		return this.getSqlSession().selectList(getStatementName("query"), m);
	}
	
	public List<T> queryOnSelectFields(String[] selectFields, Map<String, Object> param)
	{
		return queryOnSelectFields("queryOnSelectFields", "selectFields", selectFields, param);
	}
	
	@SuppressWarnings("unchecked")
	public List<T> queryOnSelectFields(String statementName, String selectFieldsKey, String[] selectFieldsValue, Map<String, Object> param)
	{
		param.put(selectFieldsKey, selectFieldsValue);
		return (List<T>) this.getSqlSession().selectList(getStatementName(statementName), param);
	}
	
	public List<Object> queryMap(Map<String, Object> m)
	{
		return this.getSqlSession().selectList(getStatementName("queryMap"), m);
	}
	
	public List<Map<String, Object>> queryMap(String statementName, Map<String, Object> m)
	{
		return this.getSqlSession().selectList(getStatementName(statementName), m);
	}
	
	public List<Map<String, Object>> queryMapOnSelectFields(String[] selectFields, Map<String, Object> param)
	{
		return queryMapOnSelectFields("queryMapOnSelectFields", "selectFields", selectFields, param);
	}
	
	public List<Map<String, Object>> queryMapOnSelectFields(String statementName, String selectFieldsKey, String[] selectFieldsValue, Map<String, Object> param)
	{
		param.put(selectFieldsKey, selectFieldsValue);
		return this.getSqlSession().selectList(getStatementName(statementName), param);
	}
	
	public int getCount(Map<String, Object> m)
	{
		return (Integer) this.getSqlSession().selectOne(getStatementName("getCount"), m);
	}

	/**
	 * 分页查询
	 * @param countPageEach 可点击页码个数 
	 * @param m 表单查询参数
	 * @return Page对象
	 */
	public Page<T> getPage(int countPageEach, Map<String, Object> m)
	{
		int curPage = !m.containsKey("offset")?DEFAULT_OFFSET:Integer.parseInt(m.get("offset").toString());
		int limit = !m.containsKey("limit")?DEFAULT_LIMIT:Integer.parseInt(m.get("limit").toString());
		int offset = curPage*limit-(limit-1);
		//总记录数
		int totalCount = getCount(m);
		//总页码
		int countPage = totalCount%limit>0?totalCount/limit+1:totalCount/limit;
		//mysql index 从0开始，所以要减一；oracle index 从1开始
		offset--;
		
		m.put("offset", String.valueOf(offset));
		m.put("limit", String.valueOf(limit));
		List<T> result = query(m);
		return new Page<T>(curPage, countPage, countPageEach, limit, result);
	}
	
	public List queryMapPage(Map<String, Object> m)
	{
		List result = queryMap(m);
		return result;
	}
	
	public int add(String statementName, Object param)
	{
		return this.getSqlSession().insert(getStatementName(statementName), param);
	}
	
	public int del(String statementName, Object param)
	{
		return this.getSqlSession().delete(getStatementName(statementName), param);
	}
	
	public int upd(String statementName, Object param)
	{
		return this.getSqlSession().update(getStatementName(statementName), param);
	}
	
	public Object get(String statementName, Object param)
	{
		return this.getSqlSession().selectOne(getStatementName(statementName), param);
	}
	
	public List<T> query(String statementName, Object param)
	{
		return this.getSqlSession().selectList(getStatementName(statementName), param);
	}
	
	public int getCount(String statementName, Object param)
	{
		return this.getSqlSession().selectOne(getStatementName(statementName), param);
	}
}
