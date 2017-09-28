import { Side, SIZE } from '../constants'

export default function simpleAI(pieces: Side[], aiSide: Side) {
  for (let t = 0; t < SIZE * SIZE; t++) {
    if (pieces[t] === Side.none) {
      return t
    }
  }
  return -1
}
