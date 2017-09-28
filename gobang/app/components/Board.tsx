import * as React from 'react'
import { connect } from 'react-redux'
import { List } from 'immutable'
import BoardBase from './BoardBase'
import Piece, { PieceProps } from './Piece'
import { SIZE, GRID_SIZE, BOARD_DX, BOARD_DY, Stage, Side, ActionType } from '../constants'
import { StateType } from '../reducer'
import { getRow, getCol, getT } from '../common'

function getPos(node: Element, event: React.MouseEvent<any>) {
  const clientRect = node.getBoundingClientRect()
  const left = document.body.scrollLeft + clientRect.left + node.clientLeft
  const top = document.body.scrollTop + clientRect.top + node.clientTop
  return { x: event.pageX - left, y: event.pageY - top }
}

function getRowColFromXY({ x, y }: { x: number, y: number }) {
  const row = (y - BOARD_DY) / GRID_SIZE
  const col = (x - BOARD_DX) / GRID_SIZE
  return { row, col }
}

function roundPoint({ row, col }: { row: number, col: number }) {
  const rrow = Math.round(row)
  const rcol = Math.round(col)
  if (rcol >= 0 && rcol < SIZE && rrow >= 0 && rrow < SIZE
    && ((col - rcol) * (col - rcol)) + ((row - rrow) * (row - rrow)) <= 0.48 * 0.48) {
    return { row: rrow, col: rcol }
  }
  return null
}

const LastIndicator = ({ lastT }: { lastT: number }) => (
  <g
    transform={`translate(${(getCol(lastT) - 0.5) * GRID_SIZE},
                          ${(getRow(lastT) - 0.5) * GRID_SIZE})`}
  >
    <polygon
      fill="green"
      points={`0,0 0,${0.3 * GRID_SIZE} ${0.3 * GRID_SIZE},0`}
    />
  </g>
)

interface BoardProps {
  stage: Stage
  pieces: List<Side>
  side: Side
  playerSide: Side
  lastT: number
  dispatch: Redux.Dispatch<StateType>
}

interface BoardState {
  row: number
  col: number
  valid: boolean
}

class Board extends React.Component<BoardProps, BoardState> {
  svg: Element

  state = {
    row: 0,
    col: 0,
    valid: false,
  }

  onMouseMove = (event: React.MouseEvent<any>) => {
    const pos = getPos(this.svg, event)
    const { row, col } = getRowColFromXY(pos)
    this.setState({ row, col, valid: true })
  }

  onClick = (event: React.MouseEvent<any>) => {
    const { side, playerSide, stage, pieces } = this.props
    // 只在用户回合的时候点击才有效
    if (stage === Stage.ON && side === playerSide) {
      // todo 需要验证下棋的位置上 目前没有棋子
      const pos = getPos(this.svg, event)
      const point = getRowColFromXY(pos)
      const roundedPoint = roundPoint(point)
      if (roundedPoint) {
        const t = getT(roundedPoint.row, roundedPoint.col)
        if (pieces.get(t) === Side.none) {
          this.props.dispatch({ type: ActionType.STEP, side, t })
        }
      }
    }
  }

  onMouseLeave = () => {
    this.setState({ valid: false } as BoardState)
  }

  svgRef = (node: Element) => {
    this.svg = node
  }

  render() {
    const { pieces, side, playerSide, lastT, stage } = this.props

    let prompt: React.ReactElement<PieceProps> = null
    if (stage === Stage.ON && this.state.valid) {
      let valid = false
      if (side === playerSide) {
        const roundedPoint = roundPoint(this.state)
        valid = roundedPoint
          && pieces.get(getT(roundedPoint.row, roundedPoint.col)) === Side.none
      }
      const { row, col } = this.state
      prompt = <Piece row={row} col={col} side={playerSide} prompt valid={valid} />
    }

    return (
      <svg
        ref={this.svgRef}
        width={(SIZE + 1) * GRID_SIZE}
        height={(SIZE + 1) * GRID_SIZE}
        style={{
          WebkitUserSelect: 'none',
          userSelect: 'none',
          cursor: 'default',
          flexShrink: 0,
          background: 'url(/static/bg.jpg)',
        }}
        onMouseMove={this.onMouseMove}
        onClick={this.onClick}
        onMouseLeave={this.onMouseLeave}
      >
        <defs>
          <radialGradient id="black-piece" cx="0.45" cy="0.45" fx="0.3" fy="0.3" r="0.5">
            <stop offset="0" stopColor="#aaa" />
            <stop offset="1" stopColor="#000" />
          </radialGradient>
        </defs>
        <g transform={`translate(${BOARD_DX}, ${BOARD_DY})`}>
          <BoardBase />
          {pieces.map((side, t) =>
            <Piece key={t} row={getRow(t)} col={getCol(t)} side={side} />
          ).toArray()}
          {prompt}
          {lastT !== -1 ? (
            <LastIndicator lastT={lastT} />
          ) : null}
        </g>
      </svg>
    )
  }
}

export default connect((state: StateType) => state.toObject())(Board)
