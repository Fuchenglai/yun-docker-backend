# 使用官方OpenJDK 8基础镜像
FROM openjdk:8-jdk-alpine

# 构建镜像时所需要执行的命令，这里作者是在容器内部创建了一个工作目录
RUN mkdir "/home/app"

# 设置工作目录
WORKDIR /home/app

# 将构建的jar包复制到容器中
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

# 暴露应用运行的端口（根据您的Spring Boot应用配置）
EXPOSE 8088

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"]