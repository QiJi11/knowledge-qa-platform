package com.design.chat.service;

import com.design.chat.client.OpenAiChatClient;
import com.design.common.ApiException;
import com.design.faq.entity.Faq;
import com.design.faq.repository.FaqRepository;
import com.design.chat.model.ChatMessage;
import com.design.faq.service.FaqBloomFilterService;
import okhttp3.Call;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class ChatService {
  public static final String FALLBACK_ANSWER = "系统正在思考中，请稍后再试 🤔";

  private static final Logger log = LoggerFactory.getLogger(ChatService.class);

  private final FaqRepository faqRepository;
  private final OpenAiChatClient openAiChatClient;
  private final SessionService sessionService;
  private final ChatCacheService chatCacheService;
  private final FaqBloomFilterService faqBloomFilterService;
  private final FaqEmbeddingService faqEmbeddingService;

  public ChatService(
    FaqRepository faqRepository,
    OpenAiChatClient openAiChatClient,
    SessionService sessionService,
    ChatCacheService chatCacheService,
    FaqBloomFilterService faqBloomFilterService,
    FaqEmbeddingService faqEmbeddingService
  ) {
    this.faqRepository = faqRepository;
    this.openAiChatClient = openAiChatClient;
    this.sessionService = sessionService;
    this.chatCacheService = chatCacheService;
    this.faqBloomFilterService = faqBloomFilterService;
    this.faqEmbeddingService = faqEmbeddingService;
  }

  public String ask(String message, String sessionId) {
    List<ChatMessage> historyMessages = sessionService.getRecentMessages(sessionId);
    boolean useKnowledgeBase = faqBloomFilterService.mightContain(message);
    String cachedAnswer = chatCacheService.getCachedAnswer(message);
    if (cachedAnswer != null && !cachedAnswer.isBlank()) {
      sessionService.appendConversation(sessionId, message, cachedAnswer);
      return cachedAnswer;
    }

    // 双路召回：优先向量语义检索，降级到关键词检索
    List<Faq> matchedFaqs = useKnowledgeBase ? faqEmbeddingService.search(message, 5) : List.of();
    String systemPrompt = buildSystemPrompt(sessionId, matchedFaqs);
    String answer;

    try {
      answer = openAiChatClient.ask(systemPrompt, historyMessages, message);
    } catch (OpenAiChatClient.AiClientTimeoutException e) {
      log.warn("AI 调用超时，sessionId={}", sessionId, e);
      answer = mockAnswer(message);
    } catch (OpenAiChatClient.AiClientUnavailableException e) {
      log.info("AI 不可用，使用模拟回答，sessionId={}, reason={}", sessionId, e.getMessage());
      answer = mockAnswer(message);
    }

    if (!FALLBACK_ANSWER.equals(answer)) {
      chatCacheService.cacheAnswer(message, answer);
    }
    sessionService.appendConversation(sessionId, message, answer);
    return answer;
  }

  /**
   * 模拟 AI 回答：基于关键词匹配返回预设的高质量回答。
   * 当 OpenAI API Key 未配置或调用失败时使用。
   */
  private String mockAnswer(String message) {
    String msg = message.toLowerCase().trim();

    // 日常问候
    if (msg.matches(".*(你好|hello|hi|嗨|在吗).*")) {
      return "你好！我是 AI 知识问答助手，可以回答编程技术、计算机基础、软件开发等方面的问题。直接问我吧！";
    }
    if (msg.matches(".*(谢谢|感谢|thanks).*")) {
      return "不客气！有其他问题随时问我。";
    }

    // 课程推荐（只有明确要求推荐课程时才提及）
    if (msg.contains("推荐") && (msg.contains("课程") || msg.contains("学"))) {
      return "可以在 [课程页面](/courses) 浏览所有课程。如果你告诉我你目前的技术栈和学习目标，我可以给你更有针对性的建议。";
    }

    // Spring Boot
    if (msg.contains("spring boot") || msg.contains("springboot") || msg.contains("spring")) {
      return "**Spring Boot** 是基于 Spring 框架的快速开发脚手架，核心设计思想是**约定优于配置**。\n\n"
        + "**核心机制：**\n"
        + "- **自动配置（Auto-Configuration）**：通过 `@EnableAutoConfiguration` 扫描 classpath 下的 `META-INF/spring.factories`，自动注册 Bean\n"
        + "- **条件装配**：`@ConditionalOnClass`、`@ConditionalOnMissingBean` 等条件注解控制 Bean 是否生效\n"
        + "- **外部化配置**：`application.yml` 支持多环境 Profile、配置优先级覆盖\n\n"
        + "**启动流程简述：**\n"
        + "1. `SpringApplication.run()` 创建 ApplicationContext\n"
        + "2. 加载所有 `@Configuration` 类和自动配置类\n"
        + "3. 执行 Bean 的依赖注入和初始化\n"
        + "4. 启动内嵌 Tomcat/Jetty/Undertow 服务器\n\n"
        + "如果你想了解某个具体功能（如 AOP、事务、拦截器等），可以继续问我。";
    }

    // Vue
    if (msg.contains("vue")) {
      return "**Vue.js** 是一个渐进式前端框架，核心特点是**响应式数据绑定**和**组件化开发**。\n\n"
        + "**Vue 3 核心概念：**\n"
        + "- **响应式原理**：基于 `Proxy` 代替 Vue 2 的 `Object.defineProperty`，支持对象新增/删除属性的监听\n"
        + "- **Composition API**：`setup()` + `ref()`/`reactive()` 组织逻辑，解决 Options API 在复杂组件中逻辑分散的问题\n"
        + "- **虚拟 DOM**：diff 算法对比新旧 VNode 树，最小化真实 DOM 操作\n\n"
        + "**常用生态：**\n"
        + "- 路由：Vue Router\n"
        + "- 状态管理：Pinia（替代 Vuex）\n"
        + "- 构建工具：Vite\n"
        + "- UI 库：Element Plus、Ant Design Vue\n\n"
        + "有具体问题可以继续问我。";
    }

    // 前端
    if (msg.contains("前端") || msg.contains("css") || msg.contains("html") || msg.contains("javascript") || msg.contains("js")) {
      return "**前端开发**涉及用户界面的构建和交互实现。\n\n"
        + "**核心三件套：**\n"
        + "- **HTML**：页面结构，语义化标签（header/nav/main/footer）\n"
        + "- **CSS**：样式布局，Flexbox/Grid 是现代布局的基础\n"
        + "- **JavaScript**：交互逻辑，ES6+ 语法（箭头函数、解构、async/await）\n\n"
        + "**主流框架对比：**\n"
        + "| 框架 | 特点 | 适用场景 |\n"
        + "|------|------|----------|\n"
        + "| Vue | 渐进式、易上手 | 中小型项目、快速迭代 |\n"
        + "| React | 组件化、生态丰富 | 大型 SPA、跨端开发 |\n"
        + "| Angular | 全家桶、强类型 | 企业级后台管理系统 |\n\n"
        + "你想了解哪个方向可以具体问我。";
    }

    // Python
    if (msg.contains("python")) {
      return "**Python** 以简洁的语法和丰富的生态著称，是目前最流行的编程语言之一。\n\n"
        + "**主要应用领域：**\n"
        + "- **数据科学**：Pandas（数据清洗）、NumPy（科学计算）、Matplotlib/Seaborn（可视化）\n"
        + "- **机器学习**：Scikit-learn（传统 ML）、PyTorch/TensorFlow（深度学习）\n"
        + "- **Web 后端**：Django（全功能）、Flask/FastAPI（轻量级）\n"
        + "- **自动化**：脚本编写、爬虫（Scrapy/BeautifulSoup）、运维自动化\n\n"
        + "**Python 特性：**\n"
        + "- 动态类型 + 强类型\n"
        + "- GIL（全局解释器锁）限制多线程 CPU 密集型任务\n"
        + "- 列表推导式、装饰器、上下文管理器等 Pythonic 语法\n\n"
        + "想了解哪个方向可以继续问我。";
    }

    // Java 具体技术概念
    if (msg.contains("hashmap") || msg.contains("hashtable") || msg.contains("concurrenthashmap")) {
      return "**HashMap** 是 Java 中最常用的键值对集合，基于哈希表实现。\n\n"
        + "**底层结构（JDK 8+）：** 数组 + 链表 + 红黑树\n"
        + "- 链表长度 ≥ 8 且数组容量 ≥ 64 时，链表转红黑树\n"
        + "- 红黑树节点 ≤ 6 时退化回链表\n\n"
        + "**核心参数：**\n"
        + "- 初始容量：16（必须是 2 的幂）\n"
        + "- 负载因子：0.75（时间与空间的折中）\n"
        + "- 扩容：容量翻倍 + rehash\n\n"
        + "**put 流程：**\n"
        + "1. 计算 key 的 hash → `(h = key.hashCode()) ^ (h >>> 16)` 高位参与运算减少碰撞\n"
        + "2. `(n - 1) & hash` 定位数组下标\n"
        + "3. 空位直接放入；有碰撞则用 equals 比较，相同覆盖，不同链表/红黑树追加\n\n"
        + "**线程安全替代方案：**\n"
        + "- `ConcurrentHashMap`：分段锁（JDK 7）→ CAS + synchronized（JDK 8），推荐使用\n"
        + "- `Collections.synchronizedMap()`：全表锁，性能差\n"
        + "- `Hashtable`：过时，不建议使用";
    }
    if (msg.contains("arraylist") || msg.contains("linkedlist") || msg.contains("集合")) {
      return "**Java 集合框架（JCF）**是 Java 的核心数据结构库。\n\n"
        + "**List 对比：**\n"
        + "| 特性 | ArrayList | LinkedList |\n"
        + "|------|-----------|------------|\n"
        + "| 底层 | 动态数组（Object[]） | 双向链表 |\n"
        + "| 随机访问 | O(1) | O(n) |\n"
        + "| 尾部追加 | 均摊 O(1) | O(1) |\n"
        + "| 中间插入 | O(n)，需移动元素 | O(1)，但定位 O(n) |\n"
        + "| 内存 | 紧凑连续，CPU 缓存友好 | 每个节点额外 16 字节指针 |\n\n"
        + "**实际选择：** 绝大多数场景用 ArrayList（缓存局部性好），LinkedList 仅在频繁头部插删时有优势。\n\n"
        + "**Map 对比：** HashMap（O(1)查找）、TreeMap（有序，O(log n)）、LinkedHashMap（维护插入顺序）\n\n"
        + "有具体集合的问题可以继续问我。";
    }
    if (msg.contains("多线程") || msg.contains("并发") || msg.contains("线程池") || msg.contains("synchronized")) {
      return "**Java 并发编程**核心知识点：\n\n"
        + "**线程池（ThreadPoolExecutor）参数：**\n"
        + "- `corePoolSize` — 核心线程数，即使空闲也不回收\n"
        + "- `maximumPoolSize` — 最大线程数\n"
        + "- `workQueue` — 任务队列：`LinkedBlockingQueue`（无界）、`ArrayBlockingQueue`（有界）\n"
        + "- `rejectedHandler` — 拒绝策略：AbortPolicy（抛异常）、CallerRunsPolicy（调用者执行）\n\n"
        + "**锁的演进：**\n"
        + "- `synchronized`：偏向锁 → 轻量级锁（CAS）→ 重量级锁（Monitor）\n"
        + "- `ReentrantLock`：可中断、公平锁、条件变量\n"
        + "- `volatile`：保证可见性和禁止指令重排序，不保证原子性\n\n"
        + "**常见问题：**\n"
        + "- 死锁四条件：互斥、占有且等待、不可剥夺、循环等待\n"
        + "- CAS 的 ABA 问题 → `AtomicStampedReference` 解决";
    }
    if (msg.contains("设计模式") || msg.contains("单例") || msg.contains("工厂模式") || msg.contains("策略模式") || msg.contains("代理模式")) {
      return "**设计模式**是解决特定问题的经验总结，GoF 23 种模式分三类：\n\n"
        + "**创建型（5 种）：**\n"
        + "- 单例：双重检查锁 + volatile、枚举实现（推荐）\n"
        + "- 工厂方法 / 抽象工厂：解耦对象创建\n"
        + "- 建造者：链式构建复杂对象\n\n"
        + "**结构型（7 种）：**\n"
        + "- 代理：JDK 动态代理（接口）vs CGLIB（继承），Spring AOP 底层实现\n"
        + "- 适配器：将不兼容接口转换为目标接口\n"
        + "- 装饰器：动态增加功能（Java I/O 流就是典型例子）\n\n"
        + "**行为型（11 种）：**\n"
        + "- 策略：封装算法族，运行时替换\n"
        + "- 观察者：一对多通知机制\n"
        + "- 模板方法：定义骨架，子类实现细节\n\n"
        + "理解设计模式的关键是**识别问题场景**，而不是死记 UML 图。";
    }

    // JVM 专项
    if (msg.contains("jvm") || msg.contains("垃圾回收") || msg.contains("gc") || msg.contains("类加载") || msg.contains("双亲委派") || msg.contains("元空间") || msg.contains("metaspace")) {
      return "**JVM 核心知识：**\n\n"
        + "**内存结构：**\n"
        + "- **堆（Heap）**：对象实例，GC 主区域，分新生代（Eden + 2×Survivor）和老年代\n"
        + "- **虚拟机栈**：每线程私有，存栈帧（局部变量 + 操作数栈 + 方法出口）\n"
        + "- **方法区**：类元信息，JDK 8 后为元空间（本地内存，无 OOM 上限）\n"
        + "- **程序计数器**：线程私有，当前字节码行号\n\n"
        + "**GC 回收器演进：** Serial → Parallel → CMS（并发标记清除，有碎片）→ G1（JDK 9 默认，分 Region 可预测停顿）→ ZGC（停顿 <10ms）\n\n"
        + "**类加载双亲委派：** Bootstrap → Ext → App → 自定义，先委托父加载器，找不到才自己加载。防止核心类被篡改。\n\n"
        + "想了解 GC 调优参数、OOM 排查或类加载细节可以继续问我。";
    }

    // Java / JVM 通用
    if (msg.contains("java") || msg.contains("jvm")) {
      return "**Java** 知识体系：\n\n"
        + "**语言特性：** 面向对象（封装/继承/多态）、强类型、自动内存管理（GC）\n\n"
        + "**集合框架：** ArrayList（动态数组）、HashMap（哈希表，JDK8红黑树优化）、ConcurrentHashMap（线程安全）\n\n"
        + "**并发：** synchronized（Monitor锁）、volatile（可见性+禁重排）、ReentrantLock（可中断/公平锁）、线程池 ThreadPoolExecutor\n\n"
        + "**JVM：** 内存分区（堆/栈/方法区）、GC 算法（标记清除/整理/复制）、类加载双亲委派\n\n"
        + "**进阶方向：** JVM 调优、Spring 生态、分布式架构、微服务\n\n"
        + "想深入了解哪个方向可以直接问我。";
    }

    // 数据库
    if (msg.contains("数据库") || msg.contains("mysql") || msg.contains("redis") || msg.contains("sql") || msg.contains("索引")) {
      return "**数据库**核心知识：\n\n"
        + "**MySQL 重点：**\n"
        + "- **索引**：B+ 树结构，最左前缀原则，覆盖索引避免回表\n"
        + "- **事务 ACID**：原子性（undo log）、持久性（redo log）、隔离性（MVCC + 锁）\n"
        + "- **隔离级别**：读未提交 → 读已提交 → 可重复读（MySQL 默认）→ 串行化\n"
        + "- **锁**：共享锁/排他锁、间隙锁、Next-Key Lock\n\n"
        + "**Redis 重点：**\n"
        + "- 数据结构：String、Hash、List、Set、Sorted Set\n"
        + "- 持久化：RDB（快照） + AOF（追加日志）\n"
        + "- 缓存问题：穿透（布隆过滤器）、击穿（互斥锁）、雪崩（随机过期时间）\n\n"
        + "有具体的数据库问题可以继续问我。";
    }

    // 操作系统
    if (msg.contains("进程") || msg.contains("线程") || msg.contains("协程") || msg.contains("死锁") || msg.contains("虚拟内存") || msg.contains("页表") || msg.contains("tlb") || msg.contains("操作系统") || msg.contains("cpu调度") || msg.contains("时间片")) {
      return "**操作系统核心知识：**\n\n"
        + "**进程 vs 线程：** 进程是资源分配单位（独立地址空间），线程是 CPU 调度单位（共享堆）。进程间通信（IPC）需管道/消息队列/共享内存；线程通信直接读写共享内存（需同步）。\n\n"
        + "**死锁四条件：** 互斥 + 占有且等待 + 不可剥夺 + 循环等待，破坏任一即可解除。Java 用 `jstack <pid>` 检测死锁。\n\n"
        + "**虚拟内存：** 每个进程拥有独立虚拟地址空间，由 MMU + 页表映射到物理内存。TLB 是页表的硬件缓存（命中率 >99%）。缺页时触发 Page Fault，OS 从 swap 加载数据。\n\n"
        + "**CPU 调度：** 时间片轮转（RR）、优先级调度、多级反馈队列。Linux CFS 基于虚拟运行时间实现公平调度。\n\n"
        + "想了解某个具体知识点可以继续问我。";
    }

    // 消息队列
    if (msg.contains("kafka") || msg.contains("rocketmq") || msg.contains("rabbitmq") || msg.contains("消息队列") || msg.contains("mq") || msg.contains("异步解耦") || msg.contains("削峰")) {
      return "**消息队列核心知识：**\n\n"
        + "**三大作用：异步 + 削峰 + 解耦**\n"
        + "- 异步：耗时操作（发邮件/短信）放 MQ，接口快速返回\n"
        + "- 削峰：流量高峰请求堆积 MQ，下游按能力消费\n"
        + "- 解耦：生产者/消费者只通过消息通信，互不依赖\n\n"
        + "**主流 MQ 对比：**\n"
        + "| MQ | 吞吐 | 特点 |\n"
        + "|----|------|------|\n"
        + "| Kafka | 百万级/s | 顺序写磁盘 + 零拷贝，适合日志/流处理 |\n"
        + "| RocketMQ | 十万级/s | 事务消息、顺序消息，金融级可靠 |\n"
        + "| RabbitMQ | 万级/s | AMQP 协议，路由灵活，运维友好 |\n\n"
        + "**可靠性保障：** 生产者 ACK + MQ 持久化 + 消费者手动确认。重复消费用幂等处理（唯一键/Redis setnx）。\n\n"
        + "有具体 MQ 问题可以继续问我。";
    }

    // Spring Cloud / 微服务
    if (msg.contains("spring cloud") || msg.contains("springcloud") || msg.contains("微服务") || msg.contains("nacos") || msg.contains("feign") || msg.contains("openfeign") || msg.contains("熔断") || msg.contains("降级") || msg.contains("限流") || msg.contains("网关") || msg.contains("sentinel") || msg.contains("eureka")) {
      return "**Spring Cloud / 微服务核心知识：**\n\n"
        + "**核心组件：**\n"
        + "| 组件 | 功能 | 常见实现 |\n"
        + "|------|------|----------|\n"
        + "| 注册中心 | 服务发现 | Nacos、Eureka |\n"
        + "| 配置中心 | 统一配置热刷新 | Nacos Config、Apollo |\n"
        + "| 网关 | 路由/鉴权/限流 | Spring Cloud Gateway |\n"
        + "| 远程调用 | HTTP 服务调用 | OpenFeign |\n"
        + "| 熔断限流 | 防止雪崩 | Sentinel（阿里）、Resilience4j |\n\n"
        + "**熔断 vs 降级 vs 限流：**\n"
        + "- 熔断：失败率达阈值断开调用（防雪崩），半开状态试探恢复\n"
        + "- 降级：资源不足时返回兜底数据，保障核心功能\n"
        + "- 限流：令牌桶/漏桶/滑动窗口控制请求速率\n\n"
        + "想深入了解哪个组件可以继续问我。";
    }

    // MyBatis
    if (msg.contains("mybatis") || msg.contains("mapper") || msg.contains("mybatis plus") || msg.contains("orm")) {
      return "**MyBatis 核心知识：**\n\n"
        + "**MyBatis vs JPA/Hibernate：**\n"
        + "- MyBatis：手写 SQL，灵活可控，适合复杂查询和 DBA 协作\n"
        + "- Hibernate/JPA：自动生成 SQL，开发快，但复杂查询优化难\n\n"
        + "**MyBatis Plus（国内主流）：** 在 MyBatis 基础上内置单表 CRUD、LambdaQueryWrapper 条件构造器，大幅减少重复代码。\n\n"
        + "**常见面试点：**\n"
        + "- `#{}` vs `${}`：前者预编译防 SQL 注入，后者直接字符串替换\n"
        + "- 一级缓存（SqlSession 级）、二级缓存（Mapper 级，默认关闭）\n"
        + "- 动态 SQL：`<if>`、`<foreach>`、`<choose>` 标签\n"
        + "- 分页插件 PageHelper：拦截 SQL 自动追加 LIMIT\n\n"
        + "有具体问题可以继续问我。";
    }

    // 算法
    if (msg.contains("排序") || msg.contains("算法") || msg.contains("动态规划") || msg.contains("dp") || msg.contains("二叉树") || msg.contains("红黑树") || msg.contains("bfs") || msg.contains("dfs") || msg.contains("贪心") || msg.contains("递归") || msg.contains("时间复杂度")) {
      return "**数据结构与算法：**\n\n"
        + "**常见排序时间复杂度：**\n"
        + "| 算法 | 平均 | 最坏 | 稳定性 |\n"
        + "|------|------|------|--------|\n"
        + "| 快速排序 | O(n log n) | O(n²) | 不稳定 |\n"
        + "| 归并排序 | O(n log n) | O(n log n) | 稳定 |\n"
        + "| 堆排序 | O(n log n) | O(n log n) | 不稳定 |\n"
        + "| 冒泡/插入 | O(n²) | O(n²) | 稳定 |\n\n"
        + "**动态规划（DP）四步：** 定义状态 → 写转移方程 → 确定初始值 → 自底向上计算\n\n"
        + "**树：** BST（二叉搜索树）→ AVL（严格平衡）→ 红黑树（近似平衡，旋转少，Java HashMap/TreeMap 用）→ B+树（MySQL 索引，多路，叶子链表）\n\n"
        + "**BFS vs DFS：** BFS（队列，层序，最短路径）；DFS（栈/递归，拓扑排序，连通性）\n\n"
        + "有具体题目或算法想深入了解可以继续问我。";
    }

    // Spring 事务 / Bean 生命周期
    if (msg.contains("事务传播") || msg.contains("@transactional") || msg.contains("bean生命周期") || msg.contains("bean 生命周期") || msg.contains("循环依赖") || msg.contains("三级缓存")) {
      return "**Spring 进阶知识：**\n\n"
        + "**@Transactional 失效场景：** 同类内自调用（AOP 代理不生效）、非 public 方法、异常被 catch 吞掉、非 RuntimeException 未配 rollbackFor\n\n"
        + "**事务传播行为（重要）：**\n"
        + "- REQUIRED（默认）：有事务加入，没有新建\n"
        + "- REQUIRES_NEW：无论如何新建，挂起外层\n"
        + "- NESTED：嵌套（Savepoint 回滚点）\n\n"
        + "**Bean 生命周期：** 实例化 → 属性注入 → Aware 回调 → BeanPostProcessor 前置 → 初始化（@PostConstruct）→ BeanPostProcessor 后置（AOP 代理）→ 使用 → 销毁\n\n"
        + "**循环依赖：** 三级缓存（singletonObjects/earlySingletonObjects/singletonFactories）解决 Setter/Field 注入循环依赖；构造器注入无法解决。";
    }

    // Docker / K8s
    if (msg.contains("docker") || msg.contains("k8s") || msg.contains("kubernetes") || msg.contains("容器")) {
      return "**Docker** 是容器化技术的事实标准。\n\n"
        + "**核心概念：**\n"
        + "- **镜像（Image）**：只读模板，分层构建（UnionFS）\n"
        + "- **容器（Container）**：镜像的运行实例，轻量级隔离\n"
        + "- **Dockerfile**：定义镜像构建步骤\n\n"
        + "**常用命令：**\n"
        + "```\n"
        + "docker build -t app:v1 .    # 构建镜像\n"
        + "docker run -d -p 8080:80 app:v1  # 运行容器\n"
        + "docker compose up -d       # 编排多个服务\n"
        + "```\n\n"
        + "**Kubernetes** 核心对象：Pod（最小调度单元）、Deployment（声明式部署）、Service（服务发现）、Ingress（外部访问）\n\n"
        + "有具体问题可以继续问。";
    }

    // AI / 大模型
    if (msg.contains("ai") || msg.contains("人工智能") || msg.contains("大模型") || msg.contains("gpt") || msg.contains("深度学习") || msg.contains("机器学习") || msg.contains("transformer")) {
      return "**人工智能**核心概念：\n\n"
        + "**深度学习基础架构：**\n"
        + "- **CNN**（卷积神经网络）：图像识别，核心操作是卷积 + 池化\n"
        + "- **RNN/LSTM**：序列数据处理（已逐渐被 Transformer 取代）\n"
        + "- **Transformer**：自注意力机制（Self-Attention），GPT/BERT 的基础架构\n\n"
        + "**大语言模型（LLM）关键技术：**\n"
        + "- 预训练 + 微调（Fine-tuning）\n"
        + "- RLHF（基于人类反馈的强化学习）\n"
        + "- RAG（检索增强生成）\n"
        + "- Prompt Engineering（提示词工程）\n\n"
        + "想了解某个具体方向可以继续问我。";
    }

    // 网络 / HTTP / TCP
    if (msg.contains("http") || msg.contains("tcp") || msg.contains("网络") || msg.contains("三次握手") || msg.contains("四次挥手")) {
      return "**计算机网络**核心知识：\n\n"
        + "**TCP 三次握手：** SYN → SYN+ACK → ACK（建立可靠连接）\n"
        + "**TCP 四次挥手：** FIN → ACK → FIN → ACK（TIME_WAIT 等待 2MSL）\n\n"
        + "**HTTP 版本对比：**\n"
        + "| 版本 | 特点 |\n"
        + "|------|------|\n"
        + "| HTTP/1.1 | 持久连接、管线化（实际很少用）|\n"
        + "| HTTP/2 | 多路复用、头部压缩、服务器推送 |\n"
        + "| HTTP/3 | 基于 QUIC（UDP），解决队头阻塞 |\n\n"
        + "**HTTPS = HTTP + TLS**，通过非对称加密交换对称密钥，然后用对称加密通信。\n\n"
        + "有具体网络问题可以继续问我。";
    }

    // Git
    if (msg.contains("git") || msg.contains("版本控制")) {
      return "**Git** 常用操作：\n\n"
        + "```\n"
        + "git branch feature/xxx    # 创建分支\n"
        + "git checkout -b fix/xxx   # 创建并切换分支\n"
        + "git merge --no-ff feature # 合并（保留分支记录）\n"
        + "git rebase main           # 变基（线性历史）\n"
        + "git stash / git stash pop # 暂存/恢复工作区\n"
        + "git reset --soft HEAD~1   # 撤销提交但保留修改\n"
        + "```\n\n"
        + "**分支策略：** main（生产）→ develop（开发）→ feature/（功能）→ hotfix/（紧急修复）\n\n"
        + "有具体的 Git 操作问题可以问我。";
    }

    // 平台使用
    if (msg.contains("怎么买") || msg.contains("怎么购买") || msg.contains("如何购买") || msg.contains("购买")) {
      return "购买课程步骤：\n\n"
        + "1. 登录账号\n"
        + "2. 在课程列表找到课程，点击进入详情\n"
        + "3. 点「立即购买」，完成支付\n"
        + "4. 购买的课程会出现在「我的课程」和「学习计划」中";
    }

    if (msg.contains("平台") || msg.contains("功能") || msg.contains("怎么用")) {
      return "本平台的主要功能：\n\n"
        + "- **课程中心**：浏览和购买课程\n"
        + "- **学习计划**：管理学习进度\n"
        + "- **学习统计**：查看学习数据\n"
        + "- **AI 问答**：技术问题随时问我\n\n"
        + "有具体使用问题可以继续问我。";
    }

    // 兜底回答 — 知识助手风格
    return "关于「" + message + "」，我目前的 mock 知识库暂未覆盖此话题的详细内容。\n\n"
      + "你可以换个关键词再问，或者问以下方向的问题：\n"
      + "- **Java 基础**：集合、并发、泛型、注解\n"
      + "- **JVM**：内存结构、GC、类加载、双亲委派\n"
      + "- **数据库**：MySQL 索引、事务、MVCC、Redis 缓存\n"
      + "- **设计模式**：单例、工厂、代理、策略\n"
      + "- **算法**：排序、动态规划、二叉树、BFS/DFS\n"
      + "- **操作系统**：进程/线程、死锁、虚拟内存、CPU 调度\n"
      + "- **消息队列**：Kafka、RocketMQ、消息可靠性\n"
      + "- **Spring**：AOP、事务、Bean 生命周期、循环依赖\n"
      + "- **Spring Cloud**：Nacos、熔断降级、网关、OpenFeign\n"
      + "- **计算机网络**：TCP/IP、HTTP、HTTPS/TLS、Cookie/Session/JWT";
  }

  public SseEmitter askStream(String message, String sessionId) {
    SseEmitter emitter = new SseEmitter(60000L);

    AtomicBoolean finished = new AtomicBoolean(false);
    AtomicBoolean persisted = new AtomicBoolean(false);
    AtomicBoolean sentToken = new AtomicBoolean(false);
    StringBuilder answerBuilder = new StringBuilder();
    AtomicReference<Call> callRef = new AtomicReference<>();

    List<ChatMessage> historyMessages = sessionService.getRecentMessages(sessionId);
    boolean useKnowledgeBase = faqBloomFilterService.mightContain(message);
    // 双路召回：优先向量语义检索，降级到关键词检索
    List<Faq> matchedFaqs = useKnowledgeBase ? faqEmbeddingService.search(message, 5) : List.of();
    String systemPrompt = buildSystemPrompt(sessionId, matchedFaqs);

    try {
      Call call = openAiChatClient.askStream(systemPrompt, historyMessages, message,
        new OpenAiChatClient.StreamHandler() {
          @Override
          public void onToken(String token) {
            sentToken.set(true);
            answerBuilder.append(token);
            try {
              emitter.send(token);
            } catch (IOException e) {
              log.debug("SSE token 发送失败，sessionId={}", sessionId, e);
            }
          }

          @Override
          public void onComplete() {
            persistConversation(sessionId, message, answerBuilder.toString(), persisted);
            completeStream(emitter, finished, callRef);
          }

          @Override
          public void onError(RuntimeException error) {
            handleStreamFailure(emitter, sessionId, message, sentToken, finished, persisted, answerBuilder, callRef, error);
          }
        });
      callRef.set(call);
      log.info("AI 流式调用已发起，sessionId={}", sessionId);
    } catch (OpenAiChatClient.AiClientUnavailableException e) {
      // API key 未配置等情况，直接用 mockAnswer
      log.info("AI 不可用，使用模拟回答，sessionId={}, reason={}", sessionId, e.getMessage());
      new Thread(() -> {
        String answer = mockAnswer(message);
        try { sendMockStream(emitter, answer); } catch (IOException ignored) {}
        persistConversation(sessionId, message, answer, persisted);
        if (finished.compareAndSet(false, true)) {
          try { emitter.send("[DONE]"); emitter.complete(); } catch (Exception ignored) {}
        }
      }).start();
    }

    return emitter;
  }

  private String buildSystemPrompt(String sessionId, List<Faq> matchedFaqs) {
    StringBuilder prompt = new StringBuilder();
    prompt.append("你是一个在线学习平台的AI学习助手。你的职责是：1)推荐合适的课程；2)回答技术问题；3)解答平台使用问题。");
    prompt.append("请用简洁友好的中文回答。对于日常问候请热情回复。");
    prompt.append("当前会话ID：").append(sessionId).append("。");

    if (matchedFaqs.isEmpty()) {
      prompt.append("知识库暂无匹配内容。请根据通用知识回答，但不要编造本平台的具体课程名称或价格。如果用户问课程推荐，请引导用户到课程列表页浏览。");
      return prompt.toString();
    }

    prompt.append("以下是知识库中与用户问题相关的参考资料，请优先基于这些资料回答：");

    int maxFaqCount = Math.min(matchedFaqs.size(), 5);
    for (int i = 0; i < maxFaqCount; i++) {
      Faq faq = matchedFaqs.get(i);
      prompt
        .append("\n")
        .append(i + 1)
        .append(". 问题：")
        .append(faq.getQuestion())
        .append("\n   关键词：")
        .append(faq.getKeywords() == null ? "-" : faq.getKeywords())
        .append("\n   分类：")
        .append(faq.getCategory() == null ? "-" : faq.getCategory())
        .append("\n   回答：")
        .append(faq.getAnswer());
    }

    return prompt.toString();
  }

  private void handleStreamFailure(
    SseEmitter emitter,
    String sessionId,
    String userMessage,
    AtomicBoolean sentToken,
    AtomicBoolean finished,
    AtomicBoolean persisted,
    StringBuilder answerBuilder,
    AtomicReference<Call> callRef,
    Throwable error
  ) {
    if (finished.get()) {
      return;
    }

    if (error != null) {
      log.warn("AI 流式调用结束异常，sessionId={}，尝试非流式回退", sessionId);
    }

    if (!sentToken.get()) {
      String answer;
      // 用 HttpURLConnection 直接调 API（绕过 OkHttp 连接问题）
      try {
        answer = fallbackHttpCall(userMessage);
        log.info("HttpURLConnection 回退成功，sessionId={}", sessionId);
      } catch (Exception fallbackErr) {
        log.info("回退也失败，使用模拟回答，sessionId={}", sessionId);
        answer = mockAnswer(userMessage);
      }

      answerBuilder.setLength(0);
      answerBuilder.append(answer);
      try {
        sendMockStream(emitter, answer);
        sentToken.set(true);
      } catch (IOException e) {
        log.debug("SSE 兜底文案发送失败，sessionId={}", sessionId, e);
      }
    }

    persistConversation(sessionId, userMessage, answerBuilder.toString(), persisted);
    completeStream(emitter, finished, callRef);
  }

  /**
   * 将 mock 回答按行拆分发送，每行末尾带 \n，确保前端 SSE 解析后能还原 Markdown 格式。
   */
  private void sendMockStream(SseEmitter emitter, String content) throws IOException {
    String[] lines = content.split("\n", -1);
    for (int i = 0; i < lines.length; i++) {
      String line = lines[i];
      if (i < lines.length - 1) {
        emitter.send(line + "\n");
      } else if (!line.isEmpty()) {
        emitter.send(line);
      }
    }
  }

  /**
   * 用 HttpURLConnection 直连 Pollinations API（绕过 OkHttp 连接问题）。
   */
  private String fallbackHttpCall(String userMessage) throws Exception {
    String apiUrl = "https://text.pollinations.ai/openai";
    String body = "{\"model\":\"openai\",\"messages\":"
      + "[{\"role\":\"system\",\"content\":\"你是一个在线学习平台的AI学习助手。请用简洁专业的中文回答。\"},"
      + "{\"role\":\"user\",\"content\":\"" + userMessage.replace("\\", "\\\\").replace("\"", "\\\"") + "\"}]}";

    URL url = new URL(apiUrl);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("POST");
    conn.setDoOutput(true);
    conn.setConnectTimeout(3000);
    conn.setReadTimeout(5000);
    conn.setRequestProperty("Content-Type", "application/json");
    conn.setRequestProperty("Authorization", "Bearer sk-pollinations-free-api-key");

    try (OutputStream os = conn.getOutputStream()) {
      os.write(body.getBytes("UTF-8"));
    }

    if (conn.getResponseCode() != 200) {
      throw new RuntimeException("HTTP " + conn.getResponseCode());
    }

    StringBuilder sb = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
      String line;
      while ((line = reader.readLine()) != null) {
        sb.append(line);
      }
    }

    // 解析 JSON 响应提取 content
    String resp = sb.toString();
    int contentIdx = resp.indexOf("\"content\":\"");
    if (contentIdx == -1) {
      throw new RuntimeException("无法解析 AI 响应");
    }
    int start = contentIdx + 11;
    StringBuilder content = new StringBuilder();
    for (int i = start; i < resp.length(); i++) {
      char c = resp.charAt(i);
      if (c == '\\' && i + 1 < resp.length()) {
        char next = resp.charAt(i + 1);
        if (next == 'n') { content.append('\n'); i++; }
        else if (next == '"') { content.append('"'); i++; }
        else if (next == '\\') { content.append('\\'); i++; }
        else { content.append(c); }
      } else if (c == '"') {
        break;
      } else {
        content.append(c);
      }
    }
    return content.toString();
  }

  private void completeStream(SseEmitter emitter, AtomicBoolean finished, AtomicReference<Call> callRef) {
    if (!finished.compareAndSet(false, true)) {
      return;
    }

    try {
      emitter.send("[DONE]");
    } catch (IOException e) {
      log.debug("SSE [DONE] 发送失败", e);
    } finally {
      cancelCall(callRef);
      emitter.complete();
    }
  }

  private void persistConversation(
    String sessionId,
    String userMessage,
    String assistantMessage,
    AtomicBoolean persisted
  ) {
    if (!persisted.compareAndSet(false, true)) {
      return;
    }

    String resolvedAssistantMessage = assistantMessage;
    if (resolvedAssistantMessage == null || resolvedAssistantMessage.isBlank()) {
      resolvedAssistantMessage = FALLBACK_ANSWER;
    }

    sessionService.appendConversation(sessionId, userMessage, resolvedAssistantMessage);
  }

  private void cancelCall(AtomicReference<Call> callRef) {
    Call call = callRef.get();
    if (call != null && !call.isCanceled()) {
      call.cancel();
    }
  }
}
