import React from 'react'

const Footer: React.FC = () => {
    return (
        <div className="text-center mt-8">
            <div className="text-tui-muted text-xs">
                <p>&copy; {new Date().getFullYear()} tohuwabohu.io</p>
                <p className="mt-1">Terminal Interface v{process.env.NODE_ENV === 'development' ? 'DEV' : '1.0'}</p>
            </div>
        </div>
    )
}

export default Footer