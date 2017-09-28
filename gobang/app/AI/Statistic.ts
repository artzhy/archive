import { GO_BANG } from '../constants'

export default class Statistic {
  open: Array<number>
  blocked: Array<number>
  dead: Array<number>
  gobang: boolean

  constructor() {
    this.open = new Array(5)
    this.blocked = new Array(5)
    this.dead = new Array(5)
    this.gobang = false

    this.open.fill(0)
    this.blocked.fill(0)
    this.dead.fill(0)
  }

  add(blockedCount: number, steak: number) {
    if (blockedCount === 0) {
      this.addOpen(steak)
    } else if (blockedCount === 1) {
      this.addBlocked(steak)
    } else if (blockedCount === 2) {
      this.addDead(steak)
    } else {
      throw new Error(`Invalid blockedCount: ${blockedCount}`)
    }
  }

  addOpen(number: number) {
    if (number >= 5) {
      this.gobang = true
    } else if (number >= 1) {
      this.open[number] += 1
    }
  }

  removeOpen(number: number) {
    this.open[number] -= 1
  }

  addBlocked(number: number) {
    if (number >= 5) {
      this.gobang = true
    } else if (number >= 1) {
      this.blocked[number] += 1
    }
  }

  removeBlocked(number: number) {
    this.blocked[number] -= 1
  }

  addDead(number: number) {
    if (number >= 5) {
      this.gobang = true
    } else if (number >= 1) {
      this.dead[number] += 1
    }
  }

  removeDead(number: number) {
    this.dead[number] -= 1
  }

  getScore() {
    if (this.gobang) {
      return GO_BANG
    }
    const openScore = this.open[1]
      + (this.open[2] << 3)
      + (this.open[3] << 6)
      + (this.open[4] << 9)

    const blockedScore = this.blocked[2]
      + (this.blocked[3] << 3)
      + (this.blocked[4] << 6)

    return openScore + blockedScore
  }
}
