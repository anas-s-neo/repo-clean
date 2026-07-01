import { Routes, Route, Navigate } from 'react-router-dom'
import Sidebar from './components/Sidebar'
import Overview from './pages/Overview'
import StaleBranches from './pages/StaleBranches'
import DeletedBranches from './pages/DeletedBranches'
import EmailedUsers from './pages/EmailedUsers'
import Dashboard from './pages/Dashboard'

export default function App() {
  return (
    <div className="flex min-h-screen bg-slate-50">
      <Sidebar />
      <main className="flex-1 min-w-0 overflow-auto">
        <Routes>
          <Route path="/"                index element={<Navigate to="/overview" replace />} />
          <Route path="/overview"        element={<Overview />} />
          <Route path="/stale-branches"  element={<StaleBranches />} />
          <Route path="/deleted"         element={<DeletedBranches />} />
          <Route path="/emailed-users"   element={<EmailedUsers />} />
          <Route path="/dashboard"       element={<Dashboard />} />
          <Route path="*"               element={<Navigate to="/overview" replace />} />
        </Routes>
      </main>
    </div>
  )
}