import { useState, useMemo } from 'react'
import { api } from '../api/dashboard'
import { useFetch } from '../hooks/useFetch'
import { Table, SearchBar, LoadingBox, ErrorBox } from '../components/ui'
import PageHeader from '../components/PageHeader'
import RefreshButton from '../components/RefreshButton'

function AvatarCircle({ name }) {
  const initial = name?.charAt(0)?.toUpperCase() || '?'
  return (
    <div className="w-9 h-9 rounded-full bg-blue-100 flex items-center justify-center text-blue-700 font-medium text-sm">{initial}</div>
  )
}

export default function EmailedUsers() {
  const { data, loading, error, refetch } = useFetch(api.getEmailedUsers)
  const [search, setSearch] = useState('')
  const filtered = useMemo(() => {
    if (!data) return []
    return data.filter(u =>
      u.email.toLowerCase().includes(search.toLowerCase()) ||
      u.name?.toLowerCase().includes(search.toLowerCase())
    )
  }, [data, search])

  return (
    <div className="p-6">
      <PageHeader icon="📧" title="Emailed Users" action={<RefreshButton onRefresh={refetch} />} />
      <div className="mb-4"><SearchBar value={search} onChange={setSearch} /></div>
      {loading ? <LoadingBox /> : error ? <ErrorBox message={error} onRetry={refetch} /> : (
        <Table columns={['Name','Email','Emails Received','Last Notified']}>
          {filtered.map((u,i) => (
            <tr key={i} className="hover:bg-slate-50">
              <td className="px-4 py-3 text-sm flex items-center gap-2">
                <AvatarCircle name={u.name} /> {u.name || 'Unknown'}
              </td>
              <td className="px-4 py-3 text-sm text-slate-500">{u.email}</td>
              <td className="px-4 py-3">
                <span className="badge bg-blue-50 text-blue-700">{u.emailCount}</span>
              </td>
              <td className="px-4 py-3 text-xs text-slate-400">{u.lastSentAt ? new Date(u.lastSentAt).toLocaleString() : ''}</td>
            </tr>
          ))}
        </Table>
      )}
    </div>
  )
}