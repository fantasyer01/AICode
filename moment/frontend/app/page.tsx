'use client'

import { useState, useEffect, useRef } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import Image from 'next/image'
import { 
  Grid3X3, 
  Clock, 
  Sparkles, 
  Github, 
  ExternalLink, 
  Filter,
  Layers,
  Palette,
  Plus,
  Globe
} from 'lucide-react'
import { Button, buttonVariants } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { cn } from '@/lib/utils'
import { fetchProjects, type Project } from '@/lib/api'
import { AddProjectDialog } from '@/components/AddProjectDialog'
import { I18nProvider, useI18n } from '@/lib/i18n-context'
import type { Language } from '@/lib/translations'

// Types
type ViewMode = 'grid' | 'timeline' | 'masonry'
type Category = 'all' | 'web' | 'mobile' | 'ai' | 'data'
type ThemeStyle = 'pastel' | 'ocean' | 'sunset' | 'forest' | 'midnight' | 'candy'

// Theme Definitions
const themes: Record<ThemeStyle, {
  name: string
  colors: {
    primary: string
    secondary: string
    accent: string
    gradient: string
    cardColors: string[]
  }
}> = {
  pastel: {
    name: 'Pastel',
    colors: {
      primary: '262 83% 68%',
      secondary: '168 60% 70%',
      accent: '340 75% 75%',
      gradient: 'from-violet-50 via-sky-50 to-rose-50',
      cardColors: ['bg-violet-100', 'bg-emerald-100', 'bg-rose-100', 'bg-sky-100', 'bg-amber-100']
    }
  },
  ocean: {
    name: 'Ocean',
    colors: {
      primary: '200 85% 50%',
      secondary: '180 70% 45%',
      accent: '220 80% 60%',
      gradient: 'from-cyan-50 via-blue-50 to-indigo-50',
      cardColors: ['bg-cyan-100', 'bg-blue-100', 'bg-indigo-100', 'bg-teal-100', 'bg-sky-100']
    }
  },
  sunset: {
    name: 'Sunset',
    colors: {
      primary: '25 95% 55%',
      secondary: '350 80% 60%',
      accent: '45 95% 55%',
      gradient: 'from-orange-50 via-rose-50 to-amber-50',
      cardColors: ['bg-orange-100', 'bg-rose-100', 'bg-amber-100', 'bg-red-100', 'bg-yellow-100']
    }
  },
  forest: {
    name: 'Forest',
    colors: {
      primary: '150 60% 40%',
      secondary: '120 50% 50%',
      accent: '80 60% 45%',
      gradient: 'from-green-50 via-emerald-50 to-lime-50',
      cardColors: ['bg-green-100', 'bg-emerald-100', 'bg-lime-100', 'bg-teal-100', 'bg-cyan-100']
    }
  },
  midnight: {
    name: 'Midnight',
    colors: {
      primary: '250 70% 60%',
      secondary: '280 60% 55%',
      accent: '220 80% 65%',
      gradient: 'from-slate-100 via-purple-50 to-slate-100',
      cardColors: ['bg-slate-100', 'bg-purple-100', 'bg-indigo-100', 'bg-violet-100', 'bg-blue-100']
    }
  },
  candy: {
    name: 'Candy',
    colors: {
      primary: '330 85% 60%',
      secondary: '280 75% 65%',
      accent: '200 85% 60%',
      gradient: 'from-pink-50 via-purple-50 to-cyan-50',
      cardColors: ['bg-pink-100', 'bg-purple-100', 'bg-cyan-100', 'bg-fuchsia-100', 'bg-violet-100']
    }
  }
}

const categories: { value: Category; label: string }[] = [
  { value: 'all', label: 'All' },
  { value: 'web', label: 'Web' },
  { value: 'mobile', label: 'Mobile' },
  { value: 'ai', label: 'AI/ML' },
  { value: 'data', label: 'Data' }
]

// Animation Variants
const containerVariants = {
  hidden: { opacity: 0 },
  visible: {
    opacity: 1,
    transition: { staggerChildren: 0.08 }
  }
}

const itemVariants = {
  hidden: { opacity: 0, y: 16 },
  visible: { opacity: 1, y: 0 }
}

