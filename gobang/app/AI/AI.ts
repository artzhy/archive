import { Side, SIZE_SQUARE, IMMEDIATE_WIN } from '../constants'
import { swapSide, getRandom, range } from '../common'
import { getInitBestExp, better, getExp, shouldBreak } from './utils'
import info from './debugInfo'
import Board from './Board'

const log = (...args: Array<any>) => console.log('[AI]:', ...args)

function getMoves(board: Board, cntSide: Side) {
  const candidates = Array.from(range(0, SIZE_SQUARE)).filter(t =>
    (board.get(t) === Side.none && board.hasNearbyPiece(t)))

  const getNextExp = (t: number) => {
    board.toggle(t, cntSide)
    const exp = getExp(board)
    board.toggle(t, cntSide)
    return exp
  }

  candidates.sort((t1, t2) => {
    const exp1 = getNextExp(t1)
    const exp2 = getNextExp(t2)
    return cntSide === Side.black ? exp2 - exp1 : exp1 - exp2
  })

  return candidates
}

function minimax(board: Board, cntSide: Side, depth: number) {
  let bestMoves: Array<number> = []
  let bestExp = getInitBestExp(cntSide)

  for (const t of range(0, SIZE_SQUARE)) {
    if (board.get(t) === Side.none && board.hasNearbyPiece(t)) {
      board.toggle(t, cntSide)
      const exp = backtrack(board, swapSide(cntSide), depth - 1)
      if (better(cntSide, exp, bestExp)) {
        bestExp = exp
        bestMoves = [t]
      } else if (exp === bestExp) {
        bestMoves.push(t)
      }
      board.toggle(t, cntSide)
    }
  }

  if (bestMoves.length === 0) {
    throw new Error('AI lose')
  }
  return getRandom(bestMoves)
}

function backtrack(board: Board, cntSide: Side, depth: number,
                   alpha: number = -IMMEDIATE_WIN, beta: number = IMMEDIATE_WIN) {
  // if (depth === 0) {
  //   return getExp(board)
  // }
  if (depth === 1) {
    const t = getMoves(board, cntSide)[0]
    board.toggle(t, cntSide)
    const result = getExp(board)
    board.toggle(t, cntSide)
    return result
  }
  let bestExp = getInitBestExp(cntSide, alpha, beta)

  for (const t of getMoves(board, cntSide)) {
    if (shouldBreak(cntSide, bestExp, alpha, beta)) {
      break
    }
    board.toggle(t, cntSide)
    const exp = cntSide === Side.black ?
      backtrack(board, swapSide(cntSide), depth - 1, bestExp, beta) :
      backtrack(board, swapSide(cntSide), depth - 1, alpha, bestExp)

    if (better(cntSide, exp, bestExp)) {
      bestExp = exp
    }
    board.toggle(t, cntSide)
  }
  return bestExp
}

export default function AI(pieces: Array<Side>, aiSide: Side) {
  info.reset()
  console.time('AI')
  const result = minimax(new Board(pieces), aiSide, 2)
  console.timeEnd('AI')
  log(info)
  return result
}
