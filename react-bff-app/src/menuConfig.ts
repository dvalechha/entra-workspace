export interface MenuItem {
  id: string
  role: string
  label: string
  port: number
}

export const menuConfig: MenuItem[] = [
  {
    id: 'metrics',
    role: 'role.alpha',
    label: 'Metrics',
    port: 5177,
  },
  {
    id: 'analytics',
    role: 'role.beta',
    label: 'Analytics',
    port: 5178,
  },
]
