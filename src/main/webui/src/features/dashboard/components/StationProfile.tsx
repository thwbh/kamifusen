import React from 'react';

interface Material {
  name: string;
  yield: string;
  color: string;
}

interface StationProfileProps {
  materials: Material[];
  selectedMaterial: number;
  onMaterialSelect: (index: number) => void;
}

const StationProfile: React.FC<StationProfileProps> = ({ 
  materials, 
  selectedMaterial, 
  onMaterialSelect 
}) => {
  return (
    <div className="tui-panel m-4 flex-shrink-0">
      <div className="tui-panel-header">
        <h3 className="font-mono text-sm">STATION PROFILE</h3>
      </div>
      <div className="p-4">
        <div className="text-tui-accent text-xs mb-2">{'//'} MATERIAL SPECIALIZATIONS</div>
        
        <table className="w-full text-xs">
          <thead>
            <tr className="text-tui-muted">
              <th className="text-left pb-2">MATERIAL</th>
              <th className="text-right pb-2">YIELD</th>
            </tr>
          </thead>
          <tbody>
            {materials.map((material, index) => (
              <tr 
                key={index}
                className={`${index === selectedMaterial ? 'bg-tui-accent bg-opacity-20' : ''} cursor-pointer hover:bg-tui-border hover:bg-opacity-50 transition-colors duration-150`}
                onClick={() => onMaterialSelect(index)}
              >
                <td className="py-1 text-tui-light flex items-center">
                  <div className="w-2 h-2 rounded-full bg-tui-muted mr-2"></div>
                  {material.name}
                </td>
                <td className={`text-right py-1 ${material.color}`}>
                  {material.yield}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default StationProfile;