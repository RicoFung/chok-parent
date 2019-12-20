package chok.jwt;

import java.util.Date;
import java.util.Map;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;

public class JwtUtil
{
	private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

	private static final String AUTHORIZATION_HEADER_PREFIX = "Bearer ";

	// 构造私有
	private JwtUtil()
	{
	}

	/**
	 * 获取原始token信息
	 * 
	 * @param authorizationHeader
	 *            授权头部信息
	 * @return
	 */
	public static String getRawToken(String authorizationHeader)
	{
		return authorizationHeader.substring(AUTHORIZATION_HEADER_PREFIX.length());
	}

	/**
	 * 获取授权头部信息
	 * 
	 * @param rawToken
	 *            token信息
	 * @return
	 */
	public static String getAuthorizationHeader(String rawToken)
	{
		return AUTHORIZATION_HEADER_PREFIX + rawToken;
	}

	/**
	 * 校验授权头部信息格式合法性
	 * 
	 * @param authorizationHeader
	 *            授权头部信息
	 * @return
	 */
	public static boolean validate(String authorizationHeader)
	{
		return StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith(AUTHORIZATION_HEADER_PREFIX);
	}

	/**
	 * 生成token, 只在用户登录成功以后调用
	 * 
	 * @param userId
	 *            用户id
	 * @param jwtConfig
	 *            JWT加密所需信息
	 * @return
	 */
	public static String createToken(String userId, JwtConfig jwtConfig)
	{
		return createToken(userId, null, jwtConfig);
	}

	/**
	 * 生成token, 只在用户登录成功以后调用
	 * 
	 * @param userId
	 *            用户id
	 * @param claim
	 *            声明
	 * @param jwtConfig
	 *            JWT加密所需信息
	 * @return
	 */
	public static String createToken(String userId, Map<String, Object> claim, JwtConfig jwtConfig)
	{
		try
		{
			// 使用HS256加密算法
			SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

			long nowMillis = System.currentTimeMillis();
			Date now = new Date(nowMillis);

			// 生成签名密钥
			byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(jwtConfig.getBase64Secret());
			SecretKeySpec signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

			// 添加构成JWT的参数
			JwtBuilder jwtBuilder = Jwts.builder().setHeaderParam("typ", "JWT").claim(JwtConstant.USER_ID_KEY, userId)
					.addClaims(claim).setIssuer(jwtConfig.getName()).setIssuedAt(now)
					.signWith(signatureAlgorithm, signingKey);

			// 添加token过期时间（默认：毫秒）
			long TTLMillis = jwtConfig.getExpires();
			if ("s".equals(jwtConfig.getExpiresUtil()))
			{
				// 添加token过期时间（秒）
				TTLMillis = TTLMillis * 1000;
			}
			else if ("mi".equals(jwtConfig.getExpiresUtil()))
			{
				// 添加token过期时间（分）
				TTLMillis = TTLMillis * 60 * 1000;
			}
			
			if (TTLMillis >= 0)
			{
				long expMillis = nowMillis + TTLMillis;
				Date exp = new Date(expMillis);
				jwtBuilder.setExpiration(exp).setNotBefore(now);
			}

			return jwtBuilder.compact();
		}
		catch (Exception e)
		{
			log.error(JwtErrorType.TOKEN_SIGNING_FAILED.toString(), e);
			return null;
		}
	}

	/**
	 * 解析token
	 * 
	 * @param authToken
	 *            授权头部信息
	 * @param base64Secret
	 *            base64加密密钥
	 * @return
	 * @throws Exception 
	 */
	public static Claims parseToken(String authToken, String base64Secret) throws Exception
	{
		try
		{
			Claims claims = Jwts.parser().setSigningKey(DatatypeConverter.parseBase64Binary(base64Secret))
					.parseClaimsJws(authToken).getBody();
			return claims;
		}
		catch (SignatureException e1)
		{
			log.error(JwtErrorType.TOKEN_MISMATCH.toString(), e1);
			throw new Exception(JwtErrorType.TOKEN_MISMATCH.toString());
		}
		catch (ExpiredJwtException e2)
		{
			log.error(JwtErrorType.TOKEN_EXPIRED.toString(), e2);
			throw new Exception(JwtErrorType.TOKEN_EXPIRED.toString());
		}
		catch (Exception e3)
		{
			log.error(JwtErrorType.TOKEN_PARSING_FAILED.toString(), e3);
			throw new Exception(JwtErrorType.TOKEN_PARSING_FAILED.toString());
		}
	}
}
