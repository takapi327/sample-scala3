package jwt

import java.security.Key

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.io.Encoders

object Jwt extends App:

  val key = Keys.secretKeyFor(SignatureAlgorithm.HS256)
  val jws = Jwts.builder().setSubject("takapi").signWith(key).base64UrlEncodeWith(Encoders.BASE64URL).compact()

  val secretString = Encoders.BASE64.encode(key.getEncoded)
  println(secretString)
  println(jws)

  println(Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jws).getBody.getSubject)

  val keyPair = Keys.keyPairFor(SignatureAlgorithm.RS256)
  val jws2    = Jwts.builder()
    .setSubject("takapi")
    .setHeaderParam("kid", "myKeyId")
    .setIssuer("me") // JWTを発行したプリンシパルを識別するためのもの (issuer = 発行者)
    .setAudience("you") // JWTが意図する受信者を特定する。 (audience = 受信者)
    //.setExpiration(new java.util.Date()) // JWTの処理を受け入れてはならない有効期限を特定する。 (expiration time = 有効期限)
    //.setNotBefore(new java.util.Date()) // JWTの処理を受け入れてはならない時間を特定する。 (not before = 有効時間)
    //.setIssuedAt(new java.util.Date()) // JWTが発行された時刻を特定する。(issued at = 発行時刻)
    .setId(java.util.UUID.randomUUID().toString) // JWTのための一意な識別子を提供する。
    .claim("hello", "world")
    .signWith(keyPair.getPrivate)
    .compact()

  println(jws2)
  println(Jwts.parserBuilder().setSigningKey(keyPair.getPublic).build().parseClaimsJws(jws2).getBody)
