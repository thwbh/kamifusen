export interface Project {
  id: string;
  name: string;
  description: string;
  stack: string;
  githubUrl: string;
  status?: 'active' | 'archived' | 'beta';
}