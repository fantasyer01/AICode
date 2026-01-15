'use client'

import { useState, useEffect } from 'react'
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
  Palette
} from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { cn } from '@/lib/utils'
import { fetchProjects, type Project } from '@/lib/api'

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
  setTheme 
}: { 
  currentTheme: ThemeStyle
  setTheme: (theme: ThemeStyle) => void
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
        <span className="hidden sm:inline">{themes[currentTheme].name}</span>
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
                  <span>{themes[theme].name}</span>
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
  setTheme
}: { 
  viewMode: ViewMode
  setViewMode: (mode: ViewMode) => void
  currentTheme: ThemeStyle
  setTheme: (theme: ThemeStyle) => void
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
            <span className="font-semibold text-foreground hidden sm:inline">AI Portfolio</span>
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
          
          {/* Theme Selector */}
          <ThemeSelector currentTheme={currentTheme} setTheme={setTheme} />
        </div>
      </nav>
    </motion.header>
  )
}

function FilterBar({ 
  selectedCategory, 
  setSelectedCategory 
}: { 
  selectedCategory: Category
  setSelectedCategory: (cat: Category) => void
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
          {cat.label}
        </Button>
      ))}
    </motion.div>
  )
}

function ProjectCard({ project, cardColors }: { project: Project; cardColors: string[] }) {
  const colorIndex = parseInt(project.color) % cardColors.length
  
  return (
    <motion.div
      variants={itemVariants}
      layout
      className="group"
    >
      <Card className="overflow-hidden h-full transition-all duration-300 hover:shadow-lg hover:-translate-y-1">
        <div className={cn(
          "h-36 flex items-center justify-center transition-colors relative overflow-hidden",
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
        <CardHeader className="pb-2">
          <div className="flex items-start justify-between gap-2">
            <CardTitle className="text-base">{project.title}</CardTitle>
            <span className="text-xs text-muted-foreground shrink-0">
              {project.date}
            </span>
          </div>
          <CardDescription className="line-clamp-2 text-sm">
            {project.description}
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-3">
          <div className="flex flex-wrap gap-1">
            {project.tools.map((tool) => (
              <span 
                key={tool}
                className="text-xs bg-muted text-muted-foreground px-2 py-0.5 rounded"
              >
                {tool}
              </span>
            ))}
          </div>
          <div className="flex gap-2">
            {project.github && (
              <Button variant="ghost" size="sm" className="gap-1.5 h-8">
                <Github className="w-3.5 h-3.5" />
                Code
              </Button>
            )}
            {project.link && (
              <Button variant="ghost" size="sm" className="gap-1.5 h-8">
                <ExternalLink className="w-3.5 h-3.5" />
                Demo
              </Button>
            )}
          </div>
        </CardContent>
      </Card>
    </motion.div>
  )
}

function GridView({ projects, cardColors }: { projects: Project[]; cardColors: string[] }) {
  return (
    <motion.div
      variants={containerVariants}
      initial="hidden"
      animate="visible"
      className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5"
    >
      {projects.map((project) => (
        <ProjectCard key={project.id} project={project} cardColors={cardColors} />
      ))}
    </motion.div>
  )
}

function TimelineView({ projects, cardColors }: { projects: Project[]; cardColors: string[] }) {
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
                        <div className="flex flex-wrap gap-1">
                          {project.tools.map((tool) => (
                            <span 
                              key={tool}
                              className="text-xs bg-muted text-muted-foreground px-2 py-0.5 rounded"
                            >
                              {tool}
                            </span>
                          ))}
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
export default function Home() {
  const [viewMode, setViewMode] = useState<ViewMode>('grid')
  const [selectedCategory, setSelectedCategory] = useState<Category>('all')
  const [currentTheme, setCurrentTheme] = useState<ThemeStyle>('pastel')
  const [projects, setProjects] = useState<Project[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  // Fetch projects when category changes
  useEffect(() => {
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
  }, [selectedCategory])

  const theme = themes[currentTheme]
  
  // Apply theme CSS variables
  useEffect(() => {
    document.documentElement.style.setProperty('--primary', theme.colors.primary)
    document.documentElement.style.setProperty('--secondary', theme.colors.secondary)
    document.documentElement.style.setProperty('--accent', theme.colors.accent)
  }, [theme])

  const renderView = () => {
    const cardColors = theme.colors.cardColors
    switch (viewMode) {
      case 'timeline':
        return <TimelineView projects={projects} cardColors={cardColors} />
      case 'masonry':
        return <MasonryView projects={projects} cardColors={cardColors} />
      default:
        return <GridView projects={projects} cardColors={cardColors} />
    }
  }

  return (
    <main className={cn("min-h-screen bg-gradient-to-br", theme.colors.gradient)}>
      <Navigation 
        viewMode={viewMode}
        setViewMode={setViewMode}
        currentTheme={currentTheme}
        setTheme={setCurrentTheme}
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
              A curated selection of projects built using AI programming assistants.
            </p>
          </motion.div>
          
          <FilterBar 
            selectedCategory={selectedCategory}
            setSelectedCategory={setSelectedCategory}
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
                    <p className="text-muted-foreground">Loading projects...</p>
                  </div>
                </div>
              ) : error ? (
                <div className="flex items-center justify-center py-20">
                  <div className="text-center">
                    <p className="text-destructive mb-2">{error}</p>
                    <p className="text-muted-foreground text-sm">Please ensure the backend server is running.</p>
                  </div>
                </div>
              ) : projects.length === 0 ? (
                <div className="flex items-center justify-center py-20">
                  <p className="text-muted-foreground">No projects found.</p>
                </div>
              ) : (
                renderView()
              )}
            </motion.div>
          </AnimatePresence>
        </div>
      </section>
      
      {/* Minimal Footer */}
      <footer className="py-6 px-4 text-center">
        <p className="text-sm text-muted-foreground">
          Crafted with AI assistance
        </p>
      </footer>
    </main>
  )
}
