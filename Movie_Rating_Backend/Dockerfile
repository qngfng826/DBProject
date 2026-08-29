# ---- 构建阶段：Maven 打包 ----
FROM maven:3.8-openjdk-8 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests -B

# ---- 运行阶段：JRE 11 运行 jar（JDK 8 的 alpine 镜像 TLS 栈过老，连 TiDB 会握手失败） ----
# 字节码由上方 JDK 8 以 target 1.8 编译，JRE 11 兼容运行
FROM eclipse-temurin:11-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# Render 免费实例 512MB 内存，限制堆大小避免被 OOM Kill
ENV JAVA_OPTS="-Xms128m -Xmx350m"
# Render 通过 PORT 环境变量指定监听端口，Spring 端已配置 ${PORT:8088}
EXPOSE 8088
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
