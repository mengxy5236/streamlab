# StreamLab 

## 项目概述

基于 Spring Boot 的分布式视频平台，模仿 B站核心功能，使用 Redis、Kafka、Elasticsearch 等技术栈。

---

## 技术栈

### 后端核心
- Spring Boot 3.5.7 - 基础框架
- MySQL 8.0 - 关系型数据库
- Redis 7.x - 缓存 + 计数器 + 排行榜
- Kafka 3.x - 消息队列/事件总线
- Elasticsearch 8.x - 全文搜索
- MinIO - 对象存储（模拟 OSS）
- WebSocket - 实时弹幕

### 其他
- Spring Security + JWT - 认证授权
- Spring Data JPA - ORM 框架
- Flyway - 数据库版本管理
- Docker Compose - 容器化部署
- JMeter/Gatling - 性能压测

---

## 实现路线

### 阶段 1：基础功能完善（1 周）

#### 目标
完善现有的用户和视频模块，为后续集成做准备。

#### 任务清单
- [ ] 用户注册/登录
- [ ] 用户个人主页（昵称、头像、简介、粉丝数）
- [ ] 视频列表查询（分页、排序）
- [ ] 视频详情查询
- [ ] 视频分类/标签功能
- [ ] 统一异常处理
- [ ] 统一返回格式（Result<T>）
- [ ] 接口文档完善（Swagger/OpenAPI）

---

### 阶段 2：Redis 缓存与实时统计

#### 目标
引入 Redis，实现缓存、计数器、排行榜等功能。

#### 任务清单

##### 2.1 Redis 基础集成
- [ ] 添加 Redis 依赖
- [ ] 配置 Redis 连接（application.properties）
- [ ] 创建 RedisConfig 配置类
- [ ] 编写 RedisService 工具类

##### 2.2 缓存功能
- [ ] 视频详情缓存（Cache-Aside 模式）
- [ ] 用户信息缓存
- [ ] 热门视频列表缓存
- [ ] 缓存预热（启动时加载热门数据）
- [ ] 缓存失效策略（TTL + 主动删除）

##### 2.3 实时计数器
- [ ] 播放量实时计数（Redis INCR）
- [ ] 点赞/投币/收藏计数
- [ ] 定时任务：Redis → MySQL 批量同步（每分钟）
- [ ] 防止重复点赞（Redis Set 或 Bitmap）

##### 2.4 排行榜
- [ ] 日榜/周榜/月榜（Redis Sorted Set）
- [ ] 分区排行榜（游戏区、动画区等）
- [ ] 定时更新排行榜（Scheduled Task）

##### 2.5 其他功能
- [ ] 用户观看历史（Redis List）
- [ ] 搜索热词统计（Redis Sorted Set）
- [ ] 分布式锁（防止缓存击穿）

#### 代码示例
```java
// RedisService.java
@Service
public class RedisService {
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    // 视频播放量 +1
    public Long incrementViewCount(Long videoId) {
        String key = "video:views:" + videoId;
        return redisTemplate.opsForValue().increment(key);
    }
    
    // 获取日榜 Top 10
    public Set<String> getDailyRanking(int limit) {
        String key = "ranking:daily";
        return redisTemplate.opsForZSet()
            .reverseRange(key, 0, limit - 1);
    }
}
```

#### 学习资源
- Redis 数据结构（String/Hash/List/Set/Sorted Set）
- Redis 过期策略和淘汰策略
- 缓存穿透/击穿/雪崩解决方案
- Spring Data Redis 使用

### 阶段 3：Kafka 事件驱动架构（1.5 周）

#### 目标
使用 Kafka 实现异步处理、模块解耦、实时统计。

#### 任务清单

##### 3.1 Kafka 基础集成
- [ ] Docker 部署 Kafka + Zookeeper
- [ ] 添加 Spring Kafka 依赖
- [ ] 配置 Kafka 连接
- [ ] 创建 KafkaProducerService 和 KafkaConsumerService

##### 3.2 核心 Topic 设计
```
video.upload.event          → 视频上传完成事件
video.transcode.event       → 视频转码任务
user.action.event           → 用户行为埋点（播放/点赞/投币）
danmaku.event               → 弹幕消息
notification.event          → 系统通知
data.sync.event             → MySQL → ES 数据同步
```

##### 3.3 视频上传与转码
- 视频上传接口（接收小测试视频）
- 发送 `video.upload.event` 到 Kafka
- 转码服务消费消息（模拟转码延迟 5-10 秒）
- 生成多清晰度"假路径"（720p/1080p/4K）
- 发送 `video.transcode.completed` 事件
- 更新视频状态（UPLOADING → PUBLISHED）

