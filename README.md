# yun-docker-backend 🐳

[![GitHub stars](https://img.shields.io/github/stars/Fuchenglai/yun-docker-backend?style=flat-square)](https://github.com/Fuchenglai/yun-docker-backend/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/Fuchenglai/yun-docker-backend?style=flat-square)](https://github.com/Fuchenglai/yun-docker-backend/network)
[![GitHub issues](https://img.shields.io/github/issues/Fuchenglai/yun-docker-backend?style=flat-square)](https://github.com/Fuchenglai/yun-docker-backend/issues)

## 📖 项目概述

yun-docker-backend 是一个基于微服务架构的云托管平台后端系统。该项目起源于华南农业大学虚拟云实验平台的开发经验，旨在为开发者提供一站式的容器化应用部署和管理解决方案。

通过本平台，开发者可以轻松地将项目打包为Docker镜像，实现一键部署、监控和管理，大幅简化了传统服务器配置和运维的复杂性。

## 🎯 核心功能

### 🐋 镜像管理
- **镜像拉取**: 支持从Docker Hub等registry拉取公共/私有镜像
- **镜像查看**: 可视化展示可用镜像列表和详情信息  
- **镜像删除**: 安全删除不再需要的镜像资源

### 📦 容器管理
- **容器创建**: 一键创建基于指定镜像的容器实例
- **生命周期管理**: 完整的启动、停止、重启、删除操作
- **实时监控**: 查看容器运行状态和资源使用情况
- **日志查看**: 实时获取容器运行日志输出

### 💰 积分管理
- **积分消费**: 容器创建和运行消耗用户积分
- **充值功能**: 集成支付宝沙箱支付系统
- **订单管理**: 完整的订单创建、查询、状态跟踪

### 🔧 高级特性
- **实时通信**: WebSocket实现容器监控数据实时推送
- **性能监控**: Prometheus + Grafana监控体系
- **分布式架构**: 微服务架构确保系统可扩展性

## 🏗️ 系统架构

### 微服务模块
- **yun-docker-master** (控制节点): 负责业务逻辑处理、用户管理、订单处理等核心功能
- **yun-docker-worker** (计算节点): 基于Docker技术实现容器操作和资源管理

### 技术架构
```
用户请求 → Master节点 (业务处理) → RPC调用 → Worker节点 (容器操作)
                         ↳ MySQL持久化        ↳ Docker Engine交互
```

### 通信机制
- **Dubbo RPC**: 微服务间高性能远程调用
- **WebSocket**: 实时数据推送和监控
- **RESTful API**: 对外提供标准接口服务

## 🛠️ 技术栈

### 后端框架
- **Spring Boot 2.x**: 微服务基础框架
- **Dubbo 3.x**: 高性能RPC框架
- **MyBatis-Plus**: 数据持久层框架

### 数据存储
- **MySQL**: 关系型数据库，存储业务数据
- **Redis**: 缓存和会话管理
- **Redisson**: 分布式锁和协调

### 容器技术
- **Docker Java API**: 容器操作和管理
- **Docker Engine**: 容器运行时环境

### 监控运维
- **Prometheus**: 指标收集和监控
- **Grafana**: 数据可视化和仪表盘

### 其他组件
- **WebSocket**: 实时通信协议
- **ZooKeeper**: 服务注册和发现
- **支付宝沙箱**: 支付系统集成
- **Swagger/Knife4j**: API文档生成

## 🌐 相关项目

- **前端项目**: [yun-docker-frontend](https://github.com/Fuchenglai/yun-docker-frontend) - 基于Vue3 + Ant Design的现代化管理界面

## 🚀 快速开始

详细的使用和部署指南请参考项目文档中的[快速入门](repowiki/zh/content/快速入门.md)章节。

## 📊 系统特色

1. **全栈开发**: 前后端均由个人独立完成，架构设计完整
2. **生产就绪**: 包含完整的用户系统、支付系统、监控体系
3. **技术前沿**: 采用主流微服务技术和云原生架构
4. **易于扩展**: 模块化设计支持功能快速迭代

## 🤝 贡献指南

欢迎提交Issue和Pull Request来帮助改进这个项目！

## 📄 许可证

本项目采用MIT许可证，详见LICENSE文件。

---
💡 *让容器化部署变得更加简单高效*
