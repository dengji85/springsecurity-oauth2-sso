package com.monkeyk.sos.config;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.SqlLobValue;
import org.springframework.security.oauth2.common.util.SerializationUtils;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.code.RandomValueAuthorizationCodeServices;
import org.springframework.util.Assert;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Implementation of authorization code services that stores the codes and authentication in a database.
 * 
 * @author Ken Dombeck
 * @author Dave Syer
 */
public class MyJdbcAuthorizationCodeServices extends RandomValueAuthorizationCodeServices {

	private static final String DEFAULT_SELECT_STATEMENT = "select code, authentication from oauth_code where code = ?";
	private static final String DEFAULT_INSERT_STATEMENT = "insert into oauth_code (code, authentication) values (?, ?)";
	private static final String DEFAULT_DELETE_STATEMENT = "delete from oauth_code where code = ?";

	//my
	private static final String DEFAULT_SELECT_SSOKEY = "select ssokey from oauth_code where code = ?";
	
	private String selectAuthenticationSql = DEFAULT_SELECT_STATEMENT;
	private String insertAuthenticationSql = DEFAULT_INSERT_STATEMENT;
	private String deleteAuthenticationSql = DEFAULT_DELETE_STATEMENT;

	private final JdbcTemplate jdbcTemplate;

	public MyJdbcAuthorizationCodeServices(DataSource dataSource) {
		Assert.notNull(dataSource, "DataSource required");
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	protected void store(String code, OAuth2Authentication authentication) {
        HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
		jdbcTemplate.update(insertAuthenticationSql,
				new Object[] { code, new SqlLobValue(SerializationUtils.serialize(authentication)) }, new int[] {
						Types.VARCHAR, Types.BLOB});
        String ssoAT = CookieUtil.getCookieByName(request, OauthConstants.SSO_ACCESS_TOKEN_NAME);
		MyJwtTokenStore.map.put(code, ssoAT);
	}

	public OAuth2Authentication remove(String code) {
		OAuth2Authentication authentication;

		try {
			authentication = jdbcTemplate.queryForObject(selectAuthenticationSql,
					new RowMapper<OAuth2Authentication>() {
						public OAuth2Authentication mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							return SerializationUtils.deserialize(rs.getBytes("authentication"));
						}
					}, code);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}

		if (authentication != null) {
			jdbcTemplate.update(deleteAuthenticationSql, code);
		}

		return authentication;
	}

	public String selectSsoKeyBycode(String code) {
		return jdbcTemplate.queryForObject(MyJdbcAuthorizationCodeServices.DEFAULT_SELECT_SSOKEY,new String[] {code},String.class);
	}
	public void setSelectAuthenticationSql(String selectAuthenticationSql) {
		this.selectAuthenticationSql = selectAuthenticationSql;
	}

	public void setInsertAuthenticationSql(String insertAuthenticationSql) {
		this.insertAuthenticationSql = insertAuthenticationSql;
	}

	public void setDeleteAuthenticationSql(String deleteAuthenticationSql) {
		this.deleteAuthenticationSql = deleteAuthenticationSql;
	}
}