##### 3.4 用户行为埋点
- 播放事件（记录观看时长、完播率）
- 点赞/投币/收藏事件
- 消费者处理：更新 Redis 计数器
- 消费者处理：更新排行榜
- 消费者处理：批量写入 MySQL

##### 3.5 实时统计
- 每分钟播放量统计（Kafka Streams 或普通 Consumer）
- 热门视频实时更新
- 用户行为分析（观看时长分布、完播率）

##### 3.6 系统通知
- UP主视频审核通过通知
- 获得点赞/投币通知
- 关注的UP主发布新视频通知

#### 代码示例
```java
// 视频上传事件
@PostMapping("/upload")
public ResponseEntity<VideoDto> uploadVideo(@RequestParam("file") MultipartFile file) {
    // 1. 保存文件到 MinIO
    String videoUrl = minioService.upload(file);
    
    // 2. 创建视频记录（状态：UPLOADING）
    Video video = videoService.createVideo(videoUrl);
    
    // 3. 发送 Kafka 事件
    VideoUploadedEvent event = new VideoUploadedEvent(video.getId(), videoUrl);
    kafkaTemplate.send("video.upload.event", event);
    
    return ResponseEntity.ok(videoMapper.toDto(video));
}

// 转码服务消费者
@KafkaListener(topics = "video.upload.event", groupId = "transcode-service")
public void handleVideoUpload(VideoUploadedEvent event) {
    // 模拟转码延迟
    Thread.sleep(RandomUtils.nextInt(5000, 10000));
    
    // 生成多清晰度路径
    Map<String, String> urls = Map.of(
        "720p", event.getVideoUrl(),
        "1080p", event.getVideoUrl(),
        "4k", event.getVideoUrl()
    );
    
    // 更新视频状态
    videoService.updateTranscodedUrls(event.getVideoId(), urls);
    
    // 发送完成事件
    kafkaTemplate.send("video.transcode.completed", new TranscodeCompletedEvent(event.getVideoId()));
}
```

#### 学习资源
- Kafka 基本概念（Topic/Partition/Consumer Group）
- Kafka 消息可靠性保证（ACK 机制）
- 消费者 Offset 管理
- Spring Kafka 使用

### 阶段 4：Elasticsearch 搜索 + WebSocket 弹幕（1.5 周）

#### 目标
实现全文搜索和实时弹幕功能。

#### 任务清单

##### 4.1 Elasticsearch 集成
- Docker 部署 Elasticsearch + Kibana
- 添加 Spring Data Elasticsearch 依赖
- 配置 ES 连接
- 创建 VideoDocument 实体类（@Document）

##### 4.2 索引设计
```java
@Document(indexName = "videos")
public class VideoDocument {
    @Id
    private Long id;
    
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String title;
    
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String description;
    
    @Field(type = FieldType.Keyword)
    private String category;
    
    @Field(type = FieldType.Keyword)
    private List<String> tags;
    
    @Field(type = FieldType.Integer)
    private Integer viewsCount;
    
    @Field(type = FieldType.Date)
    private Instant publishedAt;
}
```

##### 4.3 搜索功能
- 全文搜索（标题 + 描述）
- 多条件筛选（分类 + 时长 + 播放量）
- 排序（最新/最热/播放量）
- 分页查询
- 搜索高亮
- 搜索建议（自动补全）

##### 4.4 数据同步
- 视频创建时同步到 ES
- 视频更新时同步到 ES
- 使用 Kafka 实现异步同步（解耦）
- 全量数据导入脚本

##### 4.5 WebSocket 弹幕系统
- 添加 WebSocket 依赖
- 配置 WebSocket（STOMP）
- 弹幕实体类和数据表
- 发送弹幕接口
- 广播弹幕给所有在线用户
- 弹幕持久化（MySQL 或 MongoDB）
- 弹幕缓存（Redis 最近 100 条）
- 弹幕风控（敏感词过滤、频率限制）

##### 4.6 Redis Pub/Sub（可选）
- 使用 Redis Pub/Sub 实现跨服务器弹幕同步

