package com.monkeyk.sos.config;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * cookie操作工具类
 * 
 * @author 彭盛
 * 
 */
public class CookieUtil {

  /** accessToken存放cookie中的名字 */
  public static final String SFS_COOKIENAME_ACCESSTOKEN = "C2AT";
  /** refreshToken存放cookie中的名字 */
  public static final String SFS_COOKIENAME_REFRESHTOKEN = "C2RT";

  /**
   * 设置参数到cookie
   * 
   * @param response
   * @param name cookie名字
   * @param value cookie值
   * @param maxAge cookie生命周期(秒)
   */
  public static void setCookie(HttpServletRequest request, HttpServletResponse response,
      String name, String value, int maxAge) {
    if (null != name && !name.trim().equals("")) {

      String path = request.getContextPath();
      path = (null != path && !path.trim().equals("")) ? path : "/";

      StringBuffer cookie = new StringBuffer();
      cookie.append(name).append("=").append(value).append(";");
      if (maxAge >= 0) {
        cookie.append("Max-Age=").append(maxAge).append(";");
      }
      cookie.append("path=").append(path).append(";");
      cookie.append("HttpOnly;");

      response.addHeader("Set-Cookie", cookie.toString());
    }
  }

  /**
   * 根据名字删除cookie
   * 
   * @param response
   * @param name cookie名字
   */
  public static void removeCookie(HttpServletRequest request, HttpServletResponse response,
      String name) {
	  setCookie(request, response, name, null, 0);
  }

  /**
   * 根据名字获取cookie中的值
   * 
   * @param request
   * @param name cookie名字
   * @return cookie中的值
   */
  public static String getCookieByName(HttpServletRequest request, String name) {
    if (null != name && !name.trim().equals("")) {
      Cookie[] cookies = request.getCookies();
      if (null != cookies) {
        for (Cookie cookie : cookies) {
          if (name.equals(cookie.getName())) {
            return cookie.getValue();
          }
        }
      }
    }
    return null;
  }
}
