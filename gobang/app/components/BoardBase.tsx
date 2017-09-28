import * as React from 'react'
import { Range } from 'immutable'
import { GRID_SIZE, SIZE } from '../constants'

export default class BoardBase extends React.Component<{}, {}> {
  shouldComponentUpdate() {
    return false
  }

  render() {
    return (
      <g>
        {Range(0, SIZE).map(y =>
          <line
            key={y}
            x1="0"
            y1={y * GRID_SIZE}
            x2={(SIZE - 1) * GRID_SIZE}
            y2={y * GRID_SIZE}
            stroke="black"
          />
        )}
        {Range(0, SIZE).map(x =>
          <line
            key={x}
            x1={x * GRID_SIZE}
            y1="0"
            x2={x * GRID_SIZE}
            y2={(SIZE - 1) * GRID_SIZE}
            stroke="black"
          />
        )}
      </g>
    )
  }
}
