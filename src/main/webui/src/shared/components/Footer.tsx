import React from 'react'
import { getAppVersion } from '../utils/version'

const Footer: React.FC = () => {
  return (
    <div className="text-center mt-8">
      <div className="text-tui-muted text-xs">
        <p>&copy; {new Date().getFullYear()} tohuwabohu.io</p>
        <p className="mt-1"><a href="https://github.com/thwbh/crt-dojo">crt-dojo</a> • Terminal Interface {getAppVersion()}</p>
      </div>
    </div>
  )
}

export default Footer
