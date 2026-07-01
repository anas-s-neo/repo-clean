import { api } from '../api/dashboard'
import { useFetch } from '../hooks/useFetch'
import { StatCard, StatusBadge, MonoPill, RelativeTime, LoadingBox, ErrorBox } from '../components/ui'
import PageHeader from '../components/PageHeader'
import RefreshButton from '../components/RefreshButton'

export default function Overview() {
  const { data: stats, loading: statsLoading, error: statsError, refetch: refetchStats } = useFetch(api.getStats)
  const { data: today, loading: todayLoading, error: todayError, refetch: refetchToday } = useFetch(api.getTodaysStaleBranches)
  const { data: deleted, loading: deletedLoading, error: deletedError, refetch: refetchDeleted } = useFetch(api.getDeletedBranches)

  const handleRefresh = () => {
    refetchStats()
    refetchToday()
    refetchDeleted()
  }

  return (
    <div className="p-6">
      <PageHeader
        icon="📊"
        title="Overview"
        subtitle="Pipeline status and recent activity"
        action={<RefreshButton onRefresh={handleRefresh} />}
      />

      {statsError ? <ErrorBox message={statsError} onRetry={refetchStats} /> : (
        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4 mb-8">
          {statsLoading ? Array.from({length:6}).map((_,i)=><div key={i} className="card animate-pulse h-20"/>)
           : (
            <>
              <StatCard label="Pending" value={stats.totalPending} icon="⏳" colorClass="bg-slate-100" />
              <StatCard label="Email Sent" value={stats.totalEmailSent} icon="✉️" colorClass="bg-blue-50" />
              <StatCard label="Approved" value={stats.totalApproved} icon="✅" colorClass="bg-amber-50" />
              <StatCard label="Denied" value={stats.totalDenied} icon="🚫" colorClass="bg-green-50" />
              <StatCard label="Reminder Sent" value={stats.totalReminderSent} icon="🔔" colorClass="bg-orange-50" />
              <StatCard label="Deleted" value={stats.totalDeleted} icon="🗑️" colorClass="bg-red-50" />
            </>
          )}
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="card">
          <h2 className="font-semibold text-slate-800 mb-4">Latest Detection Run</h2>
          {todayLoading ? <LoadingBox /> : todayError ? <ErrorBox message={todayError} onRetry={refetchToday} />
           : !today?.length ? <p className="text-slate-400 text-sm">No recent stale branches.</p>
           : <ul className="space-y-3">
               {today.slice(0,8).map(b => (
                 <li key={b.id} className="flex items-center justify-between text-sm">
                   <div>
                     <MonoPill>{b.branchName}</MonoPill>
                     <div className="text-xs text-slate-400 mt-0.5">{b.repoFullName}</div>
                   </div>
                   <StatusBadge status={b.status} />
                 </li>
               ))}
             </ul>
          }
        </div>

        <div className="card">
          <h2 className="font-semibold text-slate-800 mb-4">Recently Deleted</h2>
          {deletedLoading ? <LoadingBox /> : deletedError ? <ErrorBox message={deletedError} onRetry={refetchDeleted} />
           : !deleted?.length ? <p className="text-slate-400 text-sm">No deletions yet.</p>
           : <ul className="space-y-3">
               {deleted.slice(0,8).map(d => (
                 <li key={d.id} className="flex items-center justify-between text-sm">
                   <div>
                     <MonoPill>{d.branchName}</MonoPill>
                     <div className="text-xs text-slate-400">{d.repoName}</div>
                   </div>
                   <RelativeTime iso={d.deletedAt} />
                 </li>
               ))}
             </ul>
          }
        </div>
      </div>
    </div>
  )
}