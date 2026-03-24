-- FAQ 知识库初始数据
-- 来源：面试口述训练营 + 课程推荐 + 平台使用指导

CREATE TABLE IF NOT EXISTS faq (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'FAQ ID',
  question VARCHAR(500) NOT NULL COMMENT '问题',
  answer TEXT NOT NULL COMMENT '答案',
  keywords VARCHAR(500) NULL COMMENT '关键词，逗号分隔',
  category VARCHAR(100) NULL COMMENT '分类',
  hit_count INT NOT NULL DEFAULT 0 COMMENT '命中次数',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',

  PRIMARY KEY (id),
  KEY idx_faq_hit_count (hit_count),
  KEY idx_faq_category (category),
  FULLTEXT KEY idx_faq_fulltext (question, keywords) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='FAQ 知识库表';

-- ===== 一、面试技术知识（来自面试口述训练营） =====
INSERT INTO faq (question, answer, keywords, category) VALUES
('怎么解决大模型幻觉？Prompt 是怎么拼的？RAG 是什么？',
 '为了让模型回答业务问题而不编答案，采用 RAG（检索增强生成）方案：先用用户的提问去 FAQ 知识库里检索相关文档，查出来后设计一个 SystemPrompt 模板（"你是专业助手，请根据以下参考资料回答"），把检索到的文本塞进去，最后把完整 prompt 传给大模型 API。通过 RAG 方式，准确率从瞎猜提升到约 90%。口诀：先查后拼再调用，不让模型自己编。',
 'RAG,大模型,幻觉,Prompt,知识库,检索增强', 'AI技术'),

('滑动窗口保留最近八轮对话，怎么截断 Token？',
 '大模型 API 按 token 收费且有长度限制，所以必须控制上下文。做法是：在拦截器里拿到用户的 SessionId，从 Redis 取出对话历史列表。如果轮数超过阈值（如 8 轮），就删掉最老的那条，只保留最新的拼接进上下文。另外设定半小时无请求时 Redis 自动过期清空。口诀：拦截器拿 Session，Redis 存历史，超八轮砍老的，半小时自动清。',
 'Token,滑动窗口,对话历史,Redis,Session,上下文', 'AI技术'),

('外部大模型 API 崩了或者很慢怎么办？',
 '在调用端加了超时限制（如 15 秒）。超时未回则捕获异常，返回友好的兜底提示语"系统正在思考中，请稍后再试"，而不是让前端一直转圈。同时使用 SSE 流式接口，让字一个一个蹦出来，缓解用户等待焦虑。口诀：超时兜底不卡死，SSE 流式缓焦虑。',
 'SSE,超时,流式,兜底,容错', 'AI技术'),

('什么时候用 ArrayList，什么时候用 HashMap？',
 'ArrayList：存一组有序数据或做列表展示，底层是数组，通过索引查询快，但中间随机插入删除慢。HashMap：存键值对，比如通过用户 ID 快速找详细信息，查询接近 O(1)。口诀：有序列表用 Array，键值映射用 Hash。',
 'ArrayList,HashMap,集合,数据结构', 'Java基础'),

('什么是 IOC？什么是 DI？',
 'IOC（控制反转）：把创建对象的控制权交给 Spring 容器。以前自己 new 对象，耦合度高。现在由 Spring 启动时帮我们创建好，统一管理。DI（依赖注入）：Spring 怎么把创建好的对象交给我。最常用的方式是给属性加 @Autowired 或 @Resource 注解。口诀：IOC 是容器管对象，DI 是注解拿对象。',
 'IOC,DI,Spring,控制反转,依赖注入', 'Spring框架'),

('AOP 是什么？在项目里怎么用的？',
 'AOP 是面向切面编程，用来提取非核心业务的通用逻辑，比如日志打印、权限校验。在项目中用拦截器实现：在请求到达 Controller 之前，先拦下来校验 Token，这样就不用在每个接口里都写一遍校验代码。口诀：切面提通用，拦截做校验。',
 'AOP,切面,拦截器,权限校验,日志', 'Spring框架'),

('什么是 RESTful 接口规范？',
 'RESTful 是一种 API 设计风格。用 URL 定位资源，用名词而不是动词（如 /users）。用 HTTP 动作表示操作：GET 查询，POST 新增，PUT 更新，DELETE 删除。口诀：名词定资源，动词分增删改查。',
 'RESTful,REST,API,HTTP,接口规范', 'Web开发'),

('SQL 优化怎么做？Explain 怎么看？',
 '查询慢时在 SQL 前加 EXPLAIN。主要看两个字段：type（如果是 ALL 就是全表扫描需优化，至少要 ref 或 range）和 key（看是否走了索引）。优化方法：避免 SELECT *，在 WHERE 条件里不对字段做函数操作（会导致索引失效），多条件查询建联合索引（注意最左匹配原则）。口诀：EXPLAIN 看 type 和 key，ALL 全扫必优化。',
 'SQL优化,EXPLAIN,索引,全表扫描,最左匹配', '数据库'),

('Redis 缓存策略是怎么做的？',
 '使用 Cache Aside 策略。读：先读缓存，命中直接返回；未命中则查数据库，放进缓存再返回。写：先更新数据库，然后删除缓存（淘汰机制，下次读时再从库里拿最新的）。口诀：读先缓存后数据库，写先数据库后删缓存。',
 'Redis,缓存,Cache Aside,读写策略', '缓存技术'),

('异步解耦是怎么做的？',
 '比如点赞操作除了增加点赞数，还要发系统通知。发通知不需要马上完成，如果同线程会拖慢接口。解决方案：在发通知的方法上加 @Async 注解，让它在后台另一个线程运行，主线程直接返回。需要在启动类开启 @EnableAsync。口诀：非核心任务扔后台，Async 加注解主线程不等。',
 '@Async,异步,解耦,多线程', 'Spring框架'),

('RBAC 权限隔离是怎么做的？',
 'RBAC 是基于角色的权限控制（Role-Based Access Control）。表结构包括用户表、角色表和权限资源表，通过角色把用户和权限关联。判断用户角色（普通人、版主、管理员），管理员才能访问后台管理接口。口诀：用户绑角色，角色绑权限，三张表搞定。',
 'RBAC,权限,角色,用户,权限控制', '架构设计')
ON DUPLICATE KEY UPDATE answer=VALUES(answer), keywords=VALUES(keywords);

-- ===== 二、课程推荐知识 =====
INSERT INTO faq (question, answer, keywords, category) VALUES
('有什么课程推荐？推荐课程，选课',
 '我们平台有 30 门精品课程涵盖后端开发、前端开发、Java、Python、人工智能、大数据、云计算、移动开发、数据库等方向。热门推荐：1) 大模型提示词工程指南（¥99，4.5万人浏览）适合 AI 入门；2) Java 高级架构师进阶（¥399）适合 3 年+经验的后端工程师；3) Vue 3 从入门到精通（¥149）适合前端新人；4) Docker 容器实操指南（¥79）运维入门必备。你可以在「课程」页面按热度排序选择，也可以告诉我你的技术方向，我帮你推荐。',
 '推荐,课程,选课,学什么,热门', '课程推荐'),

('Java 后端学什么？Java 学习路线',
 '推荐学习路线：1) Spring Boot 实战（¥199）掌握企业级开发基础；2) 深入理解 JVM 原理（¥329）理解 GC 和性能调优；3) Java 高级架构师进阶（¥399）学分布式和微服务；4) Spring Cloud 第四代微服务（¥449）实战秒杀架构。如果对底层网络感兴趣，还可以学深入解析 Netty 底层架构（¥339）。',
 'Java,后端,学习路线,Spring Boot,JVM', '课程推荐'),

('前端学什么？前端学习路线',
 '推荐学习路线：1) Vue 3 从入门到精通（¥149）入门首选；2) Vue 3 高级组件化实战（¥229）深入 Composition API；3) React 18 全家桶实战（¥189）掌握 React 生态；4) TypeScript 企业级进阶（¥169）提升代码健壮性；5) 前端工程化体系剖析（¥259）Webpack/Vite 原理。',
 '前端,Vue,React,TypeScript,学习路线', '课程推荐'),

('怎么入门 AI 人工智能？',
 '推荐路线：1) 大模型提示词工程指南（¥99，入门级）先学 Prompt Engineering；2) 深度学习与计算机视觉（¥499）学 PyTorch 和 CNN；3) 自然语言处理进阶（¥459）手撕 Transformer。Python 基础不够的话先学 Python 数据分析实战（¥179）。',
 'AI,人工智能,深度学习,NLP,入门', '课程推荐'),

('平台怎么用？怎么购买课程？',
 '使用流程：1) 在「课程」页面浏览所有课程，可以按热度、购买量排序；2) 点击感兴趣的课程进入详情页查看大纲和价格；3) 点击「立即购买」进入确认订单页；4) 选择支付方式（支付宝/微信）后提交订单；5) 在支付页完成付款后即可开始学习。你也可以在「学习计划」页面管理学习进度。',
 '购买,使用,教程,怎么用,学习计划', '平台使用'),

('你是谁？你能做什么？',
 '我是学习平台的 AI 学习助手！我可以帮你：1) 推荐适合你的课程；2) 回答 Java、Spring、数据库、前端、AI 等技术问题；3) 解答平台使用相关问题。你可以直接问我技术问题，比如"什么是 IOC"、"Redis 缓存怎么用"，或者问我"推荐一门前端课程"。',
 '你好,介绍,功能,AI助手', '平台使用')
ON DUPLICATE KEY UPDATE answer=VALUES(answer), keywords=VALUES(keywords);

-- ===== 三、JVM 与 Java 进阶 =====
INSERT INTO faq (question, answer, keywords, category) VALUES
('JVM 内存结构是什么？堆和栈的区别？',
 'JVM 运行时数据区分为五块：\n1. **堆（Heap）**：存放对象实例和数组，GC 主要作用区域，所有线程共享。分为新生代（Eden + 两个 Survivor）和老年代。\n2. **虚拟机栈（Stack）**：每个线程私有，存放局部变量、操作数栈、方法出口等。每个方法调用对应一个栈帧。\n3. **方法区（Method Area）**：存储类信息、常量、静态变量，JDK 8 后实现为元空间（Metaspace），改用本地内存。\n4. **程序计数器（PC Register）**：线程私有，记录当前字节码执行位置。\n5. **本地方法栈**：为 Native 方法服务。\n\n**堆 vs 栈**：堆空间大、GC 管理、生命周期长；栈空间小、自动回收（出栈即销毁）、访问速度快。',
 'JVM,内存,堆,栈,方法区,元空间', 'JVM'),

('Java 垃圾回收（GC）算法有哪些？G1 和 CMS 的区别？',
 '**基础 GC 算法：**\n- **标记-清除**：标记可达对象，清除未标记的。缺点：内存碎片化。\n- **标记-整理**：清除后将存活对象压缩到一端，无碎片，但效率低。\n- **复制算法**：将存活对象复制到另一块区域，清空原区域。新生代 Eden/Survivor 就用此算法。\n\n**主流回收器对比：**\n| 回收器 | 特点 | 适用 |\n|--------|------|------|\n| Serial | 单线程，STW | 客户端小内存 |\n| Parallel | 多线程，注重吞吐量 | 批处理 |\n| CMS | 并发标记清除，低停顿 | 响应时间敏感，有碎片问题 |\n| G1 | 分 Region，可预测停顿时间，JDK 9+ 默认 | 大堆，均衡吞吐和延迟 |\n| ZGC | 停顿 <10ms，适合超大堆 | 超低延迟场景 |',
 'GC,垃圾回收,G1,CMS,ZGC,STW', 'JVM'),

('什么是类加载机制？双亲委派模型是什么？',
 '**类加载过程（5步）：**\n加载 → 验证 → 准备 → 解析 → 初始化\n- **加载**：读取字节码到方法区，生成 Class 对象。\n- **准备**：为静态变量分配内存，赋默认值（int 为 0，引用为 null）。\n- **初始化**：执行 `<clinit>` 方法（静态代码块和静态变量赋值）。\n\n**双亲委派模型：**\n类加载器收到请求，先委托父加载器处理，父加载器找不到时才自己加载。层次：BootstrapClassLoader → ExtClassLoader → AppClassLoader → 自定义 ClassLoader。\n\n**作用：** 防止核心类被篡改（java.lang.String 只能由 Bootstrap 加载）；避免同一个类被加载多次。\n\n**打破双亲委派的场景：** JDBC SPI、Tomcat（隔离不同 Web App 的类）、OSGi 模块化。',
 '类加载,双亲委派,ClassLoader,JVM', 'JVM'),

('Java 中 volatile 关键字的作用是什么？',
 'volatile 有两个核心作用：\n\n**1. 可见性：** 对 volatile 变量的写操作会立即刷入主内存；读操作会从主内存读取，而不是线程本地缓存（L1/L2 Cache）。\n\n**2. 禁止指令重排序：** 通过内存屏障（Memory Barrier）实现，确保 volatile 写之前的操作不会被重排到写之后，读之后的操作也不会重排到读之前。\n\n**不能保证原子性：** `count++` 是三步操作（读-改-写），volatile 无法使其原子化，需用 `AtomicInteger` 或 `synchronized`。\n\n**典型使用场景：** 双重检查锁单例的 instance 变量、状态标志位（如 `volatile boolean running`）。',
 'volatile,可见性,内存屏障,指令重排,原子性', 'Java并发')
ON DUPLICATE KEY UPDATE answer=VALUES(answer), keywords=VALUES(keywords);

-- ===== 四、操作系统 =====
INSERT INTO faq (question, answer, keywords, category) VALUES
('进程和线程的区别是什么？',
 '**进程（Process）：** 操作系统资源分配的基本单位，拥有独立的地址空间、文件描述符、内存等资源。进程间通信（IPC）需要特殊机制：管道、消息队列、共享内存、Socket。\n\n**线程（Thread）：** CPU 调度的基本单位，同一进程内的线程共享地址空间和堆，但每个线程有自己的栈和程序计数器。线程通信直接读写共享内存（需要同步）。\n\n**对比：**\n| 维度 | 进程 | 线程 |\n|------|------|------|\n| 资源 | 独立 | 共享进程资源 |\n| 开销 | 大（上下文切换慢）| 小 |\n| 通信 | IPC（复杂）| 共享内存（简单但需同步）|\n| 崩溃影响 | 不影响其他进程 | 可能导致整个进程崩溃 |\n\n**协程（Coroutine）：** 比线程更轻量，由用户态调度，无内核切换开销（Go goroutine / Python asyncio）。',
 '进程,线程,协程,IPC,上下文切换', '操作系统'),

('什么是死锁？如何避免、检测和解除？',
 '**死锁（Deadlock）：** 两个或多个线程/进程互相持有对方所需的资源，都在等待对方释放，导致永久阻塞。\n\n**四个必要条件（缺一不可）：**\n1. 互斥：资源不能共享\n2. 占有且等待：持有资源还在等待新资源\n3. 不可剥夺：资源只能由持有者自愿释放\n4. 循环等待：A 等 B，B 等 A\n\n**避免策略：**\n- **破坏循环等待：** 按固定顺序获取锁（所有线程都先抢锁 A 再抢锁 B）\n- **一次性获取所有资源**（银行家算法）\n- 使用带超时的 `tryLock(timeout)`，超时后释放已持有锁\n\n**Java 检测：** `jstack <pid>` 可检测死锁，输出中有 "Found one Java-level deadlock"。',
 '死锁,deadlock,四个条件,银行家算法,jstack', '操作系统'),

('什么是 CPU 调度算法？常见算法有哪些？',
 '**CPU 调度：** 操作系统决定哪个进程/线程获得 CPU 时间片的策略。\n\n**常见算法：**\n| 算法 | 特点 | 缺点 |\n|------|------|------|\n| FCFS（先来先服务）| 简单公平 | 长任务阻塞短任务（护航效应）|\n| SJF（最短作业优先）| 平均等待时间最短 | 长任务可能饿死 |\n| 优先级调度 | 重要任务优先 | 低优先级任务饿死 |\n| 时间片轮转（RR）| 公平，适合交互系统 | 时间片太短则上下文切换频繁 |\n| 多级反馈队列 | 综合以上，Linux 主要使用 | 实现复杂 |\n\n**Linux 调度器 CFS（完全公平调度）：** 基于虚拟时间，给每个进程分配"虚拟运行时间"，每次选择虚拟时间最小的进程运行，实现相对公平。',
 'CPU调度,FCFS,SJF,时间片轮转,CFS', '操作系统'),

('什么是虚拟内存？页表和 TLB 是什么？',
 '**虚拟内存：** 让每个进程以为自己独占了一块连续的完整内存，实际上由操作系统和硬件将虚拟地址映射到物理地址。好处：进程隔离、内存超用（将部分内容换到磁盘）、简化编程。\n\n**分页机制：**\n- 将虚拟地址空间和物理地址空间都切成固定大小的**页（Page）**，通常 4KB。\n- **页表（Page Table）：** 记录虚拟页到物理页帧的映射关系，存放在内存中。\n- CPU 地址翻译：虚拟地址 = 页号 + 页偏移，查页表得物理帧号，组合成物理地址。\n\n**TLB（Translation Lookaside Buffer）：** 页表的硬件缓存（位于 CPU 内部），缓存最近使用的虚拟-物理地址映射，命中率通常 >99%，大幅加速地址翻译。\n\n**缺页中断（Page Fault）：** 访问的页不在物理内存时触发，OS 从磁盘（swap）加载页，可能置换已有页（LRU/Clock 算法）。',
 '虚拟内存,页表,TLB,分页,缺页中断,swap', '操作系统')
ON DUPLICATE KEY UPDATE answer=VALUES(answer), keywords=VALUES(keywords);

-- ===== 五、数据结构与算法 =====
INSERT INTO faq (question, answer, keywords, category) VALUES
('二叉树、平衡二叉树（AVL）、红黑树的区别？',
 '**二叉搜索树（BST）：** 左子树所有节点 < 根 < 右子树所有节点，查找 O(log n)。最坏情况退化为链表 O(n)（插入有序数据时）。\n\n**AVL 树（平衡二叉树）：** 任意节点的左右子树高度差不超过 1，通过旋转保持平衡。查找/插入/删除均 O(log n)，但插入删除时旋转频繁，维护成本高。\n\n**红黑树：** 通过颜色标记（红/黑）和特定规则保证最长路径不超过最短路径的两倍，虽然不是严格平衡，但查找/插入/删除最坏 O(log n)，旋转次数少，实际性能更好。\n\n**Java 中的应用：**\n- `TreeMap`/`TreeSet`：红黑树\n- `HashMap`：链表长度≥8时转红黑树\n- `PriorityQueue`：堆（完全二叉树）\n\n**B+树（数据库索引）：** 多路平衡搜索树，所有数据存叶子节点，叶子节点链表连接，适合磁盘 I/O，MySQL InnoDB 索引使用 B+ 树。',
 '二叉树,AVL,红黑树,BST,B+树,TreeMap', '数据结构'),

('常见排序算法的时间复杂度？快速排序原理？',
 '**常见排序算法：**\n| 算法 | 平均时间 | 最坏时间 | 空间 | 稳定性 |\n|------|---------|---------|------|--------|\n| 冒泡 | O(n²) | O(n²) | O(1) | 稳定 |\n| 选择 | O(n²) | O(n²) | O(1) | 不稳定 |\n| 插入 | O(n²) | O(n²) | O(1) | 稳定 |\n| 希尔 | O(n log n) | O(n²) | O(1) | 不稳定 |\n| 归并 | O(n log n) | O(n log n) | O(n) | 稳定 |\n| **快速** | O(n log n) | O(n²) | O(log n) | 不稳定 |\n| 堆排序 | O(n log n) | O(n log n) | O(1) | 不稳定 |\n\n**快速排序原理：** 分治思想。选一个基准数（Pivot），将数组分为"比 Pivot 小"和"比 Pivot 大"两部分，再递归排序。分区操作是核心（双指针从两端向中间扫描）。\n\n**最坏情况：** 每次选到最大/最小值作为 Pivot（有序数组），退化为 O(n²)。优化：随机选 Pivot 或三数取中。\n\n**Java `Arrays.sort()` 对基本类型用 DualPivotQuicksort（双轴快排），对对象用 TimSort（归并+插入的混合，稳定）。**',
 '排序,快速排序,归并排序,时间复杂度,稳定性', '数据结构'),

('动态规划（DP）是什么？如何解题？',
 '**动态规划：** 将复杂问题分解为子问题，利用"最优子结构"和"重叠子问题"特性，通过缓存子问题结果避免重复计算。\n\n**解题四步法：**\n1. **定义状态**：dp[i] 或 dp[i][j] 代表什么\n2. **写状态转移方程**：dp[i] = f(dp[i-1], dp[i-2]...)\n3. **确定初始值**：dp[0]、dp[1] 等边界\n4. **确定计算顺序**：自底向上（通常用循环）\n\n**经典题型：**\n- 斐波那契数列：dp[i] = dp[i-1] + dp[i-2]\n- 背包问题：01背包、完全背包\n- 最长公共子序列（LCS）：dp[i][j]\n- 最长递增子序列（LIS）：O(n log n) 优化\n- 最短路径（拓扑排序 + DP）\n\n**贪心 vs DP：** 贪心每步选局部最优，不回退（适合：会议排程、霍夫曼编码）；DP 考虑全局，需要穷举所有子问题（适合：有重叠子问题的最优化题）。',
 '动态规划,DP,背包问题,LCS,LIS,贪心', '数据结构'),

('图的常用算法有哪些？BFS 和 DFS 的区别？',
 '**图的表示：**\n- 邻接矩阵：二维数组，查询快 O(1)，空间 O(V²)，适合稠密图\n- 邻接表：链表/HashMap，空间 O(V+E)，适合稀疏图\n\n**BFS（广度优先搜索）：** 用队列，逐层扩展，找到的路径是最短路径（无权图）。适用：最短路径、层序遍历。\n\n**DFS（深度优先搜索）：** 用栈（递归），一条路走到底再回溯。适用：连通性检测、环检测、拓扑排序。\n\n**最短路径算法：**\n- **Dijkstra**：单源最短路径，不能有负权边，O((V+E) log V)\n- **Bellman-Ford**：可处理负权边，检测负权环，O(VE)\n- **Floyd-Warshall**：多源最短路径，O(V³)\n\n**最小生成树：**\n- **Kruskal**：按边权排序，贪心加边，用并查集判断是否成环\n- **Prim**：从一个顶点出发，每次加入最近的顶点',
 'BFS,DFS,图,Dijkstra,最短路径,最小生成树,拓扑排序', '数据结构')
ON DUPLICATE KEY UPDATE answer=VALUES(answer), keywords=VALUES(keywords);

-- ===== 六、计算机网络进阶 =====
INSERT INTO faq (question, answer, keywords, category) VALUES
('HTTPS 的 TLS 握手过程是什么？',
 'TLS 1.3 握手过程（简化版）：\n\n1. **ClientHello**：客户端发送支持的 TLS 版本、加密套件列表、随机数（Client Random）。\n2. **ServerHello + 证书**：服务端选定加密套件，发送随机数（Server Random）和数字证书（含公钥）。\n3. **证书验证**：客户端用 CA 的公钥验证证书签名，确认服务端身份真实可信。\n4. **密钥交换（ECDHE）**：双方通过椭圆曲线 Diffie-Hellman 算法，在不传输私钥的情况下协商出对称加密密钥（Session Key）。TLS 1.3 中此步可与 ClientHello 合并，实现 1-RTT。\n5. **Finished**：双方用协商好的对称密钥加密通信，后续所有 HTTP 数据用对称加密（性能好）。\n\n**为什么同时用非对称和对称加密？** 非对称加密（RSA/ECDHE）安全但慢，仅用于密钥协商；对称加密（AES）快，用于数据传输。',
 'HTTPS,TLS,握手,SSL,证书,ECDHE,加密', '计算机网络'),

('HTTP 常见状态码有哪些？301 和 302 的区别？',
 '**常见状态码分类：**\n- **1xx**：信息性，如 101 Switching Protocols（WebSocket 升级）\n- **2xx**：成功，如 200 OK、201 Created、204 No Content\n- **3xx**：重定向，如 301 永久重定向、302 临时重定向、304 Not Modified（缓存生效）\n- **4xx**：客户端错误，如 400 Bad Request、401 Unauthorized（未登录）、403 Forbidden（无权限）、404 Not Found、429 Too Many Requests\n- **5xx**：服务端错误，如 500 Internal Server Error、502 Bad Gateway、503 Service Unavailable、504 Gateway Timeout\n\n**301 vs 302：**\n- **301（永久重定向）**：浏览器会缓存新地址，以后直接访问新 URL，不再请求原 URL。SEO 权重也会转移。\n- **302（临时重定向）**：浏览器不缓存，每次都请求原 URL，由服务器指示跳转。常用于 A/B 测试、登录跳转。',
 'HTTP,状态码,301,302,304,404,500', '计算机网络'),

('什么是 cookie、session、JWT 的区别？',
 '**Cookie：** 浏览器端存储的小型文本数据（≤4KB），随请求自动发送给同域服务器。可设置 HttpOnly（防 XSS）、Secure（只走 HTTPS）、SameSite（防 CSRF）属性。\n\n**Session：** 服务器端会话存储。用户登录后，服务端创建 Session 对象存用户信息，将 SessionId 通过 Cookie 返回客户端。优点：服务端可控（可主动失效）；缺点：有状态，水平扩展需共享 Session（Redis 存储）。\n\n**JWT（JSON Web Token）：** 服务端生成的签名字符串（Header.Payload.Signature），存于客户端（Cookie 或 localStorage）。无状态，天然支持分布式。\n- 优点：无需服务端存储 Session，适合微服务\n- 缺点：Token 下发后无法主动撤销（需配合黑名单）；Payload 明文可见（勿存敏感信息）\n\n**选择原则：** 单体应用 → Session；微服务/跨域 → JWT。',
 'Cookie,Session,JWT,Token,认证,登录', '计算机网络')
ON DUPLICATE KEY UPDATE answer=VALUES(answer), keywords=VALUES(keywords);

-- ===== 七、消息队列 =====
INSERT INTO faq (question, answer, keywords, category) VALUES
('消息队列的作用是什么？常见 MQ 对比？',
 '**消息队列三大作用：**\n1. **异步**：耗时操作放到 MQ，接口快速返回（如发送邮件、短信）\n2. **削峰**：流量高峰时请求堆积在 MQ，下游按能力消费（如秒杀）\n3. **解耦**：生产者和消费者互不依赖，只通过消息通信\n\n**主流 MQ 对比：**\n| MQ | 吞吐量 | 延迟 | 特点 |\n|----|--------|------|------|\n| RabbitMQ | 万级 | 微秒 | AMQP协议，路由灵活，管理界面友好 |\n| RocketMQ | 十万级 | 毫秒 | 阿里开源，事务消息，顺序消息，金融级 |\n| Kafka | 百万级 | 毫秒 | 吞吐量最高，适合日志/流处理，消费者组 |\n| ActiveMQ | 万级 | 毫秒 | 老牌，功能全，性能一般 |\n\n**技术选型：** 高并发大数据流 → Kafka；业务消息 + 事务 → RocketMQ；灵活路由 + 服务解耦 → RabbitMQ。',
 'MQ,消息队列,RocketMQ,Kafka,RabbitMQ,异步,削峰,解耦', '消息队列'),

('消息队列如何保证消息不丢失？如何处理重复消费？',
 '**防止消息丢失（三个环节）：**\n\n1. **生产者到 MQ：** 开启 ACK 确认，MQ 返回成功才认为发送完成；RocketMQ 支持同步/异步/单向发送；Kafka 设置 `acks=all`。\n2. **MQ 自身持久化：** 消息写磁盘（RocketMQ CommitLog / Kafka Log Segment），不丢内存数据。\n3. **消费者消费后确认：** 手动 ACK，业务处理完再确认，避免消费失败但 MQ 认为已消费。\n\n**处理重复消费（幂等性）：** 网络抖动可能导致消息被投递多次，需要消费者保证幂等：\n- **数据库唯一键**：插入时用唯一索引防重\n- **Redis incr 去重**：消息 ID 作 Key，`setnx` 判断是否已处理\n- **业务状态判断**：如订单已"支付完成"则跳过重复支付消息\n\n**死信队列（DLQ）：** 消费失败的消息重试 N 次后进入死信队列，人工兜底处理。',
 '消息不丢失,幂等,重复消费,ACK,死信队列,消息队列', '消息队列'),

('Kafka 的核心概念和高吞吐量原理是什么？',
 '**Kafka 核心概念：**\n- **Topic**：消息的逻辑分类，一个 Topic 可有多个 Partition\n- **Partition**：分区，Topic 的物理分区，消费顺序在同一Partition内保证\n- **Producer/Consumer**：生产者/消费者\n- **Consumer Group**：消费者组，组内每个分区只被一个消费者消费（水平扩展）\n- **Broker**：Kafka 服务节点\n- **Offset**：消费位移，Consumer 自己维护已消费到哪条消息\n\n**高吞吐量原因：**\n1. **顺序写磁盘**：追加写 Log 文件（顺序 I/O 比随机 I/O 快 100x）\n2. **零拷贝（Zero Copy）**：`sendfile()` 系统调用，数据从磁盘直接到网卡，不经过用户态\n3. **批量发送 + 压缩**：Producer 批量打包消息，减少网络往返\n4. **分区并行**：多 Partition 可并发读写，多 Consumer 并发消费',
 'Kafka,Topic,Partition,消费者组,零拷贝,高吞吐', '消息队列')
ON DUPLICATE KEY UPDATE answer=VALUES(answer), keywords=VALUES(keywords);

-- ===== 八、Spring Cloud 与微服务 =====
INSERT INTO faq (question, answer, keywords, category) VALUES
('微服务架构是什么？和单体架构的区别？',
 '**单体架构：** 所有功能打包成一个可部署单元（WAR/JAR）。优点：简单、调试方便；缺点：代码耦合、扩展困难、技术栈单一、一个 Bug 可能宕整个应用。\n\n**微服务架构：** 将应用拆分为一组小而独立的服务，每个服务围绕业务能力（如用户服务、订单服务、支付服务），通过 API（HTTP/RPC）通信，独立部署和扩展。\n\n**微服务优点：**\n- 独立部署，技术栈灵活\n- 按需扩展（订单服务压力大单独扩容）\n- 故障隔离（一个服务挂不影响全局）\n\n**微服务挑战：**\n- 分布式事务（Seata / TCC 模式）\n- 服务发现与注册（Nacos / Eureka）\n- 链路追踪（Zipkin / SkyWalking）\n- 网络延迟和故障（熔断器 Resilience4j）\n\n**适用场景：** 大型团队、需要独立扩展的模块、有 DevOps 能力的团队。小团队和早期项目建议先单体再演进。',
 '微服务,单体架构,服务拆分,分布式', 'Spring Cloud'),

('Spring Cloud 核心组件有哪些？各自的作用？',
 '**Spring Cloud 常用组件：**\n\n| 组件 | 功能 | 常见实现 |\n|------|------|----------|\n| 注册中心 | 服务注册与发现 | Nacos、Eureka、Consul |\n| 配置中心 | 统一配置管理，热刷新 | Nacos Config、Apollo |\n| 网关 | 统一入口，路由、鉴权、限流 | Spring Cloud Gateway、Nginx |\n| 负载均衡 | 客户端负载均衡 | Spring Cloud LoadBalancer（替代 Ribbon）|\n| 熔断器 | 防止级联故障 | Resilience4j（替代 Hystrix）|\n| 远程调用 | 服务间 HTTP 调用 | OpenFeign |\n| 链路追踪 | 分布式请求链路追踪 | Micrometer Tracing / SkyWalking |\n\n**请求流程：** 客户端 → 网关（鉴权/限流）→ 注册中心（查找实例）→ 负载均衡（选实例）→ 微服务（熔断保护）。\n\n**Nacos 一站式：** 同时提供注册中心 + 配置中心功能，是目前国内最流行的选择。',
 'Spring Cloud,Nacos,网关,OpenFeign,熔断,Eureka,注册中心', 'Spring Cloud'),

('什么是服务熔断和服务降级？和限流的区别？',
 '**服务熔断（Circuit Breaker）：** 当下游服务失败率超过阈值（如 50% 在 10s 内），熔断器"断开"，后续请求不再调用下游，直接返回兜底（fail-fast），防止雪崩。一段时间后进入"半开"状态，试探性放行少量请求，成功则关闭熔断器。\n\n**服务降级（Degradation）：** 在系统资源不足或下游不可用时，返回简化版结果或默认值（如"当前服务不可用，请稍后再试"），牺牲部分功能保障核心功能可用。降级不一定伴随熔断。\n\n**限流（Rate Limiting）：** 控制接口的请求速率，超出阈值拒绝或排队。常用算法：\n- **令牌桶（Token Bucket）**：以固定速率生成令牌，请求消耗令牌，多余的令牌可积累（允许突发流量）\n- **漏桶（Leaky Bucket）**：请求入桶，以固定速率漏出，平滑流量，不允许突发\n- **滑动窗口**：统计最近时间窗口内的请求数\n\n**实现：** Resilience4j（熔断）、Sentinel（阿里，熔断+降级+限流，一体化）、Spring Cloud Gateway + 令牌桶。',
 '熔断,降级,限流,Sentinel,Resilience4j,令牌桶,雪崩', 'Spring Cloud')
ON DUPLICATE KEY UPDATE answer=VALUES(answer), keywords=VALUES(keywords);

-- ===== 九、MyBatis / 数据库进阶 =====
INSERT INTO faq (question, answer, keywords, category) VALUES
('MyBatis 和 JPA/Hibernate 的区别？什么时候用 MyBatis？',
 '**MyBatis：** SQL 映射框架，SQL 由开发者手写（XML 或注解），灵活可控，适合复杂查询。\n\n**JPA / Hibernate：** ORM（对象关系映射）框架，通过对象操作自动生成 SQL，开发效率高，但复杂查询难控制，性能调优困难。\n\n**对比：**\n| 维度 | MyBatis | JPA/Hibernate |\n|------|---------|---------------|\n| SQL 控制 | 完全手写，可优化 | 自动生成，优化难 |\n| 学习曲线 | 低 | 高 |\n| 复杂查询 | 简单 | 复杂（需 JPQL / Criteria）|\n| 代码量 | 略多（需写 Mapper）| 少（CRUD 自动）|\n\n**MyBatis Plus：** MyBatis 增强版，内置单表 CRUD 方法、条件构造器（LambdaQueryWrapper），兼顾便利性和灵活性，是国内主流选择。\n\n**选型建议：** 互联网业务（复杂 SQL、DBA 协作）→ MyBatis；管理后台（CRUD 为主）→ JPA / MyBatis Plus。',
 'MyBatis,Hibernate,JPA,ORM,MyBatis Plus,Mapper', '数据库'),

('MySQL 索引失效的常见场景有哪些？',
 '以下情况会导致 MySQL 索引失效，触发全表扫描：\n\n1. **联合索引不满足最左前缀**：索引 (a, b, c)，`WHERE b=1` 无法使用索引。\n2. **对索引列做函数运算**：`WHERE YEAR(create_time)=2024` → 对 create_time 做了函数，索引失效。改为范围查询：`WHERE create_time BETWEEN '2024-01-01' AND '2024-12-31'`。\n3. **隐式类型转换**：索引列为 varchar，查询传入 int，MySQL 会做隐式转换导致索引失效。例：`WHERE phone=13888888888`（phone 是 varchar）。\n4. **LIKE 前缀通配符**：`LIKE '%xxx'` 失效，`LIKE 'xxx%'` 有效。\n5. **使用 OR 连接非索引列**：`WHERE a=1 OR b=2`，若 b 没有索引，全表扫描。\n6. **NOT IN / != / <>**：通常不走索引（小表可能走，具体看优化器）。\n7. **索引列参与运算**：`WHERE age+1=18`，改为 `WHERE age=17`。\n\n**排查方法：** `EXPLAIN SELECT...` 看 type 和 key 字段。',
 'MySQL,索引失效,EXPLAIN,最左前缀,LIKE,函数', '数据库'),

('什么是 MySQL 的 MVCC？如何实现可重复读？',
 '**MVCC（多版本并发控制）：** MySQL InnoDB 实现读写不互相阻塞的机制。读操作不加锁，而是读取数据的历史版本（快照），从而提高并发性。\n\n**核心数据结构：**\n- **undo log**：记录数据的历史版本链，每次修改前将旧值写入 undo log。\n- **Read View**：事务开始时（RR级别）或每次查询时（RC级别）生成，记录当前活跃事务 ID 列表。\n- **隐藏列**：每行含 trx_id（最后修改该行的事务ID）和 roll_pointer（指向 undo log 版本链）。\n\n**可重复读（Repeatable Read）实现：**\n1. 事务开始时生成一个 Read View（快照）。\n2. 读取数据时，遍历 undo log 版本链，找到 trx_id < Read View 最小活跃 ID 的版本，即为当前事务可见的最新版本。\n3. 同一事务内多次读，始终使用同一个 Read View，所以结果一致——实现可重复读。\n\n**RC（读已提交）：** 每次查询都生成新 Read View，所以能读到其他事务已提交的最新数据，但同一事务内两次读可能不同。',
 'MVCC,可重复读,Read View,undo log,InnoDB,事务隔离', '数据库')
ON DUPLICATE KEY UPDATE answer=VALUES(answer), keywords=VALUES(keywords);

-- ===== 十、Spring 框架进阶 =====
INSERT INTO faq (question, answer, keywords, category) VALUES
('Spring 事务的传播行为有哪些？@Transactional 失效的场景？',
 '**事务传播行为（7种）：**\n| 传播类型 | 含义 |\n|---------|------|\n| REQUIRED（默认）| 有事务就加入，没有就新建 |\n| REQUIRES_NEW | 无论如何都新建，挂起外层事务 |\n| SUPPORTS | 有事务加入，没有就非事务运行 |\n| NOT_SUPPORTED | 非事务运行，挂起已有事务 |\n| MANDATORY | 必须在事务中，否则抛异常 |\n| NEVER | 不能在事务中，否则抛异常 |\n| NESTED | 外层事务的嵌套事务（Savepoint）|\n\n**@Transactional 失效的常见场景：**\n1. **自调用**：同一个类中 A 方法调 B 方法（B 加了 @Transactional），因为 AOP 代理不生效，事务不起作用。解决：抽到另一个 Bean。\n2. **非 public 方法**：@Transactional 只对 public 方法生效（SpringAOP 的代理机制）。\n3. **异常被 catch 吞掉**：方法内 try-catch 捕获了异常，没有向外抛，事务不会回滚。\n4. **非运行时异常**：默认只回滚 RuntimeException，需配 `rollbackFor=Exception.class`。\n5. **数据库引擎不支持事务**：MySQL MyISAM 不支持事务，需用 InnoDB。',
 'Spring事务,@Transactional,传播行为,事务失效,AOP', 'Spring框架'),

('Spring AOP 的原理？JDK 动态代理和 CGLIB 的区别？',
 '**AOP（面向切面编程）** 通过动态代理在方法执行前后织入横切逻辑（日志、事务、权限等），不侵入业务代码。\n\n**Spring AOP 实现原理：**\n- Spring 默认用 JDK 动态代理或 CGLIB 在运行时生成代理类\n- 代理类拦截方法调用，在目标方法前后执行 Advice（通知）\n- 核心注解：`@Aspect`（声明切面）、`@Pointcut`（切点表达式）、`@Before/@After/@Around`（通知类型）\n\n**JDK 动态代理 vs CGLIB：**\n| 维度 | JDK 动态代理 | CGLIB |\n|------|-------------|-------|\n| 要求 | 目标类必须实现接口 | 无需接口，直接子类化 |\n| 原理 | `Proxy.newProxyInstance()` + `InvocationHandler` | 字节码增强（ASM），生成目标类的子类 |\n| 性能 | 反射调用略慢 | JDK 8 后性能差别不大 |\n| 限制 | 无法代理没有接口的类 | 无法代理 final 类/方法 |\n\n**Spring Boot 2.x 默认用 CGLIB**（`spring.aop.proxy-target-class=true`）。',
 'Spring AOP,JDK动态代理,CGLIB,代理,切面,@Aspect', 'Spring框架'),

('Spring Bean 的生命周期是什么？',
 'Spring Bean 的完整生命周期：\n\n1. **实例化（Instantiation）**：调用构造器或工厂方法创建 Bean 对象。\n2. **属性注入（Populate Properties）**：DI，将依赖注入（@Autowired/@Value）。\n3. **Aware 接口回调**：如果 Bean 实现了 BeanNameAware、ApplicationContextAware 等接口，调用对应方法。\n4. **BeanPostProcessor#postProcessBeforeInitialization**：所有 BeanPostProcessor 的前置处理（AOP 代理是在后置处理生成的）。\n5. **@PostConstruct / InitializingBean#afterPropertiesSet / init-method**：初始化回调。\n6. **BeanPostProcessor#postProcessAfterInitialization**：后置处理，生成 AOP 代理对象。\n7. **使用（Bean Ready）**：Bean 放入容器，可被注入使用。\n8. **销毁**：容器关闭时，调用 `@PreDestroy` / `DisposableBean#destroy` / destroy-method。\n\n**循环依赖：** Spring 通过三级缓存（singletonObjects、earlySingletonObjects、singletonFactories）解决 Setter/Field 注入的循环依赖，但构造器注入无法解决。',
 'Spring Bean,生命周期,@PostConstruct,BeanPostProcessor,循环依赖,三级缓存', 'Spring框架')
ON DUPLICATE KEY UPDATE answer=VALUES(answer), keywords=VALUES(keywords);
