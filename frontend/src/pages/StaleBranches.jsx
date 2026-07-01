import { useState, useMemo } from 'react'
// Fixed: Corrected import format and path resolving to use standard sibling directory resolution
import { api } from '../api/dashboard'
import { useFetch } from '../hooks/useFetch'
import { Table, StatusBadge, MonoPill, RelativeTime, SearchBar, LoadingBox, ErrorBox } from '../components/ui'
import PageHeader from '../components/PageHeader'
import RefreshButton from '../components/RefreshButton'

const statusFilterOptions = ['ALL','PENDING','EMAIL_SENT','APPROVED','DENIED','REMINDER_SENT','DELETED','FAILED']

export default function StaleBranches() {
  const { data, loading, error, refetch } = useFetch(api.getAllStaleBranches)
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState('ALL')

  const filtered = useMemo(() => {
    if (!data) return []
    return data.filter(b => {
      const matchSearch = search === '' ||
        b.branchName.toLowerCase().includes(search.toLowerCase()) ||
        b.repoFullName.toLowerCase().includes(search.toLowerCase()) ||
        b.committerName?.toLowerCase().includes(search.toLowerCase()) ||
        b.committerEmail?.toLowerCase().includes(search.toLowerCase())
      const matchStatus = statusFilter === 'ALL' || b.status === statusFilter
      return matchSearch && matchStatus
    })
  }, [data, search, statusFilter])

  return (
    <div className="p-6">
      <PageHeader
        icon="🌿"
        title="Stale Branches"
        subtitle={`${filtered.length} of ${data?.length ?? 0} branches`}
        action={<RefreshButton onRefresh={refetch} />}
      />

      <div className="flex flex-col sm:flex-row gap-4 mb-4">
        <SearchBar value={search} onChange={setSearch} />
        <div className="flex flex-wrap gap-2">
          {statusFilterOptions.map(s => (
            <button
              key={s}
              onClick={() => setStatusFilter(s)}
              className={`px-3 py-1 text-xs font-medium rounded-full border ${s === statusFilter ? 'bg-blue-600 text-white border-blue-600' : 'bg-white text-slate-600 border-slate-200 hover:bg-slate-50'}`}
            >{s}</button>
          ))}
        </div>
      </div>

      {loading ? <LoadingBox /> : error ? <ErrorBox message={error} onRetry={refetch} /> : (
        <Table columns={['Repository','Branch','Last Committer','Last Commit','Detected','Status']} empty="No stale branches match filters">
          {filtered.map(b => (
            <tr key={b.id} className="hover:bg-slate-50">
              <td className="px-4 py-3 text-sm text-slate-500">{b.repoFullName}</td>
              <td className="px-4 py-3"><MonoPill>{b.branchName}</MonoPill></td>
              <td className="px-4 py-3 text-sm">
                <div className="font-medium">{b.committerName || 'Unknown'}</div>
                <div className="text-xs text-slate-400">{b.committerEmail}</div>
              </td>
              <td className="px-4 py-3 text-sm">
                <div className="max-w-[200px] truncate" title={b.lastCommitMessage}>{b.lastCommitMessage}</div>
                <div className="text-xs text-slate-400"><RelativeTime iso={b.lastCommitDate} /></div>
              </td>
              <td className="px-4 py-3 text-xs text-slate-400"><RelativeTime iso={b.detectedAt} /></td>
              <td className="px-4 py-3"><StatusBadge status={b.status} /></td>
            </tr>
          ))}
        </Table>
      )}
    </div>
  )
}