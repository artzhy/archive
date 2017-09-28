import { range, getT } from '../common'
import { SIZE } from '../constants'

// 横向
const horizontals: Array<Array<number>> = []
for (const row of range(0, SIZE)) {
  horizontals.push([])
  for (const col of range(0, SIZE)) {
    horizontals[row][col] = getT(row, col)
  }
}
// 纵向
const verticals: Array<Array<number>> = []
for (const col of range(0, SIZE)) {
  verticals.push([])
  for (const row of range(0, SIZE)) {
    verticals[col][row] = getT(row, col)
  }
}

// 从左上到右下的线 (左下角的线在前)
const lines: Array<Array<number>> = []
for (const delta of range(-SIZE + 1, SIZE)) {
  const index = delta + SIZE - 1
  lines[index] = []
  if (delta <= 0) {
    for (const col of range(0, SIZE)) {
      const row = col - delta
      lines[index][col] = getT(row, col)
    }
  } else {
    for (const row of range(0, SIZE - delta)) {
      const col = row + delta
      lines[index][row] = getT(row, col)
    }
  }
}

// 从左下到右上方向的线 (左上角的线在前)
const lines2: Array<Array<number>> = []
for (const sum of range(0, 2 * SIZE - 1)) {
  lines2.push([])
  if (sum <= SIZE - 1) {
    for (const col of range(0, sum + 1)) {
      const row = sum - col
      lines2[sum][col] = getT(row, col)
    }
  } else {
    for (const t of range(0, 2 * SIZE - 1 - sum)) {
      const row = SIZE - 1 - t
      const col = sum - row
      lines2[sum][t] = getT(row, col)
    }
  }
}

export const ALL_SCAN_LINES: Array<Array<number>> = [
  ...horizontals, ...verticals, ...lines, ...lines2
]
