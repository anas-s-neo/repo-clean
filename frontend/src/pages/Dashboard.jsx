import { useMemo } from 'react'
import { api } from '../api/dashboard'
import { useFetch } from '../hooks/useFetch'
import { LoadingBox, ErrorBox, StatCard } from '../components/ui'
import PageHeader from '../components/PageHeader'
import RefreshButton from '../components/RefreshButton'
import { PieChart, Pie, Cell, Tooltip, Legend, BarChart, Bar, XAxis, YAxis, CartesianGrid, ResponsiveContainer } from 'recharts'

const COLORS = {
  PENDING: '#94a3b8',
  EMAIL_SENT: '#3b82f6',
  APPROVED: '#f59e0b',
  DENIED: '#16a34a',
  REMINDER_SENT: '#f97316',
  DELETED: '#dc2626',
  FAILED: '#be123c',
}

export default function Dashboard() {
  const { data: stats, loading: sLoad, error: sErr, refetch: rStats } = useFetch(api.getStats)
  const { data: today, loading: tLoad, error: tErr, refetch: rToday } = useFetch(api.getTodaysStaleBranches)
  const { data: deleted, loading: dLoad, error: dErr, refetch: rDel } = useFetch(api.getDeletedBranches)

  const handleRefresh = () => { rStats(); rToday(); rDel() }

  const pieData = useMemo(() => {
    if (!stats) return []
    return [
      { name: 'Pending', value: stats.totalPending },
      { name: 'Email Sent', value: stats.totalEmailSent },
      { name: 'Approved', value: stats.totalApproved },
      { name: 'Denied', value: stats.totalDenied },
      { name: 'Reminder Sent', value: stats.totalReminderSent },
      { name: 'Deleted', value: stats.totalDeleted },
    ].filter(d => d.value > 0)
  }, [stats])

  const topRepos = useMemo(() => {
    if (!today) return []
    const repoMap = {}
    today.forEach(b => { repoMap[b.repoFullName] = (repoMap[b.repoFullName] || 0) + 1 })
    return Object.entries(repoMap).sort((a,b) => b[1]-a[1]).slice(0,10).map(([name,count]) => ({ name: name.length>16 ? name.slice(0,16)+'...' : name, count }))
  }, [today])

  const topCommitters = useMemo(() => {
    if (!deleted) return []
    const map = {}
    deleted.forEach(d => { const key = d.committerName || d.committerEmail; map[key] = (map[key]||0)+1 })
    return Object.entries(map).sort((a,b)=>b[1]-a[1]).slice(0,10).map(([name,count])=>({name,count}))
  }, [deleted])

  const summary = useMemo(() => {
    if (!stats) return { awaiting:0, approveRate:0, denyRate:0, totalDeleted:0, pendingReminder:0, failed:0 }
    const total = stats.totalPending+stats.totalEmailSent+stats.totalApproved+stats.totalDenied+stats.totalReminderSent+stats.totalDeleted
    const approved = stats.totalApproved+stats.totalReminderSent+stats.totalDeleted
    return {
      awaiting: stats.totalPending+stats.totalEmailSent,
      approveRate: total ? Math.round((approved/total)*100) : 0,
      denyRate: total ? Math.round((stats.totalDenied/total)*100) : 0,
      totalDeleted: stats.totalDeleted,
      pendingReminder: stats.totalReminderSent,
      failed: 0,
    }
  }, [stats])

  if (sLoad || tLoad || dLoad) return <LoadingBox message="Loading charts..." />
  if (sErr || tErr || dErr) return <ErrorBox message={sErr||tErr||dErr} onRetry={handleRefresh} />

  return (
    <div className="p-6">
      <PageHeader icon="📈" title="Analytics Dashboard" action={<RefreshButton onRefresh={handleRefresh} />} />

      <div className="grid grid-cols-2 lg:grid-cols-6 gap-4 mb-8">
        <StatCard label="Awaiting action" value={summary.awaiting} icon="⏳" colorClass="bg-slate-100" />
        <StatCard label="Approve rate" value={`${summary.approveRate}%`} icon="✅" colorClass="bg-amber-50" />
        <StatCard label="Deny rate" value={`${summary.denyRate}%`} icon="🚫" colorClass="bg-green-50" />
        <StatCard label="Total deleted" value={summary.totalDeleted} icon="🗑️" colorClass="bg-red-50" />
        <StatCard label="Pending reminder" value={summary.pendingReminder} icon="🔔" colorClass="bg-orange-50" />
        <StatCard label="Failed" value={summary.failed} icon="❌" colorClass="bg-rose-50" />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        <div className="card">
          <h3 className="font-semibold mb-4">Status Breakdown</h3>
          {pieData.length === 0 ? <p className="text-sm text-slate-400">No data</p> :
          <ResponsiveContainer width="100%" height={300}>
            <PieChart>
              <Pie data={pieData} dataKey="value" nameKey="name" cx="50%" cy="50%" innerRadius={70} outerRadius={110} paddingAngle={3}>
                {pieData.map((entry,index) => <Cell key={`cell-${index}`} fill={COLORS[entry.name.replace(' ','_').toUpperCase()] || '#ccc'} />)}
              </Pie>
              <Tooltip />
              <Legend />
            </PieChart>
          </ResponsiveContainer>
          }
          </div>

        <div className="card">
          <h3 className="font-semibold mb-4">Top 10 Repos by Stale Branches</h3>
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={topRepos} layout="vertical">
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis type="number" />
              <YAxis dataKey="name" type="category" width={150} tick={{fontSize:12}} />
              <Tooltip />
              <Bar dataKey="count" fill="#3b82f6" radius={[0,4,4,0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>

        <div className="card lg:col-span-2">
          <h3 className="font-semibold mb-4">Top 10 Committers by Deletions</h3>
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={topCommitters}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="name" angle={-30} textAnchor="end" height={80} tick={{fontSize:12}} />
              <YAxis allowDecimals={false} />
              <Tooltip />
              <Bar dataKey="count" fill="#dc2626" radius={[4,4,0,0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>
    </div>
  )
}