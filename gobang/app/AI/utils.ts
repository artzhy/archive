import { Side, IMMEDIATE_WIN } from '../constants'
import Statistic from './Statistic'
import info from './debugInfo'
import Board from './Board'

export function getInitBestExp(side: Side, alpha: number = -IMMEDIATE_WIN, beta: number = IMMEDIATE_WIN) {
  return side === Side.black ? alpha : beta
}

function getScore(side: Side, board: Board) {
  const statistic = new Statistic()
  for (const line of board.flat()) {
    let blocked = false
    let steak = 0
    for (const piece of line) {
      switch (piece) {
        case Side.none:
          if (steak > 0) {
            if (blocked) {
              statistic.addBlocked(steak)
            } else {
              statistic.addOpen(steak)
            }
            steak = 0
            blocked = false
          }
          break
        case side:
          steak += 1
          break
        default: // other side
          if (steak > 0) {
            if (blocked) {
              statistic.addDead(steak)
            } else {
              statistic.addBlocked(steak)
            }
            steak = 0
            blocked = true
          }
      }
    }
    if (steak > 0) {
      if (blocked) {
        statistic.addDead(steak)
      } else {
        statistic.addBlocked(steak)
      }
    }
  }

  return statistic.getScore()
}

const cache = new Map<number, number>()

export function getExp(pieces: Board) {
  info.total += 1
  const key = pieces.getHash()
  if (cache.has(key)) {
    info.hit += 1
    return cache.get(key)
  }
  info.real += 1
  const exp = getScore(Side.black, pieces) - getScore(Side.white, pieces)
  cache.set(key, exp)
  return exp
}

export function better(cntSide: Side, exp1: number, exp2: number) {
  return cntSide === Side.black ? exp1 > exp2 : exp1 < exp2
}

export function shouldBreak(cntSide: Side, bestExp: number, alpha: number, beta: number) {
  return cntSide === Side.black && bestExp >= beta
    || cntSide === Side.white && bestExp <= alpha
}
