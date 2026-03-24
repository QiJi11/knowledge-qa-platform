// 课程相关类型：给前端页面/模拟数据用（不和后端接口强绑定）

export interface Category {
  id: number
  name: string
  parentId?: number
  sortOrder?: number
  // 首页会用到 icon 字段（字符串形式）
  icon?: string
}

export interface Teacher {
  id: number
  nickname: string
  avatar?: string
  intro?: string
  courseCount?: number
  studentCount?: number
}

export interface Lesson {
  id: number
  chapterId: number
  courseId: number
  title: string
  // 学习页会给当前课时塞一个播放地址；目录里的课时可能没有
  videoUrl?: string
  duration: number
  isFree: boolean
  sortOrder: number
}

export interface Chapter {
  id: number
  courseId: number
  title: string
  sortOrder: number
  lessons?: Lesson[]
}

export interface Course {
  id: number
  title: string
  description: string
  coverUrl: string
  teacherId: number
  teacher?: Teacher
  categoryId: number
  category?: Category
  price: number
  originalPrice?: number
  status: number
  buyCount: number
  viewCount: number
  chapterCount?: number
  lessonCount?: number
  totalDuration?: number
  createdAt: string
  updatedAt: string
  isBought?: boolean
}

export interface CourseDetail extends Course {
  chapters: Chapter[]
}
