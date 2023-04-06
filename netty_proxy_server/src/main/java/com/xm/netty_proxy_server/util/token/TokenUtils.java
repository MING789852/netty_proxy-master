package com.xm.netty_proxy_server.util.token;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.xm.netty_proxy_server.exception.CommonException;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TokenUtils {
    //设置过期时间
    private static final long EXPIRE_DATE=5*60*60*1000;
    //token秘钥
    private static final String TOKEN_SECRET = "xm87654321xm";

    /**
     * @param username
     * @param password MD5加密密码
     * @return
     */
    public static String getToken (String username,String password){
        String token="";
        try {
            //过期时间
            Date date=new Date(System.currentTimeMillis()+EXPIRE_DATE);
            //秘钥及加密算法
            Algorithm algorithm = Algorithm.HMAC256(TOKEN_SECRET);
            //设置头部信息
            Map<String,Object> header = new HashMap<>();
            header.put("typ","JWT");
            header.put("alg","HS256");
            //携带username，password信息，生成签名
            token=JWT.create().withHeader(header)
                    .withClaim("username",username)
                    .withClaim("password",password)
                    .withExpiresAt(date)
                    .sign(algorithm);
            return token;
        }catch (Exception e){
            return null;
        }
    }

    /**
     * 验证token，通过返回true
     * @param token
     * @return
     */
    public static boolean verify(String token){
        try {
            Algorithm algorithm = Algorithm.HMAC256(TOKEN_SECRET);
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT jwt = verifier.verify(token);
            return true;
        }catch (TokenExpiredException e){
            e.printStackTrace();
            throw new CommonException(909,"token过期");
        } catch (Exception e){
            e.printStackTrace();
            return  false;
        }
    }
}
