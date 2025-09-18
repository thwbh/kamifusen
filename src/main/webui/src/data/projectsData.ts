import { Project } from '../types/project';

export const projectsData: Project[] = [
  {
    id: 'librefit',
    name: 'librefit',
    description: 'Your FOSS calorie tracker!',
    stack: 'Rust, Tauri, TailwindCSS, Sqlite, Diesel ORM',
    githubUrl: 'https://github.com/thwbh/librefit',
    status: 'active'
  },
  {
    id: 'kamifusen',
    name: 'kamifusen',
    description: 'An easy to use page hit counter intended for serverless environments',
    stack: 'Kotlin, Quarkus, HTMX, Postgres, Hibernate Reactive',
    githubUrl: 'https://github.com/thwbh/kamifusen',
    status: 'active'
  },
  {
    id: 'veilchen',
    name: 'veilchen',
    description: 'A set of reusable Svelte components based on DaisyUI',
    stack: 'Svelte, CSS3, TailwindCSS, DaisyUI',
    githubUrl: 'https://github.com/thwbh/veilchen',
    status: 'active'
  },
  {
    id: 'tauri-typegen',
    name: 'tauri-typegen',
    description: 'Automatically generate TypeScript models and bindings from your tauri commands',
    stack: 'Rust',
    githubUrl: 'https://github.com/thwbh/tauri-typegen',
    status: 'active'
  }
];
