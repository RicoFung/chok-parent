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
		return createToken(userId, null, null, null, jwtConfig);
	}

	/**
	 * 生成token, 只在用户登录成功以后调用
	 * 
	 * @param userId
	 *            用户id
	 * @param claim
	 *            如果有私有声明，一定要先设置这个自己创建的私有的声明，这个是给builder的claim赋值，一旦写在标准的声明赋值之后，就是覆盖了那些标准的声明的
	 * @param jwtId
	 *            设置jti(JWT
	 *            ID)：是JWT的唯一标识，根据业务需要，这个可以设置为一个不重复的值，主要用来作为一次性token,从而回避重放攻击。
	 * @param subject
	 *            代表这个JWT的主体，即它的所有人，这个是一个json格式的字符串，可以存放什么userid，roldid之类的，作为什么用户的唯一标志。
	 * @param jwtConfig
	 *            JWT加密所需信息
	 * @return
	 */
	public static String createToken(String userId, Map<String, Object> claim, String jwtId, String subject,
			JwtConfig jwtConfig)
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
			JwtBuilder jwtBuilder = Jwts.builder().setHeaderParam("typ", "JWT")
					// 如果有私有声明，一定要先设置这个自己创建的私有的声明，这个是给builder的claim赋值，一旦写在标准的声明赋值之后，就是覆盖了那些标准的声明的
					.claim(JwtConstant.USER_ID_KEY, userId).addClaims(claim)
					// 设置jti(JWT ID)：是JWT的唯一标识，根据业务需要，这个可以设置为一个不重复的值，主要用来作为一次性token,从而回避重放攻击。
					.setId(jwtId)
					// issuer：jwt签发人
					.setIssuer(jwtConfig.getIssuer())
					// iat: jwt的签发时间
					.setIssuedAt(now)
					// sub(Subject)：代表这个JWT的主体，即它的所有人，这个是一个json格式的字符串，可以存放什么userid，roldid之类的，作为什么用户的唯一标志。
					.setSubject(subject)
					// 设置签名使用的签名算法和签名使用的秘钥
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
