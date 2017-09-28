import { SIZE, Side } from './constants'
import { List } from 'immutable'

export function* range(start: number, end: number): IterableIterator<number> {
  for (let t = start; t < end; t++) {
    yield t
  }
}

export function randomSide() {
  return Math.random() > 0.5 ? Side.black : Side.white
}

export function getRow(t: number) {
  return Math.floor(t / SIZE)
}

export function getCol(t: number) {
  return t % SIZE
}

export function getT(row: number, col: number) {
  return row * SIZE + col
}

export function swapSide(side: Side) {
  return side === Side.black ? Side.white : Side.black
}

const directions = [
  { deltaRow: 0, deltaCol: -1 },
  { deltaRow: -1, deltaCol: -1 },
  { deltaRow: -1, deltaCol: 0 },
  { deltaRow: -1, deltaCol: 1 },
]

interface ForwardFunc {
  (t: number, delta: { deltaRow: number, deltaCol: number }): number
}

const forward: ForwardFunc = (t, { deltaRow, deltaCol }) => {
  const baseRow = getRow(t)
  const baseCol = getCol(t)
  const row = baseRow + deltaRow
  const col = baseCol + deltaCol
  if (row >= 0 && row < SIZE && col >= 0 && col < SIZE) {
    return getT(row, col)
  } else {
    return -1
  }
}

/** 判断游戏是否结束 */
export function isGameover(pieces: List<Side>, lastT: number) {
  if (pieces.every(side => (side !== Side.none))) {
    return true
  }
  const side = pieces.get(lastT)
  for (const direction of directions) {
    let count = 1
    for (let t = forward(lastT, direction);
         t !== -1 && pieces.get(t) === side;
         t = forward(t, direction)) {
      count++
    }

    const reverse = { deltaRow: -direction.deltaRow, deltaCol: -direction.deltaCol }
    for (let t = forward(lastT, reverse);
         t !== -1 && pieces.get(t) === side;
         t = forward(t, reverse)) {
      count++
    }
    if (count >= 5) {
      return true
    }
  }
  return false
}

export function getRandom<T>(array: T[]): T {
  return array[Math.floor(Math.random() * array.length)]
}
