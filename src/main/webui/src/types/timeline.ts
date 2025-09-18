export interface TimelineItem {
  id: string;
  company: string;
  url?: string;
  timeframe: string;
  role: string;
  stack: string;
  description: string;
  isActive?: boolean;
}