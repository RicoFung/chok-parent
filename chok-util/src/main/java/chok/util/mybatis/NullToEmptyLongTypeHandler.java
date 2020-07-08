package chok.util.mybatis;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

public class NullToEmptyLongTypeHandler implements TypeHandler<Long>
{

	@Override
	public Long getResult(ResultSet rs, String columnName) throws SQLException
	{
		return (rs.getString(columnName) == null) ? 0l : rs.getLong(columnName);
	}

	@Override
	public Long getResult(ResultSet rs, int columnIndex) throws SQLException
	{
		return (rs.getString(columnIndex) == null) ? 0l : rs.getLong(columnIndex);
	}

	@Override
	public Long getResult(CallableStatement cs, int columnIndex) throws SQLException
	{
		return (cs.getString(columnIndex) == null) ? 0l : cs.getLong(columnIndex);
	}

	@Override
	public void setParameter(PreparedStatement ps, int i, Long parameter, JdbcType jdbcType) throws SQLException
	{
		// TODO Auto-generated method stub
		
	}
}
