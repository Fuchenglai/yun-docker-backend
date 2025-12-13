# RESTful API

<cite>
**本文档引用的文件**   
- [UserController.java](file://yun-docker-master/src/main/java/com/lfc/yundocker/controller/UserController.java)
- [ContainerController.java](file://yun-docker-master/src/main/java/com/lfc/yundocker/controller/ContainerController.java)
- [ImageController.java](file://yun-docker-master/src/main/java/com/lfc/yundocker/controller/ImageController.java)
- [OrderController.java](file://yun-docker-master/src/main/java/com/lfc/yundocker/controller/OrderController.java)
- [BaseResponse.java](file://yun-docker-common/src/main/java/com/lfc/yundocker/common/model/dto/BaseResponse.java)
- [UserLoginRequest.java](file://yun-docker-common/src/main/java/com/lfc/yundocker/common/model/dto/user/UserLoginRequest.java)
- [CtrRunRequest.java](file://yun-docker-common/src/main/java/com/lfc/yundocker/common/model/dto/CtrRunRequest.java)
- [PageRequest.java](file://yun-docker-common/src/main/java/com/lfc/yundocker/common/model/dto/PageRequest.java)
- [ErrorCode.java](file://yun-docker-common/src/main/java/com/lfc/yundocker/common/model/enums/ErrorCode.java)
- [AuthCheck.java](file://yun-docker-master/src/main/java/com/lfc/yundocker/annotation/AuthCheck.java)
- [UserVO.java](file://yun-docker-master/src/main/java/com/lfc/yundocker/common/model/vo/UserVO.java)
- [ContainerVO.java](file://yun-docker-master/src/main/java/com/lfc/yundocker/common/model/vo/ContainerVO.java)
- [UserConstant.java](file://yun-docker-common/src/main/java/com/lfc/yundocker/common/constant/UserConstant.java)
- [ResultUtils.java](file://yun-docker-common/src/main/java/com/lfc/yundocker/common/util/ResultUtils.java)
</cite>

## 目录
1. [简介](#简介)
2. [API通用规范](#api通用规范)
3. [用户模块](#用户模块)
4. [容器模块](#容器模块)
5. [镜像模块](#镜像模块)
6. [订单模块](#订单模块)
7. [错误码说明](#错误码说明)

## 简介
本文档详细描述了云Docker后端系统的RESTful API接口，涵盖用户、容器、镜像、订单四大核心模块。所有API均基于Spring Boot框架实现，采用统一的响应格式和权限控制机制。系统通过JWT令牌进行身份验证，所有敏感操作都需要有效的登录状态。

## API通用规范

### 响应格式
所有API响应均封装在BaseResponse泛型类中，包含以下字段：
- **code**: 整数类型的状态码
- **data**: 泛型数据体，包含实际返回数据
- **message**: 描述性消息

```json
{
  "code": 200,
  "data": {},
  "message": "ok"
}
```

### 分页参数
所有分页接口均使用PageRequest对象作为请求体，包含以下参数：
- **current**: 当前页码，从1开始，默认为1
- **pageSize**: 每页大小，最大限制为20条，防止爬虫过度请求
- **sortField**: 排序字段
- **sortOrder**: 排序顺序，ASC为升序，DESC为降序

### 认证机制
- 需要在HTTP请求头中包含Authorization字段
- 值为登录成功后返回的token
- 未携带有效token的请求将返回401未登录错误

### 权限控制
- 使用@AuthCheck注解进行权限校验
- mustRole属性指定所需角色，如ADMIN_ROLE
- 普通用户角色为"user"，管理员角色为"admin"

**Section sources**
- [BaseResponse.java](file://yun-docker-common/src/main/java/com/lfc/yundocker/common/model/dto/BaseResponse.java#L1-L37)
- [PageRequest.java](file://yun-docker-common/src/main/java/com/lfc/yundocker/common/model/dto/PageRequest.java#L1-L34)
- [AuthCheck.java](file://yun-docker-master/src/main/java/com/lfc/yundocker/annotation/AuthCheck.java#L1-L29)
- [UserConstant.java](file://yun-docker-common/src/main/java/com/lfc/yundocker/common/constant/UserConstant.java#L1-L34)

## 用户模块

### 用户注册
- **HTTP方法**: POST
- **URL路径**: /user/register
- **权限要求**: 无需登录
- **请求体**: UserRegisterRequest
  - userAccount: 用户账号
  - userPassword: 用户密码
  - checkPassword: 确认密码
- **响应**: BaseResponse<Long>，返回用户ID
- **curl示例**:
```bash
curl -X POST http://localhost:8080/user/register \
  -H "Content-Type: application/json" \
  -d '{"userAccount":"testuser","userPassword":"123456","checkPassword":"123456"}'
```
- **Java HttpClient代码片段**:
```java
HttpClient client = HttpClient.newHttpClient();
HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create("http://localhost:8080/user/register"))
    .header("Content-Type", "application/json")
    .POST(HttpRequest.BodyPublishers.ofString(
        "{\"userAccount\":\"testuser\",\"userPassword\":\"123456\",\"checkPassword\":\"123456\"}"
    ))
    .build();
```

### 用户登录
- **HTTP方法**: POST
- **URL路径**: /user/login
- **权限要求**: 无需登录
- **请求体**: UserLoginRequest
  - userAccount: 用户账号
  - userPassword: 用户密码
- **响应**: BaseResponse<LoginUserVO>，包含用户信息和token
- **curl示例**:
```bash
curl -X POST http://localhost:8080/user/login \
  -H "Content-Type: application/json" \
  -d '{"userAccount":"testuser","userPassword":"123456"}'
```

### 获取当前登录用户
- **HTTP方法**: GET
- **URL路径**: /user/get/login
- **权限要求**: 需要登录态
- **请求头**: Authorization: Bearer {token}
- **响应**: BaseResponse<LoginUserVO>
- **curl示例**:
```bash
curl -X GET http://localhost:8080/user/get/login \
  -H "Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9..."
```

### 创建用户（管理员）
- **HTTP方法**: POST
- **URL路径**: /user/add
- **权限要求**: ADMIN_ROLE
- **请求体**: UserAddRequest
  - userName: 用户昵称
  - userAccount: 用户账号
  - userPassword: 用户密码
  - userRole: 用户角色
- **响应**: BaseResponse<Long>，返回用户ID
- **curl示例**:
```bash
curl -X POST http://localhost:8080/user/add \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {admin_token}" \
  -d '{"userName":"newuser","userAccount":"newuser","userPassword":"123456","userRole":"user"}'
```

### 分页获取用户列表（管理员）
- **HTTP方法**: POST
- **URL路径**: /user/list/page
- **权限要求**: ADMIN_ROLE
- **请求体**: UserQueryRequest（继承PageRequest）
  - current: 当前页码
  - pageSize: 每页大小（≤20）
  - userName: 用户名查询条件
  - userRole: 用户角色查询条件
- **响应**: BaseResponse<Page<User>>
- **安全限制**: pageSize最大为20，防止数据泄露

### 分页获取用户VO列表
- **HTTP方法**: POST
- **URL路径**: /user/list/page/vo
- **权限要求**: 无需特殊权限，但需登录
- **请求体**: UserQueryRequest
- **响应**: BaseResponse<Page<UserVO>>
- **VO与Entity转换**: UserVO是User实体的脱敏视图，隐藏了密码等敏感信息

**Section sources**
- [UserController.java](file://yun-docker-master/src/main/java/com/lfc/yundocker/controller/UserController.java#L1-L309)
- [UserLoginRequest.java](file://yun-docker-common/src/main/java/com/lfc/yundocker/common/model/dto/user/UserLoginRequest.java#L1-L21)
- [UserRegisterRequest.java](file://yun-docker-common/src/main/java/com/lfc/yundocker/common/model/dto/user/UserRegisterRequest.java#L1-L23)
- [UserVO.java](file://yun-docker-master/src/main/java/com/lfc/yundocker/common/model/vo/UserVO.java#L1-L43)

## 容器模块

### 获取我的容器列表
- **HTTP方法**: POST
- **URL路径**: /container/list/page
- **权限要求**: 需要登录态
- **请求体**: PageRequest
  - current: 当前页码
  - pageSize: 每页大小（≤20）
- **响应**: BaseResponse<Page<ContainerVO>>
- **业务逻辑**: 只返回当前登录用户创建的容器
- **curl示例**:
```bash
curl -X POST http://localhost:8080/container/list/page \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{"current":1,"pageSize":10}'
```

### 启动或停止容器
- **HTTP方法**: GET
- **URL路径**: /container/startOrStop
- **权限要求**: 需要登录态
- **请求参数**: containerId（容器ID）
- **响应**: BaseResponse<String>
- **业务逻辑**: 切换容器运行状态，需验证容器属于当前用户

### 重启容器
- **HTTP方法**: GET
- **URL路径**: /container/restart
- **权限要求**: 需要登录态
- **请求参数**: containerId
- **响应**: BaseResponse<String>

### 创建（运行）容器
- **HTTP方法**: POST
- **URL路径**: /container/run
- **权限要求**: 需要登录态
- **请求体**: CtrRunRequest
  - imageId: 镜像ID
  - hostPort: 宿主机端口
  - containerPort: 容器端口
  - name: 自定义容器名称
- **响应**: BaseResponse<?>
- **前置校验**: 
  1. 检查用户余额是否充足
  2. 验证镜像存在性
- **curl示例**:
```bash
curl -X POST http://localhost:8080/container/run \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{"imageId":"nginx:latest","hostPort":8080,"containerPort":80,"name":"my-nginx"}'
```

### 删除容器
- **HTTP方法**: GET
- **URL路径**: /container/remove
- **权限要求**: 需要登录态
- **请求参数**: containerId
- **响应**: BaseResponse<String>

### 下载容器日志
- **HTTP方法**: GET
- **URL路径**: /container/downloadLog
- **权限要求**: 需要登录态
- **请求参数**: containerId
- **响应**: 直接返回日志文件流
- **注意**: 此接口不返回BaseResponse，而是直接输出文件

**Section sources**
- [ContainerController.java](file://yun-docker-master/src/main/java/com/lfc/yundocker/controller/ContainerController.java#L1-L173)
- [CtrRunRequest.java](file://yun-docker-common/src/main/java/com/lfc/yundocker/common/model/dto/CtrRunRequest.java#L1-L35)
- [ContainerVO.java](file://yun-docker-master/src/main/java/com/lfc/yundocker/common/model/vo/ContainerVO.java#L1-L83)

## 镜像模块

### 拉取镜像
- **HTTP方法**: GET
- **URL路径**: /image/pull
- **权限要求**: 需要登录态
- **请求参数**: image（镜像名称，如nginx:latest）
- **响应**: BaseResponse<?>
- **默认标签**: 如果未指定标签，默认使用":latest"
- **curl示例**:
```bash
curl -X GET "http://localhost:8080/image/pull?image=nginx:latest" \
  -H "Authorization: Bearer {token}"
```

### 删除镜像
- **HTTP方法**: GET
- **URL路径**: /image/remove
- **权限要求**: 需要登录态
- **请求参数**: id（镜像记录ID）
- **响应**: BaseResponse<?>
- **权限校验**: 只能删除自己拉取的镜像

### 获取我的镜像列表
- **HTTP方法**: POST
- **URL路径**: /image/list/page
- **权限要求**: 需要登录态
- **请求体**: PageRequest
- **响应**: BaseResponse<Page<YunImage>>
- **数据范围**: 返回用户自己拉取的镜像 + 公共镜像（image_type=0）

**Section sources**
- [ImageController.java](file://yun-docker-master/src/main/java/com/lfc/yundocker/controller/ImageController.java#L1-L116)

## 订单模块

### 获取我的订单列表
- **HTTP方法**: POST
- **URL路径**: /order/list/page
- **权限要求**: 需要登录态
- **请求体**: PageRequest
- **响应**: BaseResponse<Page<YunOrderVO>>
- **数据范围**: 只返回当前用户的订单

### 创建订单
- **HTTP方法**: GET
- **URL路径**: /order/create
- **权限要求**: 需要登录态
- **请求参数**: credit（积分金额）
- **响应**: BaseResponse<String>，返回订单ID
- **业务流程**: 
  1. 生成唯一订单号
  2. 创建订单记录
  3. 返回订单ID用于支付

### 订单详情
- **HTTP方法**: GET
- **URL路径**: /order/detail
- **权限要求**: 需要登录态
- **请求参数**: orderId
- **响应**: BaseResponse<YunOrderVO>
- **安全校验**: 验证订单属于当前用户

### 取消订单
- **HTTP方法**: GET
- **URL路径**: /order/cancel
- **权限要求**: 需要登录态
- **请求参数**: orderId
- **响应**: BaseResponse<?>
- **状态校验**: 只有未支付订单才能取消

**Section sources**
- [OrderController.java](file://yun-docker-master/src/main/java/com/lfc/yundocker/controller/OrderController.java#L1-L121)

## 错误码说明

```mermaid
erDiagram
ERROR_CODE {
int code PK
string message
}
```

| 错误码 | HTTP状态 | 说明 |
|-------|---------|------|
| 200 | 200 | 成功 |
| 40000 | 400 | 请求参数错误 |
| 40100 | 401 | 未登录 |
| 40101 | 401 | 无权限 |
| 40400 | 404 | 请求数据不存在 |
| 40300 | 403 | 禁止访问 |
| 50000 | 500 | 系统内部异常 |
| 50001 | 500 | 操作失败 |
| 50002 | 500 | 计算节点Docker异常 |
| 50003 | 500 | 余额不足 |
| 50004 | 500 | 订单状态异常 |

**Section sources**
- [ErrorCode.java](file://yun-docker-common/src/main/java/com/lfc/yundocker/common/model/enums/ErrorCode.java#L1-L49)
- [ResultUtils.java](file://yun-docker-common/src/main/java/com/lfc/yundocker/common/util/ResultUtils.java#L1-L56)