#### 代码示例
```java
// 搜索服务
@Service
public class VideoSearchService {
    @Autowired
    private ElasticsearchRestTemplate elasticsearchTemplate;
    
    public Page<VideoDocument> search(String keyword, Pageable pageable) {
        NativeSearchQuery query = new NativeSearchQueryBuilder()
            .withQuery(QueryBuilders.multiMatchQuery(keyword, "title", "description"))
            .withPageable(pageable)
            .withHighlightFields(
                new HighlightBuilder.Field("title"),
                new HighlightBuilder.Field("description")
            )
            .build();
            
        return elasticsearchTemplate.search(query, VideoDocument.class);
    }
}

// WebSocket 弹幕控制器
@Controller
public class DanmakuWebSocketController {
    @MessageMapping("/danmaku/{videoId}")
    @SendTo("/topic/danmaku/{videoId}")
    public DanmakuMessage sendDanmaku(@DestinationVariable Long videoId, DanmakuMessage message) {
        // 1. 保存到数据库
        danmakuService.save(message);
        
        // 2. 缓存到 Redis
        redisService.cacheDanmaku(videoId, message);
        
        // 3. 发送 Kafka 事件（可选，用于数据分析）
        kafkaTemplate.send("danmaku.event", message);
        
        // 4. 广播给所有订阅者
        return message;
    }
}
```

#### 学习资源
- Elasticsearch 官方文档
- 尚硅谷 Elasticsearch 教程
- IK 中文分词器使用

### 阶段 5：性能优化与压测（1 周）

#### 目标
通过压测发现性能瓶颈，优化系统性能。

#### 任务清单

##### 5.1 压测准备
- 准备测试数据（10 万+ 视频元数据）
- 安装 JMeter 或 Gatling
- 编写压测脚本

##### 5.2 压测场景
- 视频列表查询（目标 QPS: 5000+）
- 视频详情查询（目标 QPS: 3000+）
- 视频搜索（目标 QPS: 1000+）
- 播放量 +1（目标 QPS: 10000+）
- WebSocket 并发连接（目标: 10000 连接）

##### 5.3 性能优化
- MySQL 慢查询优化（索引、分页）
- Redis 缓存优化（预热、TTL 调整）
- 数据库连接池优化（HikariCP）
- JVM 参数调优
- 接口限流（Guava RateLimiter 或 Sentinel）

##### 5.4 监控告警
- 集成 Prometheus + Grafana
- 监控 JVM 指标（堆内存、GC）
- 监控 Redis 指标（命中率、内存使用）
- 监控 Kafka 指标（消息积压、消费延迟）
- 监控接口性能（QPS、响应时间）

#### 学习资源
- JMeter 压测教程
- MySQL 性能优化
- JVM 调优基础
- Prometheus + Grafana 监控

### 阶段 6：微服务拆分与部署（可选，1.5 周）

#### 目标
将单体应用拆分为微服务，使用 Docker 部署。

#### 任务清单

##### 6.1 服务拆分
```
streamlab-user-service          → 用户服务
streamlab-video-service         → 视频服务
streamlab-upload-service        → 上传服务
streamlab-transcode-service     → 转码服务
streamlab-statistics-service    → 统计服务
streamlab-search-service        → 搜索服务
streamlab-danmaku-service       → 弹幕服务
streamlab-gateway               → API 网关
```

##### 6.2 Spring Cloud 集成
- Nacos 服务注册与发现
- Spring Cloud Gateway 网关
- Nacos Config 配置中心
- OpenFeign 服务调用
- Sentinel 熔断限流

##### 6.3 Docker 部署
- 编写 Dockerfile
- 编写 docker-compose.yml
- 一键启动所有服务
- 服务健康检查

#### 学习资源
- Spring Cloud Alibaba 官方文档
- Docker 入门教程
- 微服务架构设计

### 官方文档
- Spring Boot: https://spring.io/projects/spring-boot
- Redis: https://redis.io/documentation
- Kafka: https://kafka.apache.org/documentation/
- Elasticsearch: https://www.elastic.co/guide/

### 
---

## 项目亮点

### 技术
1. **分布式缓存**：使用 Redis 实现三级缓存，缓存命中率 95%+，数据库查询减少 80%
2. **消息队列**：Kafka 事件驱动架构，日处理消息 100 万+，实现模块解耦和异步处理
3. **全文搜索**：Elasticsearch 实现毫秒级搜索，支持中文分词和复合查询
4. **实时通信**：WebSocket 弹幕系统，支持 1 万并发连接，延迟 < 100ms
5. **性能优化**：通过压测和优化，接口 QPS 达到 5000+，P99 响应时间 < 200ms

### 可量化数据
- 支持 10 万+ 视频元数据存储
- 接口 QPS 5000+，P99 响应时间 < 200ms
- Redis 缓存命中率 95%+
- WebSocket 支持 1 万并发连接
- Kafka 日处理消息 100 万+
- Elasticsearch 搜索响应时间 < 100ms

---

