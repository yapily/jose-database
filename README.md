| |Current Status|
|---|---|
|Build|[![Build Status](https://img.shields.io/endpoint.svg?url=https%3A%2F%2Factions-badge.atrox.dev%2Fyapily%2Fjose-database%2Fbadge%3Fref%3Dmaster&style=flat)](https://actions-badge.atrox.dev/yapily/jose-database/goto?ref=master)|
|Code coverage|[![codecov](https://codecov.io/gh/yapily/jose-database/branch/master/graph/badge.svg)](https://codecov.io/gh/yapily/jose-database)
|Bintray|[![Bintray](https://img.shields.io/bintray/v/qcastel-yapily/jose-database/jose-database.svg)](https://bintray.com/qcastel-yapily/jose-database/jose-database)|
|License|![license](https://img.shields.io/github/license/yapily/jose-database)|

# Jose Database

Encrypt/sign a field of your database using the JOSE standard.

Instead of storing data like the emails of a user in plain text, this library will help you encrypt data into a JWS(JWE), like:

```
eyJraWQiOiJ2YWxpZC1zaWduaW5nLWtleSIsImFsZyI6IlBTNTEyIn0.ZXlKcmFXUWlPaUoyWVd4cFpDMWxibU55ZVhCMGFXOXVMV3RsZVNJc0ltVnVZeUk2SWtFeU5UWkhRMDBpTENKaGJHY2lPaUpTVTBFdFQwRkZVQzB5TlRZaWZRLm45ZnMzVTl4YWYtLS1MQjBZVWlLdmVZWEZsMU9HcHBDTWxtREVxMnFkU1daR0RjZVk3NWpkX3dvdTdVOWo4Z0RNbUlFN3Z0Z2tXeWdxQlIzcFlOWUJyRzFVVEp2SVFRMWdPY1Y2TmtXbmN2ZlZyNWw2MEI2cHFQWi0wWHc3Z2w4UW9fVjZURWFyRjhMYi05RlZ2RGVpMDY4eHprXzZzSUlNcWFLallOT3RUNGV0S3BNR2MtQ3ZDRmlveWlEQjR3WmlJWTNSWUp1ZkJsbFV6UlY4emtaeWhWMVN5SkIya2p5aERtT0Z1UFJRampmc0o5aHhsRGVuM3dBeFctcy1mclFGb0t0RVFfSjBzVGswSVFVclJhY2Z0bWRFeEdDQjNka21XYk5jWnVac1M2bFM0YTRTMk4zOG9Zcnp0MnlaMWlEYm4tUUhjekhTanJKcm5rcDRnWUxndy5QZ2FhMVFlVzNLLTNMUVFLLnhUaUdWZnVPdFEuY3JhSU0ybWZDbHRTWlpBXzVpaGxxQQ.n3wVbDKcCGuDItQ6ct00L3Iq8-Q_PdSm956IhyJHIzKy_vgkODimEVmIO8hH_3VYaTTOa-Kgswg1W1k-b2UHIDLNZqrqDFjPXYjT70b0rSkGmWqtjvtrrfMPszsmdQkdFcqTV7GtZb6K-Kw4bZBao3_MZN1DYLNa8uDfiC-3ccZVCKHohY4awf6o6bvZHR6JzlSEgv0srjaZjsl5o5GsYoES2RJr9SqHp7-Vy9DRrdTiqtaNOyYNAp3MmK3KbaWvripyCgqd5U-5idWPgQ9KJNzPSAu1M2tEAaOXUH3lmmjOiOU_ldca0AnWtN0zBOarcKN-ArViRL-fPMv2t0BwcQ
```

The main advantage is the inter-operability of using JOSE; It can be used to both encrypt/sign data fields and to encrypt the encryption keys.


# Features:

- The ability to format data fields as JWS, JWE, JWS(JWE) or JWE(JWS)
- The format of the keys are also in the JWK format for better inter-operability
- A key rotation mechanism through the concept of valid keys, expired keys and revoked keys
- A custom actuator endpoint for your micro-service to identify the current state of the keys

# How to install

## Dependency

You will first need to download the libaray from jcenter by addding the following repository to your pom

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
    <groupId>com.yapily.jose.database</groupId>
    <artifactId>jose-database</artifactId>
    <version>{latest.version}</version>
</dependency>
```
Replacing `{latest.tag}`:

As we are using CI and every commit is a release, it's very hard to predict the version number in this readme.
In order to know the latest version, have a look at the tags list on this repo. The latest tag should be the version you should use

Alternatively, go directly to the jcenter central repository, and look for this project: https://bintray.com/beta/#/qcastel-yapily/jose-database/jose-database?tab=overview
Jcenter will point you to the latest version to use.

## Injecting the keys to your micro-services
The library expects a set of keys as input. The recommendation would be to inject them from your kubernetes secrets and configuring your microservice to expect them in the classpath.
Once accessible by the service, the library will load the keys and use them for encrypting/signing/decrypting/validating the fields of your database.

See the `setup the keys` section.

## Specifying the field you want to encrypt
For specifying a field that requires encryption:

```
    @Convert(converter = JoseDatabaseAttributeConverter.class)
    private String name;
```

In spring R2DBC, you will need to bundle the attribute into a `org.springframework.core.convert.converter.Converter`. It's super easy don't worry ;)

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


The library is expecting to have three JWKs to represent three classifications of keys. Each type would be stored in a dedicated json file following the JWK set format.

You will need to put the:
- valid keys into a file `valid-keys.json`
- expired keys into a file `expired-keys.json`
- revoked keys into a file `revoked-keys.json`

When running for the first time, you probably won't have any expired or any revoked keys. In that case, just set the keys attribute to an empty array (see example bellow).

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

This is to allow key rotation. In order to avoid downtime of your service during the key rotation, you will want to have to expired keys still available in order to decrypt fields that are not yet rotated.
This allows a smooth migration window when you rotate your keys.
The revoked keys is mainly for history but also proves useful in the event of an audit. If you have snapshots of your database for the period of the audit, you will be able to decrypt the fields and verify the value at the time using the now expired keys.
Generally, it is good practice to mark a key as revoked rather than to delete a key.

The micro-service is not using the revoked keys at all, only displaying them in the actuator endpoints so you can track where you are in your key rotation.

## Why would I want to encrypt a field using a JWT instead of using KMS?

Performance wise, you can avoid a network call and do the action locally. You also save money by avoiding the cost of intense KMS usage. 
This library offers you the possibility to use custom keys that you can encrypt with KMS, for example and then commit them to a git repo.

## Why JOSE?

It's a known standard and well established in the industry with libraries in multiple languages. This will facilitate the inter-operability between your different micro-services.
It also has the advantage of offering the JWK and JWT in a relatively readable form. 
If you want to know more about JOSE, we recommend reading the following standards:
- JWE - https://tools.ietf.org/html/rfc7516
- JWS - https://tools.ietf.org/html/rfc7515
- JWK - https://tools.ietf.org/html/rfc7517


## Should I always use JWS_JWE ?

JWS_JWE is a format where you first encrypt the message and then sign it. Encrypting is quite computationally expensive, therefore encrypting the smallest payload possible is a good idea.
The signing becomes handy if you want to have confidence that the field you read has not be corrupted: Encryption is done using the public key, which is by definition public. 
The name implies that you can share this key publicly, although this would mean anyone could inject an encrypted field if you do. However, by signing after encrypting, you will know if the data has been altered.

You may say that you should only use JWE_JWS and never share the public key. Theoretically, you are right, but you can't guarantee that someone will have the brillant of saying: "It's a public key so lets make it public!"
Because JWE_JWS is quite error prone, we recommend to use JWS_JWE which gives you robustness at the expense of performance.
