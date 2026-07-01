import { clsx } from 'clsx'
import SearchBar from './SearchBar'

export { SearchBar }

// Status badge colors
const statusColor = {
  PENDING:       'bg-slate-200 text-slate-700',
  EMAIL_SENT:    'bg-blue-100 text-blue-800',
  APPROVED:      'bg-amber-100 text-amber-800',
  DENIED:        'bg-green-100 text-green-800',
  REMINDER_SENT: 'bg-orange-100 text-orange-800',
  DELETED:       'bg-red-100 text-red-800',
  FAILED:        'bg-rose-100 text-rose-800',
}

export function StatCard({ label, value, icon, colorClass }) {
  return (
    <div className={clsx("card flex items-center gap-4", colorClass)}>
      {icon && <span className="text-2xl">{icon}</span>}
      <div>
        <div className="text-3xl font-bold">{value ?? '-'}</div>
        <div className="text-sm text-slate-500">{label}</div>
      </div>
    </div>
  )
}

export function StatusBadge({ status }) {
  return (
    <span className={clsx('badge', statusColor[status] || 'bg-gray-100 text-gray-600')}>
      {status}
    </span>
  )
}

export function Table({ columns, children, empty }) {
  return (
    <div className="overflow-x-auto rounded-lg border border-slate-200">
      <table className="min-w-full divide-y divide-slate-200">
        <thead className="bg-slate-50">
          <tr>
            {columns.map(col => (
              <th key={col} className="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase tracking-wider">{col}</th>
            ))}
          </tr>
        </thead>
        <tbody className="bg-white divide-y divide-slate-100">
          {children}
          {(!children || children.length === 0) && (
            <tr><td colSpan={columns.length} className="px-4 py-8 text-center text-slate-400">{empty || 'No data'}</td></tr>
          )}
        </tbody>
      </table>
    </div>
  )
}

export function LoadingBox({ message = 'Loading...' }) {
  return (
    <div className="flex items-center justify-center py-20 text-slate-400 gap-2">
      <svg className="animate-spin h-5 w-5" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v4a4 4 0 00-4 4H4z" />
      </svg>
      <span>{message}</span>
    </div>
  )
}

export function ErrorBox({ message, onRetry }) {
  return (
    <div className="bg-red-50 border border-red-200 rounded-xl p-6 text-center">
      <span className="text-2xl">⚠️</span>
      <p className="text-red-700 mt-2">{message}</p>
      {onRetry && <button onClick={onRetry} className="mt-3 text-sm bg-red-600 text-white px-4 py-2 rounded-lg hover:bg-red-700">Retry</button>}
    </div>
  )
}

export function Tabs({ tabs, active, onChange }) {
  return (
    <nav className="flex space-x-1 overflow-x-auto pb-1">
      {tabs.map(tab => (
        <button
          key={tab.id}
          onClick={() => onChange(tab.id)}
          className={clsx(
            'px-4 py-2 text-sm font-medium rounded-lg whitespace-nowrap transition-colors',
            active === tab.id ? 'bg-blue-600 text-white' : 'text-slate-600 hover:bg-slate-100'
          )}
        >
          {tab.label}
          {tab.count !== undefined && <span className="ml-2 opacity-75">({tab.count})</span>}
        </button>
      ))}
    </nav>
  )
}

export function MonoPill({ children, maxLen = 32 }) {
  const text = typeof children === 'string' ? children : ''
  return (
    <code className="inline-block bg-slate-100 text-slate-800 px-2 py-0.5 rounded text-sm font-mono truncate" style={{maxWidth: '15rem'}}>
      {text.length > maxLen ? text.substring(0, maxLen) + '...' : text}
    </code>
  )
}

export function RelativeTime({ iso }) {
  if (!iso) return null
  const now = new Date()
  const date = new Date(iso)
  const diffMs = now - date
  const diffSec = Math.floor(diffMs / 1000)
  const diffMin = Math.floor(diffSec / 60)
  const diffHr  = Math.floor(diffMin / 60)
  const diffDay = Math.floor(diffHr / 24)
  const diffWeek = Math.floor(diffDay / 7)
  const diffMonth = Math.floor(diffDay / 30)
  const diffYear = Math.floor(diffDay / 365)
  let value
  if (diffSec < 60) value = 'just now'
  else if (diffMin < 60) value = `${diffMin}m ago`
  else if (diffHr < 24) value = `${diffHr}h ago`
  else if (diffDay < 7) value = `${diffDay}d ago`
  else if (diffWeek < 4) value = `${diffWeek}w ago`
  else if (diffMonth < 12) value = `${diffMonth}mo ago`
  else value = `${diffYear}y ago`
  const full = date.toLocaleString()
  return <span title={full}>{value}</span>
}