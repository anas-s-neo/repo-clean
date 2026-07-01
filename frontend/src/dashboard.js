const BASE = import.meta.env.VITE_API_URL || '/api/dashboard'

async function get(path) {
  const res = await fetch(`${BASE}${path}`)
  if (!res.ok) throw new Error(`API error: ${res.status}`)
  return res.json()
}

export const api = {
  getStats:               () => get('/stats'),
  getDeletedBranches:     () => get('/deleted-branches'),
  getEmailedUsers:        () => get('/emailed-users'),
  getTodaysStaleBranches: () => get('/stale-branches/today'),
  getAllStaleBranches:     () => get('/stale-branches'),
}