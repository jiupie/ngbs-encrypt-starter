## 介绍

`@SeparateEncrypt`注解用于使用POST请求`Content-Type: application/json`数据加密和返回响应数据加密

- 使用过滤器实现

`@SignEncrypt`注解用于POST请求`Content-Type: application/json`验签

- 使用AOP实现

`@SignEncrypt` 和`@SeparateEncrypt`同时使用时

- 在请求时对原始JSON数据先把验签结果计算出来，在把数据加密，请求服务端

## 使用

测试项目地址：https://gitee.com/nangubeisan/ngbs-encrypt-test

1.下载源码，编译，`mvn install`到本地，在自己的项目中引入该starter坐标

 ```xml

<dependency>
    <groupId>com.wl</groupId>
    <artifactId>ngbs-encrypt-starter</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
 ```

2.开启全局注解

```java

@EnableEncrypt
@SpringBootApplication
public class SignApplication {
    public static void main(String[] args) {
        SpringApplication.run(SignApplication.class, args);
    }
}
```

3.配置密钥

```properties
#开启@SeparateEncrypt使用
encrypt.enable=true
#选择使用的加解密方式
encrypt.type=AES
#密钥
encrypt.secret=1x4s5x4d5sx4d5s3
#验签密钥
encrypt.signSecret=sadfcxzfrewqfsdfsafdxc


```

4.在controller上使用

```java

@RestController
public class SignController {

    @SignEncrypt(timeout = 60000L)
    @PostMapping("/sign")
    public String sign(@RequestBody People people) {
        if (people.getSex() == 1) {
            return people.getName() + "先生,你好！";
        } else {
            return people.getName() + "女士,你好！";
        }
    }

    /**
     *  验签和加密
     */
    @SignEncrypt(timeout = 60000L)
    @SeparateEncrypt
    @PostMapping("/encryptAndSign")
    public String encryptAndSign(@RequestBody People people) {
        if (people.getSex() == 1) {
            return people.getName() + "先生,你好！";
        } else {
            return people.getName() + "女士,你好！";
        }
    }

    @PostMapping("/noSignAndEncrypt")
    public String noSignAndEncrypt(@RequestBody People people) {
        if (people.getSex() == 1) {
            return people.getName() + "先生,你好！";
        } else {
            return people.getName() + "女士,你好！";
        }
    }
}
```

## 示例请求

###  验签

```http request
POST http://127.0.0.1:8064/sign
Content-Type: application/json
timestamp:1650355840100
sign:7e6315a7c25afc2aec89d9eaac182cf9

{
  "sex": 1,
  "name": "小明",
  "sa": "dsa"
}

```


