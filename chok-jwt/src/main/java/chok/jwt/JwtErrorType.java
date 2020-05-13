package chok.jwt;

public enum JwtErrorType
{
	// 用户未认证
	USER_NO_AUTHENTICATED,
	// 请求头授权配置不正确
	AUTHORIZATION_HEADER_INCORRECT,
	// 不匹配
	TOKEN_MISMATCH,
	// 超时
	TOKEN_EXPIRED,
	// 解释失败
	TOKEN_PARSING_FAILED,
	// 签名失败
	TOKEN_SIGNING_FAILED;
}