// Components
function ThemeSelector({ 
  currentTheme, 
  setTheme,
  themeNames
}: { 
  currentTheme: ThemeStyle
  setTheme: (theme: ThemeStyle) => void
  themeNames: Record<ThemeStyle, string>
}) {
  const [isOpen, setIsOpen] = useState(false)
  
  const themePreviewColors: Record<ThemeStyle, string> = {
    pastel: 'bg-gradient-to-r from-violet-400 via-rose-400 to-emerald-400',
    ocean: 'bg-gradient-to-r from-cyan-400 via-blue-500 to-indigo-500',
    sunset: 'bg-gradient-to-r from-orange-400 via-rose-500 to-amber-400',
    forest: 'bg-gradient-to-r from-green-500 via-emerald-500 to-lime-500',
    midnight: 'bg-gradient-to-r from-slate-500 via-purple-500 to-indigo-500',
    candy: 'bg-gradient-to-r from-pink-400 via-purple-500 to-cyan-400'
  }
  
  return (
    <div className="relative">
      <Button 
        variant="ghost" 
        size="sm" 
        onClick={() => setIsOpen(!isOpen)}
        className="gap-2"
      >
        <Palette className="w-4 h-4" />
        <span className="hidden sm:inline">{themeNames[currentTheme]}</span>
      </Button>
      
      <AnimatePresence>
        {isOpen && (
          <>
            <div 
              className="fixed inset-0 z-40" 
              onClick={() => setIsOpen(false)} 
            />
            <motion.div
              initial={{ opacity: 0, scale: 0.95, y: -8 }}
              animate={{ opacity: 1, scale: 1, y: 0 }}
              exit={{ opacity: 0, scale: 0.95, y: -8 }}
              className="absolute right-0 top-full mt-2 z-50 bg-card border border-border rounded-xl shadow-lg p-2 min-w-[160px]"
            >
              {(Object.keys(themes) as ThemeStyle[]).map((theme) => (
                <button
                  key={theme}
                  onClick={() => {
                    setTheme(theme)
                    setIsOpen(false)
                  }}
                  className={cn(
                    "w-full flex items-center gap-3 px-3 py-2 rounded-lg text-sm transition-colors",
                    currentTheme === theme 
                      ? "bg-accent text-accent-foreground" 
                      : "hover:bg-muted"
                  )}
                >
                  <div className={cn("w-5 h-5 rounded-full", themePreviewColors[theme])} />
                  <span>{themeNames[theme]}</span>
                </button>
              ))}
            </motion.div>
          </>
        )}
      </AnimatePresence>
    </div>
  )
}

function LanguageSelector({ 
  language, 
  setLanguage,
  languageLabels
}: { 
  language: Language
  setLanguage: (lang: Language) => void
  languageLabels: { en: string; zh: string }
}) {
  const [isOpen, setIsOpen] = useState(false)
  
  const languages: { code: Language; label: string }[] = [
    { code: 'en', label: languageLabels.en },
    { code: 'zh', label: languageLabels.zh }
  ]
  
  return (
    <div className="relative">
      <Button 
        variant="ghost" 
        size="sm" 
        onClick={() => setIsOpen(!isOpen)}
        className="gap-2"
      >
        <Globe className="w-4 h-4" />
        <span className="hidden sm:inline">{languages.find(l => l.code === language)?.label}</span>
      </Button>
      
      <AnimatePresence>
        {isOpen && (
          <>
            <div 
              className="fixed inset-0 z-40" 
              onClick={() => setIsOpen(false)} 
            />
            <motion.div
              initial={{ opacity: 0, scale: 0.95, y: -8 }}
              animate={{ opacity: 1, scale: 1, y: 0 }}
              exit={{ opacity: 0, scale: 0.95, y: -8 }}
              className="absolute right-0 top-full mt-2 z-50 bg-card border border-border rounded-xl shadow-lg p-2 min-w-[120px]"
            >
              {languages.map((lang) => (
                <button
                  key={lang.code}
                  onClick={() => {
                    setLanguage(lang.code)
                    setIsOpen(false)
                  }}
                  className={cn(
                    "w-full flex items-center gap-3 px-3 py-2 rounded-lg text-sm transition-colors",
                    language === lang.code 
                      ? "bg-accent text-accent-foreground" 
                      : "hover:bg-muted"
                  )}
                >
                  <span>{lang.label}</span>
                </button>
              ))}
            </motion.div>
          </>
        )}
      </AnimatePresence>
    </div>
  )
}

