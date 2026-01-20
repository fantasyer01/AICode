export type Language = 'en' | 'zh'

export const translations: Record<Language, {
  navigation: {
    title: string
    add: string
  }
  categories: {
    all: string
    web: string
    mobile: string
    ai: string
    data: string
  }
  themes: {
    pastel: string
    ocean: string
    sunset: string
    forest: string
    midnight: string
    candy: string
  }
  language: {
    en: string
    zh: string
  }
  project: {
    code: string
    demo: string
  }
  content: {
    description: string
    loading: string
    error: string
    errorHint: string
    noProjects: string
    footer: string
  }
  dialog: {
    title: string
    description: string
    labels: {
      title: string
      description: string
      category: string
      date: string
      technologies: string
      colorIndex: string
      demoLink: string
      githubLink: string
      imageFilename: string
    }
    placeholders: {
      title: string
      description: string
      date: string
      technologies: string
      colorIndex: string
      demoLink: string
      githubLink: string
      imageFilename: string
    }
    helpers: {
      colorIndex: string
      imageFilename: string
    }
    buttons: {
      addTechnology: string
      creating: string
      create: string
      cancel: string
    }
  }
}> = {
  en: {
    navigation: {
      title: 'AI Portfolio',
      add: 'Add'
    },
    categories: {
      all: 'All',
      web: 'Web',
      mobile: 'Mobile',
      ai: 'AI/ML',
      data: 'Data'
    },
    themes: {
      pastel: 'Pastel',
      ocean: 'Ocean',
      sunset: 'Sunset',
      forest: 'Forest',
      midnight: 'Midnight',
      candy: 'Candy'
    },
    language: {
      en: 'English',
      zh: '中文'
    },
    project: {
      code: 'Code',
      demo: 'Demo'
    },
    content: {
      description: 'A curated selection of projects built using AI programming assistants.',
      loading: 'Loading projects...',
      error: 'Error loading projects',
      errorHint: 'Please ensure the backend server is running.',
      noProjects: 'No projects found.',
      footer: 'Crafted with AI assistance'
    },
    dialog: {
      title: 'Add New AI Project',
      description: 'Fill in the details about your AI project',
      labels: {
        title: 'Title',
        description: 'Description',
        category: 'Category',
        date: 'Date (YYYY-MM)',
        technologies: 'Technologies/Tools',
        colorIndex: 'Color Index',
        demoLink: 'Demo Link (Optional)',
        githubLink: 'GitHub Link (Optional)',
        imageFilename: 'Image Filename (Optional)'
      },
      placeholders: {
        title: 'e.g., AI Chat Interface',
        description: 'Brief description of your project...',
        date: 'e.g., 2024-01',
        technologies: 'e.g., React, OpenAI, Python',
        colorIndex: 'e.g., 0, 1, 2...',
        demoLink: 'https://demo.example.com',
        githubLink: 'https://github.com/user/repo',
        imageFilename: 'e.g., project-image.jpg'
      },
      helpers: {
        colorIndex: 'Used for card background colors in the UI',
        imageFilename: 'Place image in /public/images/projects/ folder'
      },
      buttons: {
        addTechnology: 'Add Technology',
        creating: 'Creating...',
        create: 'Create Project',
        cancel: 'Cancel'
      }
    }
  },
  zh: {
    navigation: {
      title: 'AI 作品集',
      add: '添加'
    },
    categories: {
      all: '全部',
      web: '网站',
      mobile: '移动端',
      ai: 'AI/机器学习',
      data: '数据'
    },
    themes: {
      pastel: '柔和',
      ocean: '海洋',
      sunset: '日落',
      forest: '森林',
      midnight: '午夜',
      candy: '糖果'
    },
    language: {
      en: 'English',
      zh: '中文'
    },
    project: {
      code: '代码',
      demo: '演示'
    },
    content: {
      description: '精选使用 AI 编程助手构建的项目集合。',
      loading: '加载中...',
      error: '加载项目失败',
      errorHint: '请确保后端服务器正在运行。',
      noProjects: '暂无项目。',
      footer: '由 AI 辅助创建'
    },
    dialog: {
      title: '添加新的 AI 项目',
      description: '填写您的 AI 项目详情',
      labels: {
        title: '标题',
        description: '描述',
        category: '类别',
        date: '日期 (YYYY-MM)',
        technologies: '技术/工具',
        colorIndex: '颜色索引',
        demoLink: '演示链接 (可选)',
        githubLink: 'GitHub 链接 (可选)',
        imageFilename: '图片文件名 (可选)'
      },
      placeholders: {
        title: '例如：AI 聊天界面',
        description: '项目简要描述...',
        date: '例如：2024-01',
        technologies: '例如：React, OpenAI, Python',
        colorIndex: '例如：0, 1, 2...',
        demoLink: 'https://demo.example.com',
        githubLink: 'https://github.com/user/repo',
        imageFilename: '例如：project-image.jpg'
      },
      helpers: {
        colorIndex: '用于 UI 中卡片背景颜色',
        imageFilename: '请将图片放入 /public/images/projects/ 文件夹'
      },
      buttons: {
        addTechnology: '添加技术',
        creating: '创建中...',
        create: '创建项目',
        cancel: '取消'
      }
    }
  }
}
