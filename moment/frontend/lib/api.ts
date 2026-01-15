const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'

export interface Project {
  id: number
  title: string
  description: string
  category: 'web' | 'mobile' | 'ai' | 'data'
  date: string
  year: number
  tools: string[]
  color: string
  link?: string
  github?: string
  imageUrl?: string
}

export interface ProjectCreateRequest {
  title: string
  description: string
  category: 'web' | 'mobile' | 'ai' | 'data'
  date: string
  tools: string[]
  color: string
  link?: string
  github?: string
  imageUrl?: string
}

export async function fetchProjects(category?: string): Promise<Project[]> {
  const url = category && category !== 'all'
    ? `${API_BASE_URL}/api/projects?category=${category}`
    : `${API_BASE_URL}/api/projects`

  const response = await fetch(url)
  
  if (!response.ok) {
    throw new Error(`Failed to fetch projects: ${response.status}`)
  }
  
  return response.json()
}

export async function fetchProjectById(id: number): Promise<Project> {
  const response = await fetch(`${API_BASE_URL}/api/projects/${id}`)
  
  if (!response.ok) {
    throw new Error(`Failed to fetch project: ${response.status}`)
  }
  
  return response.json()
}

export async function createProject(project: ProjectCreateRequest): Promise<Project> {
  const response = await fetch(`${API_BASE_URL}/api/projects`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(project),
  })
  
  if (!response.ok) {
    throw new Error(`Failed to create project: ${response.status}`)
  }
  
  return response.json()
}
