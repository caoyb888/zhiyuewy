ARG MAVEN_IMAGE=maven:3.8-openjdk-8-slim
ARG RUNTIME_IMAGE=eclipse-temurin:8-jre

# 构建阶段
FROM ${MAVEN_IMAGE} AS builder

WORKDIR /build

# 先复制所有 pom.xml，利用 Docker 缓存层加速后续构建
COPY pom.xml .
COPY zhaoxinwy-admin/pom.xml zhaoxinwy-admin/
COPY zhaoxinwy-common/pom.xml zhaoxinwy-common/
COPY zhaoxinwy-framework/pom.xml zhaoxinwy-framework/
COPY zhaoxinwy-system/pom.xml zhaoxinwy-system/
COPY zhaoxinwy-quartz/pom.xml zhaoxinwy-quartz/
COPY zhaoxinwy-generator/pom.xml zhaoxinwy-generator/
COPY zhaoxinwy-pms/pom.xml zhaoxinwy-pms/
COPY zhaoxinwy-workflow/pom.xml zhaoxinwy-workflow/
COPY zhaoxinwy-tenant/pom.xml zhaoxinwy-tenant/

# 复制完整源码并构建
# 注意：不使用 dependency:go-offline，因为多模块内部依赖在仅复制 pom.xml 时无法解析
COPY . .
RUN mvn clean package -B -DskipTests -q

# 运行阶段
FROM ${RUNTIME_IMAGE}
WORKDIR /app

COPY --from=builder /build/zhaoxinwy-admin/target/zhaoxinwy-admin.jar app.jar
COPY docker/backend/application-docker.yml /app/config/application-docker.yml

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.config.additional-location=/app/config/"]