function Navigation({ 
  viewMode, 
  setViewMode,
  currentTheme,
  setTheme,
  onAddProject,
  language,
  setLanguage,
  translations
}: { 
  viewMode: ViewMode
  setViewMode: (mode: ViewMode) => void
  currentTheme: ThemeStyle
  setTheme: (theme: ThemeStyle) => void
  onAddProject: () => void
  language: Language
  setLanguage: (lang: Language) => void
  translations: {
    title: string
    add: string
    themeNames: Record<ThemeStyle, string>
    languageLabels: { en: string; zh: string }
  }
}) {
  return (
    <motion.header 
      initial={{ y: -20, opacity: 0 }}
      animate={{ y: 0, opacity: 1 }}
      className="px-4 py-4"
    >
      <nav className="max-w-6xl mx-auto bg-card/80 backdrop-blur-xl rounded-2xl border border-border/50 px-4 py-2.5">
        <div className="flex items-center justify-between gap-2">
          <motion.div 
            className="flex items-center gap-2"
            whileHover={{ scale: 1.02 }}
          >
            <Sparkles className="w-5 h-5 text-primary" />
            <span className="font-semibold text-foreground hidden sm:inline">{translations.title}</span>
          </motion.div>

          {/* View Mode Toggle */}
          <div className="flex items-center gap-1 bg-muted rounded-lg p-1">
            {[
              { mode: 'grid' as ViewMode, icon: Grid3X3 },
              { mode: 'timeline' as ViewMode, icon: Clock },
              { mode: 'masonry' as ViewMode, icon: Layers }
            ].map(({ mode, icon: Icon }) => (
              <Button
                key={mode}
                variant={viewMode === mode ? "default" : "ghost"}
                size="sm"
                onClick={() => setViewMode(mode)}
                className="px-2.5"
              >
                <Icon className="w-4 h-4" />
              </Button>
            ))}
          </div>
          
          {/* Add Project, Theme Selector, and Language Selector */}
          <div className="flex items-center gap-2">
            <Button 
              variant="ghost" 
              size="sm" 
              onClick={onAddProject}
              className="gap-2"
            >
              <Plus className="w-4 h-4" />
              <span className="hidden sm:inline">{translations.add}</span>
            </Button>
            <ThemeSelector 
              currentTheme={currentTheme} 
              setTheme={setTheme} 
              themeNames={translations.themeNames}
            />
            <LanguageSelector 
              language={language} 
              setLanguage={setLanguage}
              languageLabels={translations.languageLabels}
            />
          </div>
        </div>
      </nav>
    </motion.header>
  )
}

function FilterBar({ 
  selectedCategory, 
  setSelectedCategory,
  categoryLabels
}: { 
  selectedCategory: Category
  setSelectedCategory: (cat: Category) => void
  categoryLabels: Record<Category, string>
}) {
  return (
    <motion.div 
      initial={{ opacity: 0, y: 16 }}
      animate={{ opacity: 1, y: 0 }}
      className="flex flex-wrap items-center justify-center gap-2 mb-8"
    >
      <Filter className="w-4 h-4 text-muted-foreground" />
      {categories.map((cat) => (
        <Button
          key={cat.value}
          variant={selectedCategory === cat.value ? "default" : "outline"}
          size="sm"
          onClick={() => setSelectedCategory(cat.value)}
        >
          {categoryLabels[cat.value]}
        </Button>
      ))}
    </motion.div>
  )
}

