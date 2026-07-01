import { useState, useMemo } from 'react'
import { api } from '../api/dashboard'
import { useFetch } from '../hooks/useFetch'
import { Table, MonoPill, RelativeTime, SearchBar, LoadingBox, ErrorBox } from '../components/ui'
import PageHeader from '../components/PageHeader'
import RefreshButton from '../components/RefreshButton'

export default function DeletedBranches() {
  const { data, loading, error, refetch } = useFetch(api.getDeletedBranches)
  const [search, setSearch] = useState('')

  const filtered = useMemo(() => {
    if (!data) return []
    return data.filter(d =>
      d.branchName.toLowerCase().includes(search.toLowerCase()) ||
      d.repoName.toLowerCase().includes(search.toLowerCase()) ||
      d.committerName?.toLowerCase().includes(search.toLowerCase()) ||
      d.committerEmail?.toLowerCase().includes(search.toLowerCase())
    )
  }, [data, search])

  return (
    <div className="p-6">
      <PageHeader icon="🗑️" title="Deleted Branches" action={<RefreshButton onRefresh={refetch} />} />
      <div className="mb-4"><SearchBar value={search} onChange={setSearch} /></div>
      {loading ? <LoadingBox /> : error ? <ErrorBox message={error} onRetry={refetch} /> : (
        <Table columns={['Repository','Branch','Last Committer','Last Commit Date','Deleted']}>
          {filtered.map(d => (
            <tr key={d.id} className="hover:bg-slate-50">
              <td className="px-4 py-3 text-sm text-slate-500">{d.repoFullName}</td>
              <td className="px-4 py-3"><MonoPill>{d.branchName}</MonoPill></td>
              <td className="px-4 py-3 text-sm">
                <div className="font-medium">{d.committerName || 'Unknown'}</div>
                <div className="text-xs text-slate-400">{d.committerEmail}</div>
              </td>
              <td className="px-4 py-3 text-xs text-slate-400"><RelativeTime iso={d.lastCommitDate} /></td>
              <td className="px-4 py-3 text-sm">
                <div><RelativeTime iso={d.deletedAt} /></div>
                <div className="text-xs text-slate-400">{d.deletedAt ? new Date(d.deletedAt).toLocaleString() : ''}</div>
              </td>
            </tr>
          ))}
        </Table>
      )}
    </div>
  )
}