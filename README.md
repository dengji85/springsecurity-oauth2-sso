# springsecurity-oauth2-sso
fork from https://github.com/monkeyk/spring-oauth-server</br>
实现spring oauth2的sso登陆,

在原有作者的代码下添加spring oauth2的客户端已经可以实现单点登陆，但客户端退出是不会统一退出，只会退出当前系统
想想也是，使用oauth2做sso确实有点别扭，对于接入github的认证客户端也只会退出自己

token改成jwt，只是尝试，但demo服务器和客户端都还是有状态的，尝试改造成无状态的，但为了使用自带的表单登陆，发现重写相关代码的化，成本太高也能力有限
每次登陆都会去查询当前的全局会话，以实现统一注销
网上有很多方法，比如发消息通知客户端，客户端维护黑名单token等，各有所长


