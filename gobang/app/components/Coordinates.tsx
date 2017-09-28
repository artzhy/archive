import * as React from 'react'
import { Range } from 'immutable'
import { SIZE, GRID_SIZE, BOARD_DX, BOARD_DY } from '../constants'

export default class Coordinates extends React.Component<{}, {}> {
  shouldComponentUpdate() {
    return false
  }

  render() {
    return (
      <div
        style={{
          position: 'absolute',
          left: BOARD_DX,
          top: BOARD_DY,
          fontSize: '14px',
        }}
      >
        {Range(0, SIZE).map(x =>
          <div
            key={x}
            style={{
              position: 'absolute',
              left: GRID_SIZE * (x - 0.5),
              top: -GRID_SIZE,
              width: GRID_SIZE,
              lineHeight: `${GRID_SIZE}px`,
              textAlign: 'center',
            }}
          >
            {x}
          </div>
        ).toArray()}
        {Range(0, SIZE).map(y =>
          <div
            key={y}
            style={{
              position: 'absolute',
              left: -GRID_SIZE,
              top: GRID_SIZE * (y - 0.5),
              width: GRID_SIZE,
              lineHeight: `${GRID_SIZE}px`,
              textAlign: 'center',
            }}
          >{y}</div>
        ).toArray()}
      </div>
    )
  }
}
