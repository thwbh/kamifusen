import React from 'react';

interface DataItem {
  name: string;
  value: string;
  color: string;
}

interface DataTableProps {
  title: string;
  subtitle?: string;
  data: DataItem[];
  selectedIndex: number;
  onItemSelect: (index: number) => void;
}

const DataTable: React.FC<DataTableProps> = ({ 
  title,
  subtitle,
  data, 
  selectedIndex, 
  onItemSelect 
}) => {
  return (
    <div className="tui-panel m-4 flex-shrink-0">
      <div className="tui-panel-header">
        <h3 className="font-mono text-sm">{title}</h3>
      </div>
      <div className="p-4">
        {subtitle && (
          <div className="text-tui-accent text-xs mb-2">{'//'} {subtitle}</div>
        )}
        
        <table className="w-full text-xs">
          <thead>
            <tr className="text-tui-muted">
              <th className="text-left pb-2">ITEM</th>
              <th className="text-right pb-2">VALUE</th>
            </tr>
          </thead>
          <tbody>
            {data.map((item, index) => (
              <tr 
                key={index}
                className={`${index === selectedIndex ? 'bg-tui-accent bg-opacity-20' : ''} cursor-pointer hover:bg-tui-border hover:bg-opacity-50 transition-colors duration-150`}
                onClick={() => onItemSelect(index)}
              >
                <td className="py-1 text-tui-light flex items-center">
                  <div className="w-2 h-2 rounded-full bg-tui-muted mr-2"></div>
                  {item.name}
                </td>
                <td className={`text-right py-1 ${item.color}`}>
                  {item.value}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default DataTable;