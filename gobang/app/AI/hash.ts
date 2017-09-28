import { SIZE_SQUARE, Side } from '../constants'
import { range } from '../common'

function randInt() {
  return Math.floor(Math.random() * 0x7fffffff)
}

const table: Array<Array<number>> = new Array(SIZE_SQUARE)
for (const t of range(0, SIZE_SQUARE)) {
  table[t] = [randInt(), randInt()]
}

export function hash(pieces: Array<Side>) {
  let result = 0
  for (const t of range(0, SIZE_SQUARE)) {
    if (pieces[t] !== Side.none) {
      result ^= table[t][pieces[t]]
    }
  }
  return result
}

export function toggle(old: number, t: number, side: Side) {
  return old ^ table[t][side]
}
