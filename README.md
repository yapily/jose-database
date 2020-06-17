| |Current Status|
|---|---|
|Build|[![Build Status](https://img.shields.io/endpoint.svg?url=https%3A%2F%2Factions-badge.atrox.dev%2Fyapily%2Fjose-database%2Fbadge%3Fref%3Dmaster&style=flat)](https://actions-badge.atrox.dev/yapily/jose-database/goto?ref=master)|
|Code coverage|[![codecov](https://codecov.io/gh/yapily/jose-database/branch/master/graph/badge.svg)](https://codecov.io/gh/yapily/jose-database)
|Bintray|[![Bintray](https://img.shields.io/bintray/v/qcastel-yapily/jose-database/jose-database.svg?maxAge=2592000)](https://bintray.com/qcastel-yapily/jose-database/jose-database)|
|License|![license](https://img.shields.io/github/license/yapily/jose-database)|

# Jose Database

Encrypt/sign a field of your database using the JOSE standard.

Instead of storing in clear a field, like the emails of a user, this library will help you packaging those emails into a JWS(JWE), like:

```
eyJraWQiOiJ2YWxpZC1zaWduaW5nLWtleSIsImFsZyI6IlBTNTEyIn0.ZXlKcmFXUWlPaUoyWVd4cFpDMWxibU55ZVhCMGFXOXVMV3RsZVNJc0ltVnVZeUk2SWtFeU5UWkhRMDBpTENKaGJHY2lPaUpTVTBFdFQwRkZVQzB5TlRZaWZRLm45ZnMzVTl4YWYtLS1MQjBZVWlLdmVZWEZsMU9HcHBDTWxtREVxMnFkU1daR0RjZVk3NWpkX3dvdTdVOWo4Z0RNbUlFN3Z0Z2tXeWdxQlIzcFlOWUJyRzFVVEp2SVFRMWdPY1Y2TmtXbmN2ZlZyNWw2MEI2cHFQWi0wWHc3Z2w4UW9fVjZURWFyRjhMYi05RlZ2RGVpMDY4eHprXzZzSUlNcWFLallOT3RUNGV0S3BNR2MtQ3ZDRmlveWlEQjR3WmlJWTNSWUp1ZkJsbFV6UlY4emtaeWhWMVN5SkIya2p5aERtT0Z1UFJRampmc0o5aHhsRGVuM3dBeFctcy1mclFGb0t0RVFfSjBzVGswSVFVclJhY2Z0bWRFeEdDQjNka21XYk5jWnVac1M2bFM0YTRTMk4zOG9Zcnp0MnlaMWlEYm4tUUhjekhTanJKcm5rcDRnWUxndy5QZ2FhMVFlVzNLLTNMUVFLLnhUaUdWZnVPdFEuY3JhSU0ybWZDbHRTWlpBXzVpaGxxQQ.n3wVbDKcCGuDItQ6ct00L3Iq8-Q_PdSm956IhyJHIzKy_vgkODimEVmIO8hH_3VYaTTOa-Kgswg1W1k-b2UHIDLNZqrqDFjPXYjT70b0rSkGmWqtjvtrrfMPszsmdQkdFcqTV7GtZb6K-Kw4bZBao3_MZN1DYLNa8uDfiC-3ccZVCKHohY4awf6o6bvZHR6JzlSEgv0srjaZjsl5o5GsYoES2RJr9SqHp7-Vy9DRrdTiqtaNOyYNAp3MmK3KbaWvripyCgqd5U-5idWPgQ9KJNzPSAu1M2tEAaOXUH3lmmjOiOU_ldca0AnWtN0zBOarcKN-ArViRL-fPMv2t0BwcQ
```

The main advantages is the inter-operability of using JOSE as a way of encrypting/signing this attribute.


# Features:

- Format the attribute as JWS, JWE, JWS(JWE) or JWE(JWS)
- The format of the keys are in a JWK format, for also a better inter-operability
- Allow a key rotation implementation by having the concept of valid keys, expired keys and revoked keys
- Custom actuator endpoint to identify the current state of the keys, from the angle of your micro-service

# How to install

## Dependency

The library is on jcenter, you will first need to add jcenter repository into your pom

```$xslt
<repositories>
  <repository>
    <id>jcenter</id>
    <url>https://jcenter.bintray.com/</url>
  </repository>
</repositories>
```

Then add the maven dependency:

```
<dependency>
    <groupId>dev.openbanking4.spring.security</groupId>
    <artifactId>spring-security-multi-auth-starter</artifactId>
    <version>{latest.version}</version>
</dependency>
```
Replacing `{latest.tag}`:

As we are in CI and every commit is a release, it's very hard to predict the version number in this readme.
In order to know the latest version, have a look at the tags list on this repo. The latest tag should be the version you should use

Another way is to go directly to the jcenter central repository, and look for this project: https://bintray.com/beta/#/qcastel-yapily/jose-database/jose-database?tab=overview
Jcenter will point you to the latest version to use.

## Injecting the keys to your micro-services
The library expect as input a set of keys. The recommendation would be to inject them from your kubernetes secrets and putting them in the classpath of the microservice.
Once accessible by the service, the library will load the keys and use them for encrypting/signing/decrypting/validating the fields of your database.

See the `setup the keys` section.

## Specifying the field you want to encrypt
For specifying a field that requires encryption:

```
    @Convert(converter = JoseDatabaseAttributeConverter.class)
    private String name;
```

In spring R2DBC, you will need to bundle the attribute into a  org.springframework.core.convert.converter.Converter. It's super easy don't worry ;)

# Configuration

The configuration is under the prefix `jose-database`.


The list of all the other options:

- `jose-database.keys-path`: (default is /) the path of the keys.
- `jose-database.actuator.enabled`: (default is false) enable the custom actuator endpoint
- `jose-database.actuator.show-details`: (default is false) provides the keys as public JWK if true. Otherwise only provides the kid
- `jose-database.tokenFormat`: (default and recommended is JWS_JWE) specify the format of the JWT you want for the fields
- `jose-database.jws-algorithm`: (Default is PS512) When signing the attribute, the default behaviour is to get the algorithm from the key. Indead, a JWK can contain the algorithm. Although, as this field is optional, this option offers you to setup a default algorithm. The recommendation would be to create keys with a JWK.alg always defined.
- `jose-database.jwe-algorithm`: (Default is RSA-OAEP-256) When encrypting the attribute, the default behaviour is to get the algorithm from the key. Indead, a JWK can contain the algorithm. Although, as this field is optional, this option offers you to setup a default algorithm. The recommendation would be to create keys with a JWK.alg always defined.
- `jose-database.encryption-method`: (Default is A256CBC-HS512) Encryption method to use by default.


## Setup the keys


The library is expecting to have the keys into a JWK for, categorised in 3 different type. Each type would be stored in a dedicated json file following the JWK set format.

You will need to put the:
- valid keys into a file `valid-keys.json`
- expired keys into a file `expired-keys.json`
- revoked keys into a file `revoked-keys.json`

At the start, you probably won't have expired and revoked keys. In that case, just set the keys attribute to an empty array (see example bellow).

### JWK set Format

Example of valid keys in JWK set format.

```
{
  "keys": [
    {
      "d": "ptCBdR_ILhKm11ftIA6-0w6OGBkYKFO0OVygk5vLHv7wPJpqE12Ssxq9cFxeoPRHNbXyd3ktaS0kMbppFonXW7RlKytSKjgkKY1V4EcqDlzs4Th-rfcdw-W51U4G1IzhxFhRa4D03CC7FDDzpxY2YKRbPPfnWueD6NaPU59nu8kaGEUCDbgFjw5BtDBD2gwPvnsr56J-TEdceIX6qsEv4-lW8qvwTFtpaJatJX0MSB2gTXTqPoXcMrtfVf_IB9jzzGVx7RLCknT9uY8QSJPz-8c3KylxBC90pADhLvubqmz6PCty8ppCarOnBPbqrqdeQFt9vntVaXG3UE18TvZ-oQ",
      "e": "AQAB",
      "use": "enc",
      "kid": "valid-encryption-key",
      "dp": "wRKa51YCLa6B_ZFlc0JZJ7H2Jlb_lnWNAbtMmB_eQsnIlToyBcxUujtfdjncIjWH3kEB27kJvmNbkw-GzJOnBZ7BchH92IEVjkWWhPHTN9NpYTzUhx3IuIZFoK4LH9pQq4pncyUXl2TtmkthzGGbh4OVel2g-rxDkKoexmEIiBk",
      "dq": "GSHr_5fpNkH7rtVxmoz6MIQggNkttHBbrSfrlLQixpdYGa6GQbx4MhJ_9cbb4yeTI9J_BSueojvmuF3aB2A7jDF30djZAnVAOhJIE5A6JCr8ZPCV_i5pj0eWOuz29gJUZBgNDqkjo2oA3ucUSffolU4pg-bwBssZPANCf9SO9Yc",
      "n": "p0gVhO2-Uyn0vf3W7smiOyNsVawYmun-6En-izNVOWDaFpa10wuwYBG9J5PyK33Tnm_LqDU6uhrcd5ujdymlgTWc_Sx69PR0LEj7UEgdmpLfqSeiE6_KtSBCqm0FpJa5eRYrhsQv1xiCocsONCLuTET052-tuE_nnwWGF4NyztGBK437epn4rGxSN3tLKV3dBPhhlnY46R7JW6-neDXd_llgzZoZLwSXIInkBZx9aaxnnWKgZPB9j85uU1Jsndtmsas5sqo7ZWT0luL3FBbAdxQmnu5ARVYh-cGxwrsyA08qXFcftCS0wVyQRXvCr5aHxAPmIxu_Pr5VuRzLgEp6vw",
      "p": "9AF2DMVLvdUeXaAinnm11PpHFFRyLp4UL_dNoELGasy9WGoTOSnKlpQJ8MCUohGTKl2hDVGOSUuECu9oP3Au9raK_ZfTiv79ABHf5Qcf2Dk7TB6JdzyMGAbSvZNh_zokO_xSeDC_hlf8ZvvChxLh2353ldFspIjvMf3vB76iIpk",
      "kty": "RSA",
      "q": "r4EitgxL__phE2zSeTzEcHJgGSQoajh3vDtEWOrKh0zOIyaKobij2Aexcrl6cwrIXVV7zcWs2p32kbt9UcIM8PF2HteEJBFr-THJHQEcPjk-Ybwoqh0uEDvxVrGE8z31UZlpxkk4nbWKPXt2qKKk2Bz9vNrfj0PlBohm3b2utxc",
      "qi": "llCfDYL0N5VoJYvCieMOW9UDEGNvNpfivY1orLSxzaY65RHySJeiaJq4Km5N9MTDOir8VR2wkPVzoSzYk_-8GMQg99Z7dvK7eeTovbkL9KhzQYXqQjF04rFYJVZo_Q-84wXmL7pc6x-67VDHeHhvCH2f5ydjqACBKXnWKIn11JM",
      "alg": "RSA-OAEP-256"
    },
    {
      "d": "ptCBdR_ILhKm11ftIA6-0w6OGBkYKFO0OVygk5vLHv7wPJpqE12Ssxq9cFxeoPRHNbXyd3ktaS0kMbppFonXW7RlKytSKjgkKY1V4EcqDlzs4Th-rfcdw-W51U4G1IzhxFhRa4D03CC7FDDzpxY2YKRbPPfnWueD6NaPU59nu8kaGEUCDbgFjw5BtDBD2gwPvnsr56J-TEdceIX6qsEv4-lW8qvwTFtpaJatJX0MSB2gTXTqPoXcMrtfVf_IB9jzzGVx7RLCknT9uY8QSJPz-8c3KylxBC90pADhLvubqmz6PCty8ppCarOnBPbqrqdeQFt9vntVaXG3UE18TvZ-oQ",
      "e": "AQAB",
      "use": "sig",
      "kid": "valid-signing-key",
      "dp": "wRKa51YCLa6B_ZFlc0JZJ7H2Jlb_lnWNAbtMmB_eQsnIlToyBcxUujtfdjncIjWH3kEB27kJvmNbkw-GzJOnBZ7BchH92IEVjkWWhPHTN9NpYTzUhx3IuIZFoK4LH9pQq4pncyUXl2TtmkthzGGbh4OVel2g-rxDkKoexmEIiBk",
      "dq": "GSHr_5fpNkH7rtVxmoz6MIQggNkttHBbrSfrlLQixpdYGa6GQbx4MhJ_9cbb4yeTI9J_BSueojvmuF3aB2A7jDF30djZAnVAOhJIE5A6JCr8ZPCV_i5pj0eWOuz29gJUZBgNDqkjo2oA3ucUSffolU4pg-bwBssZPANCf9SO9Yc",
      "n": "p0gVhO2-Uyn0vf3W7smiOyNsVawYmun-6En-izNVOWDaFpa10wuwYBG9J5PyK33Tnm_LqDU6uhrcd5ujdymlgTWc_Sx69PR0LEj7UEgdmpLfqSeiE6_KtSBCqm0FpJa5eRYrhsQv1xiCocsONCLuTET052-tuE_nnwWGF4NyztGBK437epn4rGxSN3tLKV3dBPhhlnY46R7JW6-neDXd_llgzZoZLwSXIInkBZx9aaxnnWKgZPB9j85uU1Jsndtmsas5sqo7ZWT0luL3FBbAdxQmnu5ARVYh-cGxwrsyA08qXFcftCS0wVyQRXvCr5aHxAPmIxu_Pr5VuRzLgEp6vw",
      "p": "9AF2DMVLvdUeXaAinnm11PpHFFRyLp4UL_dNoELGasy9WGoTOSnKlpQJ8MCUohGTKl2hDVGOSUuECu9oP3Au9raK_ZfTiv79ABHf5Qcf2Dk7TB6JdzyMGAbSvZNh_zokO_xSeDC_hlf8ZvvChxLh2353ldFspIjvMf3vB76iIpk",
      "kty": "RSA",
      "q": "r4EitgxL__phE2zSeTzEcHJgGSQoajh3vDtEWOrKh0zOIyaKobij2Aexcrl6cwrIXVV7zcWs2p32kbt9UcIM8PF2HteEJBFr-THJHQEcPjk-Ybwoqh0uEDvxVrGE8z31UZlpxkk4nbWKPXt2qKKk2Bz9vNrfj0PlBohm3b2utxc",
      "qi": "llCfDYL0N5VoJYvCieMOW9UDEGNvNpfivY1orLSxzaY65RHySJeiaJq4Km5N9MTDOir8VR2wkPVzoSzYk_-8GMQg99Z7dvK7eeTovbkL9KhzQYXqQjF04rFYJVZo_Q-84wXmL7pc6x-67VDHeHhvCH2f5ydjqACBKXnWKIn11JM",
      "alg": "RS256"
    }
  ]
}
``` 

If you don't have revoked keys yet for example, set:
```
{
  "keys": []
}
``` 

# FAQ

## Why do I need to have expired and revoked keys?

This is to allow key rotation. In order to avoid a downtime of your service during the key rotation, you will want to have to expired keys still available in order to decrypt fields that are not yet rotated.
This allows a smooth migration window when you rotate your keys.
The revoked keys is mainly for history but also audit. If for a reason, you got an audit and by chance, you got a snapshot of your database at the time, you can still decrypt the fields and verify the value at the time.
A general good practice is to not delete a key but more to mark it as revoked.

The micro-service is not using the revoked keys at all, only displaying them in the actuator endpoints so you can track where you are in your key rotation.

## Why would I want to encrypt a field using a JWT instead of using KMS?

Performance wise, you can avoid a network call and do the action locally. It's also for a cost reason, by avoiding having to pay for your intense KMS usage. 
This library offers you the possibilities to use custom keys, that you can encrypt with KMS for example and then into a git repo.

## Why JOSE?

It's a known standard and well approved in the industry, with libraries in multiple languages. This will facilitate the inter-operability between your different micro-services.
It also have the advantages to offer the JWK and JWT is a relatively readable manner. 
If you want to know more about JOSE, we recommend reading the following standards:
- JWE - https://tools.ietf.org/html/rfc7516
- JWS - https://tools.ietf.org/html/rfc7515
- JWK - https://tools.ietf.org/html/rfc7517


## Should I always use JWS_JWE ?

JWS_JWE is a format when you first encrypt the message and then sign it. Encrypting is quite heady in computation, therefore encryption the smallest payload possible is a good idea.
Then signing becomes handy if you want to always make sure that the field you read has not be corrupted: Encryption is done using the encryption public key, which is by definition public. 
The name implies you can share it publicly, although this would mean anyone could inject an encrypted field if you do. Therefore, a good practice is also to sign it on top.

You may say that you only use JWE and never share the public key. You will be right, although can you garanty that someone won't have the brillant idea to say: "It's a public key so lets make it public!"
Because it's quite error prone, we recommend to use JWS_JWE, a better robustness at the compromised of performance.