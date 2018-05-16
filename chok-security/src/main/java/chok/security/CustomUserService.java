package chok.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.User;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import chok.util.http.HttpAction;
import chok.util.http.HttpResult;
import chok.util.http.HttpUtil;

public class CustomUserService //implements UserDetailsService
{
//	static Logger log = LoggerFactory.getLogger(CustomUserService.class);
//	static String apiURL = "http://localhost:8181/api";
//	
//	@Override
//    public UserDetails loadUserByUsername(String username)
//	{
////        SysUser user = userDao.findByUserName(username);
////        if (user != null) {
//			String actJson = getActJson("1", "3");
//			List<Map<String, Object>> actList=JSON.parseObject(actJson, new TypeReference<List<Map<String,Object>>>(){});
//			
//            List<GrantedAuthority> grantedAuthorities = new ArrayList <>();
//            for (Map<String, Object> act : actList) 
//            {
//                if (act != null && act.get("id")!=null) 
//                {
//	                GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(act.get("id").toString());
//	                //1：此处将权限信息添加到 GrantedAuthority 对象中，在后面进行全权限验证时会使用GrantedAuthority 对象。
//	                grantedAuthorities.add(grantedAuthority);
//                }
//            }
//            return new User(username, "123456", grantedAuthorities);
////        } else {
////            throw new UsernameNotFoundException("admin: " + username + " do not exist!");
////        }
//    }
//
//	public String getActJson(String userId, String appId)
//	{
//		Map<String, String> param = new HashMap<String, String>();
//		param.put("tc_user_id", userId);
//		param.put("tc_app_id", appId);
//		HttpResult<String> r = HttpUtil.create(new HttpAction(apiURL+"/getActByUserId.action", param), String.class, "GET");
//		if(log.isInfoEnabled()) log.info("[ACT_JSON] <<< " + r.getData());
//		return r.getData();
//	}
}
