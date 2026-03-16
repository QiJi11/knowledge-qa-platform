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
  FULLTEXT KEY idx_faq_question (question) WITH PARSER ngram
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
