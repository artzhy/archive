import * as React from 'react'
import { GRID_SIZE, Side } from '../constants'

export interface PieceProps {
  row: number
  col: number
  side: Side
  prompt?: boolean
  valid?: boolean
}

export default class Piece extends React.Component<PieceProps, {}> {
  render() {
    const { row, col, side, prompt, valid } = this.props
    if (side === Side.none) {
      return null
    }

    const r = GRID_SIZE * 0.48
    let opacity = 1
    if (prompt) {
      opacity = valid ? 0.85 : 0.3
    }
    const fill = side === Side.black ? 'url(#black-piece)' : 'white'
    return (
      <g transform={`translate(${GRID_SIZE * col},${GRID_SIZE * row})`} opacity={opacity}>
        <circle cx={0.1 * r} cy={0.1 * r} r={r} fill="#666" opacity="0.8" />
        <circle r={r} fill={fill} />
      </g>
    )
  }
}
