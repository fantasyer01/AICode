'use client'

import { useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { X, Plus, Minus } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { createProject, type ProjectCreateRequest } from '@/lib/api'
import { useI18n } from '@/lib/i18n-context'

interface AddProjectDialogProps {
  isOpen: boolean
  onClose: () => void
  onSuccess: () => void
}

export function AddProjectDialog({ isOpen, onClose, onSuccess }: AddProjectDialogProps) {
  const { t } = useI18n()
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  
  // This holds the list of technologies/tools that the user is adding
  const [tools, setTools] = useState<string[]>([''])
  
  const [formData, setFormData] = useState<ProjectCreateRequest>({
    title: '',
    description: '',
    category: 'ai',
    date: '',
    tools: [],
    color: '0',
    link: '',
    github: '',
    imageUrl: ''
  })

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)
    setIsSubmitting(true)

    try {
      // Filter out empty tool entries and update formData
      const filteredTools = tools.filter(tool => tool.trim() !== '')
      
      const projectData: ProjectCreateRequest = {
        ...formData,
        tools: filteredTools
      }

      await createProject(projectData)
      
      // Reset form
      setFormData({
        title: '',
        description: '',
        category: 'ai',
        date: '',
        tools: [],
        color: '0',
        link: '',
        github: '',
        imageUrl: ''
      })
      setTools([''])
      
      onSuccess()
      onClose()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to create project')
    } finally {
      setIsSubmitting(false)
    }
  }

  const addToolField = () => {
    setTools([...tools, ''])
  }

  const removeToolField = (index: number) => {
    if (tools.length > 1) {
      setTools(tools.filter((_, i) => i !== index))
    }
  }

  const updateTool = (index: number, value: string) => {
    const newTools = [...tools]
    newTools[index] = value
    setTools(newTools)
  }

  if (!isOpen) return null

  return (
    <AnimatePresence>
      <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
        {/* Backdrop - this is a semi-transparent overlay behind the dialog */}
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          onClick={onClose}
          className="absolute inset-0 bg-black/50 backdrop-blur-sm"
        />
        
        {/* Dialog Card */}
        <motion.div
          initial={{ opacity: 0, scale: 0.95, y: 20 }}
          animate={{ opacity: 1, scale: 1, y: 0 }}
          exit={{ opacity: 0, scale: 0.95, y: 20 }}
          className="relative w-full max-w-2xl max-h-[90vh] overflow-y-auto"
        >
          <Card>
            <CardHeader>
              <div className="flex items-start justify-between">
                <div>
                  <CardTitle>{t.dialog.title}</CardTitle>
                  <CardDescription>
                    {t.dialog.description}
                  </CardDescription>
                </div>
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={onClose}
                  className="h-8 w-8 p-0"
                >
                  <X className="h-4 w-4" />
                </Button>
              </div>
            </CardHeader>
            
            <CardContent>
              <form onSubmit={handleSubmit} className="space-y-4">
                {/* Title field - the name of the project */}
                <div>
                  <label className="block text-sm font-medium mb-1.5">
                    {t.dialog.labels.title} <span className="text-destructive">*</span>
                  </label>
                  <input
                    type="text"
                    required
                    maxLength={255}
                    value={formData.title}
                    onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                    className="w-full px-3 py-2 border border-input rounded-lg bg-background focus:outline-none focus:ring-2 focus:ring-primary"
                    placeholder={t.dialog.placeholders.title}
                  />
                </div>

                {/* Description field - what the project does */}
                <div>
                  <label className="block text-sm font-medium mb-1.5">
                    {t.dialog.labels.description} <span className="text-destructive">*</span>
                  </label>
                  <textarea
                    required
                    maxLength={2000}
                    rows={3}
                    value={formData.description}
                    onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                    className="w-full px-3 py-2 border border-input rounded-lg bg-background focus:outline-none focus:ring-2 focus:ring-primary resize-none"
                    placeholder={t.dialog.placeholders.description}
                  />
                </div>

                {/* Category dropdown - type of project */}
                <div>
                  <label className="block text-sm font-medium mb-1.5">
                    {t.dialog.labels.category} <span className="text-destructive">*</span>
                  </label>
                  <select
                    required
                    value={formData.category}
                    onChange={(e) => setFormData({ ...formData, category: e.target.value as any })}
                    className="w-full px-3 py-2 border border-input rounded-lg bg-background focus:outline-none focus:ring-2 focus:ring-primary"
                  >
                    <option value="web">{t.categories.web}</option>
                    <option value="mobile">{t.categories.mobile}</option>
                    <option value="ai">{t.categories.ai}</option>
                    <option value="data">{t.categories.data}</option>
                  </select>
                </div>

                {/* Date field - when the project was created (format: YYYY-MM) */}
                <div>
                  <label className="block text-sm font-medium mb-1.5">
                    {t.dialog.labels.date} <span className="text-destructive">*</span>
                  </label>
                  <input
                    type="text"
                    required
                    pattern="\d{4}-(0[1-9]|1[0-2])"
                    value={formData.date}
                    onChange={(e) => setFormData({ ...formData, date: e.target.value })}
                    className="w-full px-3 py-2 border border-input rounded-lg bg-background focus:outline-none focus:ring-2 focus:ring-primary"
                    placeholder={t.dialog.placeholders.date}
                  />
                </div>

                {/* Tools/Technologies - list of technologies used */}
                <div>
                  <label className="block text-sm font-medium mb-1.5">
                    {t.dialog.labels.technologies} <span className="text-destructive">*</span>
                  </label>
                  <div className="space-y-2">
                    {tools.map((tool, index) => (
                      <div key={index} className="flex gap-2">
                        <input
                          type="text"
                          value={tool}
                          onChange={(e) => updateTool(index, e.target.value)}
                          className="flex-1 px-3 py-2 border border-input rounded-lg bg-background focus:outline-none focus:ring-2 focus:ring-primary"
                          placeholder={t.dialog.placeholders.technologies}
                        />
                        {tools.length > 1 && (
                          <Button
                            type="button"
                            variant="outline"
                            size="sm"
                            onClick={() => removeToolField(index)}
                            className="px-2"
                          >
                            <Minus className="h-4 w-4" />
                          </Button>
                        )}
                      </div>
                    ))}
                    <Button
                      type="button"
                      variant="outline"
                      size="sm"
                      onClick={addToolField}
                      className="w-full"
                    >
                      <Plus className="h-4 w-4 mr-1" />
                      {t.dialog.buttons.addTechnology}
                    </Button>
                  </div>
                </div>

                {/* Color index - used for UI display colors */}
                <div>
                  <label className="block text-sm font-medium mb-1.5">
                    {t.dialog.labels.colorIndex} <span className="text-destructive">*</span>
                  </label>
                  <input
                    type="text"
                    required
                    maxLength={10}
                    value={formData.color}
                    onChange={(e) => setFormData({ ...formData, color: e.target.value })}
                    className="w-full px-3 py-2 border border-input rounded-lg bg-background focus:outline-none focus:ring-2 focus:ring-primary"
                    placeholder={t.dialog.placeholders.colorIndex}
                  />
                  <p className="text-xs text-muted-foreground mt-1">
                    {t.dialog.helpers.colorIndex}
                  </p>
                </div>

                {/* Optional fields */}
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  {/* Demo link */}
                  <div>
                    <label className="block text-sm font-medium mb-1.5">
                      {t.dialog.labels.demoLink}
                    </label>
                    <input
                      type="url"
                      maxLength={500}
                      value={formData.link}
                      onChange={(e) => setFormData({ ...formData, link: e.target.value })}
                      className="w-full px-3 py-2 border border-input rounded-lg bg-background focus:outline-none focus:ring-2 focus:ring-primary"
                      placeholder={t.dialog.placeholders.demoLink}
                    />
                  </div>

                  {/* GitHub repository link */}
                  <div>
                    <label className="block text-sm font-medium mb-1.5">
                      {t.dialog.labels.githubLink}
                    </label>
                    <input
                      type="url"
                      maxLength={500}
                      value={formData.github}
                      onChange={(e) => setFormData({ ...formData, github: e.target.value })}
                      className="w-full px-3 py-2 border border-input rounded-lg bg-background focus:outline-none focus:ring-2 focus:ring-primary"
                      placeholder={t.dialog.placeholders.githubLink}
                    />
                  </div>
                </div>

                {/* Image filename */}
                <div>
                  <label className="block text-sm font-medium mb-1.5">
                    {t.dialog.labels.imageFilename}
                  </label>
                  <input
                    type="text"
                    maxLength={500}
                    value={formData.imageUrl}
                    onChange={(e) => setFormData({ ...formData, imageUrl: e.target.value })}
                    className="w-full px-3 py-2 border border-input rounded-lg bg-background focus:outline-none focus:ring-2 focus:ring-primary"
                    placeholder={t.dialog.placeholders.imageFilename}
                  />
                  <p className="text-xs text-muted-foreground mt-1">
                    {t.dialog.helpers.imageFilename}
                  </p>
                </div>

                {/* Error message display */}
                {error && (
                  <div className="p-3 bg-destructive/10 border border-destructive/20 rounded-lg text-sm text-destructive">
                    {error}
                  </div>
                )}

                {/* Submit and Cancel buttons */}
                <div className="flex gap-3 pt-4">
                  <Button
                    type="submit"
                    disabled={isSubmitting}
                    className="flex-1"
                  >
                    {isSubmitting ? t.dialog.buttons.creating : t.dialog.buttons.create}
                  </Button>
                  <Button
                    type="button"
                    variant="outline"
                    onClick={onClose}
                    disabled={isSubmitting}
                  >
                    {t.dialog.buttons.cancel}
                  </Button>
                </div>
              </form>
            </CardContent>
          </Card>
        </motion.div>
      </div>
    </AnimatePresence>
  )
}
