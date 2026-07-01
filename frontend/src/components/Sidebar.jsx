import { NavLink } from 'react-router-dom'

const navItems = [
  { to: '/overview',       label: 'Overview',        icon: '📊' },
  { to: '/stale-branches', label: 'Stale Branches',   icon: '🌿' },
  { to: '/deleted',        label: 'Deleted Branches', icon: '🗑️' },
  { to: '/emailed-users',  label: 'Emailed Users',    icon: '📧' },
  { to: '/dashboard',      label: 'Charts',           icon: '📈' },
]

export default function Sidebar() {
  return (
    <aside className="w-64 bg-slate-900 text-white flex flex-col">
      <div className="p-6">
        <h1 className="text-lg font-bold tracking-tight">Branch Cleaner</h1>
        <p className="text-slate-400 text-xs mt-1">Automated stale branch lifecycle</p>
      </div>
      <nav className="flex-1 px-3 space-y-1">
        {navItems.map(item => (
          <NavLink
            key={item.to}
            to={item.to}
            className={({ isActive }) =>
              `flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                isActive ? 'bg-blue-600 text-white' : 'text-slate-400 hover:bg-slate-800'
              }`
            }
          >
            <span>{item.icon}</span>{item.label}
          </NavLink>
        ))}
      </nav>
      <div className="p-4 text-xs text-slate-500 border-t border-slate-800">
        <div>Stale Branch Cleaner v1.0</div>
        <div>Runs every Sunday 02:00 UTC</div>
      </div>
    </aside>
  )
}