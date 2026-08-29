# ---- 构建阶段：Maven 打包 ----
FROM maven:3.8-openjdk-8 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests -B

# ---- 运行阶段：JRE8 运行 jar ----
FROM eclipse-temurin:8-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# Render 免费实例 512MB 内存，限制堆大小避免被 OOM Kill
ENV JAVA_OPTS="-Xms128m -Xmx350m"
# Render 通过 PORT 环境变量指定监听端口，Spring 端已配置 ${PORT:8088}
EXPOSE 8088
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
