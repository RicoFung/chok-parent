package chok.jwt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtConfig
{

	/**
	 * 发行者名
	 */
    @Value("${jwt.name}")
	private String name;

	/**
	 * base64加密密钥
	 */
    @Value("${jwt.base64-secret}")
	private String base64Secret;

	/**
	 * jwt中过期时间设置(分)
	 */
    @Value("${jwt.expires}")
	private int expires;
    
    /**
     * jwt中过期时间单位（毫秒(ms)/秒(s)/分(mi), 默认：ms）
     */
    @Value("${jwt.expires-util}")
    private String expiresUtil;

	public String getName()
	{
		return name;
	}

	public String getBase64Secret()
	{
		return base64Secret;
	}

	public int getExpires()
	{
		return expires;
	}

	public String getExpiresUtil()
	{
		return expiresUtil;
	}

}