function ProjectCard({ 
  project, 
  cardColors,
  buttonLabels 
}: { 
  project: Project
  cardColors: string[]
  buttonLabels: { code: string; demo: string }
}) {
  const colorIndex = parseInt(project.color) % cardColors.length
  
  return (
    <motion.div
      variants={itemVariants}
      layout
      className="group"
    >
      <Card 
        className="overflow-hidden h-full transition-all duration-300 hover:shadow-lg hover:-translate-y-1"
      >
        <div className={cn(
          "h-44 flex items-center justify-center transition-colors relative overflow-hidden",
          cardColors[colorIndex]
        )}>
          {project.imageUrl ? (
            <Image
              src={`/images/projects/${project.imageUrl}`}
              alt={project.title}
              fill
              className="object-cover transition-transform duration-500 group-hover:scale-105"
              sizes="(max-width: 768px) 100vw, (max-width: 1200px) 50vw, 33vw"
            />
          ) : (
            <Sparkles className="w-10 h-10 text-foreground/30" />
          )}
        </div>
        <CardHeader className="p-4 pb-2">
          <div className="flex items-start justify-between gap-2 mb-1">
            <CardTitle className="text-base font-bold leading-tight line-clamp-1">{project.title}</CardTitle>
            <span className="text-[10px] font-medium text-muted-foreground bg-muted px-1.5 py-0.5 rounded shrink-0">
              {project.date}
            </span>
          </div>
          <CardDescription className="line-clamp-2 text-xs leading-relaxed min-h-[2.5rem]">
            {project.description}
          </CardDescription>
        </CardHeader>
        <CardContent className="p-4 pt-0 space-y-3">
          <div className="flex flex-wrap gap-1">
            {project.tools.slice(0, 4).map((tool) => (
              <span 
                key={tool}
                className="text-[10px] bg-primary/5 text-primary border border-primary/10 px-1.5 py-0.5 rounded-md"
              >
                {tool}
              </span>
            ))}
            {project.tools.length > 4 && (
              <span className="text-[10px] text-muted-foreground self-center ml-1">
                +{project.tools.length - 4}
              </span>
            )}
          </div>
          <div className="flex gap-2 pt-1">
            <a 
              href={project.github || "#"} 
              target={project.github ? "_blank" : undefined}
              rel="noopener noreferrer"
              className={cn(
                buttonVariants({ variant: "outline", size: "sm" }), 
                "flex-1 gap-1.5 h-8 text-[11px] font-medium",
                !project.github && "opacity-40 cursor-not-allowed pointer-events-none"
              )}
            >
              <Github className="w-3.5 h-3.5" />
              {buttonLabels.code}
            </a>
            <a 
              href={project.link || "#"} 
              target={project.link ? "_blank" : undefined}
              rel="noopener noreferrer"
              className={cn(
                buttonVariants({ variant: "outline", size: "sm" }), 
                "flex-1 gap-1.5 h-8 text-[11px] font-medium",
                !project.link && "opacity-40 cursor-not-allowed pointer-events-none"
              )}
            >
              <ExternalLink className="w-3.5 h-3.5" />
              {buttonLabels.demo}
            </a>
          </div>
        </CardContent>
      </Card>
    </motion.div>
  )
}

function GridView({ 
  projects, 
  cardColors,
  buttonLabels 
}: { 
  projects: Project[]
  cardColors: string[]
  buttonLabels: { code: string; demo: string }
}) {
  return (
    <motion.div
      variants={containerVariants}
      initial="hidden"
      animate="visible"
      className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5"
    >
      {projects.map((project) => (
        <ProjectCard 
          key={project.id} 
          project={project} 
          cardColors={cardColors}
          buttonLabels={buttonLabels}
        />
      ))}
    </motion.div>
  )
}

function TimelineView({ 
  projects, 
  cardColors,
  buttonLabels 
}: { 
  projects: Project[]
  cardColors: string[] 
  buttonLabels: { code: string; demo: string }
}) {
  const sortedProjects = [...projects].sort((a, b) => 
    new Date(b.date).getTime() - new Date(a.date).getTime()
  )
  
  const groupedByYear = sortedProjects.reduce((acc, project) => {
    if (!acc[project.year]) acc[project.year] = []
    acc[project.year].push(project)
    return acc
  }, {} as Record<number, Project[]>)

  return (
    <motion.div
      variants={containerVariants}
      initial="hidden"
      animate="visible"
      className="max-w-3xl mx-auto"
    >
      {Object.entries(groupedByYear)
        .sort(([a], [b]) => Number(b) - Number(a))
        .map(([year, yearProjects]) => (
          <motion.div key={year} variants={itemVariants} className="mb-10">
            <div className="flex items-center gap-4 mb-5">
              <span className="text-2xl font-bold text-primary">{year}</span>
              <div className="flex-1 h-px bg-border" />
            </div>
            <div className="space-y-4 relative">
              <div className="absolute left-3 top-0 bottom-0 w-px bg-gradient-to-b from-primary/40 to-transparent" />
              {yearProjects.map((project) => {
                const colorIndex = parseInt(project.color) % cardColors.length
                return (
                  <motion.div 
                    key={project.id}
                    className="relative pl-10"
                    whileHover={{ x: 4 }}
                  >
                    <div className={cn(
                      "absolute left-1 top-4 w-4 h-4 rounded-full border-2 border-card",
                      cardColors[colorIndex]
                    )} />
                    <Card className="transition-shadow hover:shadow-md">
                      <CardHeader className="pb-2 py-4">
                        <div className="flex items-center justify-between">
                          <CardTitle className="text-sm font-medium">{project.title}</CardTitle>
                          <span className="text-xs text-muted-foreground">{project.date}</span>
                        </div>
                        <CardDescription className="text-sm">{project.description}</CardDescription>
                      </CardHeader>
                      <CardContent className="pt-0 pb-4">
                        <div className="flex flex-wrap items-center justify-between gap-3">
                          <div className="flex flex-wrap gap-1">
                            {project.tools.map((tool) => (
                              <span 
                                key={tool}
                                className="text-[10px] bg-primary/5 text-primary border border-primary/10 px-1.5 py-0.5 rounded"
                              >
                                {tool}
                              </span>
                            ))}
                          </div>
                          <div className="flex gap-2">
                            <a 
                              href={project.github || "#"} 
                              target={project.github ? "_blank" : undefined}
                              rel="noopener noreferrer"
                              className={cn(
                                buttonVariants({ variant: "ghost", size: "sm" }), 
                                "gap-1.5 h-7 text-[10px] px-2",
                                !project.github && "opacity-30 cursor-not-allowed pointer-events-none"
                              )}
                            >
                              <Github className="w-3 h-3" />
                              {buttonLabels.code}
                            </a>
                            <a 
                              href={project.link || "#"} 
                              target={project.link ? "_blank" : undefined}
                              rel="noopener noreferrer"
                              className={cn(
                                buttonVariants({ variant: "ghost", size: "sm" }), 
                                "gap-1.5 h-7 text-[10px] px-2",
                                !project.link && "opacity-30 cursor-not-allowed pointer-events-none"
                              )}
                            >
                              <ExternalLink className="w-3 h-3" />
                              {buttonLabels.demo}
                            </a>
                          </div>
                        </div>
                      </CardContent>
                    </Card>
                  </motion.div>
                )
              })}
            </div>
          </motion.div>
        ))}
    </motion.div>
  )
}

