declare module 'react-calendar-heatmap' {
  import * as React from 'react'

  export interface CalendarHeatmapValue {
    date: string | Date
    count?: number
  }

  export interface CalendarHeatmapProps {
    startDate: string | Date
    endDate: string | Date
    values: CalendarHeatmapValue[]
    showWeekdayLabels?: boolean
    gutterSize?: number
    classForValue?: (value: CalendarHeatmapValue | null) => string
    tooltipDataAttrs?: (
      value: CalendarHeatmapValue | null
    ) => Record<string, any>
  }

  export default class CalendarHeatmap extends React.Component<CalendarHeatmapProps> {}
}
