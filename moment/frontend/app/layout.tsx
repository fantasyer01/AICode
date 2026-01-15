import type { Metadata } from 'next'
import { Inter } from 'next/font/google'
import './globals.css'

const inter = Inter({ subsets: ['latin'], variable: '--font-inter' })

export const metadata: Metadata = {
  title: 'AI Portfolio | Projects Built with AI Programming Tools',
  description: 'Explore my collection of innovative projects developed using cutting-edge AI programming tools. From web applications to creative solutions.',
  keywords: ['portfolio', 'AI projects', 'web development', 'programming', 'AI tools'],
  authors: [{ name: 'Developer' }],
  openGraph: {
    title: 'AI Portfolio | Projects Built with AI Programming Tools',
    description: 'Explore my collection of innovative projects developed using cutting-edge AI programming tools.',
    type: 'website',
  },
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="en" className={inter.variable}>
      <body className={inter.className}>{children}</body>
    </html>
  )
}
