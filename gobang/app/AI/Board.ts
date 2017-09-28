import { Side, SIZE } from '../constants'
import * as hash from './hash'
import { getRow, getCol, getT } from '../common'
import { ALL_SCAN_LINES } from './scanLines'

export default class Pieces {
  private pieces: Array<Side>
  private hash: number

  constructor(pieces: Array<Side>) {
    this.pieces = pieces
    this.hash = hash.hash(pieces)
  }

  toggle(t: number, side: Side) {
    if (this.pieces[t] === Side.none) {
      this.pieces[t] = side
    } else {
      this.pieces[t] = Side.none
    }
    this.hash = hash.toggle(this.hash, t, side)
  }

  getHash() {
    return this.hash
  }

  get(t: number) {
    return this.pieces[t]
  }

  hasNearbyPiece(baseT: number, searchRange: number = 2) {
    const baseRow = getRow(baseT)
    const baseCol = getCol(baseT)

    const top = Math.max(0, baseRow - searchRange)
    const bottom = Math.min(SIZE, baseRow + searchRange + 1)
    const left = Math.max(0, baseCol - searchRange)
    const right = Math.min(SIZE, baseCol + searchRange + 1)

    for (let row = top; row < bottom; row++) {
      for (let col = left; col < right; col++) {
        const t = getT(row, col)
        if (this.pieces[t] !== Side.none) {
          return true
        }
      }
    }
    return false
  }

  flat() {
    const result: Array<Array<Side>> = []
    for (const line of ALL_SCAN_LINES) {
      const row: Array<Side> = []
      for (const t of line) {
        row.push(this.pieces[t])
      }
      result.push(row)
    }
    return result
  }
}