function MasonryView({ projects, cardColors }: { projects: Project[]; cardColors: string[] }) {
  const heights = ['h-56', 'h-72', 'h-64', 'h-80', 'h-56', 'h-72']
  
  return (
    <motion.div
      variants={containerVariants}
      initial="hidden"
      animate="visible"
      className="columns-1 md:columns-2 lg:columns-3 gap-5 space-y-5"
    >
      {projects.map((project, index) => {
        const colorIndex = parseInt(project.color) % cardColors.length
        return (
          <motion.div
            key={project.id}
            variants={itemVariants}
            layout
            className="break-inside-avoid"
          >
            <Card className={cn(
              "overflow-hidden transition-all duration-300 hover:shadow-lg",
              heights[index % heights.length]
            )}>
              <div className={cn(
                "h-1/2 flex items-center justify-center relative overflow-hidden",
                cardColors[colorIndex]
              )}>
                {project.imageUrl ? (
                  <Image
                    src={`/images/projects/${project.imageUrl}`}
                    alt={project.title}
                    fill
                    className="object-cover"
                    sizes="(max-width: 768px) 100vw, (max-width: 1200px) 50vw, 33vw"
                  />
                ) : (
                  <Sparkles className="w-10 h-10 text-foreground/30" />
                )}
              </div>
              <CardHeader className="pb-2 py-3">
                <CardTitle className="text-sm font-medium">{project.title}</CardTitle>
                <CardDescription className="text-xs line-clamp-2">
                  {project.description}
                </CardDescription>
              </CardHeader>
              <CardContent className="pt-0">
                <div className="flex flex-wrap gap-1">
                  {project.tools.slice(0, 2).map((tool) => (
                    <span 
                      key={tool}
                      className="text-xs bg-muted text-muted-foreground px-2 py-0.5 rounded"
                    >
                      {tool}
                    </span>
                  ))}
                  {project.tools.length > 2 && (
                    <span className="text-xs text-muted-foreground">
                      +{project.tools.length - 2}
                    </span>
                  )}
                </div>
              </CardContent>
            </Card>
          </motion.div>
        )
      })}
    </motion.div>
  )
}


