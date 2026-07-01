import { useState } from 'react'

export default function RefreshButton({ onRefresh }) {
  const [spinning, setSpinning] = useState(false)
  const handleClick = async () => {
    if (onRefresh) {
      setSpinning(true)
      try { await onRefresh() } finally { setSpinning(false) }
    }
  }
  return (
    <button
      onClick={handleClick}
      className="inline-flex items-center gap-2 px-4 py-2 bg-white border border-slate-200 rounded-lg text-sm font-medium text-slate-700 hover:bg-slate-50 transition-colors"
    >
      <span className={spinning ? 'animate-spin' : ''}>↻</span>
      Refresh
    </button>
  )
}