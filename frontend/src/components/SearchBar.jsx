export default function SearchBar({ value, onChange }) {
  const clear = () => onChange('')
  return (
    <div className="relative max-w-md w-full">
      <span className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400">🔍</span>
      <input
        type="text"
        value={value}
        onChange={e => onChange(e.target.value)}
        placeholder="Search..."
        className="w-full pl-10 pr-10 py-2.5 rounded-lg border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
      />
      {value && (
        <button onClick={clear} className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600">✕</button>
      )}
    </div>
  )
}