// Main Page Component
function HomeContent() {
  const { language, setLanguage, t } = useI18n()
  const [viewMode, setViewMode] = useState<ViewMode>('grid')
  const [selectedCategory, setSelectedCategory] = useState<Category>('all')
  const [currentTheme, setCurrentTheme] = useState<ThemeStyle>('pastel')
  const [projects, setProjects] = useState<Project[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [isAddDialogOpen, setIsAddDialogOpen] = useState(false)

  // Fetch projects when category changes
  useEffect(() => {
    loadProjects()
  }, [selectedCategory])

  const loadProjects = () => {
    setIsLoading(true)
    setError(null)
    
    fetchProjects(selectedCategory)
      .then((data) => {
        setProjects(data)
        setIsLoading(false)
      })
      .catch((err) => {
        setError(err.message || 'Failed to load projects')
        setIsLoading(false)
      })
  }

  const theme = themes[currentTheme]
  
  // Apply theme CSS variables
  useEffect(() => {
    document.documentElement.style.setProperty('--primary', theme.colors.primary)
    document.documentElement.style.setProperty('--secondary', theme.colors.secondary)
    document.documentElement.style.setProperty('--accent', theme.colors.accent)
  }, [theme])

  // Prepare category labels for FilterBar
  const categoryLabels: Record<Category, string> = {
    all: t.categories.all,
    web: t.categories.web,
    mobile: t.categories.mobile,
    ai: t.categories.ai,
    data: t.categories.data
  }

  // Prepare theme names for ThemeSelector
  const themeNames: Record<ThemeStyle, string> = {
    pastel: t.themes.pastel,
    ocean: t.themes.ocean,
    sunset: t.themes.sunset,
    forest: t.themes.forest,
    midnight: t.themes.midnight,
    candy: t.themes.candy
  }

  // Button labels for project cards
  const buttonLabels = {
    code: t.project.code,
    demo: t.project.demo
  }

  const renderView = () => {
    const cardColors = theme.colors.cardColors
    switch (viewMode) {
      case 'timeline':
        return <TimelineView projects={projects} cardColors={cardColors} buttonLabels={buttonLabels} />
      case 'masonry':
        return <MasonryView projects={projects} cardColors={cardColors} />
      default:
        return <GridView projects={projects} cardColors={cardColors} buttonLabels={buttonLabels} />
    }
  }

  return (
    <main className={cn("min-h-screen bg-gradient-to-br", theme.colors.gradient)}>
      <Navigation 
        viewMode={viewMode}
        setViewMode={setViewMode}
        currentTheme={currentTheme}
        setTheme={setCurrentTheme}
        onAddProject={() => setIsAddDialogOpen(true)}
        language={language}
        setLanguage={setLanguage}
        translations={{
          title: t.navigation.title,
          add: t.navigation.add,
          themeNames,
          languageLabels: t.language
        }}
      />
      
      <section className="pb-8 px-4">
        <div className="max-w-6xl mx-auto">
          {/* Minimal Header */}
          <motion.div
            initial={{ opacity: 0, y: 16 }}
            animate={{ opacity: 1, y: 0 }}
            className="text-center mb-8"
          >
            <p className="text-muted-foreground max-w-xl mx-auto">
              {t.content.description}
            </p>
          </motion.div>
          
          <FilterBar 
            selectedCategory={selectedCategory}
            setSelectedCategory={setSelectedCategory}
            categoryLabels={categoryLabels}
          />
          
          <AnimatePresence mode="wait">
            <motion.div
              key={viewMode + selectedCategory + currentTheme}
              initial={{ opacity: 0, y: 16 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -16 }}
              transition={{ duration: 0.25 }}
            >
              {isLoading ? (
                <div className="flex items-center justify-center py-20">
                  <div className="text-center">
                    <div className="w-8 h-8 border-4 border-primary border-t-transparent rounded-full animate-spin mx-auto mb-4" />
                    <p className="text-muted-foreground">{t.content.loading}</p>
                  </div>
                </div>
              ) : error ? (
                <div className="flex items-center justify-center py-20">
                  <div className="text-center">
                    <p className="text-destructive mb-2">{t.content.error}</p>
                    <p className="text-muted-foreground text-sm">{t.content.errorHint}</p>
                  </div>
                </div>
              ) : projects.length === 0 ? (
                <div className="flex items-center justify-center py-20">
                  <p className="text-muted-foreground">{t.content.noProjects}</p>
                </div>
              ) : (
                renderView()
              )}
            </motion.div>
          </AnimatePresence>
        </div>
      </section>
      
      {/* Add Project Dialog */}
      <AddProjectDialog 
        isOpen={isAddDialogOpen}
        onClose={() => setIsAddDialogOpen(false)}
        onSuccess={loadProjects}
      />
      
      {/* Minimal Footer */}
      <footer className="py-6 px-4 text-center">
        <p className="text-sm text-muted-foreground">
          {t.content.footer}
        </p>
      </footer>
    </main>
  )
}

export default function Home() {
  return (
    <I18nProvider>
      <HomeContent />
    </I18nProvider>
  )
}
